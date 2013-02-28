package com.vdesmet.lib.calendar;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.Calendar;

public class MultiCalendarView extends AbstractCalendarView {

    private int mCount;

    private ViewPager mViewPager;
    private MultiCalendarAdapter mAdapter;

    public MultiCalendarView(final Context context) {
        super(context);
        init();
    }

    public MultiCalendarView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MultiCalendarView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setOrientation(VERTICAL);

        mIsViewInitialized = false;
        mFirstDayOfWeek = Calendar.MONDAY;
        mLastDayOfWeek = -1;
    }

    @Override
    protected void initView() {
        final ViewPager viewPager = new ViewPager(getContext());
        final MultiCalendarAdapter adapter = new MultiCalendarAdapter(getContext(), this);
        viewPager.setAdapter(adapter);

        TitlePageIndicator indicator = new TitlePageIndicator(getContext());
        indicator.setViewPager(viewPager);
        addView(indicator);

        mAdapter = adapter;
        mViewPager = viewPager;

        addView(viewPager);
    }

}
