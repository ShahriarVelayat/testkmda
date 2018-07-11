package com.komodaa.app.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.komodaa.app.R;
import com.komodaa.app.activities.ChatActivity;
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
import butterknife.Unbinder;

/**
 * Created by nevercom on 1/25/17.
 */

public class ConversationsFragment extends BaseFragment {
    @BindView(R.id.recList) RecyclerView recList;
    @BindView(R.id.progress) ProgressView itemsProgress;
    @BindView(R.id.rlProgress) RelativeLayout rlProgress;
    private ConversationsAdapter adapter;
    private Unbinder unbinder;


    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.activity_conversations, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recList.setLayoutManager(mLayoutManager);
        recList.setHasFixedSize(true);
        recList.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity())
                .size(Utils.dpToPx(getActivity(), 1))
                .margin(Utils.dpToPx(getActivity(), 8), Utils.dpToPx(getActivity(), 64))
                .color(Color.parseColor("#d2d2d2")).build());

        adapter = new ConversationsAdapter();
        recList.setAdapter(adapter);

        adapter.setOnItemClickListener((v, position, data) -> {
            Conversation c = (Conversation) data;
            Intent intent = new Intent(getActivity(), ChatActivity.class)
                    .putExtra("user_id", c.getUser().getId())
                    .putExtra("user_name", c.getUser().getFirstName())
                    .putExtra("avatar", c.getUser().getAvatarUrl());
            startActivity(intent);
        });
    }

    @Override public void onResume() {
        super.onResume();

        refresh();
    }

    private void refresh() {
        rlProgress.setVisibility(View.VISIBLE);
        itemsProgress.setVisibility(View.VISIBLE);

        ApiClient
                .makeRequest(getActivity(), "GET", "/private_message/conversations", null, new NetworkCallback() {
                    @Override public void onSuccess(int status, JSONObject data) {
                        try {
                            if (rlProgress != null) {
                                rlProgress.setVisibility(View.INVISIBLE);
                            }
                            if (itemsProgress != null) {

                                itemsProgress.setVisibility(View.INVISIBLE);
                            }
                            JSONArray          activities = data.getJSONArray("data");
                            List<Conversation> list       = new ArrayList<>();
                            for (int i = 0; i < activities.length(); i++) {
                                list.add(new Conversation(activities.getJSONObject(i)));
                            }
                            if (list.size() > 0) {

                                adapter.setData(list, false);
                                adapter.notifyDataSetChanged();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            if (rlProgress != null) {
                                rlProgress.setVisibility(View.INVISIBLE);
                            }
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
