package com.komodaa.app.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.komodaa.app.App;
import com.komodaa.app.R;
import com.komodaa.app.activities.ProductDetailsActivity;
import com.komodaa.app.interfaces.NetworkCallback;
import com.komodaa.app.interfaces.OnItemClickListener;
import com.komodaa.app.models.Error;
import com.komodaa.app.models.Product;
import com.komodaa.app.utils.ApiClient;
import com.komodaa.app.utils.Blur;
import com.komodaa.app.utils.UserUtils;
import com.komodaa.app.utils.Utils;
import com.rey.material.widget.FloatingActionButton;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

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

public class FollowingsItemsFragment extends BaseFragment {
    @BindView(R.id.recyclerView) RecyclerView mRecyclerView;
    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout mRefresh;
    @BindView(R.id.fabTop) android.support.design.widget.FloatingActionButton fabTop;
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
    ListAdapter adapter;

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
            fabTop.setVisibility(View.VISIBLE);
            llNoContent.setVisibility(View.GONE);
            performSearch();
        } else {

            mRefresh.setVisibility(View.INVISIBLE);
            fabTop.setVisibility(View.INVISIBLE);
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
        setHasOptionsMenu(true);
    }

    @Override public void onPrepareOptionsMenu(Menu menu) {
        if (menu == null || !isAdded()) {
            return;
        }
        try {
            //menu.findItem(R.id.item_search).setVisible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPrepareOptionsMenu(menu);
        SharedPreferences prefs = App.getPrefs();
        if (prefs.getBoolean("is_first_time_following_fragment", true)) {
            prefs.edit().putBoolean("is_first_time_following_fragment", false).apply();

            TapTargetView.showFor(getActivity(), TapTarget.forBounds(
                    getRectForPosition(3, 3),
                    Utils.generateTypeFacedString(getActivity(), "توی کمدا می تونی افرادی رو که می خوای دنبال (فالو) کنی.\nاز این قسمت فقط آیتم های دوستات دیده می شن.")).id(1));
        }
    }


    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new ListAdapter();

        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
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
                if (recyclerView.computeVerticalScrollOffset() > scWidth * 10) {
                    fabTop.show();
                } else {
                    fabTop.hide();
                }
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
            loading = false;
            performSearch();
        });

        adapter.setOnItemClickListener((view1, position, data) -> {
            Intent i = new Intent(getActivity(), ProductDetailsActivity.class);
            i.putExtra("item", (Product)data);
            startActivity(i);
        });


    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    void performSearch() {
        JSONObject data = null;
        try {
            data = new JSONObject();
            data.put("count", 20);
            data.put("page", this.page);
            data.put("followings_items", 1);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        mRefresh.setRefreshing(true);

        ApiClient.makeRequest(getActivity(), "GET", "/products", data, new NetworkCallback() {
            @Override public void onSuccess(int status, JSONObject data) {
                try {
                    if (mRefresh == null || !isAdded()) {
                        return;
                    }
                    mRefresh.setRefreshing(false);
                    Log.d("", "onSuccess: " + data.toString());
                    maxPages = data.getInt("total_pages");
                    JSONArray products = data.getJSONArray("data");
                    List<Product> list = new ArrayList<>();
                    for (int i = 0; i < products.length(); i++) {
                        list.add(new Product(products.getJSONObject(i)));
                    }
                    if (list.size() > 0) {
                        mRefresh.setVisibility(View.VISIBLE);
                        fabTop.setVisibility(View.VISIBLE);
                        llNoContent.setVisibility(View.INVISIBLE);
                        adapter.setData(list, page > 1);
                        adapter.notifyDataSetChanged();
                    } else {
                        mRefresh.setVisibility(View.INVISIBLE);
                        fabTop.setVisibility(View.INVISIBLE);
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
                    fabTop.setVisibility(View.INVISIBLE);
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

    public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
        private List<Product> list;
        private Context context;
        OnItemClickListener mItemClickListener;
        private OnItemRemoveListener mItemRemoveListener;

        @Override
        public ListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            context = parent.getContext();
            View v = LayoutInflater.from(context).inflate(R.layout.row_product, parent, false);
            return new ListAdapter.ViewHolder(v);
        }


        void setData(List<Product> data, boolean append) {
            if (append && list != null) {
                list.addAll(data);
            } else {
                list = data;
            }
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN) @Override
        public void onBindViewHolder(final ListAdapter.ViewHolder holder, int position) {
            Product p = list.get(position);

            holder.tvPrice.setText(String.format("%s تومان", Utils.formatNumber(p.getPrice())));

            String itemStatus = p.isBrandNew() ? "کاملاً نو" : "در حد نو";
            holder.tvNewStatus.setText(itemStatus);
            if (p.isFeatured()) {
                holder.tvFeatured.setVisibility(View.VISIBLE);
            } else {
                holder.tvFeatured.setVisibility(View.GONE);
            }

            if (p.isBrandNew()) {
                holder.tvNewStatus.setBackgroundColor(Utils.getColorFromResource(context, R.color.new_item));
            } else {
                holder.tvNewStatus.setBackgroundColor(Utils.getColorFromResource(context, R.color.used_item));
            }
            holder.tvCommentsCount.setText(p.getCommentsCount() + "");
            Picasso.with(context).load(p.getCover().getPath()).fit().centerInside().placeholder(R.drawable.loading_main_activity).into(holder.imgProductImage, new Callback() {
                @Override public void onSuccess() {
                    if (!isAdded()) {
                        return;
                    }
                    Bitmap resource = ((BitmapDrawable) holder.imgProductImage.getDrawable()).getBitmap();
                    int intrinsicWidth = resource.getWidth();
                    int width = holder.imgProductImage.getWidth();
                    int intrinsicHeight = resource.getHeight();
                    int height = holder.imgProductImage.getHeight();
                    if ((intrinsicWidth < width) || intrinsicHeight < height) {
                        Log.d("Image Loaded", "onResourceReady: r.w = " + intrinsicWidth + ", i.w = " + width);
                        holder.imgProductImage.setBackground(new BitmapDrawable(getResources(), Blur.fastBlur(context, Utils.scaleImage(context, resource, 50), 25)));
                    }
                }

                @Override public void onError() {

                }
            });
        }

        @Override public int getItemCount() {
            return list == null ? 0 : list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            @BindView(R.id.imgProductImage) ImageView imgProductImage;
            @BindView(R.id.tvPrice) TextView tvPrice;
            @BindView(R.id.tvComments) TextView tvCommentsCount;
            @BindView(R.id.tvNewStatus) TextView tvNewStatus;
            @BindView(R.id.tvFeatured) TextView tvFeatured;
            @BindView(R.id.fabPurchase) FloatingActionButton fab;

            ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                itemView.setOnClickListener(this);
                fab.setOnClickListener(this);
                imgProductImage.getLayoutParams().height = ((Activity) context).getWindowManager().getDefaultDisplay().getWidth();

            }

            @Override public void onClick(View v) {
                if (mItemClickListener != null) {
                    int adapterPosition = getAdapterPosition();
                    mItemClickListener.onItemClick(v, adapterPosition, list.get(adapterPosition));
                }
            }
        }

        public void setOnItemClickListener(final OnItemClickListener itemClickListener) {
            this.mItemClickListener = itemClickListener;
        }

        public void setOnItemRemoveListener(final OnItemRemoveListener itemRemoveListener) {
            this.mItemRemoveListener = itemRemoveListener;
        }
    }


    public interface OnItemRemoveListener {
        void onItemRemove(int id);
    }


}
