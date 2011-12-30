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

package com.zhaoshouren.android.apps.deskclock.receiver;

import static com.zhaoshouren.android.apps.deskclock.DeskClock.DEVELOPER_MODE;
import static com.zhaoshouren.android.apps.deskclock.DeskClock.FORMAT_DATE_TIME;
import static com.zhaoshouren.android.apps.deskclock.util.Alarm.INVALID_ID;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.zhaoshouren.android.apps.deskclock.DeskClock;
import com.zhaoshouren.android.apps.deskclock.R;
import com.zhaoshouren.android.apps.deskclock.provider.AlarmContract;
import com.zhaoshouren.android.apps.deskclock.ui.AlarmAlertActivity;
import com.zhaoshouren.android.apps.deskclock.ui.AlarmAlertFullScreenActivity;
import com.zhaoshouren.android.apps.deskclock.ui.DeskClockActivity;
import com.zhaoshouren.android.apps.deskclock.ui.SetAlarmPreferenceActivity;
import com.zhaoshouren.android.apps.deskclock.util.Alarm;
import com.zhaoshouren.android.apps.deskclock.util.Days;

public class AlarmBroadcastReceiver extends BroadcastReceiver {
    /**
     * If the alarm is older than STALE_WINDOW seconds, ignore. It is probably the result of a time
     * or timezone change
     */
    private static final int STALE_WINDOW = 60 * 30 * 1000;

    private static final String TAG = "ZS.AlarmBroadcastReceiver";

    /**
     * filtered Intent Actions for this receiver:
     * <dl>
     * <dt>ACTION_ALARM_KILLED
     * <dd>com.zhaoshouren.android.apps.deskclock.ACTION_ALARM_KILLED
     * <dt>ACTION_CANCEL_SNOOZE
     * <dd>com.zhaoshouren.android.apps.deskclock.ACTION_CANCEL_SNOOZE
     * <dt>ACTION_ALARM_ALERT
     * <dd>com.zhaoshouren.android.apps.deskclock.ACTION_ALARM_ALERT
     * <dt>ACTION_POWER_CONNECTED
     * <dd>android.intent.action.ACTION_POWER_CONNECTED
     * <dt>ACTION_POWER_DISCONNECTED
     * <dd>android.intent.action.ACTION_POWER_DISCONNECTED
     * <dt>BOOT_COMPLETED
     * <dd>android.intent.action.BOOT_COMPLETED
     * <dt>TIME_SET
     * <dd>android.intent.action.TIME_SET
     * <dt>TIMEZONE_CHANGED
     * <dd>android.intent.action.TIMEZONE_CHANGED
     * <dt>LOCALE_CHANGED
     * <dd>android.intent.action.LOCALE_CHANGED
     * </dl>
     */

    private static enum ACTIONS {
        // deskclock
        ACTION_ALARM_ALERT, ACTION_ALARM_KILLED, ACTION_CANCEL_SNOOZE,
        // android
        ACTION_POWER_CONNECTED, ACTION_POWER_DISCONNECTED, BOOT_COMPLETED, LOCALE_CHANGED,
        TIME_SET, TIMEZONE_CHANGED
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();

        switch (ACTIONS.valueOf(action.substring(TextUtils.lastIndexOf(action, '.') + 1))) {

        case ACTION_ALARM_ALERT:
            final Alarm alarm = new Alarm(context, intent.getByteArrayExtra(Alarm.Keys.RAW_DATA));
            final long alarmTime = alarm.toMillis(true);

            if (DEVELOPER_MODE) {
                Log.d(TAG,
                        "onReceive(): ACTION_ALARM_ALERT" + "\n   alarm.id = " + alarm.id
                                + "\n   alarm.label = " + alarm.label + "\n   alarm datetime = "
                                + alarm.format(FORMAT_DATE_TIME));
            }

            if (System.currentTimeMillis() > alarmTime + STALE_WINDOW) {
                if (DEVELOPER_MODE) {
                    Log.d(TAG, "onReceive(): Ignoring stale alarm");
                }
                break;
            }

            // Maintain a CPU wake lock until the AlarmAlertActivity or AlarmPlayerService can
            // pick it up.
            DeskClock.acquireWakeLock(context);

            context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

            // Launch UI explicitly stating that this is not due to user action so that the current
            // app's notification management is not disturbed
            context.startActivity(new Intent(context, ((KeyguardManager) context
                    .getSystemService(Context.KEYGUARD_SERVICE)).inKeyguardRestrictedInputMode()
                    ? AlarmAlertFullScreenActivity.class : AlarmAlertActivity.class).putExtra(
                    Alarm.Keys.PARCELABLE, alarm).setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION));

            // Disable the snooze alert if this alarm is the snooze.
            AlarmContract.cancelSnooze(context, alarm.id);

            // Disable this alarm if it does not repeat.
            if (alarm.days.selected == Days.NO_DAYS_SELECTED) {
                AlarmContract.disableAlarm(context, alarm);
            }
            AlarmContract.setNextAlarm(context);

            context.startService(new Intent(AlarmContract.Actions.PLAY).putExtra(
                    Alarm.Keys.PARCELABLE, alarm));

            final Notification notification =
                    getNotification(context, alarm, context.getString(R.string.alarm_notify_text),
                            PendingIntent.getActivity(context, alarm.id, new Intent(context,
                                    AlarmAlertActivity.class)
                                    .putExtra(Alarm.Keys.PARCELABLE, alarm), 0));
            notification.flags |= Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_ONGOING_EVENT;
            notification.defaults |= Notification.DEFAULT_LIGHTS;

            // Send the notification using the alarm id to easily identify the
            // correct notification.
            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(
                    alarm.id, notification);
            break;

        case ACTION_ALARM_KILLED:
            final Alarm killedAlarm = intent.getParcelableExtra(Alarm.Keys.PARCELABLE);
            final NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            // Update the notification to indicate that the alert has been
            // silenced.
            final Notification killedAlarmNotification =
                    getNotification(context, killedAlarm, context.getString(
                            R.string.alarm_alert_alert_silenced, -1), PendingIntent.getActivity(
                            context, killedAlarm.id, new Intent(context,
                                    SetAlarmPreferenceActivity.class).putExtra(Alarm.Keys.ID,
                                    killedAlarm.id), 0));
            killedAlarmNotification.flags |= Notification.FLAG_AUTO_CANCEL;
            // We have to cancel the original notification since it is in the
            // ongoing section and we want the "killed" notification to be a plain
            // notification.
            notificationManager.cancel(killedAlarm.id);
            notificationManager.notify(killedAlarm.id, killedAlarmNotification);
            break;

        case ACTION_CANCEL_SNOOZE:
            AlarmContract.cancelSnooze(context, intent.getIntExtra(Alarm.Keys.ID, INVALID_ID));
            break;

        case ACTION_POWER_CONNECTED:
            context.startActivity(new Intent(context, DeskClockActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_FROM_BACKGROUND));
            break;

        case ACTION_POWER_DISCONNECTED:
            context.startActivity(new Intent(Intent.ACTION_MAIN).addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_FROM_BACKGROUND).addCategory(Intent.CATEGORY_HOME));
            break;

        case BOOT_COMPLETED:
            AlarmContract.cancelSnooze(context, INVALID_ID);
            // no break because we want to fall through
        case TIME_SET:
        case TIMEZONE_CHANGED:
        case LOCALE_CHANGED:
            AlarmContract.setNextAlarm(context);
            break;
        }
    }

    private Notification getNotification(final Context context, final Alarm alarm,
            final CharSequence contentText, final PendingIntent pendingIntent) {
        final String tickerText = AlarmContract.getTickerText(context, alarm);
        final Notification notification =
                new Notification(R.drawable.stat_notify_alarm, tickerText, alarm.toMillis(true));
        notification.setLatestEventInfo(context, tickerText, contentText, pendingIntent);
        return notification;
    }
}
