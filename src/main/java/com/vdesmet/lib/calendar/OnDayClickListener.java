package com.vdesmet.lib.calendar;

public interface OnDayClickListener {

    /**
     * Called when the user has clicked on a day
     *
     * @param dayInMillis The date in milliseconds the user has clicked
     */
    public abstract void onDayClick(long dayInMillis);
}
