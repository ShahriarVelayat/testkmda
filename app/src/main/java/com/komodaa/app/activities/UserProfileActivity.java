package com.komodaa.app.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.komodaa.app.R;
import com.komodaa.app.interfaces.NetworkCallback;
import com.komodaa.app.interfaces.OnItemClickListener;
import com.komodaa.app.models.Error;
import com.komodaa.app.models.Product;
import com.komodaa.app.utils.ApiClient;
import com.komodaa.app.utils.Komovatar;
import com.komodaa.app.utils.UserUtils;
import com.komodaa.app.utils.Utils;
import com.pkmmte.view.CircularImageView;
import com.rey.material.widget.Button;
import com.rey.material.widget.ProgressView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class UserProfileActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.imgAvatar) CircularImageView imgAvatar;
    @BindView(R.id.btnFollow) Button btnFollow;
    @BindView(R.id.btnEditProfile) Button btnEditProfile;
    @BindView(R.id.btnChat) RelativeLayout btnChat;
    @BindView(R.id.tvFollowersCount) TextView tvFollowersCount;
    @BindView(R.id.tvItemsCount) TextView tvItemsCount;
    @BindView(R.id.tvUserName) TextView tvUserName;
    @BindView(R.id.recList) RecyclerView recList;
    @BindView(R.id.progress) ProgressView progress;
    @BindView(R.id.itemsProgress) ProgressView itemsProgress;
    @BindView(R.id.tvUserTitle) TextView tvUserTitle;
    @BindView(R.id.imgTBAvatar) CircularImageView imgTBAvatar;
    @BindView(R.id.tvTBTitle) TextView tvTBTitle;
    @BindView(R.id.TBContainer) RelativeLayout TBContainer;
    @BindView(R.id.app_bar) AppBarLayout appBar;
    @BindView(R.id.imgVerified) ImageView imgVerified;
    @BindView(R.id.ratingBarQuality) MaterialRatingBar ratingBarQuality;
    @BindView(R.id.ratingBarPack) MaterialRatingBar ratingBarPack;
    @BindView(R.id.ratingBarStory) MaterialRatingBar ratingBarStory;
    @BindView(R.id.llRatingContainer) LinearLayout llRatingContainer;
    //@BindView(R.id.activity_user_profile) NestedScrollView nestedScroll;
    private long id;
    private int followersCount = 0;
    private ItemsAdapter adapter;
    int currentPage = 1;
    int maxPages = 1;
    private boolean loading = false;
    private String name;
    private String avatarUrl;


    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR = 0.9f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION = 200;

    private boolean mIsTheTitleVisible = false;
    private boolean mIsTheTitleContainerVisible = true;
    private boolean isItMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        toolbar.setTitle(" ");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        appBar.addOnOffsetChangedListener(this);

        final GridLayoutManager layoutManager = new GridLayoutManager(this, 3);

        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        //layoutManager.setAutoMeasureEnabled(true);
        recList.setLayoutManager(layoutManager);
        recList.addItemDecoration(new ItemOffsetDecoration(Utils.dpToPx(this, 1)));

        //recList.setNestedScrollingEnabled(false);
        recList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                Log.d("SVScroll", String.format("visibleItemCount: %d, firstVisibleItemPosition: %d, lastVisible: %d, totalItemCount: %d", visibleItemCount, firstVisibleItemPosition, layoutManager.findLastVisibleItemPosition(), totalItemCount));
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 6
                        && firstVisibleItemPosition >= 0) {
                    if (maxPages <= 1 || currentPage >= maxPages || loading) {
                        return;
                    }
                    currentPage++;
                    adapter.showProgress();
                    //nestedScroll.post(() -> nestedScroll.fullScroll(NestedScrollView.FOCUS_DOWN));
                    loading = true;
                    getItems(currentPage);

                }


            }
        });

//        nestedScroll.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
//            int measuredHeight = v.getChildAt(0).getMeasuredHeight();
//            int measuredHeight1 = v.getMeasuredHeight();
//
//
//            int svHeight = measuredHeight - measuredHeight1;
//            int offset = svHeight - scrollY;
////            Log.d("NestedScroll",
////                    String.format("scrollY: %d, itemHeight: %d, ViewHeight: %d, Offset: %d, lastVisible: %d",
////                    scrollY, measuredHeight, measuredHeight1, offset, layoutManager.findLastVisibleItemPosition()));
//            if (offset >= 0 && offset < 500) {
//                // End of list, time to get more
//                if (maxPages <= 1 || currentPage >= maxPages || loading) {
//                    return;
//                }
//                currentPage++;
//                adapter.showProgress();
//                //nestedScroll.post(() -> nestedScroll.fullScroll(NestedScrollView.FOCUS_DOWN));
//                loading = true;
//                getItems(currentPage);
//            }
//        });
        adapter = new ItemsAdapter();

        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch (adapter.getItemViewType(position)) {
                    case ItemsAdapter.VIEW_ITEM:
                        return 1;
                    case ItemsAdapter.VIEW_PROGRESS:
                        return 3; //number of columns of the grid
                    default:
                        return -1;
                }
            }
        });
        recList.setAdapter(adapter);

        adapter.setOnItemClickListener((view, position, data1) -> {

            Intent i = new Intent(UserProfileActivity.this, ProductDetailsActivity.class);
            i.putExtra("item", (Product) data1);
            startActivity(i);

        });

        id = intent.getIntExtra("user_id", -10);
        if (id == -10) {
            id = intent.getLongExtra("user_id", -10);
        }
        if (id < 1) {
            finish();
        }

        isItMe = id == UserUtils.getUser().getId();

        btnFollow.setVisibility(View.INVISIBLE);
        btnChat.setVisibility(View.INVISIBLE);
        btnEditProfile.setVisibility(View.INVISIBLE);

        btnEditProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

    }

    @Override
    protected void onResume() {
        super.onResume();

        ApiClient.makeRequest(this, "GET", "/user/" + id, null, new NetworkCallback() {
            @Override
            public void onSuccess(int status, JSONObject data) {
                if (data == null) {
                    return;
                }
                if (!isItMe) {
                    btnFollow.setVisibility(View.VISIBLE);
                    btnChat.setVisibility(View.VISIBLE);
                } else {
                    btnEditProfile.setVisibility(View.VISIBLE);
                }
                try {
                    avatarUrl = data.getString("avatar");
                    name = data.getString("name");
                    String title = data.getString("title");
                    followersCount = data.getInt("followers_count");
                    int itemsCount = data.getInt("items_count");
                    boolean isInMyList = data.getInt("in_my_list") > 0;
                    boolean isVerified = data.getBoolean("is_verified");
                    JSONObject rating = null;
                    if (data.has("rating")) {
                        try {
                            rating = data.getJSONObject("rating");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    if (rating == null) {
                        llRatingContainer.setVisibility(View.INVISIBLE);
                    } else if (rating.has("avg_quality") && rating.has("avg_packaging") && rating.has("avg_story")) {

                        try {
                            llRatingContainer.setVisibility(View.VISIBLE);
                            ratingBarQuality.setRating(rating.getInt("avg_quality"));
                            ratingBarPack.setRating(rating.getInt("avg_packaging"));
                            ratingBarStory.setRating(rating.getInt("avg_story"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    //getSupportActionBar().setTitle(Utils.generateTypeFacedString(UserProfileActivity.this, name));
                    imgVerified.setVisibility(isVerified ? View.VISIBLE : View.GONE);
                    tvTBTitle.setText(name);
                    if (!TextUtils.isEmpty(avatarUrl) && avatarUrl.length() > 10) {
                        Picasso.with(UserProfileActivity.this)
                               .load(avatarUrl)
                               .placeholder(R.drawable.ic_account_circle_48px)
                               .error(R.drawable.ic_account_circle_48px)
                               .fit().centerInside()
                               .into(imgAvatar);
                        Picasso.with(UserProfileActivity.this)
                               .load(avatarUrl)
                               .placeholder(R.drawable.ic_account_circle_48px)
                               .error(R.drawable.ic_account_circle_48px)
                               .fit().centerInside()
                               .into(imgTBAvatar);
                    } else {
                        int randomAvatar = Komovatar.getRandomAvatar();
                        imgAvatar.setImageResource(randomAvatar);
                        imgTBAvatar.setImageResource(randomAvatar);
                    }


                    tvFollowersCount.setText(String.format(Locale.US, "%s دنبال کننده", Utils.formatNumber(followersCount)));
                    tvItemsCount.setText(String.format(Locale.US, "%s آیتم", Utils.formatNumber(itemsCount)));
                    tvUserName.setText(name);
                    if (isInMyList) {
                        btnFollow.setText("دنبالش می کنی");
                        btnFollow.setBackgroundColor(Utils.getColorFromResource(getBaseContext(), R.color.colorPrimary));
                    } else {
                        btnFollow.setText("دنبال کن");
                        btnFollow.setBackgroundColor(Utils.getColorFromResource(getBaseContext(), R.color.btn_bg));
                    }
                    if (!TextUtils.isEmpty(title)) {
                        tvUserTitle.setVisibility(View.VISIBLE);
                        tvUserTitle.setText(title);
                    } else {
                        tvUserTitle.setVisibility(View.INVISIBLE);

                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                itemsProgress.setVisibility(View.VISIBLE);
                getItems(1);
            }

            @Override
            public void onFailure(int status, Error error) {

            }
        });
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(offset) / (float) maxScroll;

        //handleAlphaOnTitle(percentage);
        handleToolbarTitleVisibility(percentage);
    }

    private void handleToolbarTitleVisibility(float percentage) {
        if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {

            if (!mIsTheTitleVisible) {
                startAlphaAnimation(TBContainer, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleVisible = true;
            }

        } else {

            if (mIsTheTitleVisible) {
                startAlphaAnimation(TBContainer, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleVisible = false;
            }
        }
    }

/*    private void handleAlphaOnTitle(float percentage) {
        if (percentage >= PERCENTAGE_TO_HIDE_TITLE_DETAILS) {
            if (mIsTheTitleContainerVisible) {
                startAlphaAnimation(mTitleContainer, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleContainerVisible = false;
            }

        } else {

            if (!mIsTheTitleContainerVisible) {
                startAlphaAnimation(mTitleContainer, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleContainerVisible = true;
            }
        }
    }*/

    public static void startAlphaAnimation(View v, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }

    private void getItems(final int page) {
        JSONObject data = null;
        try {
            data = new JSONObject();
            data.put("user_id", id);
            data.put("full", false);
            data.put("type", 1);
            data.put("count", 15);
            data.put("page", page);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApiClient.makeRequest(this, "GET", "/products", data, new NetworkCallback() {
            @Override
            public void onSuccess(int status, final JSONObject data) {

                try {
                    loading = false;
                    if (recList == null) {
                        return;
                    }
                    itemsProgress.setVisibility(View.INVISIBLE);
                    if (adapter != null) {
                        adapter.removeProgress();

                    }
                    JSONArray products = data.getJSONArray("data");
                    maxPages = data.getInt("total_pages");
                    List<Product> list = new ArrayList<>();
                    for (int i = 0; i < products.length(); i++) {
                        list.add(new Product(products.getJSONObject(i)));
                    }


                    if (page <= 1) {

                        adapter.clear();
                    }
                    adapter.addData(list);
                    adapter.notifyDataSetChanged();


                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onFailure(int status, Error error) {
                try {
                    loading = false;
                    itemsProgress.setVisibility(View.INVISIBLE);
                    if (adapter != null) {
                        adapter.removeProgress();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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

    @OnClick(R.id.btnChat)
    public void onChatClick() {
        if (isItMe) {
            return;
        }

        Intent intent = new Intent(this, ChatActivity.class)
                .putExtra("user_id", id)
                .putExtra("user_name", name)
                .putExtra("avatar", avatarUrl);
        startActivity(intent);
    }

    @OnClick(R.id.btnFollow)
    public void onClick() {
        if (isItMe) {
            return;
        }
        progress.setVisibility(View.VISIBLE);
        btnFollow.setEnabled(false);
        ApiClient.makeRequest(this, "POST", "/follow/" + id, null, new NetworkCallback() {
            @Override
            public void onSuccess(int status, JSONObject data) {
                try {
                    progress.setVisibility(View.GONE);
                    btnFollow.setEnabled(true);
                    data.getBoolean("is_following");
                    if (data.getBoolean("is_following")) {
                        followersCount++;
                        btnFollow.setText("دنبالش می کنی");
                        btnFollow.setBackgroundColor(Utils.getColorFromResource(getBaseContext(), R.color.colorPrimary));
                    } else {
                        followersCount--;
                        btnFollow.setText("دنبال کن");
                        btnFollow.setBackgroundColor(Utils.getColorFromResource(getBaseContext(), R.color.btn_bg));
                    }
                    tvFollowersCount.setText(String.format(Locale.US, "%s دنبال کننده", Utils.formatNumber(followersCount)));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int status, Error error) {
                progress.setVisibility(View.GONE);
                btnFollow.setEnabled(true);
            }
        });
    }

    class ItemsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        static final int VIEW_ITEM = 1;
        static final int VIEW_PROGRESS = 0;
        List<Product> list;
        private Context c;
        private OnItemClickListener mItemClickListener;

        public ItemsAdapter(List<Product> list) {
            this.list = list;
        }

        public ItemsAdapter() {
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            c = parent.getContext();
            if (viewType == VIEW_ITEM) {
                return new ItemViewHolder(LayoutInflater.from(c).inflate(R.layout.row_items_profile, parent, false));
            } else {
                return new ProgressViewHolder(LayoutInflater.from(c).inflate(R.layout.progress_bar, parent, false));
            }
        }

        @Override
        public int getItemViewType(int position) {
            return list.get(position) != null ? VIEW_ITEM : VIEW_PROGRESS;
        }

        public void addData(List<Product> items) {
            if (list == null) {
                list = items;
            } else {
                list.addAll(items);
            }
        }

        public void showProgress() {

//            if (list == null || list.size() < 1) {
//                return;
//            }
//            list.add(null);
//            notifyItemInserted(list.size());

        }

        public void removeProgress() {

//            if (list == null) {
//                return;
//            }
//            if (list.get(list.size() - 1) == null) {
//                list.remove(list.size() - 1);
//                notifyItemRemoved(list.size());
//            }


        }


        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof ItemViewHolder) {
                Product p = list.get(position);
                //holder.imgProduct.getLayoutParams().height = holder.imgProduct.getWidth();
                String imageUrl = p.getCover().getPath();
                if (!TextUtils.isEmpty(imageUrl)) {
                    imageUrl = imageUrl.replace(".jpg", "_320.jpg");
                    Picasso.with(c).load(imageUrl).fit().centerInside().placeholder(R.drawable.loading_main_activity).into(((ItemViewHolder) holder).imgProduct);
                }
                boolean isSold = p.getStatus() == 2 || p.getStatus() == 5;
                ((ItemViewHolder) holder).imgSold.setVisibility(isSold ? View.VISIBLE : View.GONE);
            } else {
                ((ProgressViewHolder) holder).progress.setVisibility(View.VISIBLE);
                ((ProgressViewHolder) holder).progress.start();
            }


        }

        @Override
        public int getItemCount() {
            return list == null ? 0 : list.size();
        }

        public void clear() {
            list = new ArrayList<>();
        }


        public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            @BindView(R.id.imgProduct)
            ImageView imgProduct;
            @BindView(R.id.imgSold)
            ImageView imgSold;

            ItemViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    int adapterPosition = getAdapterPosition();
                    if (adapterPosition == RecyclerView.NO_POSITION) {
                        return;
                    }
                    mItemClickListener.onItemClick(v, adapterPosition, list.get(adapterPosition));
                }
            }
        }

        public class ProgressViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.progress)
            ProgressView progress;

            public ProgressViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }

        public void setOnItemClickListener(final OnItemClickListener itemClickListener) {
            this.mItemClickListener = itemClickListener;
        }

    }

    public class ItemOffsetDecoration extends RecyclerView.ItemDecoration {

        private int mItemOffset;

        public ItemOffsetDecoration(int itemOffset) {
            mItemOffset = itemOffset;
        }

        public ItemOffsetDecoration(@NonNull Context context, int itemOffsetId) {
            this(itemOffsetId);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.set(mItemOffset, mItemOffset, mItemOffset, mItemOffset);
        }
    }
}
