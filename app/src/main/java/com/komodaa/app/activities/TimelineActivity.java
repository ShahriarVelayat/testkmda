package com.komodaa.app.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import com.komodaa.app.R;
import com.komodaa.app.adapters.ActivitiesAdapter;
import com.komodaa.app.interfaces.NetworkCallback;
import com.komodaa.app.models.Error;
import com.komodaa.app.models.UserActivity;
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

public class TimelineActivity extends AppCompatActivity {

    @BindView(R.id.recList) RecyclerView recList;
    @BindView(R.id.itemsProgress) ProgressView itemsProgress;
    private ActivitiesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        ButterKnife.bind(this);

        getSupportActionBar().setTitle(Utils.generateTypeFacedString(this, getTitle().toString()));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        recList.setLayoutManager(mLayoutManager);
        recList.setHasFixedSize(true);
        //recList.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this).margin(Utils.dpToPx(this, 16)).build());
        recList.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this)
                .size(Utils.dpToPx(this, 1))
                .margin(Utils.dpToPx(this, 8), Utils.dpToPx(this, 64))
                .color(Color.parseColor("#d2d2d2")).build());
        adapter = new ActivitiesAdapter();
        recList.setAdapter(adapter);

        adapter.setOnItemClickListener((view, position, data) -> {
            UserActivity ac = (UserActivity) data;
            if (view.getId() == R.id.imgAvatar) {
                Intent intent = new Intent(this, UserProfileActivity.class);
                intent.putExtra("user_id", ac.getUser().getId());
                startActivity(intent);
                return;
            }
            if (ac.getObjectType() == UserActivity.TYPE_ITEM) {
                Intent intent = new Intent(this, ProductDetailsActivity.class);
                intent.putExtra("item_id", ac.getObjectId() + "");
                startActivity(intent);
            } else if (ac.getObjectType() == UserActivity.TYPE_USER) {
                Intent intent = new Intent(this, UserProfileActivity.class);
                intent.putExtra("user_id", ac.getObjectId());
                startActivity(intent);
            }
        });

        itemsProgress.setVisibility(View.VISIBLE);

        ApiClient.makeRequest(this, "GET", "/event_stream", null, new NetworkCallback() {
            @Override public void onSuccess(int status, JSONObject data) {
                try {
                    itemsProgress.setVisibility(View.INVISIBLE);
                    JSONArray activities = data.getJSONArray("data");
                    List<UserActivity> list = new ArrayList<>();
                    for (int i = 0; i < activities.length(); i++) {
                        list.add(new UserActivity(activities.getJSONObject(i)));
                    }
                    if (list.size() > 0) {

                        adapter.setData(list, false);
                        adapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
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
