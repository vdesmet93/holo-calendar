package com.vdesmet.lib.calendar;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import com.viewpagerindicator.TitleProvider;

import java.util.Calendar;

public class MultiCalendarAdapter extends PagerAdapter implements TitleProvider{
    private final Context mContext;
    private final MultiCalendarView mCalendarView;


    public MultiCalendarAdapter(final Context context, final MultiCalendarView calendarView) {
        super();
        this.mCalendarView = calendarView;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        final MultiCalendarView calendarView = mCalendarView;

        final Calendar firstDay = calendarView.getFirstValidDay();
        final Calendar lastDay = calendarView.getLastValidDay();

        // get the difference in years and in months
        // note that months may be smaller than zero,
        // for example, when firstDay is December 2012 and lastDay is January 2013: (1*12) + (0 - 11) = 1 month
        final int years = lastDay.get(Calendar.YEAR) - firstDay.get(Calendar.YEAR);
        final int months = lastDay.get(Calendar.MONTH) - firstDay.get(Calendar.MONTH);

        final int diffMonths =  (years * 12 ) + months;

        // January - February is 1 month later, but we have 2 months to show
        return diffMonths + 1;

    }

    @Override
    public void destroyItem(final ViewGroup container, final int position, final Object item) {
        if(item instanceof View) {
            container.removeView((View)item);
        }
    }

    @Override
    public View instantiateItem(final ViewGroup container, final int position) {
        // initialize variables
        final MultiCalendarView multiCalendarView = mCalendarView;
        final Context context = multiCalendarView.getContext();
        final Calendar firstDay = multiCalendarView.getFirstValidDay();
        final Calendar lastDay = multiCalendarView.getLastValidDay();
        final DayAdapter dayAdapter = multiCalendarView.getDayAdapter();
        final OnDayClickListener onDayClickListener = multiCalendarView.getOnDayClickListener();
        final int firstDayOfWeek = multiCalendarView.getFirstDayOfWeek();
        final int lastDayOfWeek = multiCalendarView.getLastDayOfWeek();

        // create first day of the monthView
        final Calendar firstMonthDay = Calendar.getInstance();
        firstMonthDay.setTimeInMillis(firstDay.getTimeInMillis());
        firstMonthDay.add(Calendar.MONTH, position);
        if(position != 0) {
            firstMonthDay.set(Calendar.DAY_OF_MONTH, 1);
        }


        // create and configure the view
        final CalendarView monthView = new CalendarView(context);
        monthView.setFirstValidDay(firstMonthDay);

        if(lastDay.get(Calendar.MONTH) == firstMonthDay.get(Calendar.MONTH)) {
            // the last day is in this month
            monthView.setLastValidDay(lastDay);
        }


        // add adapter and onClickListener
        monthView.setOnDayClickListener(onDayClickListener);
        monthView.setDayAdapter(dayAdapter);

        // set first and last day of week
        monthView.setFirstDayOfWeek(firstDayOfWeek);
        monthView.setLastDayOfWeek(lastDayOfWeek);

        // FIXME Should be called automatically
        monthView.initView();

        // return view
        container.addView(monthView);
        return monthView;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }


    @Override
    public boolean isViewFromObject(final View view, final Object o) {
        return view == o;
    }


    /**
     * Get the title for the current position
     * @param position - The position in the ViewPager
     * @return Title: MONTH YEAR -> MARCH 2013
     */
    @Override
    public String getTitle(final int position) {
        final Context context = mContext;
        final Calendar date = Calendar.getInstance();
        date.setTimeInMillis((mCalendarView.getFirstValidDay().getTimeInMillis()));
        date.add(Calendar.MONTH, position);

        final String month;
        switch(position + mCalendarView.getCurrentMonth()) {
            case Calendar.JANUARY:
                month = context.getString(R.string.lib_month_january);
                break;
            case Calendar.FEBRUARY:
                month = context.getString(R.string.lib_month_february);
                break;
            case Calendar.MARCH:
                month = context.getString(R.string.lib_month_march);
                break;
            case Calendar.APRIL:
                month = context.getString(R.string.lib_month_april);
                break;
            case Calendar.MAY:
                month = context.getString(R.string.lib_month_may);
                break;
            case Calendar.JUNE:
                month = context.getString(R.string.lib_month_june);
                break;
            case Calendar.JULY:
                month = context.getString(R.string.lib_month_july);
                break;
            case Calendar.AUGUST:
                month = context.getString(R.string.lib_month_august);
                break;
            case Calendar.SEPTEMBER:
                month = context.getString(R.string.lib_month_september);
                break;
            case Calendar.OCTOBER:
                month = context.getString(R.string.lib_month_october);
                break;
            case Calendar.NOVEMBER:
                month = context.getString(R.string.lib_month_november);
                break;
            default:
            case Calendar.DECEMBER:
                month = context.getString(R.string.lib_month_december);
                break;
         }

        return month + " " + date.get(Calendar.YEAR);
    }
}
