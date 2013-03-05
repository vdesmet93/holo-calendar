package com.vdesmet.lib.calendar;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.Calendar;

public class MultiCalendarView extends AbstractCalendarView {

    private int mCount;
    private boolean mShowIndicator;

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

        mShowIndicator = true;
        mIsViewInitialized = false;
        mFirstDayOfWeek = Calendar.MONDAY;
        mLastDayOfWeek = -1;
    }

    public void setIndicatorVisible(boolean visible) {
       this.mShowIndicator = visible;
    }

    @Override
    protected void initView() {
        final ViewPager viewPager = new ViewPager(getContext());
        final MultiCalendarAdapter adapter = new MultiCalendarAdapter(getContext(), this);
        viewPager.setAdapter(adapter);

        if(mShowIndicator) {
            TitlePageIndicator indicator = new TitlePageIndicator(getContext());
            indicator.setViewPager(viewPager);
            if(mTypeface != null) {
                indicator.setTypeface(mTypeface);
            }
            addView(indicator);
        }

        mAdapter = adapter;
        mViewPager = viewPager;

        addView(viewPager);
    }

}
