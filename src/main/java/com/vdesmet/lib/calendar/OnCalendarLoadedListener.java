package com.vdesmet.lib.calendar;

public interface OnCalendarLoadedListener {

    /**
     * Called when the calendar's views are done loading
     * @param view The view which is finished
     */
    public abstract void onCalendarLoaded(MultiCalendarView view);
}
