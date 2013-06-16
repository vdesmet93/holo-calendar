package com.vdesmet.lib.calendar;

import android.widget.TextView;

public interface DayAdapter {


    public abstract int[] getCategoryColors(long dayInMillis);

    public abstract boolean isDayEnabled(long dayInMillis);
    public abstract void updateTextView(TextView defaultView, long dayInMillis);
    public abstract void updateHeaderTextView(TextView header, int dayOfWeek);

}
