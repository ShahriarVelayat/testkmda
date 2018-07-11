package com.komodaa.app.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.komodaa.app.R;
import com.komodaa.app.adapters.PrivateMessagesAdapter;
import com.komodaa.app.interfaces.NetworkCallback;
import com.komodaa.app.models.Error;
import com.komodaa.app.models.Message;
import com.komodaa.app.utils.ApiClient;
import com.komodaa.app.utils.Komovatar;
import com.komodaa.app.utils.UserUtils;
import com.komodaa.app.utils.Utils;
import com.pkmmte.view.CircularImageView;
import com.rey.material.widget.ImageButton;
import com.rey.material.widget.ProgressView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChatActivity extends AppCompatActivity {

    @BindView(R.id.recList) RecyclerView recList;
    @BindView(R.id.btnSend) ImageButton btnSend;
    @BindView(R.id.etComment) EditText etComment;
    @BindView(R.id.rlSend) RelativeLayout rlSend;
    @BindView(R.id.progress) ProgressView progress;
    @BindView(R.id.rlProgress) RelativeLayout rlProgress;
    @BindView(R.id.imgTBAvatar) CircularImageView imgTBAvatar;
    @BindView(R.id.tvTBTitle) TextView tvTBTitle;
    @BindView(R.id.TBContainer) RelativeLayout TBContainer;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.newMsgNotifier) TextView newMsgNotifier;
    private PrivateMessagesAdapter adapter;
    private long userId;
    int lastMessageId = 0;
    private String TAG = "ChatActivity";
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Broadcast Message Received");
            if (intent.hasExtra("user_id") && intent.getIntExtra("user_id", -111) == userId) {
                reload(true);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Utils.getColorFromResource(this, R.color.colorPrimaryDark));
        }

        Intent intent = getIntent();
        if (intent == null || !intent.hasExtra("user_id")) {
            finish();
        }
        userId = intent.getIntExtra("user_id", -10);
        if (userId == -10) {
            userId = intent.getLongExtra("user_id", -1);
        }
        if (userId < 1) {
            finish();
        }

        String name = intent.getStringExtra("user_name");
        String avatarUrl = intent.getStringExtra("avatar");

        toolbar.setTitle(" ");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvTBTitle.setText(name);
        if (!TextUtils.isEmpty(avatarUrl) && avatarUrl.length() > 10) {
            Picasso.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_account_circle_48px)
                    .error(R.drawable.ic_account_circle_48px)
                    .fit().centerInside()
                    .into(imgTBAvatar);
        } else {
            int randomAvatar = Komovatar.getRandomAvatar();
            imgTBAvatar.setImageResource(randomAvatar);
        }

        adapter = new PrivateMessagesAdapter();
        LinearLayoutManager lManager = new LinearLayoutManager(this);
        lManager.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(lManager);
        recList.setHasFixedSize(true);
        //recList.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity()).margin(Utils.dpToPx(getActivity(), 16)).build());
        recList.setAdapter(adapter);

        reload(false);
        btnSend.setEnabled(false);

        etComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                btnSend.setEnabled(s.length() > 0);
            }
        });

        newMsgNotifier.setOnClickListener(v -> {
            if (adapter != null && adapter.getItemCount() > 0) {
                recList.smoothScrollToPosition(adapter.getItemCount() - 1);
                v.setVisibility(View.GONE);
            }
        });
    }

    @OnClick(R.id.btnSend)
    public void onBtnSendClicked() {
        if (!UserUtils.isLoggedIn()) {
            Utils.displayLoginErrorDialog(this);
            return;
        }
        String message = etComment.getText().toString();
        if (TextUtils.isEmpty(message)) {
            etComment.setError("پیامت رو بنویس");
            return;
        }
        btnSend.setEnabled(false);
        JSONObject data = null;
        try {
            data = new JSONObject();
            data.put("receiver_id", userId);
            data.put("message", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApiClient.makeRequest(this, "POST", "/private_message/", data, new NetworkCallback() {
            @Override public void onSuccess(int status, JSONObject data) {
                try {
                    btnSend.setEnabled(true);
                    etComment.setText("");
                    if (data.has("last_message_id")) {
                        lastMessageId = data.getInt("last_message_id");
                        Message m = new Message(true, message, Utils.getCurrentDate());
                        List<Message> list = new ArrayList<>();
                        list.add(m);
                        adapter.setData(list, true);
                        adapter.notifyDataSetChanged();
                        recList.smoothScrollToPosition(adapter.getItemCount() - 1);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override public void onFailure(int status, Error error) {
                btnSend.setEnabled(true);
            }
        });
    }

    @Override protected void onPause() {
        // Unregister since the activity is paused.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
                mMessageReceiver);
        super.onPause();
    }

    @Override protected void onResume() {
        // Register to receive messages.
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named "new_message_arrived".
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("new_message_arrived"));
        super.onResume();
    }

    public void reload(boolean silentRefresh) {
        if (!silentRefresh) {
            rlProgress.setVisibility(View.VISIBLE);
            progress.setVisibility(View.VISIBLE);
        }

        ApiClient.makeRequest(this, "GET", "/private_message/conversation/" + userId, null, new NetworkCallback() {
            @Override
            public void onSuccess(int status, JSONObject data) {

                try {
                    rlProgress.setVisibility(View.GONE);
                    progress.setVisibility(View.GONE);
                    lastMessageId = data.getInt("last_message_id");
                    JSONArray array = data.getJSONArray("messages");
                    List<Message> list = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        list.add(new Message(array.getJSONObject(i)));
                    }


                    if (list.size() > 0) {
                        adapter.setData(list, false);
                        adapter.notifyDataSetChanged();
                        if (recList.getAdapter() == null) {
                            recList.setAdapter(adapter);
                        }
                        if (!silentRefresh) {
                            recList.smoothScrollToPosition(adapter.getItemCount() - 1);
                        }
                        if (silentRefresh && ((LinearLayoutManager) recList.getLayoutManager()).findLastCompletelyVisibleItemPosition() < adapter.getItemCount()) {
                            newMsgNotifier.setVisibility(View.VISIBLE);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int status, Error error) {

                if (rlProgress != null) {
                    rlProgress.setVisibility(View.GONE);
                    progress.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
//        if (id == R.id.action_open_source) {
//            new LibsBuilder()
//                    //provide a style (optional) (LIGHT, DARK, LIGHT_DARK_TOOLBAR)
//                    .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
//                    //start the activity
//                    .start(this);
//            return true;
//
//        } else
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

