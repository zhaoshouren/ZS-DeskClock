package com.zhaoshouren.android.apps.clock.util;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.Time;

import com.zhaoshouren.android.apps.clock.R;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;

public class Days {
    public static final int EVERY_DAY_SELECTED = 0x7f;
    public static final int NO_DAYS_SELECTED = 0;
    public static final String[] DAY_STRING_VALUES = new String[] {
            // Integer.toString(Time.SUNDAY),
            Integer.toString(Time.MONDAY), Integer.toString(Time.TUESDAY),
            Integer.toString(Time.WEDNESDAY), Integer.toString(Time.THURSDAY),
            Integer.toString(Time.FRIDAY), Integer.toString(Time.SATURDAY),
            Integer.toString(Time.SUNDAY), };
    public static final int[] DAY_VALUES = new int[] {
            // Time.SUNDAY,
            Time.MONDAY, Time.TUESDAY, Time.WEDNESDAY, Time.THURSDAY, Time.FRIDAY, Time.SATURDAY,
            Time.SUNDAY, };
    public static final String[] CALENDAR_DAY_STRING_VALUES = new String[] {
            // Integer.toString(Calendar.SUNDAY),
            Integer.toString(Calendar.MONDAY), Integer.toString(Calendar.TUESDAY),
            Integer.toString(Calendar.WEDNESDAY), Integer.toString(Calendar.THURSDAY),
            Integer.toString(Calendar.FRIDAY), Integer.toString(Calendar.SATURDAY),
            Integer.toString(Calendar.SUNDAY), };
    public static final int[] CALENDAR_DAY_VALUES = new int[] {
            // Calendar.SUNDAY,
            Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY,
            Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY, };

    public static class Keys {
        public static final String SELECTED = "Days.Selected";
    }
    
    /**
     * Bitmask of all repeating days
     * <p>
     * 0 = No days selected<br>
     * 1 = Sunday<br>
     * 2 = Monday<br>
     * 3 = Sunday, Monday<br>
     * 4 = Tuesday<br>
     * 5 = Sunday, Tuesday<br>
     * 6 = Monday, Tuesday<br>
     * 7 = Sunday, Monday, Tuesday<br>
     * 8 = Wednesday<br>
     * ...<br>
     * 16 = Thursday<br>
     * ...<br>
     * 32 = Friday<br>
     * ...<br>
     * 64 = Saturday<br>
     * ...<br>
     * </p>
     */
    public int selected;

    public Days() {
        selected = NO_DAYS_SELECTED;
    }

    public Days(int days) {
        selected = days;
    }

    @Override
    public String toString() {
        return toString(null);
    }

    /**
     * Returns a string representing the selected days
     * 
     * @param context
     *            calling application's context used to retrieve default text
     * @return
     */
    public String toString(final Context context) {
        switch (selected) {
        case NO_DAYS_SELECTED:
            return "";
        case EVERY_DAY_SELECTED:
            if (context != null) {
                return (String) context.getText(R.string.every_day);
            }
        default:
            return TextUtils.join(", ", toStringArray(getCount() > 1));
        }
    }

    public String[] toStringArray(final boolean abbreviated) {
        return toStringArray(this, abbreviated);
    }

    private static String[] toStringArray(final Days days, final boolean abbreviated) {
        // indexes correspond to Calendar day constants
        final DateFormatSymbols dateFormatSymbols = new DateFormatSymbols();
        final String[] weekDays =
                abbreviated ? dateFormatSymbols.getShortWeekdays() : dateFormatSymbols
                        .getWeekdays();

        final ArrayList<String> daysList = new ArrayList<String>();
        for (int index = 0; index < 7; index++) {
            if (days.isSet(DAY_VALUES[index])) {
                daysList.add(weekDays[CALENDAR_DAY_VALUES[index]]);
            }
        }
        return daysList.toArray(new String[0]);
    }

    public static String[] getDays(final boolean abbreviated) {
        return toStringArray(new Days(EVERY_DAY_SELECTED), abbreviated);
    }

    /**
     * Checks if day is selected
     * 
     * @param timeDay
     *            Time constant: Time.MONDAY, Time.TUESDAY, Time.WEDNESDAY ...
     * @return
     */
    public boolean isSet(final int timeDay) {
        return (selected & 1 << timeDay) != 0;
    }

    /**
     * Checks if day is selected
     * 
     * @param calendarDay
     *            Calendar constant: Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY ...
     * @return
     */
    public boolean isCalendarDaySet(final int calendarDay) {
        return isSet(convertToTimeDay(calendarDay));
    }

    /**
     * Select/Deselect a day
     * 
     * @param timeDay
     *            Time constant: Time.MONDAY, Time.TUESDAY, Time.WEDNESDAY ...
     * @param set
     *            true to select; false to deselect
     */
    public void set(final int timeDay, final boolean set) {
        if (set) {
            selected |= 1 << timeDay;
        } else {
            selected &= ~(1 << timeDay);
        }
    }

    /**
     * Select/Deselect a day
     * 
     * @param calendarDay
     *            Calendar constant: Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY ...
     * @param set
     *            true to select; false to deselect
     */
    public void setCalendarDay(final int calendarDay, final boolean set) {
        set(convertToTimeDay(calendarDay), set);
    }

    private static int convertToTimeDay(final int calendarDay) {
        return calendarDay - 1;
    }

    /**
     * Returns an integer representing a bitmask of selected days.
     * 
     * @return integer representing a bitmask of selected days
     */
    public int toInt() {
        return selected;
    }

    /**
     * Returns an boolean array representing the selected days.
     * 
     * @return boolean array representing the selected days
     */
    public boolean[] toBooleanArray() {
        final boolean[] booleanArray = new boolean[7];
        for (int day : DAY_VALUES) {
            booleanArray[day] = isSet(day);
        }
        return booleanArray;
    }

    /**
     * Returns the number of days till next selected day from today
     * 
     * @return number of days till next selected day from today
     */
    public int getDaysTillNextFromToday() {
        final Time time = new Time();
        time.setToNow();
        return getDaysTillNext(time);
    }

    /**
     * Returns the number of days till next selected day from supplied day
     * 
     * @return number of days till next selected day from supplied day
     */
    public int getDaysTillNext(Time from) {
        if (selected == NO_DAYS_SELECTED) {
            return -1;
        }

        // normalize to make sure that weekDay is set correctly
        from.normalize(true);
        int daysTillNext = 0;
        while (daysTillNext < 7) {
            if (isSet((from.weekDay + daysTillNext) % 7)) {
                break;
            } else {
                daysTillNext++;
            }
        }
        return daysTillNext;
    }

    /**
     * Returns the number of days selected
     * 
     * @return number of days selected
     */
    public int getCount() {
        int count = 0;
        for (int days = selected; days > 0; days >>= 1) {
            if ((days & 1) == 1) {
                count++;
            }
        }
        return count;
    }
}
