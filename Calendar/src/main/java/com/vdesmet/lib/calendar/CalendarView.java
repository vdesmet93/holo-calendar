package com.vdesmet.lib.calendar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.vdesmet.lib.calendar.factory.DayStyleFactory;

import java.util.Calendar;

public class CalendarView extends AbstractCalendarView implements View.OnClickListener {

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

        // Set the default Typeface if none was set previously
        if(mTypeface == null) {
            setDefaultTypeface();
        }

        // Update day width if we have usable values
        final ViewTreeObserver observer = getViewTreeObserver();
        if(observer != null) {
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    updateDayWidth();

                    // Remove the OnGlobalLayoutListener
                    final ViewTreeObserver observer = getViewTreeObserver();
                    if(observer != null) {
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            observer.removeOnGlobalLayoutListener(this);
                        } else {
                            //noinspection deprecation
                            observer.removeGlobalOnLayoutListener(this);
                        }
                    }
                }
            });
        }
    }

    /**
     * Will be called by the TextView
     * This method will call the adapter's onDayClick method
     */
    @Override
    public void onClick(final View v) {
        // user clicked on a TextView
        if(v != null) {
            final long timeInMillis = Long.parseLong(v.getTag().toString());
            if(mOnDayClickListener != null) {
                mOnDayClickListener.onDayClick(timeInMillis);
            }
        }
    }

    /**
     * Create the view
     * Initializes the headers and views for all visible days
     */
    @Override
    protected void initView() {
        // Set the background color
        final int backgroundColor = DayStyleFactory.getBackgroundResourceForStyle(mDayStyle);
        setBackgroundResource(backgroundColor);

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
        final int dayStyle = mDayStyle;
        final int dayDisabledBackgroundColor = DayStyleFactory.getDayDisabledBackgroundColor(dayStyle, getResources());
        final int dayDisabledTextColor = getResources().getColor(R.color.lib_calendar_day_textcolor_disabled);
        final Typeface typeface = mTypeface;

        ViewGroup weekLayout = (ViewGroup) inflater.inflate(R.layout.lib_calendar_week, this, false);

        /* Continue adding days while:
         *  # We're adding the last few days of the previous month          (modulo to work in january)
         *  # We're adding the days in the current month
         *  # We're adding the first few days of the next month             (Add until we're at the end of the week)
         */
        while((currentDay.get(Calendar.MONTH) + 1) % MONTHS_IN_YEAR == currentMonth ||
                currentDay.get(Calendar.MONTH) == currentMonth ||
                currentDay.get(Calendar.DAY_OF_WEEK) != lastDayOfWeek + 1) {

            // check if we need to add this day, if not, move to the next
            final int dayOfWeek = currentDay.get(Calendar.DAY_OF_WEEK);
            boolean moveToNext = false;
            if(lastDayOfWeek < firstDayOfWeek) {
                if((dayOfWeek < firstDayOfWeek && dayOfWeek > lastDayOfWeek) ||
                        (dayOfWeek > lastDayOfWeek && dayOfWeek < firstDayOfWeek)) {

                    // we don't want to have this dayOfWeek in our calendar, so move to the next one
                    moveToNext = true;
                }
            } else if(dayOfWeek < firstDayOfWeek || dayOfWeek > lastDayOfWeek) {
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
            final ViewGroup layout = DayStyleFactory.getDayLayoutForStyle(inflater, this, dayStyle);
            final TextView dayTextView = (TextView) layout.findViewById(R.id.lib_calendar_day_text);
            final ViewGroup categories = (ViewGroup) layout.findViewById(R.id.lib_calendar_day_categories);

            // if set, use the custom Typeface
            if(typeface != null) {
                dayTextView.setTypeface(typeface);
            }

            // set the current day: 1-31
            final int dayOfMonth = currentDay.get(Calendar.DAY_OF_MONTH);
            dayTextView.setText(String.valueOf(dayOfMonth));

            /* We need to disable the view when:
             *  # The adapter says it should be disabled
             *  # This day is in another month              (We fill the rows at the begin/end of the month)
             *  # This day is before the first valid day
             *  # This day is after the last valid day
             */
            if((adapter != null && !adapter.isDayEnabled(timeInMillis)) ||
                    (currentDay.get(Calendar.MONTH) != currentMonth) ||
                    (firstValidDay != null && currentDay.before(firstValidDay)) ||
                    (lastValidDay != null && currentDay.after(lastValidDay))) {

                // change the appearance if it's disabled
                layout.setBackgroundColor(dayDisabledBackgroundColor);
                dayTextView.setTextColor(dayDisabledTextColor);
                // disable the views
                dayTextView.setEnabled(false);
                layout.setEnabled(false);
            } else {
                // allow the adapter to update the TextView
                // e.g. change font size or color based on the date
                if(adapter != null) {
                    adapter.updateTextView(dayTextView, timeInMillis);

                    // create a new view for each category
                    final int[] colors = adapter.getCategoryColors(timeInMillis);
                    if(colors != null) {
                        for(final int color : colors) {
                            // inflate a new category
                            final View category = inflater.inflate(R.layout.lib_calendar_category, categories, false);

                            // set the background color to the color provided by the adapter
                            category.setBackgroundColor(color);

                            // add the view to the ViewGroup. Note that we can't do this while inflating,
                            // because that will cause the view to match the size of his parent
                            categories.addView(category);
                        }
                    }
                }
            }

            // set tag to the timeInMillis for the onClickListener and to be able to retrieve the TextView later on
            layout.setTag(timeInMillis);
            dayTextView.setTag(timeInMillis);
            dayTextView.setOnClickListener(this);

            // add layout to view
            weekLayout.addView(layout);

            if(dayOfWeek == lastDayOfWeek) {
                // this is the last day in the week/row, add a new row
                addView(weekLayout);

                weekLayout = (ViewGroup) inflater.inflate(R.layout.lib_calendar_week, this, false);
            }

            // add 1 day
            currentDay.add(Calendar.DAY_OF_WEEK, 1);
        }

        // Make sure the weekLayout is added to the layout
        if(weekLayout.getParent() == null) {
            addView(weekLayout);
        }

        // Update the day widths
        updateDayWidth();

        // Finished initializing
        mIsViewInitialized = true;
    }

    @Override
    public TextView getTextViewForDate(final long dayInMillis) {
        // Loop through all children
        final int childCount = getChildCount();
        for(int i = 0; i < childCount; i++) {
            final View weekLayout = getChildAt(i);
            if(weekLayout != null) {
                // Let the weekLayout find a view with a correct tag
                final View dayLayout = weekLayout.findViewWithTag(dayInMillis);
                if(dayLayout != null) {
                    // Find the TextView, and return it
                    return (TextView) dayLayout.findViewById(R.id.lib_calendar_day_text);
                }
            }
        }
        // No suitable TextView found, return null
        return null;
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
        final Typeface typeface = mTypeface;

        // inflate the ViewGroup where we'll put all the headers
        final ViewGroup headers = (ViewGroup)
                inflater.inflate(R.layout.lib_calendar_headers, this, false);
        int dayOfWeek = firstDayOfWeek;

        do {
            // initialize variables for this day
            final TextView header = (TextView)
                    inflater.inflate(R.layout.lib_calendar_single_header, headers, false);
            final String nameOfDay = getNameForDay(dayOfWeek, resources);

            // if set, use the custom Typeface
            if(typeface != null) {
                header.setTypeface(typeface);
            }

            // allow adapter to update the TextView
            // e.g. change font, appearance, add click listener on all/some days
            if(adapter != null) {
                adapter.updateHeaderTextView(header, dayOfWeek);
            }

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

    private int getAvailableDayWidth() {
        return getAvailableDayWidth(getWidth());
    }

    private void updateDayWidth() {
        final int dayWidth = getAvailableDayWidth();
        final int childCount = getChildCount();

        // Make sure the available day width is valid
        if(dayWidth > 0) {
            for(int i = 0; i < childCount; i++) {
                final View child = getChildAt(i);
                if(child instanceof ViewGroup) {
                    final ViewGroup childViewGroup = (ViewGroup) child;
                    final int childItemCount = childViewGroup.getChildCount();

                    for(int index = 0; index < childItemCount; index++) {
                        final View dayView = childViewGroup.getChildAt(index);
                        if(dayView != null) {
                            final ViewGroup.LayoutParams params =
                                    dayView.getLayoutParams();

                            if(i == 0) {
                                // This is the dayOfWeek TextView(header), so we use wrap_content on the height
                                params.width = dayWidth;
                            } else {
                                // This is the layout for a single day which is a square
                                params.width = dayWidth;
                                params.height = dayWidth;
                            }
                            // Set the new LayoutParams
                            dayView.setLayoutParams(params);
                        }
                    }
                }
            }
            this.setVisibility(View.VISIBLE);
        } else {
            // We don't have a width available yet
            this.setVisibility(View.INVISIBLE);
        }
    }
}
