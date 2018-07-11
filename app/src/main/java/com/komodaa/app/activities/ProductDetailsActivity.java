package com.komodaa.app.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.komodaa.app.App;
import com.komodaa.app.R;
import com.komodaa.app.fragments.BaseFragment;
import com.komodaa.app.fragments.SlideFragment;
import com.komodaa.app.interfaces.CommentListener;
import com.komodaa.app.interfaces.NetworkCallback;
import com.komodaa.app.interfaces.OnItemClickListener;
import com.komodaa.app.models.Attribute;
import com.komodaa.app.models.Comment;
import com.komodaa.app.models.Error;
import com.komodaa.app.models.Media;
import com.komodaa.app.models.Product;
import com.komodaa.app.models.User;
import com.komodaa.app.utils.ApiClient;
import com.komodaa.app.utils.Komovatar;
import com.komodaa.app.utils.UserUtils;
import com.komodaa.app.utils.Utils;
import com.pkmmte.view.CircularImageView;
import com.rey.material.widget.Button;
import com.squareup.picasso.Picasso;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;
import com.yqritc.recyclerviewflexibledivider.VerticalDividerItemDecoration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.relex.circleindicator.CircleIndicator;

public class ProductDetailsActivity extends AppCompatActivity implements CommentListener {

    @BindView(R.id.vpImages) ViewPager vpImages;
    @BindView(R.id.tvNewStatus) TextView tvNewStatus;
    @BindView(R.id.tvDiscount) TextView tvDiscount;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.toolbar_layout) CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.tvPrice) TextView tvPrice;
    @BindView(R.id.app_bar) AppBarLayout appBarLayout;
    @BindView(R.id.fabPurchase) FloatingActionButton fabPurchase;
    @BindView(R.id.circleIndicator) CircleIndicator circleIndicator;
    @BindView(R.id.llAttributesContainer) LinearLayout llAttributesContainer;
    //    @BindView(R.id.tabLayout) TabLayout tabLayout;
//    @BindView(R.id.vpComments) ViewPager vpComments;
    @BindView(R.id.tvDesc) TextView tvDesc;
    @BindView(R.id.tvCity) TextView tvCity;
    @BindView(R.id.tvDate) TextView tvDate;
    @BindView(R.id.imgAvatar) CircularImageView imgAvatar;
    @BindView(R.id.tvUserName) TextView tvUserName;
    @BindView(R.id.tvSpecs) TextView tvSpecs;
    @BindView(R.id.recMoreProducts) RecyclerView recMoreProducts;
    @BindView(R.id.llMoreProducts) LinearLayout llMoreProducts;
    @BindView(R.id.rlUserInfoWrapper) RelativeLayout rlUserInfoWrapper;
    @BindView(R.id.nsvProductDetails) NestedScrollView nsvProductDetails;
    @BindView(R.id.coordinatorLayout) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.imgSold) ImageView imgSold;
    @BindView(R.id.tvFeatured) TextView tvFeatured;
    @BindView(R.id.recList) RecyclerView recList;
    @BindView(R.id.tvUsersComments) TextView tvUsersComments;
    @BindView(R.id.btnComments) Button btnComments;
    @BindView(R.id.cbBookmark) CheckBox cbBookmark;
    @BindView(R.id.tvMoreComments) TextView tvMoreComments;
    @BindView(R.id.imgVerified) ImageView imgVerified;
    //private CommentsListFragment commentsListFragment;
    private Product p;
    //private AddCommentFragment addCommentFragment;
    private Typeface typeface;
    private boolean from_messages;
    private CommentsAdapter commentsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);
        ButterKnife.bind(this);

        toolbar = findViewById(R.id.toolbar);

        typeface = ResourcesCompat.getFont(this, R.font.iran_sans);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        collapsingToolbar = findViewById(R.id.toolbar_layout);
        //collapsingToolbar.setTitle(" ");

        appBarLayout = findViewById(R.id.app_bar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbar.setCollapsedTitleTypeface(typeface);
                    collapsingToolbar.setTitle(Utils.generateTypeFacedString(ProductDetailsActivity.this, "مشخصات آیتم"));
                    collapsingToolbar.setCollapsedTitleGravity(Gravity.CENTER);
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");//carefull there should a space between double quote otherwise it wont work
                    isShow = false;
                }
            }
        });

        Intent intent = getIntent();
        // ATTENTION: This was auto-generated to handle app links.


        if (intent == null) {
            finish();
            return;
        }
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            for (String key : bundle.keySet()) {
                Object value = bundle.get(key);
                Log.d("Intent", String.format("%s %s (%s)", key,
                        value.toString(), value.getClass().getName()));
            }
        }
        p = intent.getParcelableExtra("item");
        from_messages = intent.hasExtra("from_messages");
        //String appLinkAction = intent.getAction();
        Uri appLinkData = intent.getData();
        if (appLinkData != null) {
            String productId = appLinkData.getLastPathSegment();
            getProductDetails(productId);
        } else if (intent.hasExtra("item_id")) {
            String productId = null;
            try {
                productId = intent.getIntExtra("item_id", -1) + "";
            } catch (Exception e) {
                productId = intent.getStringExtra("item_id");
            }
            getProductDetails(productId);
        } else if (p != null) {
            showUI();

        } else {
            finish();
        }

    }

    private void getProductDetails(String productId) {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setIndeterminate(true);
        pd.setMessage("لطفاً منتظر بمانید");
        pd.show();
        final Handler h = new Handler(getMainLooper());
        ApiClient.makeRequest(this, "GET", "/product/" + productId, null, new NetworkCallback() {
            @Override
            public void onSuccess(int status, final JSONObject data) {
                h.post(() -> {
                    pd.dismiss();
                    if (data == null) {
                        return;
                    }
                    try {
                        p = new Product(data.getJSONObject("data"));
                        showUI();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        finish();
                    }
                });
            }

            @Override
            public void onFailure(int status, Error error) {
                h.post(() -> {
                    pd.dismiss();
                    finish();
                });
            }
        });
    }

    private void showUI() {
        tvPrice.setText(String.format("%s تومان", Utils.formatNumber(p.getPrice())));
        tvSpecs.setText(String.format(Locale.US, "مشخصات %s", Utils.getCategoryName(p.getCategoryId())));

        String itemStatus = p.isHomemade() ? "کاردستی" : p.isBrandNew() ? "کاملاً نو" : "در حد نو";
        tvNewStatus.setText(itemStatus);
        if (p.isFeatured()) {
            tvFeatured.setVisibility(View.VISIBLE);
        } else {
            tvFeatured.setVisibility(View.GONE);
        }
        if (p.isHomemade()) {
            tvNewStatus.setBackgroundColor(Utils.getColorFromResource(this, R.color.homemade_item));

        } else if (p.isBrandNew()) {
            tvNewStatus.setBackgroundColor(Utils.getColorFromResource(this, R.color.new_item));
        } else {
            tvNewStatus.setBackgroundColor(Utils.getColorFromResource(this, R.color.used_item));
        }
        if (p.getDiscountPercent() > 0) {
            tvDiscount.setVisibility(View.VISIBLE);
            SpannableString span = new SpannableString(String.format(Locale.US, "%d%% Off", p.getDiscountPercent()));
            StyleSpan ssp = new StyleSpan(Typeface.BOLD);
            span.setSpan(new RelativeSizeSpan(1.4f), 0, (p.getDiscountPercent() + "").length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            span.setSpan(ssp, 0, (p.getDiscountPercent() + "").length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvDiscount.setText(span);
        } else {
            tvDiscount.setVisibility(View.GONE);
        }
        boolean isSold = p.getStatus() == 2 || p.getStatus() == 5;
        if (p.getUserId() == UserUtils.getUser().getId() || isSold || p.getStatus() == 4) {

            fabPurchase.setVisibility(View.INVISIBLE);
            if (isSold) {
                imgSold.setVisibility(View.VISIBLE);
            }

        } else {
            fabPurchase.setVisibility(View.VISIBLE);
        }
        tvDate.setText(Utils.getPersianDateText(p.getDate(), true));
        tvCity.setText(Utils.getCityName(this, p.getCityId()));
        User user = p.getUser();
        if (p != null) {
            tvUserName.setText(user.getFirstName() + " " + user.getLastName()+"\n"+Utils.getCityName(this,user.getCityId()));
            imgVerified.setVisibility(user.isVerified() ? View.VISIBLE : View.GONE);

            String avatarUrl = user.getAvatarUrl();

            if (!TextUtils.isEmpty(avatarUrl) && avatarUrl.length() > 10) {
//                int width = imgAvatar.getWidth();
//                if (width < 1) {
//                    width = 200;
//                }
//                avatarUrl = Gravatar.init().with(user.getEmail()).force404().size(width).build();
                Picasso.with(this)
                        .load(avatarUrl)
                        .placeholder(R.drawable.ic_account_circle_48px)
                        .error(R.drawable.ic_account_circle_48px)
                        .fit().centerInside()
                        .into(imgAvatar);
            } else {
                imgAvatar.setImageResource(Komovatar.getRandomAvatar());
            }

        }

        SliderAdapter adapter = new SliderAdapter(getSupportFragmentManager(), p.getMedia());
        if (!TextUtils.isEmpty(p.getDescription())) {
            tvDesc.setText(p.getDescription());
        }
        vpImages.setAdapter(adapter);

        circleIndicator.setViewPager(vpImages);

        SpannableString string = new SpannableString("دیدن سایر نظرات");
        string.setSpan(new UnderlineSpan(), 0, string.length(), 0);
        tvMoreComments.setText(string);


//        CommentAdapter cAdapter = new CommentAdapter(getSupportFragmentManager());
//
//        Bundle bundle = new Bundle();
//        bundle.putLong("item_id", p.getId());
//        addCommentFragment = new AddCommentFragment();
//        commentsListFragment = new CommentsListFragment();
//
//        addCommentFragment.setArguments(bundle);
//        commentsListFragment.setArguments(bundle);
//
//        cAdapter.addFragment(commentsListFragment);
//        cAdapter.addFragment(addCommentFragment);

//        vpComments.setAdapter(cAdapter);
//
//
//        tabLayout.setupWithViewPager(vpComments);
//        changeTabsFont();
        commentsAdapter = new CommentsAdapter();

        LinearLayoutManager lManager = new LinearLayoutManager(this);
        lManager.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(lManager);
//        recList.setHasFixedSize(true);
        recList.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this).margin(Utils.dpToPx(this, 16)).build());
        //recList.setNestedScrollingEnabled(false);
        recList.setAdapter(commentsAdapter);
        reload(false);
        if (from_messages) {
//            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
//            AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
//            if (behavior != null) {
//                behavior.onNestedFling(coordinatorLayout, appBarLayout, nsvProductDetails, 0, -params.height, true);
//                nsvProductDetails.scrollTo(0, vpComments.getBottom());
//
//            }
            nsvProductDetails.post(() -> {
                int toolbarHeight = toolbar.getHeight();

                nsvProductDetails.startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
                nsvProductDetails.dispatchNestedPreScroll(0, toolbarHeight * 3, null, null);
                nsvProductDetails.dispatchNestedScroll(0, 0, 0, 0, new int[]{0, -toolbarHeight * 3});
//                nsvProductDetails.smoothScrollTo(0, vpComments.getBottom());
//                vpComments.setCurrentItem(0);
            });

        } else {

            // vpComments.setCurrentItem(1);
        }
        displayTapTargetIfNecessary();
        populateAttributes(p.getAttributes());

        performSearch();
        try {
            String vUserName = UserUtils.getUser().getFirstName() + " " + UserUtils.getUser().getLastName();
            if (TextUtils.isEmpty(vUserName.trim())) {
                vUserName = "نا شناس";
            }
            Answers.getInstance().logContentView(new ContentViewEvent()
                    .putContentName(Utils.getCategoryName(p.getCategoryId()))
                    .putContentType("Item View")
                    .putContentId("item-" + p.getId())
                    .putCustomAttribute("user-id", UserUtils.getUser().getId() + "")
                    .putCustomAttribute("owner-id", p.getUserId() + "")
                    .putCustomAttribute("Owner", p.getUser().getFirstName() + " " + p.getUser().getLastName())
                    .putCustomAttribute("Viewer", vUserName)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        cbBookmark.setChecked(App.getPrefs().getBoolean("bookmark_item_" + p.getId(), false));
        cbBookmark.setOnClickListener((v) -> {
            cbBookmark.setEnabled(false);
            ApiClient.makeRequest(this, "POST", "/bookmark/" + p.getId(), null, new NetworkCallback() {
                @Override
                public void onSuccess(int status, JSONObject data) {
                    cbBookmark.setEnabled(true);
                    try {
                        boolean isOk = data.getBoolean("bookmark");
                        App.getPrefs().edit().putBoolean("bookmark_item_" + p.getId(), isOk).apply();
                        cbBookmark.setChecked(isOk);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int status, Error error) {
                    cbBookmark.setEnabled(true);
                }
            });
        });
    }

    private void displayTapTargetIfNecessary() {
        SharedPreferences prefs = App.getPrefs();
        if (prefs.getBoolean("is_first_time_activity_details", true)) {
            prefs.edit().putBoolean("is_first_time_activity_details", false).apply();
            final Activity c = this;
            imgAvatar.postDelayed(() -> TapTargetView.showFor(c, TapTarget.forView(imgAvatar, Utils.generateTypeFacedString(c, "اگر روی اسم فروشنده بزنی می تونی فهرست آیتم هاش رو ببینی")).id(1)), 1000);
        }
    }

    public void reload(final boolean newComment) {

        JSONObject data = null;
        try {
            data = new JSONObject();
            data.put("count", 3);
            data.put("page", 1);
            data.put("summarized", true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApiClient.makeRequest(this, "GET", "/comment/list/" + p.getId(), data, new NetworkCallback() {
            @Override
            public void onSuccess(int status, JSONObject data) {

                try {

                    int totalPages = data.getInt("total_pages");
                    JSONArray array = data.getJSONArray("data");
                    List<Comment> list = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        list.add(new Comment(array.getJSONObject(i)));
                    }
                    if (list.size() > 0) {
                        tvUsersComments.setVisibility(View.VISIBLE);
                        tvUsersComments.setText(String.format("نظرات کاربران (%d)", p.getCommentsCount()));
                        tvMoreComments.setVisibility(View.VISIBLE);
                    }
                    //toThreadedComments(list);
                    commentsAdapter.addData(list, false);
                    commentsAdapter.notifyDataSetChanged();
                    if (recList.getAdapter() == null) {
                        recList.setAdapter(commentsAdapter);
                    }
                    if (newComment) {
                        recList.smoothScrollToPosition(commentsAdapter.getItemCount() - 1);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int status, Error error) {

            }
        });
    }

    void performSearch() {
        JSONObject data = null;
        try {
            data = new JSONObject();
            data.put("count", 5);
            data.put("page", 1);
            data.put("user_id", p.getUserId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApiClient.makeRequest(this, "GET", "/products", data, new NetworkCallback() {
            @Override
            public void onSuccess(int status, JSONObject data) {

                try {

                    JSONArray products = data.getJSONArray("data");
                    List<Product> list = new ArrayList<>();
                    for (int i = 0; i < products.length(); i++) {
                        JSONObject jsonObject = products.getJSONObject(i);
                        if (jsonObject.getInt("product_id") == p.getId()) {
                            continue;
                        }
                        list.add(new Product(jsonObject));
                    }
                    if (list.size() < 1) {
                        llMoreProducts.setVisibility(View.GONE);
                        return;
                    }
                    llMoreProducts.setVisibility(View.VISIBLE);
                    final LinearLayoutManager mLayoutManager = new LinearLayoutManager(ProductDetailsActivity.this);
                    mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                    recMoreProducts.addItemDecoration(new VerticalDividerItemDecoration.Builder(ProductDetailsActivity.this).size(Utils.dpToPx(ProductDetailsActivity.this, 8)).color(Color.TRANSPARENT).build());

                    recMoreProducts.setLayoutManager(mLayoutManager);


                    MoreProductsAdapter adapter = new MoreProductsAdapter(list);
                    recMoreProducts.setAdapter(adapter);
                    adapter.setOnItemClickListener((view, position, data1) -> {
                        Intent i = new Intent(ProductDetailsActivity.this, ProductDetailsActivity.class);
                        i.putExtra("item", (Product) data1);
                        startActivity(i);
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int status, Error error) {

            }
        });
    }

//    private void changeTabsFont() {
//
//        Typeface typeface = ResourcesCompat.getFont(this, R.font.iran_sans);
//        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
//        int tabsCount = vg.getChildCount();
//        for (int j = 0; j < tabsCount; j++) {
//            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
//            int tabChildsCount = vgTab.getChildCount();
//            for (int i = 0; i < tabChildsCount; i++) {
//                View tabViewChild = vgTab.getChildAt(i);
//                if (tabViewChild instanceof TextView) {
//                    ((TextView) tabViewChild).setTypeface(typeface, Typeface.BOLD);
//                }
//            }
//        }
//    }

    private void populateAttributes(List<Attribute> attributes) {
        if (attributes == null) {
            return;
        }
        for (Attribute a : attributes) {
            llAttributesContainer.addView(createView(a));
        }
    }

    LinearLayout createView(Attribute a) {
        LinearLayout m = (LinearLayout) getLayoutInflater().inflate(R.layout.template_text_view, null);

        ((TextView) m.findViewById(R.id.tvRowTitle)).setText(a.getName());
        ((TextView) m.findViewById(R.id.tvRowText)).setText(a.getValue());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Utils.dpToPx(this, 42));
        params.setMargins(0, Utils.dpToPx(this, 4), 0, 0);
        //m.setLayoutParams(params);
        return m;
    }

    private int getColorFromResource(@ColorRes int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getColor(color);
        }
        return getResources().getColor(R.color.colorPrimary);
    }

    @OnClick({R.id.fabPurchase, R.id.rlUserInfoWrapper})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fabPurchase: {
                Intent intent = new Intent(this, PurchaseActivity.class);
                intent.putExtra("item", p);
                startActivity(intent);
                break;
            }
            case R.id.rlUserInfoWrapper: {
//                Intent intent = new Intent(this, MainActivity.class);
//                intent.putExtra("user_id", p.getUserId());
//                User user = p.getUser();
//                String userName = "کاربر";
//
//                if (p != null) {
//                    userName = user.getFirstName() + " " + user.getLastName();
//
//                }
//                intent.putExtra("user_name", userName);
//                startActivity(intent);
                Intent intent = new Intent(this, UserProfileActivity.class);
                intent.putExtra("user_id", (long) p.getUserId());
                startActivity(intent);
                break;
            }
        }
    }

    @Override
    public void commentPosted() {
        //commentsListFragment.reload(true);
        //vpComments.setCurrentItem(0);
    }

    @Override
    public void replyTo(long id, String userName) {
        //addCommentFragment.setReplyTo(id, userName);
        //vpComments.setCurrentItem(1);
    }

    @OnClick({R.id.btnComments, R.id.tvMoreComments})
    public void onViewClicked() {
        Intent intent = new Intent(this, CommentsActivity.class).putExtra("item_id", p.getId());
        startActivity(intent);
    }


    class SliderAdapter extends FragmentPagerAdapter {
        List<Media> list;

        SliderAdapter(FragmentManager fm, List<Media> list) {
            super(fm);
            this.list = list;
        }

        @Override
        public Fragment getItem(int position) {
            SlideFragment f = new SlideFragment();
            Bundle args = new Bundle();
            args.putParcelable("media", list.get(position));
            f.setArguments(args);

            return f;
        }

        @Override
        public int getCount() {
            return list == null ? 0 : list.size();
        }
    }

    class CommentAdapter extends FragmentPagerAdapter {
        List<BaseFragment> list;

        CommentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return list.get(position);
        }

        @Override
        public int getCount() {
            return list == null ? 0 : list.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return list.get(position).getTitle();
        }

        public void addFragment(BaseFragment fragment) {
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(fragment);
        }
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
//            case R.id.item_payment_help:
//                Intent intent = new Intent(this, HelpActivity.class);
//                intent.putExtra("title", "راهنمای خرید");
//                intent.putExtra("url", "https://komodaa.com/help/items");
//                startActivity(intent);
//                return true;
            case R.id.action_share:
                String text = String.format(Locale.US, "آیتم با قیمت %s تومان در کُمُدا:\nhttps://komodaa.com/web/item/%d", Utils.formatNumber(p.getPrice()), p.getId());
                Log.i("Share Text", text);
                ShareCompat.IntentBuilder
                        .from(this)
                        .setType("text/plain")
                        .setText(text)
                        .setChooserTitle("برنامه رو انتخاب کن")
                        .startChooser();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.product_details, menu);
        return true;
    }

    class MoreProductsAdapter extends RecyclerView.Adapter<MoreProductsAdapter.ViewHolder> {
        List<Product> list;
        private Context c;
        private OnItemClickListener mItemClickListener;

        public MoreProductsAdapter(List<Product> list) {
            this.list = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            c = parent.getContext();
            return new ViewHolder(getLayoutInflater().inflate(R.layout.row_more_products, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Product p = list.get(position);


            Picasso.with(c).load(p.getCover().getPath()).fit().centerInside().into(holder.image);
        }

        @Override
        public int getItemCount() {
            return list != null ? list.size() : 0;
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            @BindView(R.id.imgProduct)
            ImageView image;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    int adapterPosition = getAdapterPosition();
                    mItemClickListener.onItemClick(v, adapterPosition, list.get(adapterPosition));
                }
            }
        }

        public void setOnItemClickListener(final OnItemClickListener itemClickListener) {
            this.mItemClickListener = itemClickListener;
        }
    }

    class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {
        private final Komovatar komovatar;
        List<Comment> list;
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
            View v = LayoutInflater.from(context).inflate(R.layout.row_comment, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            final Comment c = list.get(position);

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
            @BindView(R.id.imgAvatar) CircularImageView imgAvatar;
            @BindView(R.id.imgReply) ImageView imgReply;
            @BindView(R.id.tvCommentTitle) TextView tvCommentTitle;
            @BindView(R.id.tvCommentRepliedTo) TextView tvCommentRepliedTo;
            @BindView(R.id.tvCommentText) TextView tvCommentText;

            ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                imgReply.setVisibility(View.INVISIBLE);
                imgAvatar.setOnClickListener(v -> {
                    if (UserUtils.isLoggedIn()) {
                        startActivity(new Intent(context, UserProfileActivity.class).putExtra("user_id", list.get(getAdapterPosition()).getUserId()));
                    }
                });
            }
        }
    }
}
