package com.zhaoshouren.android.apps.deskclock.utils;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.Time;

import com.zhaoshouren.android.apps.deskclock.R;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;

public class SelectedDays {
    public static final int EVERY_DAY_SELECTED = 0x7f;
    public static final int NO_DAYS_SELECTED = 0;
    public static final String[] DAY_STRING_VALUES = new String[] {
        //Integer.toString(Time.SUNDAY),
        Integer.toString(Time.MONDAY),
        Integer.toString(Time.TUESDAY),
        Integer.toString(Time.WEDNESDAY),
        Integer.toString(Time.THURSDAY),
        Integer.toString(Time.FRIDAY),
        Integer.toString(Time.SATURDAY),
        Integer.toString(Time.SUNDAY),
        };
    public static final int[] DAY_VALUES = new int[] {
        //Time.SUNDAY,
        Time.MONDAY,
        Time.TUESDAY,
        Time.WEDNESDAY,
        Time.THURSDAY,
        Time.FRIDAY,
        Time.SATURDAY,
        Time.SUNDAY,
        };
    public static final String[] CALENDAR_DAY_STRING_VALUES = new String[] {
        //Integer.toString(Calendar.SUNDAY),
        Integer.toString(Calendar.MONDAY),
        Integer.toString(Calendar.TUESDAY),
        Integer.toString(Calendar.WEDNESDAY),
        Integer.toString(Calendar.THURSDAY),
        Integer.toString(Calendar.FRIDAY),
        Integer.toString(Calendar.SATURDAY),
        Integer.toString(Calendar.SUNDAY),
        };
    public static final int[] CALENDAR_DAY_VALUES = new int[] {
        //Calendar.SUNDAY,
        Calendar.MONDAY,
        Calendar.TUESDAY,
        Calendar.WEDNESDAY,
        Calendar.THURSDAY,
        Calendar.FRIDAY,
        Calendar.SATURDAY,
        Calendar.SUNDAY,
        };

    /**
     * Bitmask of all repeating days
     *  0 = No days selected
     *  1 = Sunday
     *  2 = Monday
     *  3 = Sunday, Monday
     *  4 = Tuesday
     *  5 = Sunday, Tuesday
     *  6 = Monday, Tuesday
     *  7 = Sunday, Monday, Tuesday
     *  8 = Wednesday
     *  ...
     *  16 = Thursday
     *  ...
     *  32 = Friday
     *  ...
     *  64 = Saturday
     *  ...
     */
    private int mSelectedDays;

    public SelectedDays() {
        mSelectedDays = NO_DAYS_SELECTED;
    }
    
    public SelectedDays(int selectedDays) {
        mSelectedDays = selectedDays;
    }

    public SelectedDays(final SelectedDays selectedDays) {
        mSelectedDays = selectedDays.mSelectedDays;
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
     * @param showNever
     *            return "never" or empty string when no days are selected
     * @return
     */
    public String toString(final Context context) {
        switch (mSelectedDays) {
        case NO_DAYS_SELECTED:
            return "";
        case EVERY_DAY_SELECTED:
            if (context != null) {
                return context.getText(R.string.every_day).toString();
            }
        default:
            return TextUtils.join(", ", toStringArray(getCount() > 1));
        }
    }

    public String[] toStringArray(final boolean abbreviated) {
        return toStringArray(this, abbreviated);
    }

    private static String[]
            toStringArray(final SelectedDays selectedDays, final boolean abbreviated) {
        // indexes correspond to Calendar day constants
        final DateFormatSymbols dateFormatSymbols = new DateFormatSymbols();
        final String[] days =
                abbreviated ? dateFormatSymbols.getShortWeekdays() : dateFormatSymbols
                        .getWeekdays();

        final ArrayList<String> selectedDaysList = new ArrayList<String>();
        for (int index = 0; index < 7; index++) {
            if (selectedDays.isSet(DAY_VALUES[index])) {
                selectedDaysList.add(days[CALENDAR_DAY_VALUES[index]]);
            }
        }
        return selectedDaysList.toArray(new String[0]);
    }

    public static String[] getDays(final boolean abbreviated) {
        return toStringArray(new SelectedDays(EVERY_DAY_SELECTED), abbreviated);
    }

    /**
     * Checks if day is selected
     * 
     * @param timeDay
     *            Time constant: Time.MONDAY, Time.TUESDAY, Time.WEDNESDAY ...
     * @return
     */
    public boolean isSet(final int timeDay) {
        return (mSelectedDays & 1 << timeDay) != 0;
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
            mSelectedDays |= 1 << timeDay;
        } else {
            mSelectedDays &= ~(1 << timeDay);
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
        return mSelectedDays;
    }

    /**
     * Returns an boolean array representing the selected days.
     * 
     * @return boolean array representing the selected days
     */
    public boolean[] toBooleanArray() {
        final boolean[] booleanArray = new boolean[7];
        for (int i = 0; i < 7; i++) {
            booleanArray[i] = isSet(DAY_VALUES[i]);
        }
        return booleanArray;
    }

    /**
     * Returns true if alarm is a repeating alarm, false if it's a one time alarm
     * 
     * @return true days are selected, false if no days are selected
     */
    public boolean isRepeating() {
        return mSelectedDays > NO_DAYS_SELECTED;
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
        if (!isRepeating()) {
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
        int selectedDaysCount = 0;
        for (int selectedDays = mSelectedDays; selectedDays > 0; selectedDays >>= 1) {
            if ((selectedDays & 1) == 1) {
                selectedDaysCount++;
            }
        }
        return selectedDaysCount;
    }
}
