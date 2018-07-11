package com.komodaa.app.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.komodaa.app.fragments.BaseFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nevercom on 11/7/16.
 */

public class TabsAdapter extends FragmentPagerAdapter {
    private List<BaseFragment> list;
    private List<String> titles = new ArrayList<>();

    public TabsAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override public Fragment getItem(int position) {
        return list.get(position);
    }

    @Override public int getCount() {
        return list == null ? 0 : list.size();
    }

    @Override public CharSequence getPageTitle(int position) {
        if (titles != null && titles.size() > 0) {
            return titles.get(position);
        }
        return list.get(position).getTitle();
    }

    public void addFragment(BaseFragment fragment) {
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(fragment);
    }

    public void setTitle(int position, String title) {
        titles.add(position, title);
    }
}