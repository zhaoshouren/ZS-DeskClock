/*
 * Copyright (C) 2011 Warren Chu
 * Copyright (C) 2009 The Android Open Source Project
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

package com.zhaoshouren.android.apps.deskclock.ui;

import static com.zhaoshouren.android.apps.deskclock.DeskClock.DEVELOPER_MODE;
import static com.zhaoshouren.android.apps.deskclock.DeskClock.TAG;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.zhaoshouren.android.apps.deskclock.R;
import com.zhaoshouren.android.apps.deskclock.receivers.AlarmBroadcastReceiver;
import com.zhaoshouren.android.apps.deskclock.utils.Alarm;
import com.zhaoshouren.android.apps.deskclock.providers.AlarmContract;

/**
 * Alarm Clock alarm alert: pops visible indicator and plays alarm tone. This activity is the full
 * screen version which shows over the lock screen with the wallpaper as the background.
 */
public class AlarmAlertFullScreenActivity extends FragmentActivity implements LoaderCallbacks<Cursor> {
    public static final String SCREEN_OFF = "screen_off";
    
    private static final int FLAGS_WINDOW_SCREEN_ON = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON;
    private static final int FLAGS_WINDOW = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
    private static final IntentFilter sIntentFilter = new IntentFilter(AlarmContract.ACTION_ALARM_KILLED);
    static {
        sIntentFilter.addAction(AlarmContract.ACTION_ALARM_SNOOZE);
        sIntentFilter.addAction(AlarmContract.ACTION_ALARM_DISMISS);
    }
     
    // These constants must match the values in res/xml/settings.xml
    private static final class Settings {
        private static final String DEFAULT_SNOOZE = "10";
        private static final String DEFAULT_VOLUME_BEHAVIOR = "2";
    }

    // Receives the ALARM_KILLED action from the AlarmKlaxon,
    // and also ALARM_SNOOZE_ACTION / ALARM_DISMISS_ACTION from other
    // applications
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (action == AlarmContract.ACTION_ALARM_SNOOZE) {
                snooze();
            } else if (action == AlarmContract.ACTION_ALARM_DISMISS) {
                dismiss(false);
            } else {
                final Alarm alarm = intent.getParcelableExtra(Alarm.KEY_PARCEL);
                if (alarm != null && alarm.id == mAlarm.id) {
                    dismiss(true);
                }
            }
        }
    };
    
    protected Alarm mAlarm;
    private Cursor mCursor;
    private int mVolumeBehavior;
    
    @Override
    protected void onCreate(final Bundle icicle) {
        super.onCreate(icicle);

        final LoaderManager loaderManager = getSupportLoaderManager();
        final Intent intent = getIntent();
        mAlarm = intent.getParcelableExtra(Alarm.KEY_PARCEL);
        mVolumeBehavior =
                Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString(
                        SettingsPreferenceActivity.KEY_VOLUME_BEHAVIOR, Settings.DEFAULT_VOLUME_BEHAVIOR));

        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);

        final Window window = getWindow();
        window.addFlags(FLAGS_WINDOW);
        // Turn on the screen unless we are being launched from the AlarmAlert
        // subclass.
        if (!intent.getBooleanExtra(SCREEN_OFF, false)) {
            window.addFlags(FLAGS_WINDOW_SCREEN_ON);
        }

        updateLayout();
        
        registerReceiver(mBroadcastReceiver, sIntentFilter);
        loaderManager.initLoader(mAlarm.id, icicle, this);
    }

    private void setTitle() {
        ((TextView) findViewById(R.id.alertTitle)).setText(mAlarm.label);
    }

    private void updateLayout() {
        setContentView(LayoutInflater.from(this).inflate(R.layout.alarm_alert, null));

        /*
         * snooze behavior: pop a snooze confirmation view, kick alarm manager.
         */
        final Button snoozeButton = (Button) findViewById(R.id.snooze);
        snoozeButton.requestFocus();
        snoozeButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(final View v) {
                snooze();
            }
        });

        /* dismiss button: close notification */
        findViewById(R.id.dismiss).setOnClickListener(new Button.OnClickListener() {
            public void onClick(final View v) {
                dismiss(false);
            }
        });

        /* Set the title from the passed in alarm */
        setTitle();
    }

    // Attempt to snooze this alert.
    private void snooze() {
        // Do not snooze if the snooze button is disabled.
        if (!findViewById(R.id.snooze).isEnabled()) {
            dismiss(false);
            return;
        }
        final int snoozeMinutes =
                Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString(
                        SettingsPreferenceActivity.KEY_ALARM_SNOOZE, Settings.DEFAULT_SNOOZE));

        final long snoozeTime = System.currentTimeMillis() + 1000 * 60 * snoozeMinutes;
        AlarmContract.saveSnoozeAlert(this, mAlarm.id, snoozeTime);
        

        // Get the display time for the snooze and update the notification.
        final Alarm snoozeAlarm = new Alarm(this);
        snoozeAlarm.set(snoozeTime);

        // Append (snoozed) to the label.
        String tickerText = mAlarm.getTickerText();
        tickerText = getString(R.string.alarm_notify_snooze_label, tickerText);

        // Notify the user that the alarm has been snoozed.
        final Intent cancelSnoozeIntent = new Intent(this, AlarmBroadcastReceiver.class);
        cancelSnoozeIntent.setAction(AlarmContract.CANCEL_SNOOZE);
        cancelSnoozeIntent.putExtra(Alarm.KEY_ID, mAlarm.id);
        final PendingIntent broadcast =
                PendingIntent.getBroadcast(this, mAlarm.id, cancelSnoozeIntent, 0);

        final NotificationManager notificationManager = getNotificationManager();
        final Notification notification = new Notification(R.drawable.stat_notify_alarm, tickerText, 0);
        notification.setLatestEventInfo(this, tickerText,
                getString(R.string.alarm_notify_snooze_text, snoozeAlarm),
                broadcast);
        notification.flags |= Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONGOING_EVENT;
        notificationManager.notify(mAlarm.id, notification);

        final String displayTime = getString(R.string.alarm_alert_snooze_set, snoozeMinutes);
        if (DEVELOPER_MODE) {
            Log.d(TAG, "AlarmAlertFullScreen.snooze()"
                    +"\n    displayTime: " + displayTime);
        }

        // Display the snooze minutes in a toast.
        Toast.makeText(this, displayTime, Toast.LENGTH_LONG).show();
        stopService(new Intent(AlarmContract.ACTION_ALARM_ALERT));
        finish();
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    // Dismiss the alarm.
    private void dismiss(final boolean killed) {
        if (DEVELOPER_MODE) {
            Log.d(TAG, "AlarmAlertFullScreen.dismiss(): "
                    + (killed ? "Alarm killed" : "Alarm dismissed by user"));
        }
        // The service told us that the alarm has been killed, do not modify
        // the notification or stop the service.
        if (!killed) {
            // Cancel the notification and stop playing the alarm
            final NotificationManager notificationManager = getNotificationManager();
            notificationManager.cancel(mAlarm.id);
            stopService(new Intent(AlarmContract.ACTION_ALARM_ALERT));
        }
        finish();
    }

    /**
     * this is called when a second alarm is triggered while a previous alert window is still
     * active.
     */
    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);

        mAlarm = intent.getParcelableExtra(Alarm.KEY_PARCEL);
        
        if (DEVELOPER_MODE) {
            Log.d(TAG, "AlarmAlert.OnNewIntent()"
                    +"\n    mAlarm " + mAlarm);
        }

        setTitle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // If the alarm was deleted at some point, disable snooze.
        
        if (AlarmContract.getAlarm(this, mCursor) == null) {
            final Button snoozeButton = (Button) findViewById(R.id.snooze);
            snoozeButton.setEnabled(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (DEVELOPER_MODE) {
            Log.d(TAG, "AlarmAlert.onDestroy()");
        }
        // No longer care about the alarm being killed.
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public boolean dispatchKeyEvent(final KeyEvent event) {
        // Do this on key down to handle a few of the system keys.
        switch (event.getKeyCode()) {
        // Volume keys and camera keys dismiss the alarm
        case KeyEvent.KEYCODE_VOLUME_UP:
        case KeyEvent.KEYCODE_VOLUME_DOWN:
        case KeyEvent.KEYCODE_CAMERA:
        case KeyEvent.KEYCODE_FOCUS:
            if (event.getAction() == KeyEvent.ACTION_UP) {
                switch (mVolumeBehavior) {
                case 1:
                    snooze();
                    break;

                case 2:
                    dismiss(false);
                    break;

                default:
                    break;
                }
            }
            return true;
        default:
            break;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        // Don't allow back to dismiss. This method is overriden by AlarmAlert
        // so that the dialog is dismissed.
        return;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
        return AlarmContract.getAlarmCursorLoader(this, id);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // TODO Auto-generated method stub
        mCursor = cursor;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursor = null;
    }
}
