package com.zhaoshouren.android.apps.deskclock.utils;

import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.Time;

public class FormattedTime extends Time {
    private static final String FORMAT_DAY_MONTH_DAY_OF_MONTH = "%A, %B %d";
    private static final String FORMAT_AM_PM = "%p";
    private static final String FORMAT_12HR = "%l:%M";
    private static final String FORMAT_24HR = "%k:%M";
    private static final String FORMAT_DAY = "%A";
    private static final String FORMAT_ABBREVIATED_DAY = "%a";
    
    public String formattedTime;
    public String formattedAmPm;
    public String formattedTimeAmPm;
    public String formattedDate;
    public String formattedDayTime;
    public String formattedAbbreviatedDayTime;
       
    protected Context mContext;
    
    public FormattedTime(final Context context) {
        super();
        mContext = context;
        setFormatted();
    }
    
    public void setFormatted() {
        final boolean is24HourFormat = (mContext != null) ? DateFormat.is24HourFormat(mContext.getApplicationContext()) : false;
        formattedTime = format(is24HourFormat ? FORMAT_24HR : FORMAT_12HR).trim();
        formattedAmPm = is24HourFormat ? "" : format(FORMAT_AM_PM).toUpperCase();
        formattedTimeAmPm = formattedTime + (is24HourFormat ? "" : " " + format(FORMAT_AM_PM));
        formattedDate = format(FORMAT_DAY_MONTH_DAY_OF_MONTH);
        formattedDayTime = format(FORMAT_DAY) + " – " + formattedTime + (is24HourFormat ? "" : " " + format(FORMAT_AM_PM));   
        formattedAbbreviatedDayTime = format(FORMAT_ABBREVIATED_DAY) + " – " + formattedTime + (is24HourFormat ? "" : " " + format(FORMAT_AM_PM));
    }
    
    @Override
    public long normalize(boolean ignoreDst) {
        long timeInMillies = super.normalize(ignoreDst);
        setFormatted();
        return timeInMillies;
    }
    
    @Override
    public void set(long millis) {
        super.set(millis);
        setFormatted();
    }
    
    @Override
    public void set(Time that) {
        super.set(that);
        setFormatted();
    }
    
    @Override
    public void set(int monthDay, int month, int year) {
        super.set(monthDay, month, year);
        setFormatted();
    }
    
    @Override
    public void set(int second, int minute, int hour, int monthDay, int month, int year) {
        super.set(second, minute, hour, monthDay, month, year);
        setFormatted();
    }
    
    @Override
    public long setJulianDay(int julianDay) {
        long timeInMillies = super.setJulianDay(julianDay);
        setFormatted();
        return timeInMillies;
    }
    
    @Override
    public void setToNow() {
        super.setToNow();
        setFormatted();
    }
    
    @Override
    public String toString() {
        return format(FORMAT_12HR + " " + FORMAT_AM_PM + " -- " + FORMAT_DAY_MONTH_DAY_OF_MONTH + " " + year);
    }
}