package com.komodaa.app.activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.komodaa.app.R;
import com.komodaa.app.fragments.TopUsersFragment;
import com.komodaa.app.adapters.TabsAdapter;
import com.komodaa.app.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TopSellersActivity extends AppCompatActivity {

    @BindView(R.id.tabLayout) TabLayout tabLayout;
    @BindView(R.id.view_pager) ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_center);

        getSupportActionBar().setTitle(Utils.generateTypeFacedString(this, getTitle().toString()));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);

        TabsAdapter tAdapter = new TabsAdapter(getSupportFragmentManager());

        TopUsersFragment topSellers = new TopUsersFragment();
        Bundle sb = new Bundle();
        sb.putBoolean("sellers", true);
        topSellers.setArguments(sb);

        TopUsersFragment topBuyers = new TopUsersFragment();
        Bundle bb = new Bundle();
        bb.putBoolean("sellers", false);
        topBuyers.setArguments(bb);

        tAdapter.setTitle(0, "خوش فروش‌ها");
        tAdapter.addFragment(topSellers);

        tAdapter.setTitle(1, "خوش خریدها");
        tAdapter.addFragment(topBuyers);


        viewPager.setAdapter(tAdapter);
        viewPager.setCurrentItem(1);
        tabLayout.setupWithViewPager(viewPager);
    }
//        private void changeTabsFont() {
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
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
