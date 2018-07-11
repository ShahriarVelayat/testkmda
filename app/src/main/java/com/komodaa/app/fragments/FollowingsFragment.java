package com.komodaa.app.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.komodaa.app.App;
import com.komodaa.app.R;
import com.komodaa.app.activities.HomeActivity;
import com.komodaa.app.activities.MainActivity;
import com.komodaa.app.activities.UserProfileActivity;
import com.komodaa.app.interfaces.NetworkCallback;
import com.komodaa.app.models.Error;
import com.komodaa.app.utils.ApiClient;
import com.komodaa.app.utils.UserUtils;
import com.komodaa.app.adapters.UsersListAdapter;
import com.komodaa.app.utils.Utils;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by nevercom on 3/2/17.
 */

public class FollowingsFragment extends BaseFragment {
    @BindView(R.id.recyclerView) RecyclerView mRecyclerView;
    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout mRefresh;
    //@BindView(R.id.fabTop) android.support.design.widget.FloatingActionButton fabTop;
    @BindView(R.id.llNoContent) LinearLayout llNoContent;
    private Unbinder unbinder;

    boolean loading = true;
    int visibleThreshold = 1;
    int previousTotal = 0;
    int mMaxRecords = 0;
    int mOffset = 0;
    int firstVisibleItem, visibleItemCount, totalItemCount;
    int page = 1;
    int maxPages;
    UsersListAdapter adapter;
    int lastPage = 0;
    private int scWidth;

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_following, container, false);
        Bundle b = getArguments();

        unbinder = ButterKnife.bind(this, v);
        scWidth = getActivity().getWindowManager().getDefaultDisplay().getWidth();
        return v;
    }

    @OnClick(R.id.fabTop) void onFabClick() {
        mRecyclerView.smoothScrollToPosition(0);
    }

    @Override public void onResume() {
        super.onResume();
        if (UserUtils.isLoggedIn()) {
            mRefresh.setVisibility(View.VISIBLE);
            //fabTop.setVisibility(View.VISIBLE);
            llNoContent.setVisibility(View.GONE);
            performSearch();
        } else {

            mRefresh.setVisibility(View.INVISIBLE);
            //fabTop.setVisibility(View.INVISIBLE);
            llNoContent.setVisibility(View.VISIBLE);

        }
    }

    private Rect getRectForPosition(int totalTabs, int position) {
        View tab = getActivity().findViewById(R.id.tabs);
        int top = tab.getTop();
        int w = tab.getWidth();
        int h = tab.getHeight();
        int unit = w / totalTabs;
        Log.d("", String.format("Top =  %d, w = %d, h = %d, unit = %d", top, w, h, unit));
        return new Rect((w - (position * unit) + (unit / 2)) - (unit / 4), top + (h / 2), (w - (position * unit) + (unit / 2)) + (unit / 4), top + h + (h / 2));
    }

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            try {
                ((HomeActivity) getActivity()).displaySearchButton(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            SharedPreferences prefs = App.getPrefs();
            if (prefs.getBoolean("is_first_time_following_fragment", true)) {
                prefs.edit().putBoolean("is_first_time_following_fragment", false).apply();

                TapTargetView.showFor(getActivity(), TapTarget.forBounds(
                        getRectForPosition(3, 3),
                        Utils.generateTypeFacedString(getActivity(), "توی کمدا می تونی افرادی رو که می خوای دنبال (فالو) کنی.\nاز این قسمت فقط آیتم های دوستات دیده می شن.")).id(1));
            }
        }
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new UsersListAdapter();

        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity()).margin(Utils.dpToPx(getActivity(), 16)).build());

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                visibleItemCount = mRecyclerView.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                Log.d("", String.format("onScrolled: MaxPages: %d, Current Page: %d, Dy: %d", maxPages, page, dy));
                if (maxPages <= page) {
                    mRefresh.setRefreshing(false);
                    return;
                }
//                if (recyclerView.computeVerticalScrollOffset() > scWidth * 10) {
//                    fabTop.show();
//                } else {
//                    fabTop.hide();
//                }
                if (dy < 0) {
                    return;
                }

                if (loading) {
                    if (totalItemCount > previousTotal) {
                        loading = false;
                        previousTotal = totalItemCount;
                    }
                }
                if (!loading && (totalItemCount - visibleItemCount)
                        <= (firstVisibleItem + visibleThreshold)) {
                    // End has been reached
                    loading = true;
                    page++;
                    performSearch();
                }
                // mRefresh.setRefreshing(loading);
            }
        });

        mRecyclerView.setAdapter(adapter);


        mRefresh.setOnRefreshListener(() -> {
            mOffset = 0;
            mMaxRecords = 0;
            previousTotal = 0;
            page = 1;
            lastPage = 0;
            loading = false;
            performSearch();
        });

        adapter.setOnItemClickListener((view1, position, data) -> {
            try {
                Intent i = new Intent(getActivity(), UserProfileActivity.class);
                long id = ((JSONObject) data).getLong("user_id");
                i.putExtra("user_id", id);
                startActivity(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });


    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    void performSearch() {
        if (page == lastPage) {
            return;
        }
        lastPage = page;
        mRefresh.setRefreshing(true);
        JSONObject data = null;
        try {
            data = new JSONObject();
            data.put("page", page);
            data.put("count", 20);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApiClient.makeRequest(getActivity(), "GET", "/followings", data, new NetworkCallback() {
            @Override public void onSuccess(int status, JSONObject data) {
                try {
                    if (mRefresh == null || !isAdded()) {
                        return;
                    }
                    mRefresh.setRefreshing(false);
                    Log.d("", "onSuccess: " + data.toString());
                    maxPages = data.getInt("total_pages");
                    JSONArray users = data.getJSONArray("data");
                    List<JSONObject> list = new ArrayList<>();
                    for (int i = 0; i < users.length(); i++) {
                        list.add(users.getJSONObject(i));
                    }
                    if (list.size() > 0) {
                        mRefresh.setVisibility(View.VISIBLE);
                        //fabTop.setVisibility(View.VISIBLE);
                        llNoContent.setVisibility(View.INVISIBLE);
                        adapter.setData(list, page > 1);
                        adapter.notifyDataSetChanged();
                    } else {
                        mRefresh.setVisibility(View.INVISIBLE);
                        //fabTop.setVisibility(View.INVISIBLE);
                        llNoContent.setVisibility(View.VISIBLE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override public void onFailure(int status, Error error) {
                if (mRefresh == null || !isAdded()) {
                    return;
                }
                try {
                    mRefresh.setVisibility(View.INVISIBLE);
                    //fabTop.setVisibility(View.INVISIBLE);
                    llNoContent.setVisibility(View.VISIBLE);
                    mRefresh.setRefreshing(false);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    @Override public String getTitle() {
        return "دوستام";
    }


}
