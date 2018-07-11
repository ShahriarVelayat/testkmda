package com.komodaa.app.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.komodaa.app.R;
import com.komodaa.app.adapters.ConversationsAdapter;
import com.komodaa.app.interfaces.NetworkCallback;
import com.komodaa.app.models.Conversation;
import com.komodaa.app.models.Error;
import com.komodaa.app.utils.ApiClient;
import com.komodaa.app.utils.Utils;
import com.rey.material.widget.ProgressView;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ConversationsActivity extends AppCompatActivity {

    @BindView(R.id.recList) RecyclerView recList;
    @BindView(R.id.progress) ProgressView itemsProgress;
    @BindView(R.id.rlProgress) RelativeLayout rlProgress;
    private ConversationsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);
        ButterKnife.bind(this);

        getSupportActionBar().setTitle(Utils.generateTypeFacedString(this, getTitle().toString()));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        recList.setLayoutManager(mLayoutManager);
        recList.setHasFixedSize(true);
        recList.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this)
                .size(Utils.dpToPx(this, 1))
                .margin(Utils.dpToPx(this, 8), Utils.dpToPx(this, 64))
                .color(Color.parseColor("#d2d2d2")).build());

        adapter = new ConversationsAdapter();
        recList.setAdapter(adapter);

        adapter.setOnItemClickListener((view, position, data) -> {
            Conversation c = (Conversation) data;
            Intent intent = new Intent(this, ChatActivity.class)
                    .putExtra("user_id", c.getUser().getId())
                    .putExtra("user_name", c.getUser().getFirstName())
                    .putExtra("avatar", c.getUser().getAvatarUrl());
            startActivity(intent);
        });

    }

    @Override protected void onResume() {
        super.onResume();

        refresh();
    }

    private void refresh() {
        rlProgress.setVisibility(View.VISIBLE);
        itemsProgress.setVisibility(View.VISIBLE);

        ApiClient.makeRequest(this, "GET", "/private_message/conversations", null, new NetworkCallback() {
            @Override public void onSuccess(int status, JSONObject data) {
                try {
                    rlProgress.setVisibility(View.INVISIBLE);
                    itemsProgress.setVisibility(View.INVISIBLE);
                    JSONArray activities = data.getJSONArray("data");
                    List<Conversation> list = new ArrayList<>();
                    for (int i = 0; i < activities.length(); i++) {
                        list.add(new Conversation(activities.getJSONObject(i)));
                    }
                    if (list.size() > 0) {

                        adapter.setData(list, false);
                        adapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    rlProgress.setVisibility(View.INVISIBLE);
                    itemsProgress.setVisibility(View.INVISIBLE);
                }


            }

            @Override public void onFailure(int status, Error error) {
                //itemsProgress.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
