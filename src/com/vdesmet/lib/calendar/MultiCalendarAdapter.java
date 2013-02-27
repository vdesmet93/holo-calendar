package com.vdesmet.lib.calendar;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;

public class MultiCalendarAdapter extends FragmentPagerAdapter {
    private final Context mContext;


    public MultiCalendarAdapter(final Context context, final FragmentManager fm) {
        super(fm);
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Fragment getItem(final int i) {
        return null;
    }

    @Override
    public boolean isViewFromObject(final View view, final Object o) {
        return false;
    }

}
