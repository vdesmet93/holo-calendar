package com.vdesmet.lib.calendar;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import java.util.Calendar;

public class MultiCalendarView extends LinearLayout {

    private boolean mIsViewInitialized;
    private int mFirstDayOfWeek;
    private int mLastDayOfWeek;

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

    private void initView() {

    }

    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {
        super.onLayout(changed, l, t, r, b);

        if(!mIsViewInitialized) {
            // initialize view
            initView();
        }
    }

}
