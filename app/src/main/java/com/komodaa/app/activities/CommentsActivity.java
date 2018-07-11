package com.komodaa.app.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.komodaa.app.R;
import com.komodaa.app.interfaces.NetworkCallback;
import com.komodaa.app.models.Comment;
import com.komodaa.app.models.Error;
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
import cn.pedant.SweetAlert.SweetAlertDialog;

public class CommentsActivity extends AppCompatActivity {

    @BindView(R.id.recList) RecyclerView recList;
    @BindView(R.id.btnSend) ImageButton btnSend;
    @BindView(R.id.etComment) EditText etComment;
    @BindView(R.id.tvReplyTo) TextView tvReplyTo;
    @BindView(R.id.imgReplyClose) ImageView imgReplyClose;
    @BindView(R.id.rlReplyTo) RelativeLayout rlReplyTo;
    @BindView(R.id.rlSend) RelativeLayout rlSend;
    @BindView(R.id.progress) ProgressView progress;
    @BindView(R.id.rlProgress) RelativeLayout rlProgress;
    private CommentsAdapter adapter;
    private int itemId;
    private long replyToCommentId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        ButterKnife.bind(this);

        getSupportActionBar().setTitle(Utils.generateTypeFacedString(this, getTitle().toString()));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapter = new CommentsAdapter();

        LinearLayoutManager lManager = new LinearLayoutManager(this);
        lManager.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(lManager);
        recList.setHasFixedSize(true);
        //recList.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity()).margin(Utils.dpToPx(getActivity(), 16)).build());
        recList.setAdapter(adapter);

        Intent intent = getIntent();

        itemId = intent.getIntExtra("item_id", -1);

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
    }

    @OnClick(R.id.btnSend)
    public void onBtnSendClicked() {
        if (!UserUtils.isLoggedIn()) {
            Utils.displayLoginErrorDialog(this);
            return;
        }
        if (TextUtils.isEmpty(etComment.getText().toString())) {
            etComment.setError("نظرت رو بنویس");
            return;
        }
        btnSend.setEnabled(false);
        final Handler handler = new Handler(Looper.getMainLooper());
        JSONObject postData = new JSONObject();
        try {
            postData.put("comment", etComment.getText().toString());
            if (replyToCommentId > 0) {
                postData.put("to", replyToCommentId);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Utils.getColorFromResource(this, R.color.colorPrimary));
        pDialog.setTitleText("درحال ارسال نظرت به کمدا هستیم");
        pDialog.setContentText(getString(R.string.loadin_please_wait));
        pDialog.setCancelable(false);
        pDialog.show();
        ApiClient.makeRequest(this, "POST", "/comment/" + itemId, postData, new NetworkCallback() {
            @Override
            public void onSuccess(int status, JSONObject data) {
                handler.post(() -> {
                    btnSend.setEnabled(true);
                    etComment.setText("");
                    pDialog.setTitleText("پیام ثبت شد")
                            .setContentText("هروقت جوابت رو بده خبرت می کنیم")
                            .setConfirmText("باشه")
                            .showCancelButton(false)
                            .setConfirmClickListener(Dialog::dismiss)
                            .setCancelClickListener(Dialog::dismiss)
                            .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);

                    rlReplyTo.setVisibility(View.GONE);
                    replyToCommentId = 0;
                    etComment.setText("");
                    reload(true);
                });
            }

            @Override
            public void onFailure(int status, final Error error) {
                handler.post(() -> {
                    btnSend.setEnabled(true);
                    pDialog.setTitleText("مثل اینکه خطایی رخ داده")
                            .setContentText(error.getMessage())
                            .setConfirmText("باشه")
                            .showCancelButton(false)
                            .setConfirmClickListener(Dialog::dismiss)
                            .setCancelClickListener(Dialog::dismiss)
                            .changeAlertType(SweetAlertDialog.ERROR_TYPE);
                });
            }
        });
    }

    @OnClick(R.id.imgReplyClose)
    public void onImgReplyCloseClicked() {
        rlReplyTo.setVisibility(View.GONE);
        replyToCommentId = 0;
    }


    private void replyTo(long commentId, String userName) {
        this.replyToCommentId = commentId;
        rlReplyTo.setVisibility(View.VISIBLE);
        tvReplyTo.setText(String.format("درج نظر (در پاسخ به %s)", userName));
        etComment.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(etComment, InputMethodManager.SHOW_IMPLICIT);

    }

    public void reload(final boolean newComment) {

        rlProgress.setVisibility(View.VISIBLE);
        progress.setVisibility(View.VISIBLE);
        ApiClient.makeRequest(this, "GET", "/comment/list/" + itemId, null, new NetworkCallback() {
            @Override
            public void onSuccess(int status, JSONObject data) {

                try {
                    rlProgress.setVisibility(View.GONE);
                    progress.setVisibility(View.GONE);
                    int totalPages = data.getInt("total_pages");
                    JSONArray array = data.getJSONArray("data");
                    List<Comment> list = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        list.add(new Comment(array.getJSONObject(i)));
                    }

                    adapter.addData(toThreadedComments(list), false);
                    adapter.notifyDataSetChanged();
                    if (recList.getAdapter() == null) {
                        recList.setAdapter(adapter);
                    }
                    if (newComment) {
                        recList.smoothScrollToPosition(adapter.getItemCount() - 1);
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

    public static List<Comment> toThreadedComments(List<Comment> comments) {

        //comments should be sorted by date first

        //The resulting array of threaded comments
        List<Comment> threaded = new ArrayList<>();

        //An array used to hold processed comments which should be removed at the end of the cycle
        List<Comment> removeComments = new ArrayList<>();

        //get the root comments first (comments with no parent)
        for (int i = 0; i < comments.size(); i++) {
            Comment c = comments.get(i);
            if (c.getParentId() == 0) {
                c.setCommentDepth(0); //A property of Comment to hold its depth
                c.setChildCount(0); //A property of Comment to hold its child count
                threaded.add(c);
                removeComments.add(c);
            }
        }

        if (removeComments.size() > 0) {
            //clear processed comments
            comments.removeAll(removeComments);
            removeComments.clear();
        }

        int depth = 0;
        //get the child comments up to a max depth of 10
//        while (comments.size() > 0 && depth <= 10) {
//            depth++;
        for (int j = 0; j < comments.size(); j++) {
            Comment child = comments.get(j);
            //check root comments for match
            for (int i = 0; i < threaded.size(); i++) {
                Comment parent = threaded.get(i);
                if (parent.getId() == child.getParentId()) {
                    parent.setChildCount(parent.getChildCount() + 1);
                    child.setCommentDepth(1);
                    threaded.add(i + parent.getChildCount(), child);
                    removeComments.add(child);
                    continue;
                }
            }
        }
        if (removeComments.size() > 0) {
            //clear processed comments
            comments.removeAll(removeComments);
            removeComments.clear();
        }

        for (int k = 0; k < threaded.size(); k++) {
            Comment c = threaded.get(k);
            if (c.getCommentDepth() == 0) {
                if (k == 0) {
                    continue;
                }
                Comment last = threaded.get(k - 1);
                if (last.getCommentDepth() == 0) {
                    continue;
                }
                last.setLastChild(true);
                threaded.set(k - 1, last);
            } else if (k == threaded.size() - 1) {
                Comment last = threaded.get(k);
                if (last.getCommentDepth() == 0) {
                    continue;
                }
                last.setLastChild(true);
                threaded.set(k, last);
            }


        }
//        }
//        int index = 0;
//        for (Comment c : threaded) {
//            Log.d("ThreadedComment", String.format(Locale.US, "index: %d, id: %d, ParentId: %d, ChildsCount: %d, Depth: %d", index, c.getId(), c.getParentId(), c.getChildCount(), c.getCommentDepth()));
//            index++;
//        }
        return threaded;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                // This is called when the Home (Up) button is pressed
                // in the Action Bar.
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {
        private final Komovatar komovatar;
        List<Comment> list;
        int childsCount = 0;
        int itemsShown = 0;
        private Context context;

        public CommentsAdapter() {

            komovatar = new Komovatar();
        }

        public void addData(List<Comment> data, boolean append) {
            if (append && list != null) {
                list.addAll(data);
            } else {
                list = data;
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            context = parent.getContext();
            View v = LayoutInflater.from(context).inflate(R.layout.row_comment_nested, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            final Comment c = list.get(position);

            if (c.getCommentDepth() > 0) {
                childsCount += c.getChildCount();
                itemsShown += 1;
                holder.nested.setVisibility(View.VISIBLE);
                holder.divider.setVisibility(View.GONE);

            } else {
                if (position > 0) {
                    holder.divider.setVisibility(View.VISIBLE);
                } else {
                    holder.divider.setVisibility(View.GONE);
                }
                holder.nested.setVisibility(View.GONE);
                childsCount = c.getChildCount();
                itemsShown = 0;

            }
            if (c.isLastChild()) {
                holder.nested.setBackgroundResource(R.drawable.ic_two_way);

            } else {
                holder.nested.setBackgroundResource(R.drawable.ic_three_way);
            }
            if ((c.getChildCount() > 0 || c.getCommentDepth() > 0) && !c.isLastChild()) {
                holder.joint.setVisibility(View.VISIBLE);
            } else {
                holder.joint.setVisibility(View.GONE);

            }
            Log.d("Nested", String.format("position: %d => ChildCount: %d, Depth: %d, ItemsShown: %d", position, childsCount, c.getCommentDepth(), itemsShown));

            holder.tvCommentText.setText(c.getText());
            if (c.getParentId() > 0 && c.getTo() != null) {
                holder.tvCommentRepliedTo.setText(String.format("در پاسخ به %s", c.getTo().getFirstName()));
            } else {
                holder.tvCommentRepliedTo.setText("");
            }
            holder.tvCommentTitle.setText(c.getUserName());
            String avatarUrl = c.getAvatarUrl();

            if (!TextUtils.isEmpty(avatarUrl) && avatarUrl.length() > 10) {
//                int width = holder.imgAvatar.getWidth();
//                if (width < 1) {
//                    width = 200;
//                }
//                Log.d("Avatar", "size: " + width);
//                avatarUrl = Gravatar.init().with(c.getEmail()).force404().size(width).build();

                Picasso.with(context)
                        .load(avatarUrl)
                        .placeholder(R.drawable.no_avatar)
                        .error(R.drawable.no_avatar)
                        .fit().centerInside()
                        .into(holder.imgAvatar);
            } else {
                holder.imgAvatar.setImageResource(komovatar.getOrderedAvatar((int) c.getUserId()));
            }


        }

        @Override
        public int getItemCount() {
            return list == null ? 0 : list.size();
        }


        class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.divider) View divider;
            @BindView(R.id.rlComment) RelativeLayout rlComment;
            @BindView(R.id.imgAvatar) CircularImageView imgAvatar;
            @BindView(R.id.imgReply) ImageView imgReply;
            @BindView(R.id.imgNested) ImageView nested;
            @BindView(R.id.imgJoint) ImageView joint;
            @BindView(R.id.tvCommentTitle) TextView tvCommentTitle;
            @BindView(R.id.tvCommentRepliedTo) TextView tvCommentRepliedTo;
            @BindView(R.id.tvCommentText) TextView tvCommentText;

            ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);

                imgReply.setOnClickListener(v -> {
                    Comment c = list.get(getAdapterPosition());
                    replyTo(c.getId(), c.getUserName());

                });
                imgAvatar.setOnClickListener(v -> {
                    if (UserUtils.isLoggedIn()) {
                        startActivity(new Intent(context, UserProfileActivity.class).putExtra("user_id", list.get(getAdapterPosition()).getUserId()));
                    }
                });
            }
        }
    }
}
