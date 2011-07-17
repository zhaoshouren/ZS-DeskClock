package com.zhaoshouren.android.apps.deskclock.test;

import android.content.Context;
import android.content.res.Configuration;
import android.test.AndroidTestCase;
import android.util.Log;

import com.zhaoshouren.android.apps.deskclock.R;
import com.zhaoshouren.android.apps.deskclock.util.Alarm;

import java.util.Locale;

public class FormattedTimeTest extends AndroidTestCase {
     
    private static Locale[] sLocales = Locale.getAvailableLocales();
    private static Configuration sConfiguration = new Configuration();
    private Context mContext;
    private Alarm mAlarm; 
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getContext();
        mAlarm = new Alarm(mContext); 
        mAlarm.set(0, 30, 22, 28, 5, 2011); // 12:30 PM, Tuesday, June 28, 2011
        mAlarm.normalize(true);
        
        changeLocale(mContext, Locale.ENGLISH);
    }
    
    public void testLocales() {
        for (Locale locale : sLocales) {
            Log.i("Locale-locales", locale.toString());
        }
        
        assertEquals(88, sLocales.length);
    }
    
    public void testFormatStrings() {
        assertEquals("PM", mAlarm.formattedAmPm);
        assertEquals("Tuesday, June 28", mAlarm.formattedDate);
        assertEquals("10:30", mAlarm.formattedTime);
        assertEquals("10:30 PM", mAlarm.formattedTimeAmPm);
        
        assertEquals("Jun", mAlarm.format(mContext.getString(R.string.format_abbrev_month)));
        assertEquals("Jun 28", mAlarm.format(mContext.getString(R.string.format_abbrev_month_day)));
        assertEquals("Jun 28, 2011", mAlarm.format(mContext.getString(R.string.format_abbrev_month_day_year)));
        assertEquals("Jun 2011", mAlarm.format(mContext.getString(R.string.format_abbrev_month_year)));
        assertEquals("Tue, 22:30", mAlarm.format(mContext.getString(R.string.format_abbrev_weekday_hour_minute_24)));
        assertEquals("Tue, 10:30 PM", mAlarm.format(mContext.getString(R.string.format_abbrev_weekday_hour_minute_cap_am_pm)));
        assertEquals("Tue, June 28, 2011", mAlarm.format(mContext.getString(R.string.format_abbrev_weekday_month_day_year)));
        assertEquals("PM", mAlarm.format(mContext.getString(R.string.format_cap_am_pm)));
        assertEquals("Jun 28, 2011, 10:30:00 pm", mAlarm.format(mContext.getString(R.string.format_date_and_time)));
        assertEquals("10:30", mAlarm.format(mContext.getString(R.string.format_hour_minute)));
        assertEquals("22:30", mAlarm.format(mContext.getString(R.string.format_hour_minute_24)));
        assertEquals("10:30 pm", mAlarm.format(mContext.getString(R.string.format_hour_minute_am_pm)));
        assertEquals("10:30 PM", mAlarm.format(mContext.getString(R.string.format_hour_minute_cap_am_pm)));
        assertEquals("10:30:00 pm", mAlarm.format(mContext.getString(R.string.format_hour_minute_second_am_pm)));
        assertEquals("June", mAlarm.format(mContext.getString(R.string.format_month)));
        assertEquals("June 28", mAlarm.format(mContext.getString(R.string.format_month_day)));
        assertEquals("June 28, 2011", mAlarm.format(mContext.getString(R.string.format_month_day_year)));
        assertEquals("June 2011", mAlarm.format(mContext.getString(R.string.format_month_year)));
        assertEquals("6/28/2011", mAlarm.format(mContext.getString(R.string.format_numeric_date)));
        assertEquals("Tuesday, 22:30", mAlarm.format(mContext.getString(R.string.format_weekday_hour_minute_24)));
        assertEquals("Tuesday, 10:30 PM", mAlarm.format(mContext.getString(R.string.format_weekday_hour_minute_cap_am_pm)));
        assertEquals("Tuesday, June 28", mAlarm.format(mContext.getString(R.string.format_weekday_month_day)));
        
        mAlarm.set(0, 30, 6, 8, 5, 2011); // 12:30 PM, Tuesday, June 28, 2011
        mAlarm.normalize(true);
        
        changeLocale(mContext, Locale.CHINESE);
        
        assertEquals("时钟",mContext.getString(R.string.app_label));
        
        assertEquals("Jun", mAlarm.format(mContext.getString(R.string.format_abbrev_month)));
        
        
    }
    
    private void changeLocale(final Context context, final Locale locale) {
        Locale.setDefault(locale);
        sConfiguration.locale = locale;
        context.getResources().updateConfiguration(sConfiguration, context.getResources().getDisplayMetrics());
        
    }

}
