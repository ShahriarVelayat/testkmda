package com.komodaa.app.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.komodaa.app.App;
import com.komodaa.app.BuildConfig;
import com.komodaa.app.R;
import com.komodaa.app.adapters.NavigationItemsAdapter;
import com.komodaa.app.fragments.BaseFragment;
import com.komodaa.app.fragments.FollowingsFragment;
import com.komodaa.app.fragments.ProductsListFragment;
import com.komodaa.app.fragments.RequestsListFragment;
import com.komodaa.app.interfaces.ISearchableFragment;
import com.komodaa.app.interfaces.NetworkCallback;
import com.komodaa.app.models.Error;
import com.komodaa.app.models.NavigationItem;
import com.komodaa.app.models.Product;
import com.komodaa.app.models.User;
import com.komodaa.app.utils.ApiClient;
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

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    static final int NAV_KOMOD = 1;
    static final int NAV_TOP_SELLER = 2;
    static final int NAV_BOOKMARK = 3;
    static final int NAV_FINANCIAL_REPORT = 4;
    static final int NAV_RADIOMODA = 5;
    static final int NAV_FAQ = 6;
    static final int NAV_RULES = 7;
    static final int NAV_CONTACT = 8;
    static final int NAV_ABOUT = 9;
    @BindView(R.id.tabs) TabLayout tabs;
    @BindView(R.id.vpMain) ViewPager vpMain;
    @BindView(R.id.toolbar) RelativeLayout toolbar;
    //@BindView(R.id.nav_view) NavigationView navigationView;
    @BindView(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @BindView(R.id.fab_add) FloatingActionButton fabAdd;
    //@BindView(R.id.fab_purchased) FloatingActionButton fabReceipt;
    @BindView(R.id.fab_request) FloatingActionButton fabRequest;
    @BindView(R.id.menu_labels_right) FloatingActionMenu fabMenu;
    @BindView(R.id.imgNavToggle) ImageView actionDrawer;
    @BindView(R.id.menu_badge_my_komod) TextView menuBadgeMyKomod;
    @BindView(R.id.item_my_komod) FrameLayout itemMyKomod;
    @BindView(R.id.menu_badge_messages) TextView menuBadgeMessages;
    @BindView(R.id.item_messages) FrameLayout itemMessages;
    @BindView(R.id.imgSearch) FrameLayout imgSearch;

    @BindView(R.id.imgAvatar) CircularImageView avatar;
    @BindView(R.id.tvFullName) TextView fullName;
    @BindView(R.id.tvEmail) TextView email;
    @BindView(R.id.tvLogout) TextView logout;
    @BindView(R.id.llUserInfo) RelativeLayout layoutUser;
    @BindView(R.id.btnLogin) Button btnLogin;
    @BindView(R.id.btnSignup) Button btnSignup;
    @BindView(R.id.llButtons) LinearLayout buttons;
    @BindView(R.id.navBtnSupport) Button navBtnSupport;
    @BindView(R.id.list) ListView list;


    private int messagesBadgeCount;
    private int productsBadgeCount;
    private SharedPreferences prefs;
    private MediaPlayer mPlayer;
    private Typeface font;
    private PagesAdapter pAdapter;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        font = Typeface.createFromAsset(getAssets(), "fonts/IRANSans.ttf");
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
        fabMenu.setClosedOnTouchOutside(true);
        //navigationView.setNavigationItemSelectedListener(this);
        actionDrawer.setOnClickListener(v -> {
            if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                mDrawerLayout.closeDrawer(Gravity.RIGHT);
            } else {
                mDrawerLayout.openDrawer(Gravity.RIGHT);
            }
        });
        navBtnSupport.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("tel:09309137334")));
            mDrawerLayout.closeDrawer(GravityCompat.END);
        });

        pAdapter = new PagesAdapter(getSupportFragmentManager());
        pAdapter.addFragment(new FollowingsFragment());
        pAdapter.addFragment(new RequestsListFragment());
        Intent intent = getIntent();
        int userId = -1;
        String userName = "کاربران";
        if (intent != null && intent.hasExtra("user_id")) {
            userId = intent.getIntExtra("user_id", -1);
            userName = intent.getStringExtra("user_name");
        }
        ProductsListFragment productsFragment = new ProductsListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("user_id", userId);
        bundle.putString("user_name", userName);
        productsFragment.setArguments(bundle);
        pAdapter.addFragment(productsFragment);

        vpMain.setAdapter(pAdapter);

//        vpMain.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//
//            }
//
//            @Override public void onPageSelected(int position) {
//                Fragment fragment = pAdapter.getItem(position);
//
//                displaySearchButton(fragment instanceof ISearchableFragment);
//
//            }
//
//            @Override public void onPageScrollStateChanged(int state) {
//
//            }
//        });
        vpMain.setCurrentItem(2);
        vpMain.setOffscreenPageLimit(3);
        tabs.setupWithViewPager(vpMain);
        changeTabsFont();
        //displaySearchButton(true);
        mPlayer = MediaPlayer.create(this, R.raw.munchausen);
        mPlayer.start();

//        overrideFonts(fabAdd);
//        overrideFonts(fabRequest);
    }

    private void initDrawer() {
        List<NavigationItem> items = new ArrayList<>();

        items.add(new NavigationItem(100, "تو کمدا چه خبره ؟", R.drawable.ic_my_komod));
        items.add(new NavigationItem(NAV_KOMOD, getString(R.string.my_closet), R.drawable.ic_my_komod));
        items.add(new NavigationItem(NAV_TOP_SELLER, getString(R.string.top_sellers), R.drawable.ic_action_seller));
        items.add(new NavigationItem(NAV_BOOKMARK, "بوکمارک", R.drawable.bookmark_normal));
        items.add(new NavigationItem(NAV_FINANCIAL_REPORT, getString(R.string.financial_report), R.drawable.ic_drawer_report));
        items.add(new NavigationItem(NAV_RADIOMODA, getString(R.string.radiomoda), R.drawable.radiomoda));
        items.add(new NavigationItem(NAV_FAQ, getString(R.string.faq), R.drawable.nav_faq));
        items.add(new NavigationItem(NAV_RULES, getString(R.string.nav_rules), R.drawable.ic_nav_rules));
        items.add(new NavigationItem(NAV_CONTACT, getString(R.string.contact_us), R.drawable.ic_contact_us));
        items.add(new NavigationItem(NAV_ABOUT, getString(R.string.about), R.drawable.logo_komodaa));

        NavigationItemsAdapter adapter = new NavigationItemsAdapter(items);
        list.setAdapter(adapter);

        list.setOnItemClickListener((parent, view, position, id) -> {
            onNavigationItemClicked(id);
        });
    }

    @Override
    protected void onDestroy() {

        mPlayer.stop();
        super.onDestroy();

    }

    @Override
    protected void onResume() {
        invalidateToolbar();
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


        btnLogin.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, LoginActivity.class)));
        btnSignup.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SignupActivity.class)));
        Log.d("", "onCreate: " + (UserUtils.isLoggedIn() ? "Logged In" : "Not Logged In"));
        if (UserUtils.isLoggedIn()) {
            fabMenu.setVisibility(View.VISIBLE);
            layoutUser.setVisibility(View.VISIBLE);
            buttons.setVisibility(View.GONE);

            logout.setOnClickListener(v -> {
                UserUtils.logout();
                fabMenu.setVisibility(View.GONE);
                layoutUser.setVisibility(View.GONE);
                buttons.setVisibility(View.VISIBLE);
                invalidateToolbar();
            });
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
            layoutUser.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));
            ApiClient.makeRequest(this, "GET", "/user_info", null, new NetworkCallback() {
                @Override
                public void onSuccess(int status, JSONObject data) {
                    if (data == null) {
                        return;
                    }
                    Log.d("userInfo", data.toString());
                    App.getPrefs().edit().putString("user_meta", data.toString()).apply();
                    if (data.has("messages_count")) {
                        try {
                            messagesBadgeCount = data.getInt("messages_count");
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
                            AchievementUnlocked notif = new AchievementUnlocked(MainActivity.this)
                                    .setTitle(getString(R.string.achievement_unread_messages_title))
                                    .setIcon(Utils.getDrawableFromRes(MainActivity.this, R.drawable.tick))
                                    .setTitleColor(Color.WHITE)
                                    .setSubtitleColor(Color.WHITE)
                                    .setTypeface(ResourcesCompat.getFont(MainActivity.this, R.font.iran_sans))
                                    .setSubTitle(String.format(Locale.US, getString(R.string.achievement_unread_messages_text), data.getInt("unread_messages")))
                                    .setBackgroundColor(Utils.getColorFromResource(MainActivity.this, R.color.notification_ok_purple))
                                    .isLarge(false)

                                    .build();

                            notif.getAchievementView().setOnClickListener(v -> {
                                Intent intent = new Intent(MainActivity.this, MessageCenterActivity.class);
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
                                UIUtils.showRatingDialog(MainActivity.this, p);
                            } else if (unratedCount > 1) {
                                UIUtils.showRatingRequestDialog(MainActivity.this, unrated.getString("dlg_title"), unrated.getString("dlg_content"));
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

            fabMenu.setVisibility(View.GONE);
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
                    Log.d("update", String.format("vCode: %d, CurrentDate: %s, Last Shown Date: %s, Diff: %d, ShouldDisplay: %s", vCode, currentDate, lastShownDate, diff, String.valueOf(shouldDisplay)));


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
                        boolean showCancel = !update.has("show_cancel") || update.getBoolean("show_cancel");


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
        showTapTargetIntroIfNecessary();
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
            startActivity(new Intent(this, PopupActivity.class).putExtra("json", message.toString()));
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
            if (UserUtils.isLoggedIn()) {
                if (prefs.getBoolean("tap.target.logged_in.items.shown", false)) {
                    return;
                }
                if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
                    mDrawerLayout.closeDrawer(GravityCompat.END);
                }
                prefs.edit().putBoolean("tap.target.logged_in.items.shown", true).apply();
                final ViewTreeObserver observer = getWindow().getDecorView().getViewTreeObserver();
                observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        View m = findViewById(R.id.item_messages);
                        if (m != null) {

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            } else {
                                getWindow().getDecorView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
                            }

                            //TapTargetView.showFor(MainActivity.this, TapTarget.forView(m, "یه مشت تیتر الکی"));
                            final Display display = getWindowManager().getDefaultDisplay();

                            final Drawable droid = Utils.getDrawableFromRes(a, R.drawable.menu_w);
                            // Tell our droid buddy where we want him to appear
                            final Rect droidTarget = new Rect(16, 16, droid.getIntrinsicWidth() * 2, droid.getIntrinsicHeight() * 2);
                            // Using deprecated methods makes you look way cool
                            droidTarget.offset(Utils.dpToPx(a, 16), display.getHeight() - Utils.dpToPx(a, 64));

                            TapTarget[] tapTargets = {
                                    TapTarget.forView(m, Utils.generateTypeFacedString(a, getString(R.string.taptarget_menu_messages_text))).id(1)/*,
                                    TapTarget.forBounds(droidTarget, Utils.generateTypeFacedString(a, getString(R.string.taptarget_view_fab_options))).icon(droid).id(2)*/
                            };

                            TapTargetSequence seq = new TapTargetSequence(a).targets(tapTargets).continueOnCancel(true);
                            seq.start();
                        }

                    }
                });
            } else {
                if (prefs.getBoolean("tap.target.toggle_btn.items.shown", false)) {
                    return;
                }
                prefs.edit().putBoolean("tap.target.toggle_btn.items.shown", true).apply();

                TapTarget[] tapTargets = {
                       /* TapTarget.forView(actionDrawer,
                                Utils.generateTypeFacedString(a, getString(R.string.taptarget_menu_toggle_title)),
                                Utils.generateTypeFacedString(a, getString(R.string.taptarget_menu_toggle_text))).id(1),*/
                        TapTarget.forView(imgSearch, Utils.generateTypeFacedString(a, getString(R.string.taptarget_toolbar_item_search))).id(2)
                };
                TapTargetSequence seq = new TapTargetSequence(a).targets(tapTargets).continueOnCancel(true);
                seq.start();
            }
        }
    }


    private void changeTabsFont() {

        Typeface typeface = ResourcesCompat.getFont(this, R.font.iran_sans);
        ViewGroup vg = (ViewGroup) tabs.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildsCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildsCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTypeface(typeface, Typeface.NORMAL);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
            mDrawerLayout.closeDrawer(GravityCompat.END);
        } else if (fabMenu.isOpened()) {
            fabMenu.close(true);
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
            itemMyKomod.setVisibility(View.VISIBLE);
            if (messagesBadgeCount > 0) {
                menuBadgeMessages.setVisibility(View.VISIBLE);
                menuBadgeMessages.setText(messagesBadgeCount + "");
            } else {
                menuBadgeMessages.setVisibility(View.GONE);
            }
            if (productsBadgeCount > 0) {
                menuBadgeMyKomod.setVisibility(View.VISIBLE);
                menuBadgeMyKomod.setText(productsBadgeCount + "");
            } else {
                menuBadgeMyKomod.setVisibility(View.GONE);
            }
        } else {
            itemMessages.setVisibility(View.GONE);
            itemMyKomod.setVisibility(View.GONE);
        }
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
//                    mDrawerLayout.closeDrawer(GravityCompat.END);
//                } else {
//                    mDrawerLayout.openDrawer(GravityCompat.END);
//                }
//                return true;
//            case R.id.item_items:
//                if (!UserUtils.isLoggedIn()) {
//                    Utils.displayLoginErrorDialog(this);
//                    return true;
//                }
//                startActivity(new Intent(this, PurchasedProductsActivity.class));
//                return true;
//            case R.id.item_messages:
//                if (!UserUtils.isLoggedIn()) {
//                    Utils.displayLoginErrorDialog(this);
//                    return true;
//                }
//                startActivity(new Intent(this, MessageCenterActivity.class));
//                return true;
////            case R.id.item_search:
////                showSearchDialog();
////                return true;
//
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//
//    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_cart) {
            if (!UserUtils.isLoggedIn()) {
                Utils.displayLoginErrorDialog(this);
                return true;
            }
            startActivity(new Intent(this, PurchasedProductsActivity.class));
        } else if (id == R.id.nav_financial_report) {
            if (!UserUtils.isLoggedIn()) {
                Utils.displayLoginErrorDialog(this);
                return true;
            }
            startActivity(new Intent(this, FinancialReportActivity.class));

        } else if (id == R.id.nav_about) {
            startActivity(new Intent(this, AboutActivity.class));

        } else if (id == R.id.nav_sellers) {
            startActivity(new Intent(this, TopSellersActivity.class));

        } else if (id == R.id.nav_contact) {
            startActivity(new Intent(this, ContactActivity.class));

        } else if (id == R.id.nav_faq) {
            startActivity(new Intent(this, HelpActivity.class).putExtra("url", "https://komodaa.com/help/faq").putExtra("title", getString(R.string.faq)));

        } else if (id == R.id.nav_rules) {
            startActivity(new Intent(this, HelpActivity.class).putExtra("url", "https://komodaa.com/help/rules").putExtra("title", getString(R.string.nav_rules)));

        }
//        else if (id == R.id.nav_request) {
//            vpMain.setCurrentItem(1, true);
//
//        }
        else if (id == R.id.nav_news) {
            startActivity(new Intent(this, NewsActivity.class));

        } else if (id == R.id.nav_bookmark) {
            startActivity(new Intent(this, BookmarksActivity.class));
        }

        mDrawerLayout.closeDrawer(GravityCompat.END);
        return true;
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
            startActivity(new Intent(this, HelpActivity.class).putExtra("url", "https://komodaa.com/help/faq").putExtra("title", getString(R.string.faq)));

        } else if (id == NAV_RULES) {
            startActivity(new Intent(this, HelpActivity.class).putExtra("url", "https://komodaa.com/help/rules").putExtra("title", getString(R.string.nav_rules)));

        }
//        else if (id == R.id.nav_request) {
//            vpMain.setCurrentItem(1, true);
//
//        }
        else if (id == NAV_RADIOMODA) {
            startActivity(new Intent(this, NewsActivity.class));

        } else if (id == NAV_BOOKMARK) {
            startActivity(new Intent(this, BookmarksActivity.class));
        } else if (id == 100) {
            //startActivity(new Intent(this, TimelineActivity.class));
            startActivity(new Intent(this, ConversationsActivity.class));

        }

        return true;
    }

    @OnClick({R.id.fab_add, R.id.fab_request/*, R.id.fab_purchased*/})
    public void onFabItemClicked(View view) {
        switch (view.getId()) {
            case R.id.fab_add:
                startActivity(new Intent(this, AddProductStep1Activity.class));
                break;
            case R.id.fab_request:
                startActivity(new Intent(this, RequestActivity.class));
                break;
//            case R.id.fab_purchased:
//                startActivity(new Intent(this, PurchasedProductsActivity.class));
//                break;
        }
        if (fabMenu.isOpened()) {
            fabMenu.close(true);
        }
    }

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
                Fragment fragment = pAdapter.getItem(vpMain.getCurrentItem());
                if (fragment instanceof ISearchableFragment) {
                    ((ISearchableFragment) fragment).doSearch();
                }
        }
    }

    public void displaySearchButton(boolean show) {
        imgSearch.setVisibility(show ? View.VISIBLE : View.GONE);
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

    public void overrideFonts(final View v) {
        final boolean isBelowICS = Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH;
        try {
            if (v instanceof ViewGroup) {
                final ViewGroup vg = (ViewGroup) v;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    final View child = vg.getChildAt(i);
                    overrideFonts(child);
                }
            } else {
                if (v instanceof TextView) {
                    ((TextView) v).setTypeface(font);
                    if (isBelowICS) {
                        ((TextView) v).setText(((TextView) v).getText()
                                .toString());
                        // v.setAnimation(AnimationUtils.loadAnimation(context,
                        // R.anim.button));
                    }
                } else if (v instanceof EditText) {
                    ((EditText) v).setTypeface(font);
                    if (isBelowICS) {
                        ((EditText) v).setText(((EditText) v).getText()
                                .toString());
                    }
                } else if (v instanceof android.widget.Button) {
                    ((android.widget.Button) v).setTypeface(font);
                    if (isBelowICS) {
                        ((android.widget.Button) v).setText(((android.widget.Button) v).getText()
                                .toString());
                    }

                }
            }

        } catch (final Exception e) {
        }
    }

}
