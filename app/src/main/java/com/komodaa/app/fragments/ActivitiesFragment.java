package com.komodaa.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.komodaa.app.R;
import com.komodaa.app.activities.ProductDetailsActivity;
import com.komodaa.app.activities.UserProfileActivity;
import com.komodaa.app.adapters.ActivitiesAdapter;
import com.komodaa.app.interfaces.NetworkCallback;
import com.komodaa.app.models.Error;
import com.komodaa.app.models.UserActivity;
import com.komodaa.app.utils.ApiClient;
import com.rey.material.widget.ProgressView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by nevercom on 2/20/18.
 */

public class ActivitiesFragment extends BaseFragment {

    @BindView(R.id.itemsProgress) ProgressView itemsProgress;
    @BindView(R.id.recList) RecyclerView recList;
    private Unbinder unbinder;
    private ActivitiesAdapter adapter;

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.activity_timeline, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recList.setLayoutManager(mLayoutManager);
        recList.setHasFixedSize(true);
        //recList.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this).margin(Utils.dpToPx(this, 16)).build());

        adapter = new ActivitiesAdapter();
        recList.setAdapter(adapter);

        adapter.setOnItemClickListener((v, position, data) -> {
            UserActivity ac = (UserActivity) data;
            if (view.getId() == R.id.imgAvatar) {
                Intent intent = new Intent(getActivity(), UserProfileActivity.class);
                intent.putExtra("user_id", ac.getUser().getId());
                startActivity(intent);
                return;
            }
            if (ac.getObjectType() == UserActivity.TYPE_ITEM) {
                Intent intent = new Intent(getActivity(), ProductDetailsActivity.class);
                intent.putExtra("item_id", ac.getObjectId() + "");
                startActivity(intent);
            } else if (ac.getObjectType() == UserActivity.TYPE_USER) {
                Intent intent = new Intent(getActivity(), UserProfileActivity.class);
                intent.putExtra("user_id", ac.getObjectId());
                startActivity(intent);
            }
        });

        itemsProgress.setVisibility(View.VISIBLE);

        ApiClient.makeRequest(getActivity(), "GET", "/event_stream", null, new NetworkCallback() {
            @Override public void onSuccess(int status, JSONObject data) {
                try {
                    if (itemsProgress != null) {
                        itemsProgress.setVisibility(View.INVISIBLE);
                    }
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
                    if (itemsProgress != null) {
                        itemsProgress.setVisibility(View.INVISIBLE);
                    }
                }


            }

            @Override public void onFailure(int status, Error error) {
                //itemsProgress.setVisibility(View.INVISIBLE);
            }
        });

    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

}
