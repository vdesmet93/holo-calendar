package com.vdesmet.lib.calendar;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import java.util.Calendar;

public abstract class AbstractCalendarView extends LinearLayout {
    protected boolean mIsViewInitialized;

    protected DayAdapter mDayAdapter;
    protected Calendar mCalendarFirstDay;

    protected int mCurrentMonth;
    protected int mFirstDayOfWeek;

    protected int mLastDayOfWeek;

    protected Calendar mFirstValidDay;
    protected Calendar mLastValidDay;

    protected OnDayClickListener mOnDayClickListener;

    protected Typeface mTypeface;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public AbstractCalendarView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    public AbstractCalendarView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public AbstractCalendarView(final Context context) {
        super(context);
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

        if(mFirstValidDay != null) {
            // update calendar
            updateCalendar();
        }

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
     * Set a first valid day. All the days before this day will be disabled.
     * @param firstValidDay The first valid day
     */
    public void setFirstValidDay(final Calendar firstValidDay) {
        // set all the useless attributes to 0
        firstValidDay.set(Calendar.HOUR_OF_DAY, 0);
        firstValidDay.set(Calendar.MINUTE, 0);
        firstValidDay.set(Calendar.SECOND, 0);
        firstValidDay.set(Calendar.MILLISECOND, 0);

        mCurrentMonth = firstValidDay.get(Calendar.MONTH);

        this.mFirstValidDay = firstValidDay;

        updateCalendar();
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
     * Set a custom Typeface for the days and headers(1-31 and Mon-Sun)
     * @param newTypeFace The new Typeface which will be used
     */
    public void setTypeface(final Typeface newTypeFace) {
        this.mTypeface = newTypeFace;
    }

    /**
     * Set an onDayClick listener, which will be called when the user clicked on a valid Day
     * @param listener Listener to respond to onClick events
     */
    public void setOnDayClickListener(OnDayClickListener listener) {
        this.mOnDayClickListener = listener;
    }

    /**
     * Updates mFirstDayCalendar, so initView() knows on which day he needs to start
     * creating the views.
     */
    private void updateCalendar() {
        // throw an exception if there is no calendar available
        if(mFirstValidDay == null) {
            throw new NullPointerException("mFirstValidDay is null. " +
                    "Did you forget to call setFirstValidDay(Calendar) to set the month?");
        }
        // create a new calendar
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mFirstValidDay.getTimeInMillis());

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
     * Get a human readable name for this day of the week
     * @param dayOfWeek between Calendar.SUNDAY and Calendar.SATURDAY
     * @param resources A resources object which can be retrieved by Context.getResources()
     * @return A name for this day of the week. MON - SUN.
     * @throws IllegalArgumentException Thrown when provided dayOfWeek is invalid
     */
    protected String getNameForDay(final int dayOfWeek, final Resources resources) throws IllegalArgumentException {
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

        if(!mIsViewInitialized) {
            // initialize view
            initView(r - l);
        }
        super.onLayout(changed, l, t, r, b);
    }

    protected abstract void initView(final int width);

    /** Getter methods */
    public int getFirstDayOfWeek() {
        return mFirstDayOfWeek;
    }

    public int getLastDayOfWeek() {
        return mLastDayOfWeek;
    }

    public Calendar getFirstValidDay() {
        return mFirstValidDay;
    }

    public Calendar getLastValidDay() {
        return mLastValidDay;
    }

    public DayAdapter getDayAdapter() {
        return mDayAdapter;
    }

    public OnDayClickListener getOnDayClickListener() {
        return mOnDayClickListener;
    }

    public int getCurrentMonth() {
        return mCurrentMonth;
    }

    public int getDaysInRow() {
        int firstDayOfWeek = mFirstDayOfWeek;
        int daysInRow = 1;
        while(firstDayOfWeek != mLastDayOfWeek) {
            // Go to the next day in the week
            firstDayOfWeek = firstDayOfWeek % 7;
            firstDayOfWeek++;

            // Add another day
            daysInRow++;
        }

        return daysInRow;
    }

}
