package com.komodaa.app.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.komodaa.app.App;
import com.komodaa.app.BuildConfig;
import com.komodaa.app.R;
import com.komodaa.app.adapters.NavigationItemsAdapter;
import com.komodaa.app.fragments.BaseFragment;
import com.komodaa.app.fragments.MyClosetFragment;
import com.komodaa.app.fragments.MyProfileFragment;
import com.komodaa.app.fragments.ProductsListFragment;
import com.komodaa.app.interfaces.NetworkCallback;
import com.komodaa.app.models.Error;
import com.komodaa.app.models.NavigationItem;
import com.komodaa.app.models.Product;
import com.komodaa.app.models.User;
import com.komodaa.app.utils.ApiClient;
import com.komodaa.app.utils.BottomNavigationBehavior;
import com.komodaa.app.utils.UIUtils;
import com.komodaa.app.utils.UserUtils;
import com.komodaa.app.utils.Utils;
import com.pkmmte.view.CircularImageView;
import com.rey.material.widget.Button;
import com.squareup.picasso.Picasso;

import net.darkion.AchievementUnlockedLib.AchievementUnlocked;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.tkeunebr.gravatar.Gravatar;

public class HomeActivity extends AppCompatActivity {

    static final int NAV_KOMOD = 1;
    static final int NAV_TOP_SELLER = 2;
    static final int NAV_BOOKMARK = 3;
    static final int NAV_FINANCIAL_REPORT = 4;
    static final int NAV_RADIOMODA = 5;
    static final int NAV_FAQ = 6;
    static final int NAV_RULES = 7;
    static final int NAV_CONTACT = 8;
    static final int NAV_ABOUT = 9;
    static final int NAV_ACTIVITIES = 10;
    static final int NAV_EXIT = 11;
    static final int NAV_INVITE = 12;

    @BindView(R.id.bottomNavigation) LinearLayout bottomNav;
    @BindView(R.id.frame_container) FrameLayout frameContainer;
    @BindView(R.id.toolbar) RelativeLayout toolbar;
    //@BindView(R.id.nav_view) NavigationView navigationView;
    @BindView(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    //    @BindView(R.id.fab_add) FloatingActionButton fabAdd;
//    //@BindView(R.id.fab_purchased) FloatingActionButton fabReceipt;
//    @BindView(R.id.fab_request) FloatingActionButton fabRequest;
//    @BindView(R.id.menu_labels_right) FloatingActionMenu fabMenu;
    @BindView(R.id.imgNavToggle) ImageView actionDrawer;
    //@BindView(R.id.menu_badge_my_komod) TextView menuBadgeMyKomod;
    //@BindView(R.id.item_my_komod) FrameLayout itemMyKomod;
    @BindView(R.id.menu_badge_messages) TextView menuBadgeMessages;
    @BindView(R.id.item_messages) FrameLayout itemMessages;
    //@BindView(R.id.imgSearch) FrameLayout imgSearch;
    @BindView(R.id.imgAvatar) CircularImageView avatar;
    @BindView(R.id.tvFullName) TextView fullName;
    @BindView(R.id.tvEmail) TextView email;
    //@BindView(R.id.tvLogout) TextView logout;
    @BindView(R.id.llUserInfo) RelativeLayout layoutUser;
    @BindView(R.id.btnLogin) Button btnLogin;
    @BindView(R.id.btnSignup) Button btnSignup;
    @BindView(R.id.llButtons) LinearLayout buttons;
    //@BindView(R.id.navBtnSupport) Button navBtnSupport;
    @BindView(R.id.list) ListView list;
    //@BindView(R.id.nav_badge_chats) TextView tvBadge;


    private int messagesBadgeCount;
    private int productsBadgeCount;
    private SharedPreferences prefs;
    //private MediaPlayer mPlayer;
    private Fragment fragment;
    private int userId;
    private String userName;

    @SuppressLint("RestrictedApi")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        //toolbar.setTitle(Utils.generateTypeFacedString(this, getTitle().toString()));
        //setSupportActionBar(toolbar);
        //getSupportActionBar().setTitle("کمدا " + new String(Character.toChars(0x1F60A)));
        SharedPreferences prefs = App.getPrefs();
        if (prefs.getBoolean("first_run", true)) {
            prefs.edit().putBoolean("first_run", false).apply();
            startActivity(new Intent(this, IntroActivity.class));
        }

        initDrawer();
        //drawer = findViewById(R.id.drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.addDrawerListener(toggle);
//        toggle.syncState();

        //spCity.setSelection(getIndex(spCity, Utils.getCityName(0)));
        //fabMenu.setClosedOnTouchOutside(true);
        //navigationView.setNavigationItemSelectedListener(this);
        actionDrawer.setOnClickListener(v -> {
            if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                mDrawerLayout.closeDrawer(Gravity.RIGHT);
            } else {
                mDrawerLayout.openDrawer(Gravity.RIGHT);
            }
        });

        Intent intent = getIntent();
        userId = -1;
        userName = "کاربران";
        if (intent != null && intent.hasExtra("user_id")) {
            userId = intent.getIntExtra("user_id", -1);
            userName = intent.getStringExtra("user_name");
        }

        fragment = new ProductsListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("user_id", userId);
        bundle.putString("user_name", userName);
        fragment.setArguments(bundle);

        loadFragment(fragment);
        View.OnClickListener onNavClicked = v -> {
            int position = (int) v.getTag();
            switch (position) {
                case 0: {
                    if (v.isSelected() && fragment != null) {
                        ((ProductsListFragment) fragment).goToTop();

                        return;
                    }

                    fragment = new ProductsListFragment();
                    Bundle b = new Bundle();
                    b.putInt("user_id", userId);
                    b.putString("user_name", userName);
                    fragment.setArguments(b);
                    loadFragment(fragment);
                    break;
                }

                case 1: {
                    if (fragment != null && fragment instanceof ProductsListFragment) {
                        ((ProductsListFragment) fragment).doSearch();
                        return;
                    }

                    fragment = new ProductsListFragment();
                    Bundle b = new Bundle();
                    b.putInt("user_id", userId);
                    b.putString("user_name", userName);
                    b.putBoolean("do_search", true);
                    fragment.setArguments(b);
                    loadFragment(fragment);
                    //((ProductsListFragment) fragment).doSearch();
                    setActiveView(0);
                    return;
                }
                case 2:
                    startActivity(new Intent(this, AddProductStep1Activity.class));
                    return;
                case 3:
                    if (!UserUtils.isLoggedIn()) {
                        Utils.displayLoginErrorDialog(this);
                        return;
                    }
                    if (fragment instanceof MyClosetFragment) {
                        return;
                    }
                    fragment = new MyClosetFragment();
                    loadFragment(fragment);
                    break;
                case 4:
                    if (!UserUtils.isLoggedIn()) {
                        Utils.displayLoginErrorDialog(this);
                        return;
                    }
                    if (fragment instanceof MyProfileFragment) {
                        return;
                    }
                    fragment = new MyProfileFragment();
                    loadFragment(fragment);
                    //tvBadge.setVisibility(View.GONE);
                    break;

            }
            setActiveView(position);
        };
        for (int i = 0; i < bottomNav.getChildCount(); i++) {
            View view = bottomNav.getChildAt(i);
            view.setOnClickListener(onNavClicked);
            view.setTag(i);
            if (i == 0) {
                setActiveView(0);
            }

        }


//        navBtnSupport.setOnClickListener(v -> {
//            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("tel:09309137334")));
//            mDrawerLayout.closeDrawer(GravityCompat.END);
//        });
        /*
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) bottomNav.getChildAt(0);

        for (int i = 0; i < menuView.getChildCount(); i++) {
            BottomNavigationItemView itemView = (BottomNavigationItemView) menuView.getChildAt(i);
            itemView.setShiftingMode(false);

            itemView.setChecked(false);
        }

        bottomNav.setOnNavigationItemSelectedListener(item -> {

            switch (item.getItemId()) {
                case R.id.navigation_home: {

                    if (bottomNav.getSelectedItemId() == R.id.navigation_home && fragment != null) {
                        ((ProductsListFragment) fragment).goToTop();
                        return false;
                    }
                    Intent intent = getIntent();
                    int userId = -1;
                    String userName = "کاربران";
                    if (intent != null && intent.hasExtra("user_id")) {
                        userId = intent.getIntExtra("user_id", -1);
                        userName = intent.getStringExtra("user_name");
                    }

                    fragment = new ProductsListFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt("user_id", userId);
                    bundle.putString("user_name", userName);
                    fragment.setArguments(bundle);

                    loadFragment(fragment);
                    return true;
                }
                case R.id.navigation_search:
                    if (fragment != null) {

                        ((ProductsListFragment) fragment).doSearch();
                    }
                    break;
                case R.id.navigation_add:
                    startActivity(new Intent(this, AddProductStep1Activity.class));
                    break;
                case R.id.navigation_komod:
                    if (!UserUtils.isLoggedIn()) {
                        Utils.displayLoginErrorDialog(this);
                        break;
                    }
                    startActivity(new Intent(this, PurchasedProductsActivity.class));
                    break;
                case R.id.navigation_profile:
                    if (!UserUtils.isLoggedIn()) {
                        Utils.displayLoginErrorDialog(this);
                        break;
                    }
                    startActivity(new Intent(this, ProfileActivity.class));
                    break;
            }
            return false;
        });
        bottomNav.setSelectedItemId(R.id.navigation_home);
        */
        ViewParent parent = bottomNav.getParent();
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) ((RelativeLayout) parent)
                .getLayoutParams();
        layoutParams.setBehavior(new BottomNavigationBehavior());
    }

    private void setActiveView(int position) {
        LinearLayout v = (LinearLayout) bottomNav.getChildAt(position);
        for (int i = 0; i < bottomNav.getChildCount(); i++) {
            LinearLayout view = (LinearLayout) bottomNav.getChildAt(i);


            if ((int) v.getTag() == i) {
                ImageViewCompat.setImageTintList((ImageView) v.getChildAt(0), ContextCompat
                        .getColorStateList(this, R.color.colorPrimary));
                ((TextView) v.getChildAt(1))
                        .setTextColor(Utils.getColorFromResource(this, R.color.colorPrimary));
                v.setSelected(true);
            } else {
                ImageViewCompat.setImageTintList((ImageView) view.getChildAt(0), ContextCompat
                        .getColorStateList(this, R.color.time_text));
                ((TextView) view.getChildAt(1))
                        .setTextColor(Utils.getColorFromResource(this, R.color.time_text));
                view.setSelected(false);
            }

        }
    }

    private void initDrawer() {
        List<NavigationItem> items = new ArrayList<>();

        //items.add(new NavigationItem(100, "تو کمدا چه خبره ؟", R.drawable.ic_my_komod));
        items.add(new NavigationItem(NAV_ACTIVITIES, "جنب‌و‌جوش‌های اخیر", R.drawable.ic_activities));
        items.add(new NavigationItem(NAV_TOP_SELLER, getString(R.string.top_sellers), R.drawable.ic_action_seller));
//        items.add(new NavigationItem(NAV_BOOKMARK, "بوکمارک", R.drawable.bookmark_normal));
//        items.add(new NavigationItem(NAV_FINANCIAL_REPORT, getString(R.string.financial_report), R.drawable.ic_drawer_report));
        items.add(new NavigationItem(NAV_RADIOMODA, getString(R.string.radiomoda), R.drawable.radiomoda));
        items.add(new NavigationItem(NAV_INVITE, "دعوت از دوستان", R.drawable.ic_nav_invite));
        //items.add(new NavigationItem(NAV_FAQ, getString(R.string.faq), R.drawable.nav_faq));
        items.add(new NavigationItem(NAV_RULES, getString(R.string.nav_rules), R.drawable.ic_nav_rules));
        items.add(new NavigationItem(NAV_ABOUT, getString(R.string.about), R.drawable.logo_komodaa));
        if (UserUtils.isLoggedIn()) {
            items.add(new NavigationItem(NAV_EXIT, "خروج", R.drawable.ic_nav_exit));
        }

        NavigationItemsAdapter adapter = new NavigationItemsAdapter(items);
        list.setAdapter(adapter);

        list.setOnItemClickListener((parent, view, position, id) -> onNavigationItemClicked(id));
    }

    @Override
    protected void onDestroy() {

        //mPlayer.stop();
        super.onDestroy();

    }

    @Override
    protected void onResume() {
        invalidateToolbar();
        initDrawer();
        // load products
        //performSearch();
        prefs = App.getPrefs();
        JSONObject data = new JSONObject();
        try {
            data.put("app_version", BuildConfig.VERSION_CODE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApiClient.makeRequest(this, "GET", "/all", data, new NetworkCallback() {
            @Override
            public void onSuccess(int status, JSONObject data) {
                prefs.edit().putString("config", data.toString()).apply();

            }

            @Override
            public void onFailure(int status, Error error) {

            }
        });
        String json = prefs.getString("config", null);


        //View header = navigationView.getHeaderView(0);


        btnLogin.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
        btnSignup.setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
        Log.d("", "onCreate: " + (UserUtils.isLoggedIn() ? "Logged In" : "Not Logged In"));
        if (UserUtils.isLoggedIn()) {
            //fabMenu.setVisibility(View.VISIBLE);
            layoutUser.setVisibility(View.VISIBLE);
            buttons.setVisibility(View.GONE);

//            logout.setOnClickListener(v -> {
//                UserUtils.logout();
//                //fabMenu.setVisibility(View.GONE);
//                layoutUser.setVisibility(View.GONE);
//                buttons.setVisibility(View.VISIBLE);
//                invalidateToolbar();
//            });
            User user = UserUtils.getUser();
            fullName.setText(user.getFirstName() + " " + user.getLastName());
            email.setText(user.getEmail());

            String avatarUrl = user.getAvatarUrl();

            if (TextUtils.isEmpty(avatarUrl) || avatarUrl.length() < 10) {
                int width = avatar.getWidth();
                if (width < 1) {
                    width = 200;
                }
                Log.d("Avatar", "size: " + width);
                avatarUrl = Gravatar.init().with(user.getEmail()).force404().size(width).build();
            }
            Log.d("Avatar", "AvatarUrl: " + avatarUrl);
            Picasso.with(this)
                   .load(avatarUrl)
                   .placeholder(R.drawable.ic_account_circle_48px)
                   .error(R.drawable.ic_account_circle_48px)
                   .fit().centerInside()
                   .into(avatar);
            //layoutUser.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
            ApiClient.makeRequest(this, "GET", "/user_info", null, new NetworkCallback() {
                @Override
                public void onSuccess(int status, JSONObject data) {
                    if (data == null) {
                        return;
                    }
                    messagesBadgeCount = 0;
                    Log.d("userInfo", data.toString());
                    App.getPrefs().edit().putString("user_meta", data.toString()).apply();
                    if (data.has("messages_count")) {
                        try {
                            messagesBadgeCount = data.getInt("messages_count");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    if (data.has("unread_chat_count")) {
                        try {
                            int unreadCount = data.getInt("unread_chat_count");
//                            if (unreadCount > 0) {
//                                tvBadge.setText(unreadCount + "");
//                                tvBadge.setVisibility(View.VISIBLE);
//                            } else {
//                                tvBadge.setVisibility(View.GONE);
//                            }
                            messagesBadgeCount += unreadCount;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    if (data.has("waiting_products_count")) {
                        try {
                            productsBadgeCount = data.getInt("waiting_products_count");
//                            if (productsBadgeCount < 1) {
//                                fabReceipt.hide(false);
//                            } else {
//                                fabReceipt.show(false);
//                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    invalidateToolbar();
                    try {

                        if (data.has("unread_messages") && (data.getInt("unread_messages") > 0)) {
                            AchievementUnlocked notif = new AchievementUnlocked(HomeActivity.this)
                                    .setTitle(getString(R.string.achievement_unread_messages_title))
                                    .setIcon(Utils
                                            .getDrawableFromRes(HomeActivity.this, R.drawable.tick))
                                    .setTitleColor(Color.WHITE)
                                    .setSubtitleColor(Color.WHITE)
                                    .setTypeface(ResourcesCompat
                                            .getFont(HomeActivity.this, R.font.iran_sans))
                                    .setSubTitle(String
                                            .format(Locale.US, getString(R.string.achievement_unread_messages_text), data
                                                    .getInt("unread_messages")))
                                    .setBackgroundColor(Utils
                                            .getColorFromResource(HomeActivity.this, R.color.notification_ok_purple))
                                    .isLarge(false)

                                    .build();

                            notif.getAchievementView().setOnClickListener(v -> {
                                Intent intent = new Intent(HomeActivity.this, MessageCenterActivity.class);
                                startActivity(intent);

                            });
                            notif.show();
                        }
                        Boolean noRatingPopup = App.getPrefs().getBoolean("no_rating_popup", false);
                        if (data.has("unrated") && !noRatingPopup) {
                            JSONObject unrated = data.getJSONObject("unrated");
                            int unratedCount = unrated.getInt("count");
                            if (unratedCount == 1) {
                                Product p = new Product(unrated.getJSONObject("item"));
                                UIUtils.showRatingDialog(HomeActivity.this, p);
                            } else if (unratedCount > 1) {
                                UIUtils.showRatingRequestDialog(HomeActivity.this, unrated
                                        .getString("dlg_title"), unrated.getString("dlg_content"));
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int status, Error error) {

                }


            });
        } else {

            //fabMenu.setVisibility(View.GONE);
            layoutUser.setVisibility(View.GONE);
            buttons.setVisibility(View.VISIBLE);
        }

        if (json != null) {
            try {
                JSONObject conf = new JSONObject(json);
                if (conf.has("update")) {
                    //Log.d("Time", String.format("Start Date: %s, End Date: %s, Diff: %d", "2016-01-01", "2016-01-19", Utils.getDaysBetween("2016-01-01", "2016-01-19")));
                    JSONObject update = conf.getJSONObject("update");
                    int vCode = update.getInt("version_code");
                    String currentDate = Utils.getCurrentDate("yyyy-MM-dd");
                    String lastShownDate = prefs.getString("last_update_shown_", "2016-12-01");
                    int diff = Utils.getDaysBetween(lastShownDate, currentDate);
                    boolean shouldDisplay = BuildConfig.VERSION_CODE < vCode && diff >= 7;
                    Log.d("update", String
                            .format("vCode: %d, CurrentDate: %s, Last Shown Date: %s, Diff: %d, ShouldDisplay: %s", vCode, currentDate, lastShownDate, diff, String
                                    .valueOf(shouldDisplay)));


                    if (shouldDisplay) {
                        prefs.edit().putString("last_update_shown_", currentDate).apply();
                        final Dialog d = new Dialog(this);
                        d.getWindow().setBackgroundDrawable(
                                new ColorDrawable(
                                        Color.TRANSPARENT));
                        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        d.setContentView(R.layout.dialog_message);

                        Button btnYes = d.findViewById(R.id.btnYes);
                        Button btnNo = d.findViewById(R.id.btnNo);
                        TextView tvTitle = d.findViewById(R.id.titleDialog);
                        TextView tvText = d.findViewById(R.id.tvText);
                        ImageView imgClose = d.findViewById(R.id.imgClose);

                        String title = update.getString("title");
                        String text = update.getString("text");
                        String yesTitle = update.getString("yes_title");
                        String noTitle = update.getString("no_title");
                        final String action = update.getString("action");
                        boolean showCancel = !update.has("show_cancel") || update
                                .getBoolean("show_cancel");


                        tvTitle.setText(title);
                        tvText.setText(Html.fromHtml(text));
                        btnYes.setText(yesTitle);
                        btnYes.setOnClickListener(v -> {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(action)));
                            d.dismiss();
                        });
                        if (showCancel) {
                            imgClose.setVisibility(View.VISIBLE);
                            btnNo.setVisibility(View.VISIBLE);
                            btnNo.setText(noTitle);
                            imgClose.setOnClickListener(v -> d.dismiss());
                            btnNo.setOnClickListener(v -> d.dismiss());
                        } else {
                            imgClose.setVisibility(View.INVISIBLE);
                            btnNo.setVisibility(View.GONE);
                        }
                        d.show();
                    }
                }

                if (conf.has("message")) {
                    JSONObject message = conf.getJSONObject("message");
                    if (message != null) {
                        displayMessage(message);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        bottomNav.postDelayed(this::showTapTargetIntroIfNecessary, 1000);
        super.onResume();
    }

    private void displayMessage(JSONObject message) {
        try {
            if (!message.has("url") || !message.has("id")) {

                return;
            }
            int id = message.getInt("id");
            if (id <= prefs.getLong(App.MESSAGES_LAST_SEEN, 0)) {

                return;
            }
            String validity = message.has("expires") ? message.getString("expires") : null;
            if (!TextUtils.isEmpty(validity)) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Date date = format.parse(validity);
                Log.i("date", date.toString());
                if (date.before(new Date())) {
                    Log.i("notif", "notif expired");

                    return;
                }
            }
            startActivity(new Intent(this, PopupActivity.class)
                    .putExtra("json", message.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    private void showTapTargetIntroIfNecessary() {
        final Activity a = this;
        SharedPreferences prefs = App.getPrefs();
        if (prefs.getBoolean("intro_visited", false)) {
            //App.getPrefs().edit().putBoolean("intro_visited", false).apply();


            final ViewTreeObserver observer = getWindow().getDecorView().getViewTreeObserver();
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        getWindow().getDecorView().getViewTreeObserver()
                                   .removeOnGlobalLayoutListener(this);
                    } else {
                        getWindow().getDecorView().getViewTreeObserver()
                                   .removeGlobalOnLayoutListener(this);
                    }
                    ArrayList<TapTarget> targets = new ArrayList<>();
                    if (UserUtils.isLoggedIn()) {
                        if (!prefs.getBoolean("tap.target.logged_in.items.shown", false)) {

                            if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
                                mDrawerLayout.closeDrawer(GravityCompat.END);
                            }
                            prefs.edit().putBoolean("tap.target.logged_in.items.shown", true)
                                 .apply();
                            View m = findViewById(R.id.item_messages);
                            if (m != null) {


                                //TapTargetView.showFor(MainActivity.this, TapTarget.forView(m, "یه مشت تیتر الکی"));
                                final Display display = getWindowManager().getDefaultDisplay();

                                final Drawable droid = Utils
                                        .getDrawableFromRes(a, R.drawable.menu_w);
                                // Tell our droid buddy where we want him to appear
                                final Rect droidTarget = new Rect(16, 16, droid
                                        .getIntrinsicWidth() * 2, droid.getIntrinsicHeight() * 2);
                                // Using deprecated methods makes you look way cool
                                droidTarget.offset(Utils.dpToPx(a, 16), display.getHeight() - Utils
                                        .dpToPx(a, 64));


                                targets.add(TapTarget.forView(m, Utils
                                        .generateTypeFacedString(a, getString(R.string.taptarget_menu_messages_text)))
                                                     .id(1));
                            }
                        }

                    }
                    if (!prefs.getBoolean("tap.target.bn.items.shown", false)) {

                        prefs.edit().putBoolean("tap.target.bn.items.shown", true).apply();

                        targets.add(TapTarget.forView(findViewById(R.id.bnItemMyKomod),
                                Utils.generateTypeFacedString(a, getString(R.string.taptarget_bn_komod_title)),
                                Utils.generateTypeFacedString(a, getString(R.string.taptarget_bn_komod_text)))
                                             .id(2));
                    }


                    TapTargetSequence seq = new TapTargetSequence(a).targets(targets)
                                                                    .continueOnCancel(true);
                    seq.start();
                }
            });


        }
    }

    @Override public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
            mDrawerLayout.closeDrawer(GravityCompat.END);
        }
//        else if (fabMenu.isOpened()) {
//            fabMenu.close(true);
//        }
        else if (!(fragment instanceof ProductsListFragment)) {


            fragment = new ProductsListFragment();
            Bundle b = new Bundle();
            b.putInt("user_id", userId);
            b.putString("user_name", userName);
            b.putBoolean("do_search", false);
            fragment.setArguments(b);
            loadFragment(fragment);
            //((ProductsListFragment) fragment).doSearch();
            setActiveView(0);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        if (UserUtils.isLoggedIn()) {
//            ActionItemBadge.update(this, menu.findItem(R.id.item_messages), GoogleMaterial.Icon.gmd_chat, ActionItemBadge.BadgeStyles.PURPLE, messagesBadgeCount > 0 ? messagesBadgeCount : Integer.MIN_VALUE);
//            ActionItemBadge.update(this, menu.findItem(R.id.item_items), Utils.getDrawableFromRes(this, R.drawable.komode_man_1), ActionItemBadge.BadgeStyles.PURPLE, productsBadgeCount > 0 ? productsBadgeCount : Integer.MIN_VALUE);
//        } else {
//            ActionItemBadge.hide(menu.findItem(R.id.item_messages));
//            ActionItemBadge.hide(menu.findItem(R.id.item_items));
//        }
        //ActionItemBadge.hide(menu.findItem(R.id.item_samplebadge));


        return true;
    }

    private void invalidateToolbar() {
        if (UserUtils.isLoggedIn()) {
            itemMessages.setVisibility(View.VISIBLE);
            //itemMyKomod.setVisibility(View.VISIBLE);
            if (messagesBadgeCount > 0) {
                menuBadgeMessages.setVisibility(View.VISIBLE);
                menuBadgeMessages.setText(messagesBadgeCount + "");
            } else {
                menuBadgeMessages.setVisibility(View.GONE);
            }
            if (productsBadgeCount > 0) {
                //menuBadgeMyKomod.setVisibility(View.VISIBLE);
                //menuBadgeMyKomod.setText(productsBadgeCount + "");
            } else {
                //menuBadgeMyKomod.setVisibility(View.GONE);
            }
        } else {
            itemMessages.setVisibility(View.GONE);
            //itemMyKomod.setVisibility(View.GONE);
        }
    }


    public boolean onNavigationItemClicked(long id) {
        // Handle navigation view item clicks here.

        mDrawerLayout.closeDrawer(GravityCompat.END);

        if (id == NAV_KOMOD) {
            if (!UserUtils.isLoggedIn()) {
                Utils.displayLoginErrorDialog(this);
                return true;
            }
            startActivity(new Intent(this, PurchasedProductsActivity.class));
        } else if (id == NAV_FINANCIAL_REPORT) {
            if (!UserUtils.isLoggedIn()) {
                Utils.displayLoginErrorDialog(this);
                return true;
            }
            startActivity(new Intent(this, FinancialReportActivity.class));

        } else if (id == NAV_ABOUT) {
            startActivity(new Intent(this, AboutActivity.class));

        } else if (id == NAV_TOP_SELLER) {
            startActivity(new Intent(this, TopSellersActivity.class));

        } else if (id == NAV_CONTACT) {
            startActivity(new Intent(this, ContactActivity.class));

        } else if (id == NAV_FAQ) {
            startActivity(new Intent(this, HelpActivity.class)
                    .putExtra("url", "https://komodaa.com/help/faq")
                    .putExtra("title", getString(R.string.faq)));

        } else if (id == NAV_RULES) {
            startActivity(new Intent(this, HelpActivity.class)
                    .putExtra("url", "https://komodaa.com/help/rules")
                    .putExtra("title", getString(R.string.nav_rules)));

        }
//        else if (id == R.id.nav_request) {
//            vpMain.setCurrentItem(1, true);
//
//        }
        else if (id == NAV_RADIOMODA) {
            startActivity(new Intent(this, NewsActivity.class));
        } else if (id == NAV_BOOKMARK) {
            startActivity(new Intent(this, BookmarksActivity.class));
        } else if (id == NAV_ACTIVITIES) {
            startActivity(new Intent(this, TimelineActivity.class));

        }  else if (id == NAV_INVITE) {
            startActivity(new Intent(this, InviteActivity.class));

        } else if (id == NAV_EXIT) {
            // Logout
            UserUtils.logout();
            //fabMenu.setVisibility(View.GONE);
            layoutUser.setVisibility(View.GONE);
            buttons.setVisibility(View.VISIBLE);
            invalidateToolbar();
            initDrawer();

            UIUtils.showSuccessMessage(this,"هم اکنون از حساب کاربری خارج شدی","");
        }

        return true;
    }

//    @OnClick({R.id.fab_add, R.id.fab_request/*, R.id.fab_purchased*/})
//    public void onFabItemClicked(View view) {
//        switch (view.getId()) {
//            case R.id.fab_add:
//                startActivity(new Intent(this, AddProductStep1Activity.class));
//                break;
//            case R.id.fab_request:
//                startActivity(new Intent(this, RequestActivity.class));
//                break;
////            case R.id.fab_purchased:
////                startActivity(new Intent(this, PurchasedProductsActivity.class));
////                break;
//        }
////        if (fabMenu.isOpened()) {
////            fabMenu.close(true);
////        }
//    }

    @OnClick({R.id.item_messages, R.id.item_my_komod, R.id.imgSearch})
    public void onToolbarItemClicked(View view) {
        switch (view.getId()) {
            case R.id.item_my_komod:
                if (!UserUtils.isLoggedIn()) {
                    Utils.displayLoginErrorDialog(this);
                    break;
                }
                startActivity(new Intent(this, PurchasedProductsActivity.class));
                break;
            case R.id.item_messages:
                if (!UserUtils.isLoggedIn()) {
                    Utils.displayLoginErrorDialog(this);
                    break;
                }
                startActivity(new Intent(this, MessageCenterActivity.class));
                break;
            case R.id.imgSearch:
//                Fragment fragment = pAdapter.getItem(vpMain.getCurrentItem());
//                if (fragment instanceof ISearchableFragment) {
//                    ((ISearchableFragment) fragment).doSearch();
//                }
        }
    }

    public void displaySearchButton(boolean show) {
        // imgSearch.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    class PagesAdapter extends FragmentPagerAdapter {
        List<BaseFragment> list;

        PagesAdapter(FragmentManager fm) {
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


    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        //transaction.addToBackStack(null);
        transaction.commit();
    }
}
