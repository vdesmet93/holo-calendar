Holo Calendar
=============

A Holo Calendar library for Android. This library is based on This is [+Loranz Yousif](https://plus.google.com/+LoranzYousif "Loranz Yousif")'s design.

![Screenshot](https://raw.github.com/vdesmet93/holo-calendar/master/Calendar-preview.png)

This Calendar library makes it simple to add a full Calendar View in your Android application. The library contains a view to show a single month(CalendarView), and multiple months(MultiCalendarView) in a ViewPager.

All you need to use this library is include it in your build, and add a few lines of code:

**Layout XML file**
``` 
<?xml version="1.0" encoding="utf-8"?>
<com.vdesmet.lib.calendar.MultiCalendarView
        xmlns:android="http://schemas.android.com/apk/res/android"
        android:id="@+id/multi_calendar"
        android:layout_width="350dp"
        android:layout_height="350dp" />

```

**Activity/Fragment code**
```
// Retrieve the CalendarView
MultiCalendarView multiMonth = (MultiCalendarView) findViewById(R.id.multi_calendar);

// Set the first valid day
final Calendar firstValidDay = Calendar.getInstance();
multiMonth.setFirstValidDay(firstValidDay);

// Set the last valid day
final Calendar lastValidDay = Calendar.getInstance();
lastValidDay.add(Calendar.MONTH, 3);
multiMonth.setLastValidDay(lastValidDay);

```

The Calendar also allows for customization:
* A custom Typeface using setTypeFace()
* A custom first and last day of week. For example, from Monday - Friday
* By using the DayAdapter it's also possible to:
  * Disable a specific date. FOr example, sundays, holidays
  * Alter a TextView of both the header and Date
  * Add category colors to a specific day. For example: Show a red color for Work stuff, and a green one for holiday events
* Disable the TitlePageIndicator when using MultiCalendarView


Sample
======
A sample application is available on [Github](https://github.com/vdesmet93/holo-calendar-sample) and available for download on [Google Play](https://play.google.com/store/apps/details?id=com.vdesmet.sample.calendar).   


Special Thanks
==============
* Thanks to Loranz Yousif for designing the Calendar itself. Due to his designs, this library was made. 
