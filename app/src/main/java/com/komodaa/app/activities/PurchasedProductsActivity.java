package com.komodaa.app.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.komodaa.app.App;
import com.komodaa.app.R;
import com.komodaa.app.fragments.MyItemsFragment;
import com.komodaa.app.fragments.MyPurchaseHistoryFragment;
import com.komodaa.app.fragments.MyRequestsFragment;
import com.komodaa.app.adapters.TabsAdapter;
import com.komodaa.app.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PurchasedProductsActivity extends AppCompatActivity {


    @BindView(R.id.tabLayout) TabLayout tabLayout;
    @BindView(R.id.view_pager) ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchased_products);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(Utils.generateTypeFacedString(this, getTitle().toString()));

        TabsAdapter tAdapter = new TabsAdapter(getSupportFragmentManager());
        //tAdapter.addFragment(new MyRequestsFragment());
        tAdapter.addFragment(new MyItemsFragment());
        tAdapter.addFragment(new MyPurchaseHistoryFragment());
        viewPager.setAdapter(tAdapter);
        viewPager.setCurrentItem(1);
        tabLayout.setupWithViewPager(viewPager);
        changeTabsFont();
        displayTapTargetIfNecessary();
    }

    private void displayTapTargetIfNecessary() {
        SharedPreferences prefs = App.getPrefs();
        if (prefs.getBoolean("is_first_time_activity_komod", true)) {
            prefs.edit().putBoolean("is_first_time_activity_komod", false).apply();
            final Activity a = this;
            tabLayout.postDelayed(() -> {
                TapTarget[] tapTargets = {
                        TapTarget.forBounds(getRectForPosition(2, 1), Utils.generateTypeFacedString(a, "لیست خریدهایی که توی کمدا داشتی اینجا میاد")).outerCircleColor(R.color.colorPrimaryDark).id(1),
                        TapTarget.forBounds(getRectForPosition(2, 2), Utils.generateTypeFacedString(a, "لیست آیتم هایی که برای فروش گذاشتی اینجا دیده میشه")).outerCircleColor(R.color.colorPrimaryDark).id(2),
                        //TapTarget.forBounds(getRectForPosition(3, 3), Utils.generateTypeFacedString(a, "لیست درخواست هایی که ثبت کردی اینجاست")).outerCircleColor(R.color.colorPrimaryDark).id(3)
                };
                TapTargetSequence seq = new TapTargetSequence(a).targets(tapTargets).continueOnCancel(true);
                seq.start();
            }, 1000);


        }


    }

    private Rect getRectForPosition(int totalTabs, int position) {
        int top = getSupportActionBar().getHeight();
        int w = tabLayout.getWidth();
        int h = tabLayout.getHeight();
        int unit = w / totalTabs;
        Log.d("", String.format("Top =  %d, w = %d, h = %d, unit = %d", top, w, h, unit));
        return new Rect((w - (position * unit) + (unit / 2)) - (unit / 4), top + (h / 2), (w - (position * unit) + (unit / 2)) + (unit / 4), top + h + (h / 2));
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

    @Override protected void onResume() {
        super.onResume();
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
