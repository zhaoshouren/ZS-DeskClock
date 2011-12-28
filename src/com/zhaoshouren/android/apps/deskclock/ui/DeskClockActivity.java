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

import static android.os.BatteryManager.BATTERY_STATUS_CHARGING;
import static android.os.BatteryManager.BATTERY_STATUS_FULL;
import static android.os.BatteryManager.BATTERY_STATUS_UNKNOWN;
import static com.zhaoshouren.android.apps.deskclock.DeskClock.DEVELOPER_MODE;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.UiModeManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhaoshouren.android.apps.deskclock.R;
import com.zhaoshouren.android.apps.deskclock.ui.widget.DigitalClockLayout;
import com.zhaoshouren.android.apps.deskclock.util.Alarm;

import java.util.Date;
import java.util.Random;

/**
 * DeskClock clock view for desk docks.
 */
public class DeskClockActivity extends Activity {

    private static final String TAG = "ZS.DeskClockActivity";

    /**
     * Alarm action for midnight (so we can update the date display).
     */
    private static final String ACTION_MIDNIGHT = "com.zhaoshouren.android.apps.deskclock.MIDNIGHT";
    /**
     * Interval between forced polls of the weather widget.
     */
    private static final long QUERY_WEATHER_DELAY = 60 * 60 * 1000; // 1 hr
    /**
     * Intent to broadcast for dock settings.
     */
    private static final String DOCK_SETTINGS_ACTION = "com.android.settings.DOCK_SETTINGS";
    /**
     * Delay before engaging the burn-in protection mode (green-on-black).
     */
    private static final long SCREEN_SAVER_TIMEOUT = 5 * 60 * 1000; // 5 min
    /**
     * Repositioning delay in screen saver.
     */
    private static final long SCREEN_SAVER_MOVE_DELAY = 60 * 1000; // 1 min
    /**
     * Color to use for text & graphics in screen saver mode.
     */
    private static final int SCREEN_SAVER_COLOR = 0xFF806530;
    /**
     * Color to use for text & graphics in screen saver dimmed mode.
     */
    private static final int SCREEN_SAVER_COLOR_DIM = 0xFF302418;
    /**
     * Opacity of black layer between clock display and wallpaper.
     */
    private static final float DIM_BEHIND_AMOUNT_NORMAL = 0.4f;
    /**
     * Opacity of black layer between clock display and wallpaper when dimmed.
     */
    private static final float DIM_BEHIND_AMOUNT_DIMMED = 0.8f;

    // Internal message IDs.
    private static final int QUERY_WEATHER_DATA_MSG = 0x1000;
    private static final int UPDATE_WEATHER_DISPLAY_MSG = 0x1001;
    private static final int SCREEN_SAVER_TIMEOUT_MSG = 0x2000;
    private static final int SCREEN_SAVER_MOVE_MSG = 0x2001;

    // Weather widget query information.
    private static final String GENIE_PACKAGE_ID = "com.google.android.apps.genie.geniewidget";
    private static final String WEATHER_CONTENT_AUTHORITY = GENIE_PACKAGE_ID + ".weather";
    private static final String WEATHER_CONTENT_PATH = "/weather/current";
    private static final String[] WEATHER_CONTENT_COLUMNS = new String[] { "location", "timestamp",
            "temperature", "highTemperature", "lowTemperature", "iconUrl", "iconResId",
            "description", };

    private static final String ACTION_GENIE_REFRESH = "com.google.android.apps.genie.REFRESH";

    // State variables follow.
    private DigitalClockLayout mTime;
    private TextView mDate;

    private TextView mNextAlarmTimeDate = null;
    private TextView mNextAlarmAmPm = null;
    private TextView mBatteryDisplay;

    private TextView mWeatherCurrentTemperature;
    private TextView mWeatherHighTemperature;
    private TextView mWeatherLowTemperature;
    private TextView mWeatherLocation;
    private ImageView mWeatherIcon;

    private String mWeatherCurrentTemperatureString;
    private String mWeatherHighTemperatureString;
    private String mWeatherLowTemperatureString;
    private String mWeatherLocationString;
    private Drawable mWeatherIconDrawable;

    private Resources mGenieResources = null;

    private boolean mDimmed = false;
    private boolean mScreenSaverMode = false;

    private String mDateFormat;

    private int mBatteryLevel = -1;
    private boolean mPluggedIn = false;

    private boolean mLaunchedFromDock = false;

    private Random mRandom;

    private PendingIntent mMidnightIntent;

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (DEVELOPER_MODE) {
                Log.d(TAG, "mIntentReceiver.onReceive()" + "\n   action = " + action
                        + "\n   intent = " + intent);
            }
            if (action == Intent.ACTION_DATE_CHANGED || action == ACTION_MIDNIGHT) {
                refreshDate();
            } else if (action == Intent.ACTION_BATTERY_CHANGED) {
                handleBatteryUpdate(intent.getIntExtra("status", BATTERY_STATUS_UNKNOWN),
                        intent.getIntExtra("level", 0));
            } else if (action == UiModeManager.ACTION_EXIT_DESK_MODE) {
                if (mLaunchedFromDock) {
                    // moveTaskToBack(false);
                    finish();
                }
                mLaunchedFromDock = false;
            }
        }
    };

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(final Message m) {
            if (m.what == QUERY_WEATHER_DATA_MSG) {
                new Thread() {
                    @Override
                    public void run() {
                        queryWeatherData();
                    }
                }.start();
                scheduleWeatherQueryDelayed(QUERY_WEATHER_DELAY);
            } else if (m.what == UPDATE_WEATHER_DISPLAY_MSG) {
                updateWeatherDisplay();
            } else if (m.what == SCREEN_SAVER_TIMEOUT_MSG) {
                saveScreen();
            } else if (m.what == SCREEN_SAVER_MOVE_MSG) {
                moveScreenSaver();
            }
        }
    };

    private final ContentObserver mContentObserver = new ContentObserver(mHandler) {
        @Override
        public void onChange(final boolean selfChange) {
            if (DEVELOPER_MODE) {
                Log.d(TAG, "mContentObserver.onChange(): refreshWeather()");
            }
            refreshWeather();
        }
    };

    private void moveScreenSaver() {
        moveScreenSaverTo(-1, -1);
    }

    private void moveScreenSaverTo(int x, int y) {
        if (!mScreenSaverMode) {
            return;
        }

        final View saver_view = findViewById(R.id.saver_view);

        final DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        if (x < 0 || y < 0) {
            final int myWidth = saver_view.getMeasuredWidth();
            final int myHeight = saver_view.getMeasuredHeight();

            x = (int) (mRandom.nextFloat() * (metrics.widthPixels - myWidth));
            y = (int) (mRandom.nextFloat() * (metrics.heightPixels - myHeight));
        }

        if (DEVELOPER_MODE) {
            Log.d(TAG,
                    String.format("moveScreenSaverTo(): %d: jumping to (%d,%d)",
                            System.currentTimeMillis(), x, y));
        }

        saver_view.layout(x, y, metrics.widthPixels, metrics.heightPixels);

        // Synchronize our jumping so that it happens exactly on the second.
        mHandler.sendEmptyMessageDelayed(SCREEN_SAVER_MOVE_MSG, SCREEN_SAVER_MOVE_DELAY + 1000
                - System.currentTimeMillis() % 1000);
    }

    private void setWakeLock(final boolean hold) {
        if (DEVELOPER_MODE) {
            Log.d(TAG, "setWakeLock(): " + (hold ? "holding" : " releasing") + " wake lock"
                    + "\n   hold = " + hold);
        }
        final Window window = getWindow();
        final WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.flags |=
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
        if (hold) {
            layoutParams.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        } else {
            layoutParams.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        }
        window.setAttributes(layoutParams);
    }

    private void scheduleScreenSaver() {
        // reschedule screen saver
        mHandler.removeMessages(SCREEN_SAVER_TIMEOUT_MSG);
        mHandler.sendMessageDelayed(Message.obtain(mHandler, SCREEN_SAVER_TIMEOUT_MSG),
                SCREEN_SAVER_TIMEOUT);
    }

    private void restoreScreen() {
        if (!mScreenSaverMode) {
            return;
        }
        if (DEVELOPER_MODE) {
            Log.d(TAG, "restoreScreen()");
        }
        mScreenSaverMode = false;
        initViews();
        doDim(false); // restores previous dim mode
        // policy: update weather info when returning from screen saver
        if (mPluggedIn) {
            requestWeatherDataFetch();
        }

        scheduleScreenSaver();

        refreshAll();
    }

    // Special screen-saver mode for OLED displays that burn in quickly
    private void saveScreen() {
        if (mScreenSaverMode) {
            return;
        }
        if (DEVELOPER_MODE) {
            Log.d(TAG, "saveScreen()");
        }

        // quickly stash away the x/y of the current date
        final View oldTimeDate = findViewById(R.id.time_date);
        final int oldLoc[] = new int[2];
        oldTimeDate.getLocationOnScreen(oldLoc);

        mScreenSaverMode = true;
        final Window win = getWindow();
        final WindowManager.LayoutParams winParams = win.getAttributes();
        winParams.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        win.setAttributes(winParams);

        // give up any internal focus before we switch layouts
        final View focused = getCurrentFocus();
        if (focused != null) {
            focused.clearFocus();
        }

        setContentView(R.layout.desk_clock_saver);

        mTime = (DigitalClockLayout) findViewById(R.id.time);
        mDate = (TextView) findViewById(R.id.date);
        mNextAlarmTimeDate = (TextView) findViewById(R.id.next_alarm_time_date);
        mNextAlarmAmPm = (TextView) findViewById(R.id.next_alarm_amPm);

        final int color = mDimmed ? SCREEN_SAVER_COLOR_DIM : SCREEN_SAVER_COLOR;

        ((TextView) findViewById(R.id.timeDisplay)).setTextColor(color);
        ((TextView) findViewById(R.id.amPm)).setTextColor(color);
        mDate.setTextColor(color);
        mNextAlarmTimeDate.setTextColor(color);
        mNextAlarmTimeDate.setCompoundDrawablesWithIntrinsicBounds(
                getResources().getDrawable(
                        mDimmed ? R.drawable.ic_lock_idle_alarm_saver_dim
                                : R.drawable.ic_lock_idle_alarm_saver), null, null, null);
        mNextAlarmAmPm.setTextColor(color);

        mBatteryDisplay =
                mWeatherCurrentTemperature =
                        mWeatherHighTemperature = mWeatherLowTemperature = mWeatherLocation = null;
        mWeatherIcon = null;

        refreshDate();
        refreshAlarm();

        moveScreenSaverTo(oldLoc[0], oldLoc[1]);
    }

    @Override
    public void onUserInteraction() {
        if (mScreenSaverMode) {
            restoreScreen();
        }
    }

    // Tell the Genie widget to load new data from the network.
    @Deprecated
    private void requestWeatherDataFetch() {
        if (DEVELOPER_MODE) {
            Log.d(TAG,
                    "requestWeatherDataFetch(): sendBroadcast(new Intent(ACTION_GENIE_REFRESH).putExtra(\"requestWeather\", true)");
        }
        sendBroadcast(new Intent(ACTION_GENIE_REFRESH).putExtra("requestWeather", true));
        // we expect the result to show up in our content observer
    }

    @Deprecated
    private boolean supportsWeather() {
        return mGenieResources != null;
    }

    @Deprecated
    private void scheduleWeatherQueryDelayed(final long delay) {
        // cancel any existing scheduled queries
        unscheduleWeatherQuery();

        if (DEVELOPER_MODE) {
            Log.d(TAG, "scheduleWeatherQueryDelayed(): scheduling weather fetch message for "
                    + delay + "ms from now");
        }

        mHandler.sendEmptyMessageDelayed(QUERY_WEATHER_DATA_MSG, delay);
    }

    @Deprecated
    private void unscheduleWeatherQuery() {
        mHandler.removeMessages(QUERY_WEATHER_DATA_MSG);
    }

    @Deprecated
    private void queryWeatherData() {
        // if we couldn't load the weather widget's resources, we simply
        // assume it's not present on the device.
        if (mGenieResources == null) {
            return;
        }

        final Uri queryUri =
                new Uri.Builder().scheme(android.content.ContentResolver.SCHEME_CONTENT)
                        .authority(WEATHER_CONTENT_AUTHORITY).path(WEATHER_CONTENT_PATH)
                        .appendPath(new Long(System.currentTimeMillis()).toString()).build();

        if (DEVELOPER_MODE) {
            Log.d(TAG, "queryWeatherData()" + "\n    queryUri = " + queryUri);
        }

        Cursor cursor;
        try {
            cursor =
                    getContentResolver().query(queryUri, WEATHER_CONTENT_COLUMNS, null, null, null);
        } catch (final RuntimeException e) {
            Log.e(TAG, "Weather query failed", e);
            cursor = null;
        }

        if (cursor != null && cursor.moveToFirst()) {
            if (DEVELOPER_MODE) {
                final java.lang.StringBuilder sb =
                        new java.lang.StringBuilder("Weather query result: {");
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    sb.append(cursor.getColumnName(i)).append("=").append(cursor.getString(i));
                }
                sb.append("}");
                Log.d(TAG, sb.toString());
            }

            mWeatherIconDrawable =
                    mGenieResources.getDrawable(cursor.getInt(cursor
                            .getColumnIndexOrThrow("iconResId")));

            mWeatherLocationString = cursor.getString(cursor.getColumnIndexOrThrow("location"));

            // any of these may be NULL
            final int colTemp = cursor.getColumnIndexOrThrow("temperature");
            final int colHigh = cursor.getColumnIndexOrThrow("highTemperature");
            final int colLow = cursor.getColumnIndexOrThrow("lowTemperature");

            mWeatherCurrentTemperatureString =
                    cursor.isNull(colTemp) ? "\u2014" : String.format("%d\u00b0",
                            cursor.getInt(colTemp));
            mWeatherHighTemperatureString =
                    cursor.isNull(colHigh) ? "\u2014" : String.format("%d\u00b0",
                            cursor.getInt(colHigh));
            mWeatherLowTemperatureString =
                    cursor.isNull(colLow) ? "\u2014" : String.format("%d\u00b0",
                            cursor.getInt(colLow));
        } else {
            Log.w(TAG, "No weather information available (cur=" + cursor + ")");
            mWeatherIconDrawable = null;
            mWeatherLocationString = getString(R.string.weather_fetch_failure);
            mWeatherCurrentTemperatureString =
                    mWeatherHighTemperatureString = mWeatherLowTemperatureString = "";
        }

        if (cursor != null) {
            // clean up cursor
            cursor.close();
        }

        mHandler.sendEmptyMessage(UPDATE_WEATHER_DISPLAY_MSG);
    }

    @Deprecated
    private void refreshWeather() {
        if (supportsWeather()) {
            scheduleWeatherQueryDelayed(0);
        }
        updateWeatherDisplay(); // in case we have it cached
    }

    @Deprecated
    private void updateWeatherDisplay() {
        if (mWeatherCurrentTemperature == null) {
            return;
        }

        mWeatherCurrentTemperature.setText(mWeatherCurrentTemperatureString);
        mWeatherHighTemperature.setText(mWeatherHighTemperatureString);
        mWeatherLowTemperature.setText(mWeatherLowTemperatureString);
        mWeatherLocation.setText(mWeatherLocationString);
        mWeatherIcon.setImageDrawable(mWeatherIconDrawable);
    }

    // Adapted from KeyguardUpdateMonitor.java
    private void handleBatteryUpdate(final int plugStatus, final int batteryLevel) {
        final boolean pluggedIn =
                plugStatus == BATTERY_STATUS_CHARGING || plugStatus == BATTERY_STATUS_FULL;
        if (pluggedIn != mPluggedIn) {
            setWakeLock(pluggedIn);

            if (pluggedIn) {
                // policy: update weather info when attaching to power
                requestWeatherDataFetch();
            }
        }
        if (pluggedIn != mPluggedIn || batteryLevel != mBatteryLevel) {
            mBatteryLevel = batteryLevel;
            mPluggedIn = pluggedIn;
            refreshBattery();
        }
    }

    private void refreshBattery() {
        if (mBatteryDisplay == null) {
            return;
        }

        if (mPluggedIn /* || mBatteryLevel < LOW_BATTERY_THRESHOLD */) {
            mBatteryDisplay.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                    android.R.drawable.ic_lock_idle_charging, 0);
            mBatteryDisplay.setText(getString(R.string.battery_charging_level, mBatteryLevel));
            mBatteryDisplay.setVisibility(View.VISIBLE);
        } else {
            mBatteryDisplay.setVisibility(View.INVISIBLE);
        }
    }

    private void refreshDate() {
        final Date now = new Date();
        if (DEVELOPER_MODE) {
            Log.d(TAG, "refreshing date..." + now);
        }
        mDate.setText(DateFormat.format(mDateFormat, now));
    }

    private void refreshAlarm() {
        if (mNextAlarmTimeDate == null) {
            return;
        }
        final View nextAlarm = findViewById(R.id.next_alarm);

        final String[] nextAlarmFormatted =
                Settings.System.getString(getContentResolver(),
                        Settings.System.NEXT_ALARM_FORMATTED).split("\\s");
        if (!TextUtils.isEmpty(nextAlarmFormatted[0])) {

            mNextAlarmTimeDate.setText(nextAlarmFormatted[0] + " " + nextAlarmFormatted[1]);
            nextAlarm.setVisibility(View.VISIBLE);
            nextAlarm.setEnabled(true);

            if (mNextAlarmAmPm == null) {
                return;
            }

            if (nextAlarmFormatted.length == 3) {
                mNextAlarmAmPm.setText(nextAlarmFormatted[2].toUpperCase());
            } else {
                mNextAlarmAmPm.setText("");
            }

        } else {
            nextAlarm.setEnabled(false);
            nextAlarm.setVisibility(View.INVISIBLE);
        }
    }

    private void refreshAll() {
        refreshDate();
        refreshAlarm();
        refreshBattery();
        refreshWeather();
    }

    private void doDim(final boolean fade) {
        final View tintView = findViewById(R.id.window_tint);
        if (tintView == null) {
            return;
        }

        final Window win = getWindow();
        final WindowManager.LayoutParams winParams = win.getAttributes();

        winParams.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        winParams.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;

        // dim the wallpaper somewhat (how much is determined below)
        winParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;

        if (mDimmed) {
            winParams.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            winParams.dimAmount = DIM_BEHIND_AMOUNT_DIMMED;
            winParams.buttonBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF;

            // show the window tint
            tintView.startAnimation(AnimationUtils.loadAnimation(this, fade ? R.anim.dim
                    : R.anim.dim_instant));
        } else {
            winParams.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
            winParams.dimAmount = DIM_BEHIND_AMOUNT_NORMAL;
            winParams.buttonBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;

            // hide the window tint
            tintView.startAnimation(AnimationUtils.loadAnimation(this, fade ? R.anim.undim
                    : R.anim.undim_instant));
        }

        win.setAttributes(winParams);
    }

    @Override
    public void onNewIntent(final Intent newIntent) {
        super.onNewIntent(newIntent);
        if (DEVELOPER_MODE) {
            Log.d(TAG, "onNewIntent()" + "\n   newIntent = " + newIntent);
        }

        // update our intent so that we can consult it to determine whether or
        // not the most recent launch was via a dock event
        setIntent(newIntent);
    }

    @Override
    public void onStart() {
        super.onStart();

        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(UiModeManager.ACTION_EXIT_DESK_MODE);
        filter.addAction(ACTION_MIDNIGHT);
        registerReceiver(mIntentReceiver, filter);
    }

    @Override
    public void onStop() {
        super.onStop();

        unregisterReceiver(mIntentReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (DEVELOPER_MODE) {
            Log.d(TAG, "onResume()" + "\n   getIntent() = " + getIntent());
        }

        mTime.refresh();

        // reload the date format in case the user has changed settings
        // recently
        mDateFormat = getString(R.string.full_wday_month_day_no_year);

        // Listen for updates to weather data
        final Uri weatherNotificationUri =
                new Uri.Builder().scheme(android.content.ContentResolver.SCHEME_CONTENT)
                        .authority(WEATHER_CONTENT_AUTHORITY).path(WEATHER_CONTENT_PATH).build();
        getContentResolver()
                .registerContentObserver(weatherNotificationUri, true, mContentObserver);

        // Elaborate mechanism to find out when the day rolls over
        final Time today = new Time();
        today.setToNow();
        today.hour = 0;
        today.minute = 0;
        today.second = 0;
        today.monthDay += 1;
        today.normalize(true);
        final long alarmTimeUTC = today.toMillis(true);

        mMidnightIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_MIDNIGHT), 0);
        final AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC, alarmTimeUTC, AlarmManager.INTERVAL_DAY, mMidnightIntent);
        if (DEVELOPER_MODE) {
            Log.d(TAG, "set repeating midnight event at UTC: " + alarmTimeUTC + " ("
                    + (alarmTimeUTC - System.currentTimeMillis())
                    + " ms from now) repeating every " + AlarmManager.INTERVAL_DAY
                    + " with intent: " + mMidnightIntent);
        }

        // If we weren't previously visible but now we are, it's because we're
        // being started from another activity. So it's OK to un-dim.
        if (mTime != null && mTime.getWindowVisibility() != View.VISIBLE) {
            mDimmed = false;
        }

        // Adjust the display to reflect the currently chosen dim mode.
        doDim(false);

        restoreScreen(); // disable screen saver
        refreshAll(); // will schedule periodic weather fetch

        setWakeLock(mPluggedIn);

        scheduleScreenSaver();

        final boolean launchedFromDock = getIntent().hasCategory(Intent.CATEGORY_DESK_DOCK);

        if (supportsWeather() && launchedFromDock && !mLaunchedFromDock) {
            // policy: fetch weather if launched via dock connection
            if (DEVELOPER_MODE) {
                Log.d(TAG, "Device now docked; forcing weather to refresh right now");
            }
            requestWeatherDataFetch();
        }

        mLaunchedFromDock = launchedFromDock;
    }

    @Override
    public void onPause() {
        if (DEVELOPER_MODE) {
            Log.d(TAG, "onPause()");
        }

        // Turn off the screen saver and cancel any pending timeouts.
        // (But don't un-dim.)
        mHandler.removeMessages(SCREEN_SAVER_TIMEOUT_MSG);
        restoreScreen();

        // Other things we don't want to be doing in the background.
        // NB: we need to keep our broadcast receiver alive in case the dock
        // is disconnected while the screen is off
        getContentResolver().unregisterContentObserver(mContentObserver);

        final AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.cancel(mMidnightIntent);
        unscheduleWeatherQuery();

        super.onPause();
    }

    private void initViews() {
        // give up any internal focus before we switch layouts
        final View focused = getCurrentFocus();
        if (focused != null) {
            focused.clearFocus();
        }

        setContentView(R.layout.desk_clock);

        mTime = (DigitalClockLayout) findViewById(R.id.time);
        mDate = (TextView) findViewById(R.id.date);
        mBatteryDisplay = (TextView) findViewById(R.id.battery);
        mNextAlarmTimeDate = (TextView) findViewById(R.id.next_alarm_time_date);
        mNextAlarmAmPm = (TextView) findViewById(R.id.next_alarm_amPm);

        mTime.getRootView().requestFocus();

        mWeatherCurrentTemperature = (TextView) findViewById(R.id.weather_temperature);
        mWeatherHighTemperature = (TextView) findViewById(R.id.weather_high_temperature);
        mWeatherLowTemperature = (TextView) findViewById(R.id.weather_low_temperature);
        mWeatherLocation = (TextView) findViewById(R.id.weather_location);
        mWeatherIcon = (ImageView) findViewById(R.id.weather_icon);

        final View.OnClickListener alarmClickListener = new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                startActivity(new Intent(DeskClockActivity.this, AlarmClockActivity.class));
            }
        };

        final View.OnLongClickListener setAlarmClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                startActivity(new Intent(DeskClockActivity.this, SetAlarmPreferenceActivity.class));
                return true;
            }
        };

        final View nextAlarm = findViewById(R.id.next_alarm);
        nextAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final Intent intent =
                        new Intent(DeskClockActivity.this, SetAlarmPreferenceActivity.class);
                intent.putExtra(Alarm.Keys.ID, SetAlarmPreferenceActivity.GET_NEXT_ALARM);
                startActivity(intent);
            }
        });
        nextAlarm.setOnLongClickListener(setAlarmClickListener);

        final View timeDate = findViewById(R.id.time_date);
        timeDate.setOnClickListener(alarmClickListener);
        timeDate.setOnLongClickListener(setAlarmClickListener);

        final ImageButton nightmodeButton = (ImageButton) findViewById(R.id.nightmode_button);
        nightmodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                mDimmed = !mDimmed;
                doDim(true);
            }
        });

        nightmodeButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                saveScreen();
                return true;
            }
        });

        final View weatherView = findViewById(R.id.weather);
        weatherView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                refreshWeather();
                if (!supportsWeather()) {
                    return;
                }

                final Intent genieAppQuery =
                        getPackageManager().getLaunchIntentForPackage(GENIE_PACKAGE_ID).addFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                if (genieAppQuery != null) {
                    startActivity(genieAppQuery);
                }
            }
        });

        weatherView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                if (!supportsWeather()) {
                    return false;
                }

                final Intent genieAppQuery =
                        getPackageManager().getLaunchIntentForPackage(GENIE_PACKAGE_ID).addFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                if (genieAppQuery != null) {
                    startActivity(genieAppQuery);
                    return true;
                }
                return false;
            }
        });

        weatherView.setVisibility(supportsWeather() ? View.VISIBLE : View.GONE);

        final View tintView = findViewById(R.id.window_tint);
        tintView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View v, final MotionEvent event) {
                if (mDimmed && event.getAction() == MotionEvent.ACTION_DOWN) {
                    // We want to un-dim the whole screen on tap.
                    // ...Unless the user is specifically tapping on the dim
                    // widget, in which case let it do the work.
                    final Rect r = new Rect();
                    nightmodeButton.getHitRect(r);
                    final int[] gloc = new int[2];
                    nightmodeButton.getLocationInWindow(gloc);
                    r.offsetTo(gloc[0], gloc[1]); // convert to window coords

                    if (!r.contains((int) event.getX(), (int) event.getY())) {
                        mDimmed = false;
                        doDim(true);
                    }
                }
                return false; // always pass the click through
            }
        });

        // Tidy up awkward focus behavior: the first view to be focused in
        // trackball mode should be the alarms button
        final ViewTreeObserver vto = mTime.getViewTreeObserver();
        vto.addOnGlobalFocusChangeListener(new ViewTreeObserver.OnGlobalFocusChangeListener() {
            @Override
            public void onGlobalFocusChanged(final View oldFocus, final View newFocus) {
                if (oldFocus == null && newFocus == nightmodeButton) {
                    mTime.requestFocus();
                }
            }
        });
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mScreenSaverMode) {
            moveScreenSaver();
        } else {
            initViews();
            doDim(false);
            refreshAll();
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_item_alarms:
            startActivity(new Intent(DeskClockActivity.this, AlarmClockActivity.class));
            return true;
        case R.id.menu_item_add_alarm:
            startActivity(new Intent(this, SetAlarmPreferenceActivity.class));
            return true;
        case R.id.menu_item_dock_settings:
            startActivity(new Intent(DOCK_SETTINGS_ACTION));
            return true;
        default:
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.desk_clock_menu, menu);
        return true;
    }

    @Override
    protected void onCreate(final Bundle icicle) {
        super.onCreate(icicle);

        mRandom = new Random();

        try {
            mGenieResources = getPackageManager().getResourcesForApplication(GENIE_PACKAGE_ID);
        } catch (final PackageManager.NameNotFoundException e) {
            // no weather info available
            Log.w(TAG, "onCreate(): Can't find " + GENIE_PACKAGE_ID
                    + ". Weather forecast will not be available.");
        }

        initViews();
    }
}
