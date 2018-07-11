package com.komodaa.app.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.komodaa.app.App;
import com.komodaa.app.R;
import com.komodaa.app.adapters.TabsAdapter;
import com.komodaa.app.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by nevercom on 2/21/18.
 */

public class MyClosetFragment extends BaseFragment {
    static final String TAG = "MyCloset";
    private Unbinder unbinder;

    @BindView(R.id.tabLayout) TabLayout tabLayout;
    @BindView(R.id.view_pager) ViewPager viewPager;

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView() called with: inflater = [" + inflater + "], container = [" + container + "], savedInstanceState = [" + savedInstanceState + "]");

        View view = inflater.inflate(R.layout.activity_purchased_products, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated() called with: view = [" + view + "], savedInstanceState = [" + savedInstanceState + "]");

    }

    @Override public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");
        TabsAdapter tAdapter = new TabsAdapter(getChildFragmentManager());
        //tAdapter.addFragment(new MyRequestsFragment());
        tAdapter.addFragment(new MyItemsFragment());
        tAdapter.addFragment(new MyPurchaseHistoryFragment());
        viewPager.setAdapter(tAdapter);
        viewPager.setCurrentItem(2);
        tabLayout.setupWithViewPager(viewPager);
        changeTabsFont();
        //displayTapTargetIfNecessary();
    }

    private void displayTapTargetIfNecessary() {
        SharedPreferences prefs = App.getPrefs();
        if (prefs.getBoolean("is_first_time_activity_komod", true)) {
            prefs.edit().putBoolean("is_first_time_activity_komod", false).apply();
            final Activity a = getActivity();
            tabLayout.postDelayed(() -> {
                TapTarget[] tapTargets = {
                        TapTarget.forBounds(getRectForPosition(2, 1), Utils.generateTypeFacedString(a, "لیست خریدهایی که توی کمدا داشتی اینجا میاد")).outerCircleColor(R.color.colorPrimaryDark).id(1),
                        TapTarget.forBounds(getRectForPosition(2, 2), Utils.generateTypeFacedString(a, "لیست آیتم هایی که برای فروش گذاشتی اینجا دیده میشه")).outerCircleColor(R.color.colorPrimaryDark).id(2),
                        //TapTarget.forBounds(getRectForPosition(2, 3), Utils.generateTypeFacedString(a, "لیست درخواست هایی که ثبت کردی اینجاست")).outerCircleColor(R.color.colorPrimaryDark).id(3)
                };
                TapTargetSequence seq = new TapTargetSequence(a).targets(tapTargets).continueOnCancel(true);
                seq.start();
            }, 1000);


        }


    }

    private Rect getRectForPosition(int totalTabs, int position) {
        TypedValue tv = new TypedValue();
        int actionBarHeight = 0;
        if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }


        int top = actionBarHeight;
        int w = tabLayout.getWidth();
        int h = tabLayout.getHeight();
        int unit = w / totalTabs;
        Log.d("", String.format("Top =  %d, w = %d, h = %d, unit = %d", top, w, h, unit));
        return new Rect((w - (position * unit) + (unit / 2)) - (unit / 4), top + (h / 2), (w - (position * unit) + (unit / 2)) + (unit / 4), top + h + (h / 2));
    }

    private void changeTabsFont() {

        Typeface tf = ResourcesCompat.getFont(getActivity(), R.font.iran_sans);

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

    @Override public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView() called");
        unbinder.unbind();
    }
}
