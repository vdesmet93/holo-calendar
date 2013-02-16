package com.vdesmet.lib.calendar;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;

public class CalendarView extends LinearLayout{

    private boolean mIsViewInitialized;

    private DayAdapter mDayAdapter;

    private int mCurrentMonth;
    private Calendar mCalendar;

    private int mFirstDayOfWeek;
    private int mLastDayOfWeek;

    public CalendarView(final Context context) {
        this(context, null);
    }

    public CalendarView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CalendarView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        setOrientation(VERTICAL);

        mIsViewInitialized = false;
        mFirstDayOfWeek = Calendar.MONDAY;

    }

    public void setFirstDayOfWeek(int day) {
        if(day < Calendar.SUNDAY || day > Calendar.SATURDAY) {
            throw new IllegalArgumentException("day must be between " + Calendar.SUNDAY + " and " + Calendar.SATURDAY);
        }

        mFirstDayOfWeek = day;

        if(day != Calendar.SUNDAY) {
            mLastDayOfWeek = day - 1;
        }
        else {
            mLastDayOfWeek = Calendar.SATURDAY;
        }

    }

    public void setDayAdapter(DayAdapter newAdapter) {
        this.mDayAdapter = newAdapter;
    }

    public void setCalendar(long monthInMillis) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(monthInMillis);

        this.mCurrentMonth = calendar.get(Calendar.MONTH);

        // change day to firstDayOfWeek
        final int calendarDay = calendar.get(Calendar.DAY_OF_WEEK); // 4 -> Wednesday

        // get the number of days we need to remove from the calendar, to start the calendar at mFirstDayOfWeek;
        final int daysTowithdraw = calendarDay - mFirstDayOfWeek;

        // withdraw that number from the calendar
        calendar.add(Calendar.DAY_OF_WEEK, -daysTowithdraw);

        // remove all the useless attributes from the calendar
        calendar.clear(Calendar.HOUR_OF_DAY);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);

        // set calendar
        this.mCalendar = calendar;
    }

    private void initView() {

        createHeaders();

        final Context context = getContext();
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final DayAdapter adapter = mDayAdapter;
        final Calendar month = mCalendar;
        final int lastDayOfWeek = mLastDayOfWeek;
        final int currentMonth = mCurrentMonth;
        final int dayDisabledBackgroundColor = getResources().getColor(R.color.lib_calendar_day_background_disabled);
        final int dayDisabledTextColor = getResources().getColor(R.color.lib_calendar_day_textcolor_disabled);

        ViewGroup weekLayout = (ViewGroup) inflater.inflate(R.layout.lib_calendar_week, this, false);

        // continue adding days until we've done the day at the end of the week in the next month
        while(month.get(Calendar.MONTH) <= currentMonth || month.get(Calendar.DAY_OF_WEEK) != lastDayOfWeek + 1) {
            final long timeInMillis = month.getTimeInMillis();
            final ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.lib_calendar_day, this, false);
            final TextView dayTextView = (TextView) layout.findViewById(R.id.lib_calendar_day_text);
            final ViewGroup categories = (ViewGroup) layout.findViewById(R.id.lib_calendar_day_categories);


            // allow the adapter to update the TextView
            // e.g. change TypeFace, font size, etc
            adapter.updateTextView(dayTextView, timeInMillis);

            // set the current day: 1-31
            final int dayOfMonth = month.get(Calendar.DAY_OF_MONTH);
            dayTextView.setText(String.valueOf(dayOfMonth));

            // check if we need to disable the view
            if(month.get(Calendar.MONTH) != currentMonth) {
                // change the appearance if it's disabled
                layout.setBackgroundColor(dayDisabledBackgroundColor);
                dayTextView.setTextColor(dayDisabledTextColor);
                // disable the views
                dayTextView.setEnabled(false);
                layout.setEnabled(false);
            }
            else {
                // create a new view for each category
                final int[] colors = adapter.getCategoryColors(timeInMillis);
                if(colors != null) {
                    for(final int color : colors) {
                        // inflate a new category
                        final View category = inflater.inflate(R.layout.lib_calendar_category, categories, false);

                        // set the background color to the color provided by the adapter
                        category.setBackgroundColor(color);

                        // add the view to the ViewGroup. Note that we can't do this while inflating
                        // because that will cause the view to match the size of his parent
                        categories.addView(category);
                    }
                }
            }



            // add layout to view
            weekLayout.addView(layout);

            if(month.get(Calendar.DAY_OF_WEEK) == lastDayOfWeek) {
                // this is the last day in the week/row, add a new one
                addView(weekLayout);

                weekLayout = (ViewGroup) inflater.inflate(R.layout.lib_calendar_week, this, false);
            }

            // add 1 day
            month.add(Calendar.DAY_OF_WEEK, 1);
        }

        if(weekLayout.getParent() == null) {
            addView(weekLayout);
        }


        mIsViewInitialized = true;
    }

    private void createHeaders() {
        final Context context = getContext();
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final Resources resources = context.getResources();
        final int firstDayOfWeek = mFirstDayOfWeek;
        final int lastDayOfWeek = mLastDayOfWeek;

        final ViewGroup headers = (ViewGroup) inflater.inflate(R.layout.lib_calendar_headers, this, false);

        int i = 0;
        for(int dayOfWeek = firstDayOfWeek; i < 7; dayOfWeek++, i++) {
            final TextView header = (TextView) inflater.inflate(R.layout.lib_calendar_single_header, headers, false);
            final String name = getNameForDay(dayOfWeek, resources);

            header.setText(name);

            headers.addView(header);
        }

        addView(headers);
    }

    private String getNameForDay(final int dayOfWeek, final Resources resources) {
        return resources.getString(R.string.lib_header_monday);
    }


    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {
        super.onLayout(changed, l, t, r, b);

        if(!mIsViewInitialized) {
            // initialize view
            long time = System.currentTimeMillis();
            initView();
            time = System.currentTimeMillis() - time;
            Log.d("D", "Initializing took: " + time);
        }
    }
}
