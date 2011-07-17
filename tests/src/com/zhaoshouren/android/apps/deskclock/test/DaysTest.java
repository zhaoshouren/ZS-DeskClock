package com.zhaoshouren.android.apps.deskclock.test;

import android.test.AndroidTestCase;
import android.text.format.Time;

import com.zhaoshouren.android.apps.deskclock.util.Days;

public class DaysTest extends AndroidTestCase {
    private Time mTime;
    private Days mDays;
    
    @Override
    protected void setUp() throws Exception {
        mDays = new Days();
        mTime = new Time();
        mTime.set(0, 30, 12, 28, 5, 2011); // 12:30 PM, Tuesday June 28, 2011
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testSet() {
        assertEquals(0, mDays.toInt());
        
        mDays.set(Time.SUNDAY, true); // Time.SUNDAY = 0      
        assertEquals(1, mDays.toInt());
        
        mDays = new Days();
        mDays.set(Time.MONDAY, true); // Time.MONDAY = 1 
        assertEquals(2, mDays.toInt());
        
        mDays = new Days();
        mDays.set(Time.SUNDAY, true); // Time.SUNDAY = 0 
        mDays.set(Time.MONDAY, true); // Time.MONDAY = 1       
        assertEquals(3, mDays.toInt());
        
        mDays = new Days();
        mDays.set(Time.TUESDAY, true); // Time.TUESDAY = 2 
        assertEquals(4, mDays.toInt());
        
        mDays = new Days();
        mDays.set(Time.SUNDAY, true); // Time.SUNDAY = 0 
        mDays.set(Time.TUESDAY, true); // Time.TUESDAY = 2                 
        assertEquals(5, mDays.toInt());
        
        mDays = new Days();
        mDays.set(Time.MONDAY, true); // Time.MONDAY = 1
        mDays.set(Time.TUESDAY, true); // Time.TUESDAY = 2
        assertEquals(6, mDays.toInt());
        
        mDays = new Days();
        mDays.set(Time.SUNDAY, true); // Time.SUNDAY = 0
        mDays.set(Time.MONDAY, true); // Time.MONDAY = 1
        mDays.set(Time.TUESDAY, true); // Time.TUESDAY = 2
        assertEquals(7, mDays.toInt());
        
        mDays = new Days();
        mDays.set(Time.WEDNESDAY, true);
        assertEquals(8, mDays.toInt());
        
        mDays = new Days();
        mDays.set(Time.SUNDAY, true); // Time.SUNDAY = 0
        mDays.set(Time.MONDAY, true); // Time.MONDAY = 1
        mDays.set(Time.TUESDAY, true); // Time.TUESDAY = 2
        mDays.set(Time.WEDNESDAY, true); // Time.WEDNESDAY = 3
        mDays.set(Time.THURSDAY, true); // Time.THURSDAY = 4
        mDays.set(Time.FRIDAY, true); // Time.FRIDAY = 5
        mDays.set(Time.SATURDAY, true); // Time.SATURDAY = 6
        assertEquals(127, mDays.toInt());
    }

    public void testGetDaysTillNext() {     
        // No days selected so getDaysTillNext should return -1
        assertEquals(-1, mDays.getDaysTillNext(mTime));
        
        mDays.set(Time.TUESDAY, true);
        
        // same day selected as day of alarm, should return 0
        assertEquals(0, mDays.getDaysTillNext(mTime));
        
        mDays.set(Time.TUESDAY, false);
        mDays.set(Time.WEDNESDAY, true);
        
        // 1 day after selected as day of alarm, should return 1
        assertEquals(1, mDays.getDaysTillNext(mTime));
        
        mDays.set(Time.WEDNESDAY, false);
        mDays.set(Time.THURSDAY, true);
        
        // 2 days after selected as day of alarm, should return 2
        assertEquals(2, mDays.getDaysTillNext(mTime));
        
        mDays.set(Time.THURSDAY, false);
        mDays.set(Time.FRIDAY, true);
        
        // 3 days after selected as day of alarm, should return 3
        assertEquals(3, mDays.getDaysTillNext(mTime));
        
        mDays.set(Time.FRIDAY, false);
        mDays.set(Time.SATURDAY, true);
        
        // 4 days after selected as day of alarm, should return 4
        assertEquals(4, mDays.getDaysTillNext(mTime));
        
        mDays.set(Time.SATURDAY, false);
        mDays.set(Time.SUNDAY, true);
        
        // 5 days after selected as day of alarm, should return 5
        assertEquals(5, mDays.getDaysTillNext(mTime));
        
        mDays.set(Time.SUNDAY, false);
        mDays.set(Time.MONDAY, true);
        
        // 5 days after selected as day of alarm, should return 6
        assertEquals(6, mDays.getDaysTillNext(mTime));
    }
    
    public void testGetDays() {
        final String[] days = Days.getDays(false);
        
        assertEquals("Monday", days[0]);
        assertEquals("Tuesday", days[1]);
        assertEquals("Wednesday", days[2]);
        assertEquals("Thursday", days[3]);
        assertEquals("Friday", days[4]);
        assertEquals("Saturday", days[5]);
        assertEquals("Sunday", days[6]);
        
        final String[] abbreviatedDays = Days.getDays(true);
        
        assertEquals("Mon", abbreviatedDays[0]);
        assertEquals("Tue", abbreviatedDays[1]);
        assertEquals("Wed", abbreviatedDays[2]);
        assertEquals("Thu", abbreviatedDays[3]);
        assertEquals("Fri", abbreviatedDays[4]);
        assertEquals("Sat", abbreviatedDays[5]);
        assertEquals("Sun", abbreviatedDays[6]);
    }
    
    public void testToString() {
        assertEquals("", mDays.toString());
        
        mDays.set(Time.MONDAY, true);
        
        assertEquals("Monday", mDays.toString());
        
        mDays.set(Time.TUESDAY, true);
        
        assertEquals("Mon, Tue", mDays.toString());
        
        mDays = new Days(Days.EVERY_DAY_SELECTED);
        
        assertEquals("Mon, Tue, Wed, Thu, Fri, Sat, Sun", mDays.toString());
        assertEquals("every day", mDays.toString(getContext()));
    }
}
