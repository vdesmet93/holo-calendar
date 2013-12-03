package com.vdesmet.lib.calendar;

import android.widget.TextView;

public interface DayAdapter {

    /**
     * Retrieves the category colors which will be shown below the TextView
     * This might be useful for showing quick information at the Calendar
     * Int should be a color code, for example: 0xFFFF4444
     *
     * @param dayInMillis The date to retrieve the colors for
     * @return An int array containing the colors to be shown, or null
     */
    public abstract int[] getCategoryColors(long dayInMillis);

    /**
     * Asks the adapter if the day should be enabled.
     * This allows the adapter to disable a specific day, for example, weekdays or holidays
     *
     * @param dayInMillis The date to check whether to enable/disable the view
     * @return A boolean if the day needs to be enabled(and clickable)
     */
    public abstract boolean isDayEnabled(long dayInMillis);

    /**
     * Allows the adapter to customize the TextView.
     * For example, change the font size, text color, etc
     *
     * @param dateTextView The date TextView
     * @param dayInMillis  The date which belongs to the TextView
     */
    public abstract void updateTextView(TextView dateTextView, long dayInMillis);

    /**
     * Allows the adapter to customize the header TextView
     * For example, change the font size, text color, etc
     *
     * @param header    The header TextView
     * @param dayOfWeek The date which belongs to the TextView
     */
    public abstract void updateHeaderTextView(TextView header, int dayOfWeek);
}
