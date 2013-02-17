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
    private Calendar mCalendarFirstDay;

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
        mLastDayOfWeek = -1;

    }

    public void setFirstDayOfWeek(final int day) {
        if(day < Calendar.SUNDAY || day > Calendar.SATURDAY) {
            throw new IllegalArgumentException("day must be between " + Calendar.SUNDAY + " and " + Calendar.SATURDAY);
        }

        mFirstDayOfWeek = day;

        // update calendar
        updateCalendar();
    }

    public void setLastDayOfWeek(final int day) {
        if(day < Calendar.SUNDAY || day > Calendar.SATURDAY) {
            throw new IllegalArgumentException("day must be between " + Calendar.SUNDAY + " and " + Calendar.SATURDAY);
        }

        mLastDayOfWeek = day;

    }

    public void setDayAdapter(DayAdapter newAdapter) {
        this.mDayAdapter = newAdapter;
    }

    public void setCalendar(long monthInMillis) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(monthInMillis);

        this.mCurrentMonth = calendar.get(Calendar.MONTH);
        this.mCalendar = calendar;
        updateCalendar();

    }

    private void updateCalendar() {
        // create a new calendar
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mCalendar.getTimeInMillis());

        // change calendar to first day of month
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        // change day to firstDayOfWeek
        final int calendarDay = calendar.get(Calendar.DAY_OF_WEEK);

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
        this.mCalendarFirstDay = calendar;
    }

    @SuppressWarnings("ConstantConditions")
    private void initView() {

        // if no custom lastDayOfWeek was set, change it to the day before the first day so we show all 7 days
        if(mLastDayOfWeek == -1) {
            mLastDayOfWeek = mFirstDayOfWeek - 1;
            if(mLastDayOfWeek <= 0) {
                mLastDayOfWeek = 7;
            }
        }

        // create the headers for the day of the week
        createHeaders();

        // setup the variables we'll need
        final Context context = getContext();
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final DayAdapter adapter = mDayAdapter;
        final Calendar month = mCalendarFirstDay;
        final int firstDayOfWeek = mFirstDayOfWeek;
        final int lastDayOfWeek = mLastDayOfWeek;
        final int currentMonth = mCurrentMonth;
        final int dayDisabledBackgroundColor = getResources().getColor(R.color.lib_calendar_day_background_disabled);
        final int dayDisabledTextColor = getResources().getColor(R.color.lib_calendar_day_textcolor_disabled);

        ViewGroup weekLayout = (ViewGroup) inflater.inflate(R.layout.lib_calendar_week, this, false);

        // continue adding days until we've done the day at the end of the week(usually in the next month)
        while(month.get(Calendar.MONTH) <= currentMonth || month.get(Calendar.DAY_OF_WEEK) != lastDayOfWeek + 1) {

            // check if we need to add this day, if not, move to the next
            final int dayOfWeek = month.get(Calendar.DAY_OF_WEEK);
            boolean moveToNext = false;
            if(lastDayOfWeek < firstDayOfWeek) {
                if((dayOfWeek < firstDayOfWeek && dayOfWeek > lastDayOfWeek) ||
                        (dayOfWeek > lastDayOfWeek && dayOfWeek < firstDayOfWeek)) {

                    // we don't want to have this dayOfWeek in our calendar, so move to the next one
                    moveToNext = true;
                }
            }
            else if(dayOfWeek < firstDayOfWeek || dayOfWeek > lastDayOfWeek) {
                // we don't want to have this dayOfWeek in our calendar, so move to the next one
                moveToNext = true;
            }
            if(moveToNext) {
                // move to the next day
                month.add(Calendar.DAY_OF_WEEK, 1);
                continue;

            }
            // setup variables and layouts for this day
            final long timeInMillis = month.getTimeInMillis();
            final ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.lib_calendar_day, this, false);
            final TextView dayTextView = (TextView) layout.findViewById(R.id.lib_calendar_day_text);
            final ViewGroup categories = (ViewGroup) layout.findViewById(R.id.lib_calendar_day_categories);


            // allow the adapter to update the TextView
            // e.g. change TypeFace, font size, color based on the time
            adapter.updateTextView(dayTextView, timeInMillis);

            // set the current day: 1-31
            final int dayOfMonth = month.get(Calendar.DAY_OF_MONTH);
            dayTextView.setText(String.valueOf(dayOfMonth));

            // check if we need to disable the view, because it's in another month
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

            if(dayOfWeek == lastDayOfWeek) {
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
        final DayAdapter adapter = mDayAdapter;
        final int firstDayOfWeek = mFirstDayOfWeek;
        final int lastDayOfWeek = mLastDayOfWeek;

        final ViewGroup headers = (ViewGroup) inflater.inflate(R.layout.lib_calendar_headers, this, false);

        int dayOfWeek = firstDayOfWeek;

        do {
            final TextView header = (TextView) inflater.inflate(R.layout.lib_calendar_single_header, headers, false);
            final String name = getNameForDay(dayOfWeek, resources);

            adapter.updateHeaderTextView(header, dayOfWeek);

            header.setText(name);

            headers.addView(header);

            // increment dayOfWeek, make sure it's a valid day
            dayOfWeek = dayOfWeek % 7;
            dayOfWeek++;

        } while(dayOfWeek != lastDayOfWeek + 1);

        addView(headers);
    }

    private String getNameForDay(final int dayOfWeek, final Resources resources) {
        switch(dayOfWeek) {
            case Calendar.MONDAY:
                return resources.getString(R.string.lib_header_monday);
            case Calendar.TUESDAY:
                return resources.getString(R.string.lib_header_tuesday);
            case Calendar.WEDNESDAY:
                return resources.getString(R.string.lib_header_wednesday);
            case Calendar.THURSDAY:
                return resources.getString(R.string.lib_header_thursday);
            case Calendar.FRIDAY:
                return resources.getString(R.string.lib_header_friday);
            case Calendar.SATURDAY:
                return resources.getString(R.string.lib_header_saturday);
            case Calendar.SUNDAY:
                return resources.getString(R.string.lib_header_sunday);
            default:
                // unknown day
                throw new IllegalArgumentException("dayOfWeek is not valid. Pick a value between 1 and 7. " +
                        "dayOfWeek: " + dayOfWeek);
        }

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
