package com.zhaoshouren.android.apps.clock.util;

import android.content.Context;
import android.widget.Toast;

import com.zhaoshouren.android.apps.clock.R;

public final class Toaster {
    private static Toast sToast = null;

    /**
     * 
     */
    public static void cancelToast() {
        if (sToast != null) {
            sToast.cancel();
        }

        sToast = null;
    }

    /**
     * format "Alarm set for 2 days 7 hours and 53 minutes from now"
     * 
     * @param context
     * @param timeInMillis
     * @return
     */
    private static String formatToast(final Context context, final long timeInMillis) {
        final long delta = timeInMillis - System.currentTimeMillis();
        long hours = delta / (1000 * 60 * 60);
        final long minutes = delta / (1000 * 60) % 60;
        final long days = hours / 24;
        hours = hours % 24;

        final String daySeq =
                days == 0 ? "" : days == 1 ? context.getString(R.string.day) : context.getString(
                        R.string.days, Long.toString(days));

        final String minSeq =
                minutes == 0 ? "" : minutes == 1 ? context.getString(R.string.minute) : context
                        .getString(R.string.minutes, Long.toString(minutes));

        final String hourSeq =
                hours == 0 ? "" : hours == 1 ? context.getString(R.string.hour) : context
                        .getString(R.string.hours, Long.toString(hours));

        final boolean dispDays = days > 0;
        final boolean dispHour = hours > 0;
        final boolean dispMinute = minutes > 0;

        final int index = (dispDays ? 1 : 0) | (dispHour ? 2 : 0) | (dispMinute ? 4 : 0);

        final String[] formats = context.getResources().getStringArray(R.array.alarm_set);
        return String.format(formats[index], daySeq, hourSeq, minSeq);
    }

    /**
     * Display a toast that tells the user how long until the alarm goes off. This helps prevent
     * "am/pm" mistakes.
     */
    public static void popAlarmToast(final Context context, final Alarm alarm) {
        final Toast toast =
                Toast.makeText(context, formatToast(context, alarm.toMillis(true)),
                        Toast.LENGTH_LONG);
        setToast(toast);
        toast.show();
    }

    /**
     * 
     * @param toast
     */
    private static void setToast(final Toast toast) {
        if (sToast != null) {
            sToast.cancel();
        }

        sToast = toast;
    }
}
