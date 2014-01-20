package com.vdesmet.lib.calendar;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vdesmet.lib.calendar.factory.DayStyleFactory;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.Calendar;

public abstract class AbstractCalendarView extends LinearLayout {
    public static final int MONTHS_IN_YEAR = 12;
    private static final int DAYS_IN_WEEK = 7;
    private static final int MAX_WEEKS_IN_MONTH = 6;

    protected boolean mIsViewInitialized;

    protected DayAdapter mDayAdapter;
    protected Calendar mCalendarFirstDay;

    protected int mDayStyle;

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
        parseAttributes(context, attrs);
    }

    public AbstractCalendarView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        parseAttributes(context, attrs);
    }

    public AbstractCalendarView(final Context context) {
        super(context);
    }

    private void parseAttributes(final Context context, final AttributeSet attrs) {
        final Resources.Theme theme = context.getTheme();
        if(theme != null) {
            TypedArray a = theme.obtainStyledAttributes(
                    attrs,
                    R.styleable.AbstractCalendarView,
                    0, 0);

            try {
                // Retrieve the first day of week
                //noinspection ConstantConditions
                final int firstDay = a.getInt(R.styleable.AbstractCalendarView_firstDayOfWeek, Calendar.MONDAY);
                setFirstDayOfWeek(firstDay);

                // Retrieve the last day of week
                final int lastDay = a.getInt(R.styleable.AbstractCalendarView_firstDayOfWeek, Calendar.SUNDAY);
                setLastDayOfWeek(lastDay);

                // Retrieve the style for the day views
                mDayStyle = a.getInt(R.styleable.AbstractCalendarView_dayStyle, DayStyleFactory.DEFAULT_STYLE);
            } finally {
                //noinspection ConstantConditions
                a.recycle();
            }
        }
    }

    /**
     * Set the style for a single day. See DayStyleFactory for options.
     *
     * @param dayStyle The style
     * @see com.vdesmet.lib.calendar.factory.DayStyleFactory DayStyleFactory: Check the available values
     */
    public void setDayStyle(int dayStyle) {
        if(DayStyleFactory.isValidStyle(dayStyle)) {
            this.mDayStyle = dayStyle;
        } else {
            throw new IllegalArgumentException("Day Style is invalid. Check DayStyleFactory for options");
        }
    }

    /**
     * Set the first day of the week that needs to be shown
     * All days of the week are valid
     *
     * @param day Day of the week. e.g. Calendar.MONDAY
     */
    public void setFirstDayOfWeek(final int day) {
        if(day < Calendar.SUNDAY || day > Calendar.SATURDAY) {
            throw new IllegalArgumentException("day must be between " + Calendar.SUNDAY + " and " + Calendar.SATURDAY);
        }

        mFirstDayOfWeek = day;

        if(mFirstValidDay != null) {
            // Fix first day of week in the Calendar
            mFirstValidDay.setFirstDayOfWeek(mFirstDayOfWeek);

            // update calendar
            updateCalendar();
        }
        notifyDataSetChanged();
    }

    /**
     * (Optional) Set the last day of the week that needs to be shown
     * All days of the week are valid
     * If not set, it'll pick the day before the first day of the week
     * Possible implementation: Hide the weekends -> setLastDayOfWeek(Calendar.FRIDAY)
     *
     * @param day Day of the week. e.g. Calendar.SUNDAY
     */
    public void setLastDayOfWeek(final int day) {
        if(day < Calendar.SUNDAY || day > Calendar.SATURDAY) {
            throw new IllegalArgumentException("day must be between " + Calendar.SUNDAY + " and " + Calendar.SATURDAY);
        }

        mLastDayOfWeek = day;
        notifyDataSetChanged();
    }

    /**
     * Set a first valid day. All the days before this day will be disabled.
     *
     * @param firstValidDay The first valid day
     */
    public void setFirstValidDay(final Calendar firstValidDay) {
        // set all the useless attributes to 0
        firstValidDay.set(Calendar.HOUR_OF_DAY, 0);
        firstValidDay.set(Calendar.MINUTE, 0);
        firstValidDay.set(Calendar.SECOND, 0);
        firstValidDay.set(Calendar.MILLISECOND, 0);
        firstValidDay.setFirstDayOfWeek(mFirstDayOfWeek);

        mCurrentMonth = firstValidDay.get(Calendar.MONTH);

        this.mFirstValidDay = firstValidDay;

        updateCalendar();
        notifyDataSetChanged();
    }

    /**
     * (Optional) Set a custom last valid day. If set, all the days after this day will be disabled.
     * If not set, this will be the last day of the month
     *
     * @param lastValidDay The last valid day
     */
    public void setLastValidDay(final Calendar lastValidDay) {
        // set all the useless attributes to 0
        lastValidDay.set(Calendar.HOUR_OF_DAY, 0);
        lastValidDay.set(Calendar.MINUTE, 0);
        lastValidDay.set(Calendar.SECOND, 0);
        lastValidDay.set(Calendar.MILLISECOND, 0);

        this.mLastValidDay = lastValidDay;
        notifyDataSetChanged();
    }

    /**
     * Set a DayAdapter
     * The DayAdapter will be able to change the TextViews of the headers/days
     * and is able to add Category Colors to days
     *
     * @param newAdapter The (new) adapter to be set
     */
    public void setDayAdapter(DayAdapter newAdapter) {
        this.mDayAdapter = newAdapter;
    }

    /**
     * Set a custom Typeface for the days and headers(1-31 and Mon-Sun)
     *
     * @param newTypeFace The new Typeface which will be used, or null for default(Roboto Light)
     */
    public void setTypeface(final Typeface newTypeFace) {
        if(newTypeFace != null) {
            this.mTypeface = newTypeFace;
        } else {
            // newTypeFace is null, reset to default
            setDefaultTypeface();
        }
    }

    protected void setDefaultTypeface() {
        // Load the Roboto light typeface
        final Typeface defaultTypeface = Typeface.createFromAsset(getResources().getAssets(), "roboto_light.ttf");
        setTypeface(defaultTypeface);
    }

    /**
     * Set an onDayClick listener, which will be called when the user clicked on a valid Day
     *
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
        final int daysToWithdraw;
        if(mFirstDayOfWeek > calendarDay)
            daysToWithdraw = (calendarDay + DAYS_IN_WEEK) - mFirstDayOfWeek;
        else
            daysToWithdraw = calendarDay - mFirstDayOfWeek;

        // withdraw that number from the calendar
        calendar.add(Calendar.DAY_OF_WEEK, -daysToWithdraw);

        // set all the useless attributes to 0
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // set calendar
        this.mCalendarFirstDay = calendar;
    }

    protected int getAvailableDayWidth(int width) {
        // Calculate the available width for a single day-item
        final int paddingSides = getResources()
                .getDimensionPixelSize(R.dimen.lib_calendar_day_padding_sides);
        final int screenWidth = width;
        final int daysInRow = getDaysInRow();
        final int availableWidth = screenWidth - (paddingSides * daysInRow * 2); // padding is at both sides( * 2)
        final int widthPerTile = availableWidth / daysInRow;
        final int maxWidthPerTile =
                getResources().getDimensionPixelSize(R.dimen.lib_calendar_day_size);

        // The maximum size of a tile(e.g. a single day)
        // This is either R.dimen.lib_calendar_day_size or the width which fits the screen size
        return Math.min(widthPerTile, maxWidthPerTile);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        // Let our parent(a LinearLayout) measure first
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Check if we're allowed to resize our view's width
        final int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        final int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
        final boolean resizeWidth = widthSpecMode != MeasureSpec.EXACTLY;

        // Variables where we'll store our measured width and height
        int measuredWidth;
        int measuredHeight;

        // Retrieve some initial dimensions of our components
        final int paddingSides = getResources()
                .getDimensionPixelSize(R.dimen.lib_calendar_day_padding_sides);
        final int daysInRow = getDaysInRow();

        final int dayWidth;
        if(resizeWidth) {
            // We may resize our View, so use our preferred size
            dayWidth = getResources().getDimensionPixelSize(R.dimen.lib_calendar_day_size);
        }  else {
            // We're not allowed to resize our View, so make sure it fits
            dayWidth = getAvailableDayWidth(maxWidth);
        }

        // Calculate our width, based on the width of a single day, the padding and the number of days each week(row)
        measuredWidth = (dayWidth * daysInRow) + (paddingSides * daysInRow * 2);

        // Calculate a measured height of the headers by using a sample TextView
        // First, create a TextView with sample text
        final LayoutInflater inflater = (LayoutInflater)
                getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final TextView sampleHeader = (TextView)
                inflater.inflate(R.layout.lib_calendar_single_header, this, false);
        sampleHeader.setText(R.string.lib_header_monday);
        // Second, measure the TextView's height
        int textWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(measuredWidth / daysInRow,
                View.MeasureSpec.AT_MOST);
        int textHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        sampleHeader.measure(textWidthMeasureSpec, textHeightMeasureSpec);
        final int headerHeight = sampleHeader.getMeasuredHeight();

        // Calculate the height of the weeks
        final int weeksInMonth;
        if(this instanceof CalendarView) {
            // If we're a single month, calculate the actual height
            weeksInMonth = mFirstValidDay.getActualMaximum(Calendar.WEEK_OF_MONTH);
        }  else {
            weeksInMonth = MAX_WEEKS_IN_MONTH;
        }
        final int weekHeight = dayWidth * weeksInMonth + (paddingSides * (weeksInMonth + 1) * 2);

        // The height is the height of all the weeks, plus the headers
        measuredHeight = weekHeight + headerHeight;


        // If we're MultiCalendarView, also add the height of the TitlePageIndicator
        if(this instanceof  MultiCalendarView) {
            final MultiCalendarView multiCalendarView = (MultiCalendarView) this;
            final TitlePageIndicator indicator = multiCalendarView.getIndicator();

            // Use the width we measured, and let the Indicator calculate it's height
            int indicatorWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(
                    resolveSize(measuredWidth, widthMeasureSpec), MeasureSpec.EXACTLY);
            int indicatorHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            indicator.measure(indicatorWidthMeasureSpec, indicatorHeightMeasureSpec);
            final int indicatorHeight = indicator.getMeasuredHeight();

            // Now we have the height, update our ViewPager with the new dimensions
            indicatorHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY);
            final ViewPager viewPager = multiCalendarView.getViewPager();
            viewPager.measure(indicatorWidthMeasureSpec, indicatorHeightMeasureSpec);

            // Add the indicator height to the measured height
            measuredHeight += indicatorHeight;
        }

        // Set the measured dimensions
        setMeasuredDimension(resolveSize(measuredWidth, widthMeasureSpec),
                resolveSize(measuredHeight, heightMeasureSpec));
    }

    /**
     * Get a human readable name for this day of the week
     *
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
            initView();
        }
        super.onLayout(changed, l, t, r, b);
    }

    protected abstract void initView();

    public void notifyDataSetChanged() {

    }

    /**
     * Getter methods
     */
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

    /**
     * Get the number of days visible in one row.
     * For example, from monday to friday -> 5
     *
     * @return the number of columns in a row
     */
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

    /**
     * Return the currently selected dayStyle
     * @return the selected day style.
     */
    public int getDayStyle() {
        return mDayStyle;
    }

    /**
     * Returns the currently selected Typeface
     * @return the selected typeface
     */
    public Typeface getTypeface() {
        return mTypeface;
    }

    /**
     * Retrieve the TextView used for the [timeInMillis] date
     *
     * @param dayInMillis The time in milliseconds
     * @return The TextView representing the entered date
     */
    public abstract TextView getTextViewForDate(final long dayInMillis);

}
