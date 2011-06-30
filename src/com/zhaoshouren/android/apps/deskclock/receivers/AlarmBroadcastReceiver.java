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

package com.zhaoshouren.android.apps.deskclock.receivers;

import static com.zhaoshouren.android.apps.deskclock.DeskClock.DEVELOPER_MODE;
import static com.zhaoshouren.android.apps.deskclock.DeskClock.FORMAT_DATE_TIME;
import static com.zhaoshouren.android.apps.deskclock.DeskClock.TAG;

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
import com.zhaoshouren.android.apps.deskclock.ui.AlarmAlertActivity;
import com.zhaoshouren.android.apps.deskclock.ui.AlarmAlertFullScreenActivity;
import com.zhaoshouren.android.apps.deskclock.ui.SetAlarmPreferenceActivity;
import com.zhaoshouren.android.apps.deskclock.utils.Alarm;
import com.zhaoshouren.android.apps.deskclock.utils.Alarms;

public class AlarmBroadcastReceiver extends BroadcastReceiver {
    /**
     * If the alarm is older than STALE_WINDOW seconds, ignore. It is probably the result of a time
     * or timezone change
     */
    private static final int STALE_WINDOW = 60 * 30 * 1000;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();
        if (action == Alarms.ACTION_ALARM_KILLED) {
            // The alarm has been killed, update the notification
            updateNotification(context,
                    (Alarm) intent.getParcelableExtra(Alarm.KEY_PARCEL),
                    intent.getIntExtra(Alarms.KEY_ALARM_KILLED_TIMEOUT, -1));
            return;
        } else if (action == Alarms.CANCEL_SNOOZE) {
            Alarms.saveSnoozeAlert(context, -1, -1);
            return;
        }

        final Alarm alarm = new Alarm(context, intent.getByteArrayExtra(Alarm.KEY_RAW_DATA));
        
        final long alarmTime = alarm.toMillis(true);
        if (DEVELOPER_MODE) {
            Log.d(TAG, "AlarmBroadcastReceiver.onReceive(): Alarm[" + alarm.id + (TextUtils.isEmpty(alarm.label) ? "" : "|"
                    + alarm.label) + "] set For " + alarm.format(FORMAT_DATE_TIME));
        }

        if (System.currentTimeMillis() > alarmTime + STALE_WINDOW ) {
            if (DEVELOPER_MODE) {
                Log.d(TAG, "AlarmBroadcastReceiver.onReceive(): Ignoring stale alarm");
            }
            return;
        }

        // Maintain a CPU wake lock until the AlarmAlert and AlarmPlayerService can
        // pick it up.
        DeskClock.acquireWakeLock(context);

        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        
        // Launch UI explicitly stating that this is not due to user action so that the current
        // app's notification management is not disturbed
        context.startActivity(new Intent(context, ((KeyguardManager) context
                .getSystemService(Context.KEYGUARD_SERVICE)).inKeyguardRestrictedInputMode()
                ? AlarmAlertFullScreenActivity.class : AlarmAlertActivity.class).putExtra(
                Alarm.KEY_PARCEL, alarm).setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION));

        // Disable the snooze alert if this alarm is the snooze.
        // TODO: refactor clearSnoozePreferenceIfNeeded
        Alarms.clearSnoozeAlertIfNeeded(context, alarm);
        
        // Disable this alarm if it does not repeat.
        if (!alarm.selectedDays.isRepeating()) {
            Alarms.toggleAlarm(context, alarm, Alarms.ALARM_DISABLED);
        } else {
            // Enable the next alert if there is one. The above call to
            // enableAlarm will call setNextAlert so avoid calling it twice.
            Alarms.setNextSnoozeAlert(context);
        }

        context.startService(new Intent(Alarms.ACTION_ALARM_ALERT).putExtra(
                Alarm.KEY_PARCEL, alarm));

        final PendingIntent pendingNotify =
                PendingIntent.getActivity(context, alarm.id, new Intent(context, AlarmAlertActivity.class)
                        .putExtra(Alarm.KEY_PARCEL, alarm), 0);

        // Use the alarm's label or the default label as the ticker text and
        // main text of the notification.
        final String tickerText = alarm.getTickerText();
        final Notification notification =
                new Notification(R.drawable.stat_notify_alarm, tickerText, alarmTime);
        notification.setLatestEventInfo(context, tickerText,
                context.getString(R.string.alarm_notify_text), pendingNotify);
        notification.flags |= Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_ONGOING_EVENT;
        notification.defaults |= Notification.DEFAULT_LIGHTS;

        // Send the notification using the alarm id to easily identify the
        // correct notification.
        getNotificationManager(context).notify(alarm.id, notification);
    }
    
    
    private NotificationManager getNotificationManager(final Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private void updateNotification(final Context context, final Alarm alarm, final int timeout) {
        final NotificationManager notificationManager = getNotificationManager(context);

        // If the alarm is null, just cancel the notification.
        if (alarm == null) {
            if (DEVELOPER_MODE) {
                Log.d(TAG,
                        "AlarmBroadcastReceiver.updateNotification(): Alarm null; cannot update notification for killer callback");
            }
            return;
        }

        // Update the notification to indicate that the alert has been
        // silenced.
        final String tickerText = alarm.getTickerText();
        final Notification notification =
                new Notification(R.drawable.stat_notify_alarm, tickerText, alarm.toMillis(true));
        notification
                .setLatestEventInfo(context, tickerText, context.getString(
                        R.string.alarm_alert_alert_silenced, timeout), PendingIntent.getActivity(
                        context, alarm.id,
                        new Intent(context, SetAlarmPreferenceActivity.class).putExtra(Alarm.KEY_ID, alarm.id), 0));
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        // We have to cancel the original notification since it is in the
        // ongoing section and we want the "killed" notification to be a plain
        // notification.
        notificationManager.cancel(alarm.id);
        notificationManager.notify(alarm.id, notification);
    }
}
