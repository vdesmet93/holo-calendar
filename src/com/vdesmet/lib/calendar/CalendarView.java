package com.vdesmet.lib.calendar;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;

public class CalendarView extends LinearLayout implements View.OnClickListener {

    private boolean mIsViewInitialized;

    private DayAdapter mDayAdapter;

    private int mCurrentMonth;
    private Calendar mCalendar;
    private Calendar mCalendarFirstDay;

    private int mFirstDayOfWeek;
    private int mLastDayOfWeek;

    private Calendar mFirstValidDay;
    private Calendar mLastValidDay;

    private OnDayClickListener mOnDayClickListener;

    public CalendarView(final Context context) {
        super(context);
        init();
    }

    public CalendarView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CalendarView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init();

    }

    private void init() {
        setOrientation(VERTICAL);

        mIsViewInitialized = false;
        mFirstDayOfWeek = Calendar.MONDAY;
        mLastDayOfWeek = -1;
    }

    /**
     * Set the first day of the week that needs to be shown
     * All days of the week are valid
     * @param day Day of the week. e.g. Calendar.MONDAY
     */
    public void setFirstDayOfWeek(final int day) {
        if(day < Calendar.SUNDAY || day > Calendar.SATURDAY) {
            throw new IllegalArgumentException("day must be between " + Calendar.SUNDAY + " and " + Calendar.SATURDAY);
        }

        mFirstDayOfWeek = day;

        // update calendar
        updateCalendar();
    }

    /**
     * (Optional) Set the last day of the week that needs to be shown
     * All days of the week are valid
     * If not set, it'll pick the day before the first day of the week
     * Possible implementation: Hide the weekends -> setLastDayOfWeek(Calendar.FRIDAY)
     * @param day Day of the week. e.g. Calendar.SUNDAY
     */
    public void setLastDayOfWeek(final int day) {
        if(day < Calendar.SUNDAY || day > Calendar.SATURDAY) {
            throw new IllegalArgumentException("day must be between " + Calendar.SUNDAY + " and " + Calendar.SATURDAY);
        }

        mLastDayOfWeek = day;

    }

    /**
     * (Optional) Set a custom first valid day. If set, all the days before this day will be disabled.
     * If not set, this will be the first day of the month
     * @param firstValidDay The first valid day
     */
    public void setFirstValidDay(final Calendar firstValidDay) {
        // set all the useless attributes to 0
        firstValidDay.set(Calendar.HOUR_OF_DAY, 0);
        firstValidDay.set(Calendar.MINUTE, 0);
        firstValidDay.set(Calendar.SECOND, 0);
        firstValidDay.set(Calendar.MILLISECOND, 0);

        this.mFirstValidDay = firstValidDay;
    }

    /**
     * (Optional) Set a custom last valid day. If set, all the days after this day will be disabled.
     * If not set, this will be the last day of the month
     * @param lastValidDay The last valid day
     */
    public void setLastValidDay(final Calendar lastValidDay) {
        // set all the useless attributes to 0
        lastValidDay.set(Calendar.HOUR_OF_DAY, 0);
        lastValidDay.set(Calendar.MINUTE, 0);
        lastValidDay.set(Calendar.SECOND, 0);
        lastValidDay.set(Calendar.MILLISECOND, 0);

        this.mLastValidDay = lastValidDay;
    }

    /**
     * Set a DayAdapter
     * The DayAdapter will be able to change the TextViews of the headers/days
     * and is able to add Category Colors to days
     * @param newAdapter The (new) adapter to be set
     */
    public void setDayAdapter(DayAdapter newAdapter) {
        this.mDayAdapter = newAdapter;
    }

    /**
     * Set the time in millis of the month that will be shown
     * @param monthInMillis Time in millis of month that will be visible
     */
    public void setCalendar(long monthInMillis) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(monthInMillis);

        this.mCurrentMonth = calendar.get(Calendar.MONTH);
        this.mCalendar = calendar;
        updateCalendar();

    }

    /**
     * Set an onDayClick listener, which will be called when the user clicked on a valid Day
     * @param listener Listener to respond to onClick events
     */
    public void setOnDayClickListener(OnDayClickListener listener) {
        this.mOnDayClickListener = listener;
    }

    /**
     * Will be called by the TextView
     * This method will call the adapter's onDayClick method
     */
    @Override
    public void onClick(final View v) {
        // user clicked on a TextView
        if(v != null) {
            final long timeInMillis =  Long.parseLong(v.getTag().toString());
            if(mOnDayClickListener != null) {
                mOnDayClickListener.onDayClick(timeInMillis);
            }
        }
    }

    /**
     * Updates mFirstDayCalendar, so initView() knows on which day he needs to start
     * creating the views.
     */
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

        // set all the useless attributes to 0
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // set calendar
        this.mCalendarFirstDay = calendar;
    }

    /**
     * Create the view
     * Initializes the headers, views for all visible days
     */
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
        final Calendar currentDay = mCalendarFirstDay;
        final Calendar firstValidDay = mFirstValidDay;
        final Calendar lastValidDay = mLastValidDay;
        final int firstDayOfWeek = mFirstDayOfWeek;
        final int lastDayOfWeek = mLastDayOfWeek;
        final int currentMonth = mCurrentMonth;
        final int dayDisabledBackgroundColor = getResources().getColor(R.color.lib_calendar_day_background_disabled);
        final int dayDisabledTextColor = getResources().getColor(R.color.lib_calendar_day_textcolor_disabled);

        ViewGroup weekLayout = (ViewGroup) inflater.inflate(R.layout.lib_calendar_week, this, false);

        // continue adding days until we've done the day at the end of the week(usually in the next month)
        while(currentDay.get(Calendar.MONTH) <= currentMonth || currentDay.get(Calendar.DAY_OF_WEEK) != lastDayOfWeek + 1) {

            // check if we need to add this day, if not, move to the next
            final int dayOfWeek = currentDay.get(Calendar.DAY_OF_WEEK);
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
                currentDay.add(Calendar.DAY_OF_WEEK, 1);
                continue;

            }
            // setup variables and layouts for this day
            final long timeInMillis = currentDay.getTimeInMillis();
            final ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.lib_calendar_day, this, false);
            final TextView dayTextView = (TextView) layout.findViewById(R.id.lib_calendar_day_text);
            final ViewGroup categories = (ViewGroup) layout.findViewById(R.id.lib_calendar_day_categories);


            // allow the adapter to update the TextView
            // e.g. change TypeFace, font size, color based on the time
            adapter.updateTextView(dayTextView, timeInMillis);

            // set the current day: 1-31
            final int dayOfMonth = currentDay.get(Calendar.DAY_OF_MONTH);
            dayTextView.setText(String.valueOf(dayOfMonth));

            // check if we need to disable the view, because it's in another month, or
            // if it's before the first valid day, or after the last valid day
            if((currentDay.get(Calendar.MONTH) != currentMonth) ||
               (firstValidDay != null && currentDay.before(firstValidDay)) ||
               (lastValidDay != null && currentDay.after(lastValidDay))) {
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

            // set TextView tag to the timeInMillis for the onClickListener
            dayTextView.setTag(timeInMillis);
            dayTextView.setOnClickListener(this);


            // add layout to view
            weekLayout.addView(layout);

            if(dayOfWeek == lastDayOfWeek) {
                // this is the last day in the week/row, add a new one
                addView(weekLayout);

                weekLayout = (ViewGroup) inflater.inflate(R.layout.lib_calendar_week, this, false);
            }

            // add 1 day
            currentDay.add(Calendar.DAY_OF_WEEK, 1);
        }

        if(weekLayout.getParent() == null) {
            addView(weekLayout);
        }


        mIsViewInitialized = true;
    }

    /**
     * Create the headers for each (visible) day of the week
     * Starts at mFirstDayOfWeek, ends at mLastDayOfWeek
     */
    private void createHeaders() {
        // initialize variables
        final Context context = getContext();
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final Resources resources = context.getResources();
        final DayAdapter adapter = mDayAdapter;
        final int firstDayOfWeek = mFirstDayOfWeek;
        final int lastDayOfWeek = mLastDayOfWeek;

        // inflate the ViewGroup where we'll put all the headers
        final ViewGroup headers = (ViewGroup) inflater.inflate(R.layout.lib_calendar_headers, this, false);

        int dayOfWeek = firstDayOfWeek;

        do {
            // initialize variables for this day
            final TextView header = (TextView) inflater.inflate(R.layout.lib_calendar_single_header, headers, false);
            final String nameOfDay = getNameForDay(dayOfWeek, resources);

            // allow adapter to update the TextView
            // e.g. change font, appearance, add click listener on all/some days
            adapter.updateHeaderTextView(header, dayOfWeek);

            // set the text
            header.setText(nameOfDay);

            // add TextView to ViewGroup
            headers.addView(header);

            // increment dayOfWeek, make sure it's a valid day
            dayOfWeek = dayOfWeek % 7;
            dayOfWeek++;

        } while(dayOfWeek != lastDayOfWeek + 1);

        // add the headers View
        addView(headers);
    }

    /**
     * Get a human readable name for this day of the week
     * @param dayOfWeek between Calendar.SUNDAY and Calendar.SATURDAY
     * @param resources A resources object which can be retrieved by Context.getResources()
     * @return A name for this day of the week. MON - SUN.
     * @throws IllegalArgumentException Thrown when provided dayOfWeek is invalid
     */
    private String getNameForDay(final int dayOfWeek, final Resources resources) throws IllegalArgumentException {
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
            initView();
        }
    }


}
