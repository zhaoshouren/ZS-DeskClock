package com.zhaoshouren.android.apps.clock.util;

import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.Time;

import com.zhaoshouren.android.apps.clock.R;

public class FormattedTime extends Time {
    
    public static class Format {
        public static final int ABBREV_WEEKDAY_HOUR_MINUTE_24 = R.string.format_abbrev_weekday_hour_minute_24;
        public static final int ABBREV_WEEKDAY_HOUR_MINUTE_CAP_AM_PM = R.string.format_abbrev_weekday_hour_minute_cap_am_pm; 
        public static final int WEEKDAY_HOUR_MINUTE_24 = R.string.format_weekday_hour_minute_24;
        public static final int WEEKDAY_HOUR_MINUTE_CAP_AM_PM = R.string.format_weekday_hour_minute_cap_am_pm;
    }
    
    private static final int FORMAT_CAP_AM_PM = R.string.format_cap_am_pm;
    private static final int FORMAT_HOUR_MINUTE = R.string.format_hour_minute;
    private static final int FORMAT_HOUR_MINUTE_24 = R.string.format_hour_minute_24;
    private static final int FORMAT_HOUR_MINUTE_CAP_AM_PM = R.string.format_hour_minute_cap_am_pm;
    private static final int FORMAT_WEEKDAY_MONTH_DAY = R.string.format_weekday_month_day;
    
    private boolean isFormatSet = false;
    public boolean is24HourFormat;
    protected static String sFormatCapAmPm;
    protected static String sFormatHourMinute;
    protected static String sFormatHourMinute24;
    protected static String sFormatHourMinuteCapAmPm;
    protected static String sFormatWeekdayMonthDay;
    
    public String formattedTime;
    public String formattedAmPm;
    public String formattedTimeAmPm;
    public String formattedDate;
    
    protected FormattedTime() {
        super();
    }      
       
    public FormattedTime(final Context context) {
        super();
        setFormatted(context);
    }    
    
    public void setFormatted(Context context) {
        if (context != null ) {
            isFormatSet = true;
            is24HourFormat = DateFormat.is24HourFormat(context);
            sFormatCapAmPm = context.getString(FORMAT_CAP_AM_PM);
            sFormatHourMinute = context.getString(FORMAT_HOUR_MINUTE);
            sFormatHourMinute24 = context.getString(FORMAT_HOUR_MINUTE_24);
            sFormatHourMinuteCapAmPm = context.getString(FORMAT_HOUR_MINUTE_CAP_AM_PM);
            sFormatWeekdayMonthDay = context.getString(FORMAT_WEEKDAY_MONTH_DAY);
            
            setFormatted();
        } else {
            //TODO Log Error;
        }
    }

    protected void setFormatted() {
        if (isFormatSet) {
            formattedTime = format(is24HourFormat ? sFormatHourMinute24 : sFormatHourMinute);
            formattedAmPm = is24HourFormat ? "" : format(sFormatCapAmPm);
            formattedTimeAmPm = format(is24HourFormat ? sFormatHourMinute24 : sFormatHourMinuteCapAmPm);
            formattedDate = format(sFormatWeekdayMonthDay);
        } else {
            //TODO Log Error;
        }
    }
    
    @Override
    public long normalize(boolean ignoreDst) {
        long timeInMillies = super.normalize(ignoreDst);
        setFormatted();
        return timeInMillies;
    }
}