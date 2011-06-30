package com.zhaoshouren.android.apps.deskclock.test;

import android.test.AndroidTestCase;
import android.text.format.Time;

import com.zhaoshouren.android.apps.deskclock.utils.SelectedDays;

public class SelectedDaysTest extends AndroidTestCase {
    private Time mTime;
    private SelectedDays mSelectedDays;
    
    @Override
    protected void setUp() throws Exception {
        mSelectedDays = new SelectedDays();
        mTime = new Time();
        mTime.set(0, 30, 12, 28, 5, 2011); // 12:30 PM, Tuesday June 28, 2011
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testSet() {
        assertEquals(0, mSelectedDays.toInt());
        
        mSelectedDays.set(Time.SUNDAY, true); // Time.SUNDAY = 0      
        assertEquals(1, mSelectedDays.toInt());
        
        mSelectedDays = new SelectedDays();
        mSelectedDays.set(Time.MONDAY, true); // Time.MONDAY = 1 
        assertEquals(2, mSelectedDays.toInt());
        
        mSelectedDays = new SelectedDays();
        mSelectedDays.set(Time.SUNDAY, true); // Time.SUNDAY = 0 
        mSelectedDays.set(Time.MONDAY, true); // Time.MONDAY = 1       
        assertEquals(3, mSelectedDays.toInt());
        
        mSelectedDays = new SelectedDays();
        mSelectedDays.set(Time.TUESDAY, true); // Time.TUESDAY = 2 
        assertEquals(4, mSelectedDays.toInt());
        
        mSelectedDays = new SelectedDays();
        mSelectedDays.set(Time.SUNDAY, true); // Time.SUNDAY = 0 
        mSelectedDays.set(Time.TUESDAY, true); // Time.TUESDAY = 2                 
        assertEquals(5, mSelectedDays.toInt());
        
        mSelectedDays = new SelectedDays();
        mSelectedDays.set(Time.MONDAY, true); // Time.MONDAY = 1
        mSelectedDays.set(Time.TUESDAY, true); // Time.TUESDAY = 2
        assertEquals(6, mSelectedDays.toInt());
        
        mSelectedDays = new SelectedDays();
        mSelectedDays.set(Time.SUNDAY, true); // Time.SUNDAY = 0
        mSelectedDays.set(Time.MONDAY, true); // Time.MONDAY = 1
        mSelectedDays.set(Time.TUESDAY, true); // Time.TUESDAY = 2
        assertEquals(7, mSelectedDays.toInt());
        
        mSelectedDays = new SelectedDays();
        mSelectedDays.set(Time.WEDNESDAY, true);
        assertEquals(8, mSelectedDays.toInt());
        
        mSelectedDays = new SelectedDays();
        mSelectedDays.set(Time.SUNDAY, true); // Time.SUNDAY = 0
        mSelectedDays.set(Time.MONDAY, true); // Time.MONDAY = 1
        mSelectedDays.set(Time.TUESDAY, true); // Time.TUESDAY = 2
        mSelectedDays.set(Time.WEDNESDAY, true); // Time.WEDNESDAY = 3
        mSelectedDays.set(Time.THURSDAY, true); // Time.THURSDAY = 4
        mSelectedDays.set(Time.FRIDAY, true); // Time.FRIDAY = 5
        mSelectedDays.set(Time.SATURDAY, true); // Time.SATURDAY = 6
        assertEquals(127, mSelectedDays.toInt());
    }

    public void testGetDaysTillNext() {     
        // No days selected so getDaysTillNext should return -1
        assertEquals(-1, mSelectedDays.getDaysTillNext(mTime));
        
        mSelectedDays.set(Time.TUESDAY, true);
        
        // same day selected as day of alarm, should return 0
        assertEquals(0, mSelectedDays.getDaysTillNext(mTime));
        
        mSelectedDays.set(Time.TUESDAY, false);
        mSelectedDays.set(Time.WEDNESDAY, true);
        
        // 1 day after selected as day of alarm, should return 1
        assertEquals(1, mSelectedDays.getDaysTillNext(mTime));
        
        mSelectedDays.set(Time.WEDNESDAY, false);
        mSelectedDays.set(Time.THURSDAY, true);
        
        // 2 days after selected as day of alarm, should return 2
        assertEquals(2, mSelectedDays.getDaysTillNext(mTime));
        
        mSelectedDays.set(Time.THURSDAY, false);
        mSelectedDays.set(Time.FRIDAY, true);
        
        // 3 days after selected as day of alarm, should return 3
        assertEquals(3, mSelectedDays.getDaysTillNext(mTime));
        
        mSelectedDays.set(Time.FRIDAY, false);
        mSelectedDays.set(Time.SATURDAY, true);
        
        // 4 days after selected as day of alarm, should return 4
        assertEquals(4, mSelectedDays.getDaysTillNext(mTime));
        
        mSelectedDays.set(Time.SATURDAY, false);
        mSelectedDays.set(Time.SUNDAY, true);
        
        // 5 days after selected as day of alarm, should return 5
        assertEquals(5, mSelectedDays.getDaysTillNext(mTime));
        
        mSelectedDays.set(Time.SUNDAY, false);
        mSelectedDays.set(Time.MONDAY, true);
        
        // 5 days after selected as day of alarm, should return 6
        assertEquals(6, mSelectedDays.getDaysTillNext(mTime));
    }
    
    public void testGetDays() {
        final String[] days = SelectedDays.getDays(false);
        
        assertEquals("Monday", days[0]);
        assertEquals("Tuesday", days[1]);
        assertEquals("Wednesday", days[2]);
        assertEquals("Thursday", days[3]);
        assertEquals("Friday", days[4]);
        assertEquals("Saturday", days[5]);
        assertEquals("Sunday", days[6]);
        
        final String[] abbreviatedDays = SelectedDays.getDays(true);
        
        assertEquals("Mon", abbreviatedDays[0]);
        assertEquals("Tue", abbreviatedDays[1]);
        assertEquals("Wed", abbreviatedDays[2]);
        assertEquals("Thu", abbreviatedDays[3]);
        assertEquals("Fri", abbreviatedDays[4]);
        assertEquals("Sat", abbreviatedDays[5]);
        assertEquals("Sun", abbreviatedDays[6]);
    }
    
    public void testToString() {
        assertEquals("", mSelectedDays.toString());
        
        mSelectedDays.set(Time.MONDAY, true);
        
        assertEquals("Monday", mSelectedDays.toString());
        
        mSelectedDays.set(Time.TUESDAY, true);
        
        assertEquals("Mon, Tue", mSelectedDays.toString());
        
        mSelectedDays = new SelectedDays(SelectedDays.EVERY_DAY_SELECTED);
        
        assertEquals("Mon, Tue, Wed, Thu, Fri, Sat, Sun", mSelectedDays.toString());
        assertEquals("every day", mSelectedDays.toString(getContext()));
    }
}
