package com.vdesmet.lib.calendar.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;
import com.vdesmet.lib.calendar.R;

public class ThinTextView extends TextView {

    private static int value = 1;
    public ThinTextView(final Context context) {
        this(context, null);
    }

    public ThinTextView(final Context context, final AttributeSet attrs) {
        this(context, null, 0);
    }

    public ThinTextView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);

        Typeface robotoCond = Typeface.createFromAsset(getContext().getAssets(), "roboto_light.ttf");
        setTypeface(robotoCond);

        setTextSize(22);
        setTextColor(getContext().getResources().getColor(R.color.lib_calendar_day_textcolor));


        setText(String.valueOf(value++));
    }




}
