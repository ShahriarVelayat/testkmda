package com.komodaa.app.activities;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.komodaa.app.R;
import com.komodaa.app.fragments.MyItemsFragment;
import com.komodaa.app.interfaces.NetworkCallback;
import com.komodaa.app.models.Error;
import com.komodaa.app.models.Product;
import com.komodaa.app.utils.ApiClient;
import com.komodaa.app.utils.Blur;
import com.komodaa.app.utils.Utils;
import com.rey.material.widget.Button;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BookmarksActivity extends AppCompatActivity {

    @BindView(R.id.recList) RecyclerView recList;
    private BookmarksAdapter adapter;
    private boolean loading;
    private int maxPages = 0;
    private int page = 1;
    private int previousTotal;
    private int visibleThreshold = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);
        ButterKnife.bind(this);
        getSupportActionBar().setTitle(Utils.generateTypeFacedString(this, getTitle().toString()));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        recList.setLayoutManager(mLayoutManager);
        recList.setHasFixedSize(true);
        recList.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this).size(Utils.dpToPx(this, 4)).colorResId(android.R.color.transparent).build());
        adapter = new BookmarksAdapter();
        recList.setAdapter(adapter);
        reload();

        recList.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = recyclerView.getChildCount();
                int totalItemCount = mLayoutManager.getItemCount();
                int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

                if (maxPages <= page) {
                    return;
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
                    reload();
                }
                // mRefresh.setRefreshing(loading);
            }
        });

    }

    void reload() {
        JSONObject data = new JSONObject();
        try {
            data.put("page", page);
            data.put("count", 20);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApiClient.makeRequest(this, "GET", "/bookmarks", data, new NetworkCallback() {
            @Override
            public void onSuccess(int status, JSONObject data) {
                try {

                    Log.d("Data", data.toString());
                    JSONArray products = data.getJSONArray("data");
                    maxPages = data.getInt("total_pages");
                    List<Product> list = new ArrayList<>();
                    for (int i = 0; i < products.length(); i++) {
                        list.add(new Product(products.getJSONObject(i)));
                    }
                    adapter.setData(list, true);
                    adapter.notifyDataSetChanged();


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int status, Error error) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id) {

            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class BookmarksAdapter extends RecyclerView.Adapter<BookmarksAdapter.ViewHolder> {

        private Context c;
        List<Product> list;

        public BookmarksAdapter(List<Product> list) {
            this.list = list;
        }

        public BookmarksAdapter() {
        }

        void setData(List<Product> data, boolean append) {
            if (append && list != null) {
                list.addAll(data);
            } else {
                list = data;
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            c = parent.getContext();
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_bookmark, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Product p = list.get(position);

            String imageUrl = p.getCover().getPath();
            if (!TextUtils.isEmpty(imageUrl)) {
                Picasso.with(c).load(imageUrl).fit().centerInside().placeholder(R.drawable.loading_main_activity).into(holder.imgProduct, new Callback() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onSuccess() {

                        Bitmap resource = ((BitmapDrawable) holder.imgProduct.getDrawable()).getBitmap();
                        int intrinsicWidth = resource.getWidth();
                        int width = holder.imgProduct.getWidth();
                        int intrinsicHeight = resource.getHeight();
                        int height = holder.imgProduct.getHeight();
                        if ((intrinsicWidth < width) || intrinsicHeight < height) {
                            Log.d("Image Loaded", "onResourceReady: r.w = " + intrinsicWidth + ", i.w = " + width);
                            holder.imgProduct.setBackground(new BitmapDrawable(getResources(), Blur.fastBlur(c, Utils.scaleImage(c, resource, 50), 25)));
                        }
                    }

                    @Override
                    public void onError() {

                    }
                });
            }

            holder.tvCategory.setText(Utils.getCategoryName(p.getCategoryId()));
            holder.tvPrice.setText(String.format("قیمت: %s تومان", Utils.formatNumber(p.getPrice())));
        }

        @Override
        public int getItemCount() {
            return list != null ? list.size() : 0;
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.imgProduct) ImageView imgProduct;
            @BindView(R.id.tvCategory) TextView tvCategory;
            @BindView(R.id.tvPrice) TextView tvPrice;
            @BindView(R.id.btnPurchase) Button btnPurchase;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                btnPurchase.setOnClickListener(v -> startActivity(new Intent(c, ProductDetailsActivity.class).putExtra("item", list.get(getAdapterPosition()))));
            }
        }


    }
}
