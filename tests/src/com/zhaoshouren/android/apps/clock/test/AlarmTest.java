package com.zhaoshouren.android.apps.clock.test;

import android.app.AlarmManager;
import android.content.Context;
import android.test.AndroidTestCase;
import android.text.format.Time;

import com.zhaoshouren.android.apps.clock.R;
import com.zhaoshouren.android.apps.clock.util.Alarm;

public class AlarmTest extends AndroidTestCase {
    
    private static Context sContext;
    private static Alarm sAlarm;
    private static Time sTime;
    private static AlarmManager sAlarmManager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        sContext = getContext();
        sAlarm = new Alarm(sContext);
        sAlarm.set(0, 30, 22, 28, 5, 2011); // 10:30 PM, Tuesday, June 28, 2011
        sAlarm.normalize(true);
        
        sTime = new Time();
        sTime.set(0, 30, 22, 28, 5, 2011);
        sTime.normalize(true);
        
        sAlarmManager = (AlarmManager) sContext.getSystemService(Context.ALARM_SERVICE);
        sAlarmManager.setTime(sTime.toMillis(false));
        
        sTime.setToNow();
    }
    
    public void testFormatStrings() {        
        assertEquals("PM", sAlarm.formattedAmPm);
        assertEquals("Tuesday, June 28", sAlarm.formattedDate);
        assertEquals("10:30", sAlarm.formattedTime);
        assertEquals("10:30 PM", sAlarm.formattedTimeAmPm);

        assertEquals("Jun", sAlarm.format(sContext.getString(R.string.format_abbrev_month)));
        assertEquals("Jun 28", sAlarm.format(sContext.getString(R.string.format_abbrev_month_day)));
        assertEquals("Jun 28, 2011",
                sAlarm.format(sContext.getString(R.string.format_abbrev_month_day_year)));
        assertEquals("Jun 2011",
                sAlarm.format(sContext.getString(R.string.format_abbrev_month_year)));
        assertEquals("Tue, 22:30",
                sAlarm.format(sContext.getString(R.string.format_abbrev_weekday_hour_minute_24)));
        assertEquals("Tue, 10:30 PM", sAlarm.format(sContext
                .getString(R.string.format_abbrev_weekday_hour_minute_cap_am_pm)));
        assertEquals("Tue, June 28, 2011",
                sAlarm.format(sContext.getString(R.string.format_abbrev_weekday_month_day_year)));
        assertEquals("PM", sAlarm.format(sContext.getString(R.string.format_cap_am_pm)));
        assertEquals("Jun 28, 2011, 10:30:00 pm",
                sAlarm.format(sContext.getString(R.string.format_date_and_time)));
        assertEquals("10:30", sAlarm.format(sContext.getString(R.string.format_hour_minute)));
        assertEquals("22:30", sAlarm.format(sContext.getString(R.string.format_hour_minute_24)));
        assertEquals("10:30 pm",
                sAlarm.format(sContext.getString(R.string.format_hour_minute_am_pm)));
        assertEquals("10:30 PM",
                sAlarm.format(sContext.getString(R.string.format_hour_minute_cap_am_pm)));
        assertEquals("10:30:00 pm",
                sAlarm.format(sContext.getString(R.string.format_hour_minute_second_am_pm)));
        assertEquals("June", sAlarm.format(sContext.getString(R.string.format_month)));
        assertEquals("June 28", sAlarm.format(sContext.getString(R.string.format_month_day)));
        assertEquals("June 28, 2011",
                sAlarm.format(sContext.getString(R.string.format_month_day_year)));
        assertEquals("June 2011", sAlarm.format(sContext.getString(R.string.format_month_year)));
        assertEquals("6/28/2011", sAlarm.format(sContext.getString(R.string.format_numeric_date)));
        assertEquals("Tuesday, 22:30",
                sAlarm.format(sContext.getString(R.string.format_weekday_hour_minute_24)));
        assertEquals("Tuesday, 10:30 PM",
                sAlarm.format(sContext.getString(R.string.format_weekday_hour_minute_cap_am_pm)));
        assertEquals("Tuesday, June 28",
                sAlarm.format(sContext.getString(R.string.format_weekday_month_day)));
    }
    
    public void testToggle() {
        //by default new alarms are set to enabled
        assertTrue(sAlarm.enabled);
        
        sAlarm.toggle();
        
        assertFalse(sAlarm.enabled);
        
        sAlarm.toggle();
        
        assertTrue(sAlarm.enabled);
    }
    
    public void testGetDaysTillNextFrom() {
        sAlarm.setToNow();
        sTime.setToNow();

        sAlarm.days.set(sAlarm.weekDay, true);
        
        assertEquals(7, sAlarm.getDaysTillNext());
        
        sAlarm.hour += 1;
        sAlarm.normalize(true);
        
        assertTrue(sAlarm.after(sTime));
        
        assertEquals(0, sAlarm.getDaysTillNext());
        
        sAlarm.monthDay -= 1;
        sAlarm.normalize(true);
        
        assertEquals(1, sAlarm.getDaysTillNext());
    }
    
    public void testUpdateScheduledTime() {
        sAlarm.updateScheduledTime();
    }
}
