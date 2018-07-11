package com.komodaa.app.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.komodaa.app.App;
import com.komodaa.app.R;
import com.komodaa.app.fragments.ActivitiesFragment;
import com.komodaa.app.fragments.ConversationsFragment;
import com.komodaa.app.fragments.MessageCenterFragment;
import com.komodaa.app.interfaces.NetworkCallback;
import com.komodaa.app.models.Comment;
import com.komodaa.app.models.Error;
import com.komodaa.app.utils.ApiClient;
import com.komodaa.app.adapters.TabsAdapter;
import com.komodaa.app.utils.Utils;

import org.json.JSONArray;
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

public class MessageCenterActivity extends AppCompatActivity {


    @BindView(R.id.tabLayout) TabLayout tabLayout;
    @BindView(R.id.view_pager) ViewPager viewPager;
    private MessageCenterFragment komodaaFragment;
    private MessageCenterFragment commentsFragment;
    private String lastUpdatedDate;
    private boolean[] alreadyCleared = {false, false, false};

    @Override protected void onResume() {
        super.onResume();
        lastUpdatedDate = "2017-01-01";
        String confs = App.getPrefs().getString("user_meta", null);
        try {
            JSONObject j = new JSONObject(confs);
            // Set address field based on user meta
            if (j.has("meta")) {
                JSONArray meta = j.getJSONArray("meta");
                for (int i = 0; i < meta.length(); i++) {
                    JSONObject m = meta.getJSONObject(i);
                    if (m.getString("meta").equals("last_comment_checked_time")) {
                        lastUpdatedDate = m.getString("value");
                        break;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_center);
        ButterKnife.bind(this);

        getSupportActionBar().setTitle(Utils.generateTypeFacedString(this, getTitle().toString()));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TabsAdapter tAdapter = new TabsAdapter(getSupportFragmentManager());

        komodaaFragment = new MessageCenterFragment();

        commentsFragment = new MessageCenterFragment();

        tAdapter.setTitle(0, "پیام خصوصی");
        tAdapter.setTitle(1, "حرفای کمدا");
        tAdapter.setTitle(2, "نظرات");
        tAdapter.addFragment(new ConversationsFragment());
        tAdapter.addFragment(komodaaFragment);
        tAdapter.addFragment(commentsFragment);


        viewPager.setAdapter(tAdapter);
        viewPager.setCurrentItem(2);
        tabLayout.setupWithViewPager(viewPager);

        //changeTabsFont();
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab t = tabLayout.getTabAt(i).setCustomView(R.layout.tab_custom_view);
            ((TextView) t.getCustomView().findViewById(R.id.tab_text))
                    .setText(tAdapter.getPageTitle(i));
        }


        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
            @Override public void onTabSelected(final TabLayout.Tab tab) {
                super.onTabSelected(tab);
                clearTabBadge(tab);
            }
        });
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("fragment")) {
            try {
                viewPager.setCurrentItem(intent.getIntExtra("fragment", 2));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        refresh();

    }

    public void clearTabBadge(final TabLayout.Tab tab) {
        try {
            if (!alreadyCleared[tab.getPosition()]) {
                alreadyCleared[tab.getPosition()] = true;
                tabLayout.postDelayed(() -> updateTabBadge(tab, 0), 3000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void updateTabBadge(TabLayout.Tab tab, int badgeNumber) {
        TextView view = tab.getCustomView().findViewById(R.id.tab_badge);
        if (badgeNumber > 0) {
            view.setVisibility(View.VISIBLE);
            view.setText(Integer.toString(badgeNumber));
        } else {
            view.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void refresh() {
        final ProgressDialog pd = new ProgressDialog(MessageCenterActivity.this);
        pd.setIndeterminate(true);
        pd.setMessage(getString(R.string.loadin_please_wait));
        pd.show();
        final Handler h = new Handler(getMainLooper());
        ApiClient.makeRequest(this, "GET", "/messages", null, new NetworkCallback() {
            @Override public void onSuccess(int status, final JSONObject data) {
                h.post(() -> {
                    pd.dismiss();
                    try {
                        Log.d("", "run: lastUpdate = " + lastUpdatedDate + ", data: " + data);
                        int komodaaNewCommentsCount = 0;
                        int generalNewCommentsCount = 0;
                        JSONArray array = data.getJSONArray("messages");
                        int unreadChatsCount = data.getInt("unread_chat_count");
                        Log.d("UNREAD", "Unread chats: "+unreadChatsCount);
                        List<Comment> komodaaList = new ArrayList<>();
                        List<Comment> commentsList = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                            Comment c = new Comment(array.getJSONObject(i));
                            if (c.getSection() == 1) {
                                commentsList.add(c);
                                if (isAfterDate(lastUpdatedDate, c.getDate())) {
                                    generalNewCommentsCount++;
                                }
                            } else {
                                komodaaList.add(c);
                                if (isAfterDate(lastUpdatedDate, c.getDate())) {
                                    komodaaNewCommentsCount++;
                                }
                            }

                        }

                        commentsFragment.refreshData(commentsList);
                        komodaaFragment.refreshData(komodaaList);
                        updateTabBadge(tabLayout.getTabAt(0), unreadChatsCount);
                        updateTabBadge(tabLayout.getTabAt(1), komodaaNewCommentsCount);
                        updateTabBadge(tabLayout.getTabAt(2), generalNewCommentsCount);
                        alreadyCleared[0] = false;
                        alreadyCleared[1] = false;
                        alreadyCleared[2] = false;
                        clearTabBadge(tabLayout.getTabAt(1));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override public void onFailure(int status, Error error) {
                h.post(pd::dismiss);
            }
        });
    }

    private boolean isAfterDate(String firstDate, String secondDate) {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
        try {
            Date d1 = sdf.parse(firstDate);
            Date d2 = sdf.parse(secondDate);
            return !d2.before(d1);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void changeTabsFont() {

        Typeface tf = ResourcesCompat.getFont(this, R.font.iran_sans);

        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildsCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildsCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTypeface(tf, Typeface.BOLD);
                }
            }
        }
    }

}
