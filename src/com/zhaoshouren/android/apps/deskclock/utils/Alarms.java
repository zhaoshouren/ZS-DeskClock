/*
 * Copyright (C) 2011 Warren Chu
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zhaoshouren.android.apps.deskclock.utils;

import static com.zhaoshouren.android.apps.deskclock.DeskClock.DEVELOPER_MODE;
import static com.zhaoshouren.android.apps.deskclock.DeskClock.FORMAT_DATE_TIME;
import static com.zhaoshouren.android.apps.deskclock.DeskClock.TAG;
import static com.zhaoshouren.android.apps.deskclock.utils.Alarm.INVALID_ID;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.provider.BaseColumns;
import android.provider.Settings;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.zhaoshouren.android.apps.deskclock.R;

import java.util.Arrays;

public final class Alarms {

    
    private static final int INVALID_TIME = -1;
    /**
     * This action triggers the AlarmBroadcastReceiver as well as the AlarmPlayerService. It is a
     * public action used in the manifest for receiving Alarm broadcasts from the alarm manager.
     */
    public static final String ACTION_ALARM_ALERT = "com.zhaoshouren.android.apps.deskclock.ALARM_ALERT";
    /**
     * A public action sent by AlarmPlayerService when the alarm has stopped sounding for any reason
     * (e.g. because it has been dismissed from AlarmAlertFullScreen, or killed due to an incoming
     * phone call, etc).
     */
    public static final String ACTION_ALARM_DONE = "com.zhaoshouren.android.apps.deskclock.ALARM_DONE";
    /**
     * AlarmAlertFullScreen listens for this broadcast intent, so that other applications can snooze
     * the alarm (after ALARM_ALERT_ACTION and before ALARM_DONE_ACTION).
     */
    public static final String ACTION_ALARM_SNOOZE =
            "com.zhaoshouren.android.apps.deskclock.ALARM_SNOOZE";
    /**
     * AlarmAlertFullScreen listens for this broadcast intent, so that other applications can
     * dismiss the alarm (after ALARM_ALERT_ACTION and before ALARM_DONE_ACTION).
     */
    public static final String ACTION_ALARM_DISMISS =
            "com.zhaoshouren.android.apps.deskclock.ALARM_DISMISS";
    /**
     * This is an action used by the AlarmPlayerService to update the UI to show the alarm has been
     * killed.
     */
    public static final String ACTION_ALARM_KILLED = "ACTION_ALARM_KILLED";
    /**
     * Extra in the ACTION_ALARM_KILLED intent to indicate to the user how long the alarm played
     * before being killed.
     */
    public static final String KEY_ALARM_KILLED_TIMEOUT = "alarm_killed_timeout";
    /**
     * This intent is sent from the notification when the user cancels the snooze alert.
     */
    public static final String CANCEL_SNOOZE = "cancel_snooze";
    /**
     * value = true
     */
    public static final boolean ALARM_ENABLED = true;
    /**
     * 
     */
    public static final boolean ALARM_DISABLED = false;
    /**
     * 
     */
    static final String KEY_SNOOZE_ID = "snooze_id";
    /**
     * 
     */
    static final String KEY_SNOOZE_TIME = "snooze_time";

    private static final int DOES_NOT_EXIST = INVALID_TIME;
    public static final String PREFERENCES_DESKCLOCK = "com.zhaoshouren.android.apps.deskclock";
    
    public static final class Columns implements BaseColumns {
        // Column names
        // _ID implemented in BaseColumns
        /**
         * Alarm time in UTC milliseconds from the epoch.
         * <P>
         * Type: INTEGER
         * </P>
         */
        public static final String TIME = "time";
        /**
         * Selected days bitmask as integer
         * <P>
         * Type: INTEGER
         * </P>
         */
        public static final String SELECTED_DAYS = "selected_days";
        /**
         * True if alarm is active
         * <P>
         * Type: BOOLEAN
         * </P>
         */
        public static final String ENABLED = "enabled";
        /**
         * True if alarm should vibrate
         * <P>
         * Type: BOOLEAN
         * </P>
         */
        public static final String VIBRATE = "vibrate";
        /**
         * Message to show when alarm triggers Note: not currently used
         * <P>
         * Type: STRING
         * </P>
         */
        public static final String MESSAGE = "message";
        /**
         * Audio alert to play when alarm triggers
         * <P>
         * Type: STRING
         * </P>
         */
        public static final String RINGTONE_URI = "ringtone_uri";
        /**
         * Calculated sort value
         */
        public static final String SORT = "sort";

        // INDEX_[Column] values need to match corresponding order of columns in SELECT
        public static final int INDEX_ID = 0;
        public static final int INDEX_TIME = 1;
        public static final int INDEX_SELECTED_DAYS = 2;
        public static final int INDEX_ENABLED = 3;
        public static final int INDEX_VIBRATE = 4;
        public static final int INDEX_LABEL = 5;
        public static final int INDEX_RINGTONE_URI = 6;
        public static final int INDEX_SORT = 7;
    }

    public static final class Sql {
        // Column order needs to correspond to INDEX_[Column] constant values
        public static final String[] SELECT = { 
                Columns._ID, 
                Columns.TIME, 
                Columns.SELECTED_DAYS, 
                Columns.ENABLED, 
                Columns.VIBRATE,
                Columns.MESSAGE, 
                Columns.RINGTONE_URI 
                };
        
        public static final String TOKEN_IDS = "%ids%";
        
        public static final String SORT_ORDER = Columns.SORT + ", " + Columns.TIME + " ASC";
        public static final String WHERE_ENABLED = Columns.ENABLED + " = 1";
        public static final String WHERE_ID_IN = Columns._ID + "IN (" + TOKEN_IDS + ")";       
    }
    
    public static final class Toasts {
        private static Toast sToast = null;

        public static void setToast(final Toast toast) {
            if (sToast != null) {
                sToast.cancel();
            }

            sToast = toast;
        }

        public static void cancelToast() {
            if (sToast != null) {
                sToast.cancel();
            }

            sToast = null;
        }
    }
    
    public static final Handler sHandler = new Handler();
    
    public static void saveAlarm(final Context context, final Alarm alarm) {
        if (alarm.id == INVALID_ID) {
            alarm.id =
                    (int) ContentUris.parseId(context.getContentResolver().insert(
                            Alarm.CONTENT_URI, alarm.toContentValues()));  
        } else {
            final int rowsUpdated = context.getContentResolver().update(
                    ContentUris.withAppendedId(Alarm.CONTENT_URI, alarm.id),
                    alarm.toContentValues(), null, null);
            if (rowsUpdated == 0) {
                Log.e(TAG, "Alarms.saveAlarm(Context, Alarm): Failed to update Alarm[" + alarm.id + (TextUtils.isEmpty(alarm.label)? "" : "|" + alarm.label) + "], alarm doesn't exist in database");
                alarm.id = INVALID_ID;
                saveAlarm(context, alarm);
                return;
            }
        }
        
        if (alarm.enabled) {
            clearSnoozeAlertIfNeeded(context, alarm);
        }

        setNextSnoozeAlert(context);
    }
    

    /**
     * Disables current snooze alert and deletes alarm; then sets snooze alert for next alarm.
     */
    public static void deleteAlarm(final Context context, final int alarmId) {
        clearSnoozeById(context, alarmId);

        context.getContentResolver().delete(
                    ContentUris.withAppendedId(Alarm.CONTENT_URI, alarmId), "", null);

        setNextSnoozeAlert(context);
    }
    
    public static Cursor getAlarmsCursor(final Context context) {
        return context.getContentResolver().query(Alarm.CONTENT_URI, Sql.SELECT, null, null, Sql.SORT_ORDER);
    }
    
    public static Loader<Cursor> getAlarmsCursorLoader(final Context context) {
        return new CursorLoader(context, Alarm.CONTENT_URI, Sql.SELECT, null, null, Sql.SORT_ORDER);
    }
    
    public static Cursor getEnabledAlarmsCursor(final Context context) {
        return context.getContentResolver().query(Alarm.CONTENT_URI, Sql.SELECT, Sql.WHERE_ENABLED, null, null);
    }
    
    public static Alarm getAlarm(final Context context, final int alarmId) {
        final Cursor cursor = context.getContentResolver().query(ContentUris.withAppendedId(Alarm.CONTENT_URI, alarmId),
                        Sql.SELECT, null, null, null);
        Alarm alarm = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                alarm = new Alarm(context ,cursor);
            }
            cursor.close();
        }
        return alarm;
    }
    
    public static Alarm getNextEnabledAlarm(final Context context) {
        final Cursor cursor = getEnabledAlarmsCursor(context);
        Alarm alarm = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                alarm = new Alarm(context, cursor);
            }
            cursor.close();
        }
        return alarm;
    }
    
    /**
     * Disables non-repeating alarms that have passed. Called at boot.
     */
    public static void disableExpiredAlarms(final Context context) {
        final Cursor cursor = getEnabledAlarmsCursor(context);
        final long currentTime = System.currentTimeMillis();

        final int[] ids = {};
        int index = 0;
        if (cursor.moveToFirst()) {
            do {
                if (cursor.getLong(Columns.INDEX_TIME) < currentTime) {
                    ids[index++] = cursor.getInt(Columns.INDEX_ID);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        
        final ContentValues contentValues = new ContentValues(1);
        contentValues.put(Columns.ENABLED, 0);
        
        context.getContentResolver().update(Alarm.CONTENT_URI, contentValues, Sql.WHERE_ID_IN.replace(Sql.TOKEN_IDS, Arrays.toString(ids)), null);
    }
    
    //TODO: refactor
    public static void toggleAlarm(final Context context, final Alarm alarm, final boolean enabled) {
        toggleAlarmInternal(context, alarm, enabled);
        setNextSnoozeAlert(context);
    }

    private static void toggleAlarmInternal(final Context context, final Alarm alarm,
            final boolean enabled) {
        final ContentValues contentValues = new ContentValues(2);
        contentValues.put(Columns.ENABLED, enabled ? 1 : 0);

        /*
         * If we are enabling the alarm, calculate alarm time since the time value in Alarm may be
         * old.
         */
        if (enabled) {
            alarm.normalize(true);
            contentValues.put(Columns.TIME, alarm.toMillis(true));
        } else {
            // Clear the snooze if the id matches.
            clearSnoozeById(context, alarm.id);
        }

        context.getContentResolver().update(
                ContentUris.withAppendedId(Alarm.CONTENT_URI, alarm.id), contentValues,
                null, null);
    }

    // clear snooze if alarm will trigger before it
    public static void clearSnoozeAlertIfNeeded(final Context context, final Alarm alarm) {
        if (alarm.enabled) {
            final SharedPreferences sharedPreferences =
                    context.getSharedPreferences(PREFERENCES_DESKCLOCK, 0);
            if (alarm.toMillis(true) < sharedPreferences.getLong(KEY_SNOOZE_TIME, 0)) {
                clearSnoozeAlertPreference(context, sharedPreferences);
            }
        }
    }
    
    public static Alarm calculateNextSnoozeAlert(final Context context) {
        Alarm nextAlarm = null;
        long minTime = Long.MAX_VALUE;
        final long currentTime = System.currentTimeMillis();
        final Cursor cursor = getEnabledAlarmsCursor(context);
        if (cursor != null) {
            for(cursor.moveToFirst(); cursor.isLast(); cursor.moveToNext()) {
                final Alarm alarm = new Alarm(context, cursor);
                long scheduledTime = alarm.toMillis(true);
                if (scheduledTime < currentTime) {
                    // Expired alarm, disable it and move along.
                    if(!alarm.selectedDays.isRepeating()) {
                        toggleAlarmInternal(context, alarm, false);
                        continue;
                    }
                    alarm.normalize(true);
                    scheduledTime = alarm.toMillis(true);
                }
                if (scheduledTime < minTime) {
                    minTime = scheduledTime;
                    nextAlarm = alarm;
                }
            }
            cursor.close();
        }
        return nextAlarm;
    }

    /**
     * Called at system startup, on time/timezone change, and whenever the user changes alarm
     * settings. Activates snooze if set, otherwise loads all alarms, activates next alert.
     */
    public static void setNextSnoozeAlert(final Context context) {
        if (!enableSnoozeAlert(context)) {
            final Alarm nextSnoozeAlert = calculateNextSnoozeAlert(context);
            if (nextSnoozeAlert != null) {
                enableSnoozeAlert(context, nextSnoozeAlert);
            } else {
                disableSnoozeAlert(context);
            }
        }
    }

    /**
     * Sets alert in AlarmManger and StatusBar. This is what will actually launch the alert when the
     * alarm triggers.
     * 
     * @param snoozeAlert
     *            Alarm.
     * @param atTimeInMillis
     *            milliseconds since epoch
     */
    private static void enableSnoozeAlert(final Context context, final Alarm snoozeAlert) {
        if (DEVELOPER_MODE) {
            Log.d(TAG, "Alarms.enableSnoozeAlert(): Enable (Alarm)snoozeAlert["
                    + snoozeAlert.id
                    + (!TextUtils.isEmpty(snoozeAlert.label) ? "|" + snoozeAlert.label : "")
                    + "]  for " + snoozeAlert.format(FORMAT_DATE_TIME));
        }
       
        final long snoozeAlertTime = snoozeAlert.toMillis(true);
        
        /**
         * XXX: As of gingerbread-release PendingIntents with Parcels are inflated by the
         * AlarmManagerService which would result in a ClassNotFoundException since if we were to
         * pass the Alarm as a Parcel since it does not know about Alarm.class, hence we are passing
         * it as raw data in the form of a byte[]
         */
        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).set(
                AlarmManager.RTC_WAKEUP, snoozeAlertTime, PendingIntent.getBroadcast(context, 0,
                        new Intent(ACTION_ALARM_ALERT).putExtra(Alarm.KEY_RAW_DATA, snoozeAlert.toRawData()),
                        PendingIntent.FLAG_CANCEL_CURRENT));

        setStatusBarIcon(context, true);

        saveNextAlarm(context, snoozeAlert.formattedDayTime);
    }

    /**
     * Disables alert in AlarmManger and StatusBar.
     * 
     * @param id
     *            Alarm ID.
     */
    public static void disableSnoozeAlert(final Context context) {
        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).cancel(PendingIntent
                .getBroadcast(context, 0, new Intent(ACTION_ALARM_ALERT),
                        PendingIntent.FLAG_CANCEL_CURRENT));
        setStatusBarIcon(context, false);
        saveNextAlarm(context, "");
    }

    public static void saveSnoozeAlert(final Context context, final int id, final long time) {
        final SharedPreferences sharedPreferences =
                context.getSharedPreferences(PREFERENCES_DESKCLOCK, Context.MODE_PRIVATE);
        if (id == INVALID_ID) {
            clearSnoozeAlertPreference(context, sharedPreferences);
        } else {
            final SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(KEY_SNOOZE_ID, id);
            editor.putLong(KEY_SNOOZE_TIME, time);
            editor.commit();
        }
        // Set the next alert after updating the snooze.
        setNextSnoozeAlert(context);
    }

    /**
     * Disable the snooze alert if the given id matches the snooze id.
     */
    public static void clearSnoozeById(final Context context, final int snoozeId) {
        final SharedPreferences sharedPreferences =
                context.getSharedPreferences(PREFERENCES_DESKCLOCK, Context.MODE_PRIVATE);

        if (snoozeId == sharedPreferences.getInt(KEY_SNOOZE_ID, INVALID_ID)) {
            clearSnoozeAlertPreference(context, sharedPreferences);
        }
    }

    /*
     * Clear snooze by cancelling notification and removing the snooze related preferences. Do not
     * use clear because that will erase the clock preferences.
     */
    private static void clearSnoozeAlertPreference(final Context context,
            final SharedPreferences sharedPreferences) {
        final int snoozeId = sharedPreferences.getInt(KEY_SNOOZE_ID, DOES_NOT_EXIST);

        // cancel snooze notification
        if (snoozeId != DOES_NOT_EXIST) {
            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                    .cancel(snoozeId);
        }

        // remove snooze related preferences
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_SNOOZE_ID);
        editor.remove(KEY_SNOOZE_TIME);
        editor.commit();
    };

    /**
     * If there is a snooze set, enable it in AlarmManager
     * 
     * @return true if snooze is set
     */
    private static boolean enableSnoozeAlert(final Context context) {
        final SharedPreferences sharedPreferences =
                context.getSharedPreferences(PREFERENCES_DESKCLOCK, Context.MODE_PRIVATE);

        final int snoozeAlarmId = sharedPreferences.getInt(KEY_SNOOZE_ID, DOES_NOT_EXIST);
        if (snoozeAlarmId == DOES_NOT_EXIST) {
            return false;
        }
        final long time = sharedPreferences.getLong(KEY_SNOOZE_TIME, INVALID_TIME);

        // Get the alarm from the db.
        final Alarm snoozeAlarm = getAlarm(context, snoozeAlarmId);
        if (snoozeAlarm == null || time == INVALID_TIME) {
            return false;
        }
        // The time in the database is either 0 (repeating) or a specific time
        // for a non-repeating alarm. Update this value so the AlarmReceiver
        // has the right time to compare.
        snoozeAlarm.set(time);

        enableSnoozeAlert(context, snoozeAlarm);
        return true;
    }

    /**
     * Tells the StatusBar whether the alarm is enabled or disabled
     */
    private static void setStatusBarIcon(final Context context, final boolean enabled) {
        // broadcast ALARM_CHANGED intent to trigger status bar update
        context.sendBroadcast(new Intent("android.intent.action.ALARM_CHANGED").putExtra(
                "alarmSet", enabled));
    }

    /**
     * Save the time of the next alarm as a formatted string into the system settings
     */
    public static void saveNextAlarm(final Context context, final String timeString) {
        Settings.System.putString(context.getContentResolver(),
                Settings.System.NEXT_ALARM_FORMATTED, timeString);
    }


    /**
     * Display a toast that tells the user how long until the alarm goes off. This helps prevent
     * "am/pm" mistakes.
     */
    public static void popAlarmSetToast(final Context context, final Alarm alarm) {
        final Toast toast = Toast.makeText(context, formatToast(context, alarm.toMillis(true)), Toast.LENGTH_LONG);
        Toasts.setToast(toast);
        toast.show();
    }


    /**
     * format "Alarm set for 2 days 7 hours and 53 minutes from now"
     */
    public static String formatToast(final Context context, final long timeInMillis) {
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
}
