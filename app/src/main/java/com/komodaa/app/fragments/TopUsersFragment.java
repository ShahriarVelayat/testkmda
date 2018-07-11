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
import com.komodaa.app.activities.UserProfileActivity;
import com.komodaa.app.interfaces.NetworkCallback;
import com.komodaa.app.models.Error;
import com.komodaa.app.utils.ApiClient;
import com.komodaa.app.adapters.UsersListAdapter;
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


public class TopUsersFragment extends BaseFragment {
    @BindView(R.id.recyclerView) RecyclerView mRecyclerView;
    @BindView(R.id.itemsProgress) ProgressView itemsProgress;
    private Unbinder unbinder;
    private MessageCenterFragment.MessagesAdapter adapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.activity_top_sellers, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle b = getArguments();

        boolean isSellersList = b.getBoolean("sellers", true);

        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity()).margin(Utils.dpToPx(getActivity(), 16)).build());
        final UsersListAdapter adapter = new UsersListAdapter();


        mRecyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener((v, position, data) -> {
            try {
                Intent i = new Intent(getActivity(), UserProfileActivity.class);
                long id = ((JSONObject) data).getLong("user_id");
                i.putExtra("user_id", id);
                startActivity(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        itemsProgress.setVisibility(View.VISIBLE);
        ApiClient.makeRequest(getActivity(), "GET", isSellersList ? "/sellers/top/20" : "/buyers/top/20", null, new NetworkCallback() {
            @Override
            public void onSuccess(int status, JSONObject data) {
                try {
                    itemsProgress.setVisibility(View.INVISIBLE);
                    JSONArray users = data.getJSONArray("data");
                    List<JSONObject> list = new ArrayList<>();
                    for (int i = 0; i < users.length(); i++) {
                        list.add(users.getJSONObject(i));
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

            @Override
            public void onFailure(int status, Error error) {
                //itemsProgress.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
