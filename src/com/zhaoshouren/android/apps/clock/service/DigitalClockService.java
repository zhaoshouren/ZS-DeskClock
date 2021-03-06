/*
 * Copyright (C) 2011 Warren Chu
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

package com.zhaoshouren.android.apps.clock.service;

import static com.zhaoshouren.android.apps.clock.DeskClock.DEVELOPER_MODE;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.zhaoshouren.android.apps.clock.R;
import com.zhaoshouren.android.apps.clock.receiver.DigitalClockAppWidgetProvider;
import com.zhaoshouren.android.apps.clock.util.Action;
import com.zhaoshouren.android.apps.clock.util.DigitalClock;
import com.zhaoshouren.android.apps.clock.util.FormattedTime;

import java.util.Arrays;

public class DigitalClockService extends Service {

    public static final String TAG = "ZS.DigitalClockService";

    private DigitalClock mDigitalClock;
    private RemoteViews mRemoteViews;
    private static Context sContext;
    private static ComponentName sComponentName;

    @Override
    public IBinder onBind(final Intent intent) {
        if (DEVELOPER_MODE) {
            Log.d(TAG, "DigitalClockService.onBind()");
        }
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (DEVELOPER_MODE) {
            Log.d(TAG,
                    "onStartCommand()" + "\n    intent: " + intent + "\n    flags: " + flags
                            + "\n    startId: " + startId + "\n    appWidgetIds: "
                            + Arrays.toString(intent.getIntArrayExtra("appWidgetIds"))
                            + "\n    packageName(intent): " + intent.getStringExtra("packageName")
                            + "\n    packageName(context): " + sContext.getPackageName()
                            + "\n    mDigitalClock: " + mDigitalClock);
        }

        if (intent.getIntArrayExtra("appWidgetIds") != null) {
            mRemoteViews = new RemoteViews(sContext.getPackageName(), R.layout.appwidget_digital);
            mRemoteViews.setOnClickPendingIntent(R.id.time, PendingIntent.getActivity(
                    sContext, 0, new Intent(Action.HOME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    PendingIntent.FLAG_UPDATE_CURRENT));

            mDigitalClock.setLive(true);

            if (DEVELOPER_MODE) {
                Log.d(TAG, "\n    mRemoteViews: " + mRemoteViews);
            }

            return START_REDELIVER_INTENT;
        } else {
            stopSelf(startId);
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sContext = getApplicationContext();
        sComponentName = new ComponentName(sContext, DigitalClockAppWidgetProvider.class);

        mDigitalClock = new DigitalClock(sContext) {
            @Override
            protected void updateTimeView(final FormattedTime formattedTime) {
                if (DEVELOPER_MODE) {
                    Log.d(TAG,
                            "onCreate()"
                                    + "\n> DigitalClock.updateTimeView(DateTime)"
                                    + "\n    time: "
                                    + formattedTime.formattedTime
                                    + "\n    amPm: "
                                    + formattedTime.formattedAmPm
                                    + "\n    appWidgetIds: "
                                    + Arrays.toString(AppWidgetManager.getInstance(sContext)
                                            .getAppWidgetIds(sComponentName))
                                    + "\n    remoteViews: " + mRemoteViews
                                    + "\n    mDigitalClock: " + mDigitalClock);
                }

                if (mRemoteViews != null) {
                    mRemoteViews.setTextViewText(R.id.time, formattedTime.formattedTime);

                    if (formattedTime.formattedAmPm.length() > 0) {
                        mRemoteViews.setTextViewText(R.id.time, formattedTime.formattedAmPm);
                        mRemoteViews.setViewVisibility(R.id.am_pm, View.VISIBLE);
                    } else {
                        mRemoteViews.setViewVisibility(R.id.am_pm, View.GONE);
                    }

                    AppWidgetManager.getInstance(sContext).updateAppWidget(sComponentName,
                            mRemoteViews);
                }
            }

            @Override
            protected void updateDateView(final FormattedTime formattedTime) {
                if (DEVELOPER_MODE) {
                    Log.d(TAG,
                            "onCreate()"
                                    + "\n> DigitalClock.updateDateView(DateTime)"
                                    + "\n    date: "
                                    + formattedTime.formattedDate
                                    + "\n    appWidgetIds: "
                                    + Arrays.toString(AppWidgetManager.getInstance(sContext)
                                            .getAppWidgetIds(sComponentName))
                                    + "\n    remoteViews.layoutId: " + mRemoteViews.getLayoutId()
                                    + "\n    mDigitalClock: " + mDigitalClock);
                }

                if (mRemoteViews != null) {
                    mRemoteViews.setTextViewText(R.id.date, formattedTime.formattedDate);

                    AppWidgetManager.getInstance(sContext).updateAppWidget(sComponentName,
                            mRemoteViews);
                }
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (DEVELOPER_MODE) {
            Log.d(TAG, "onDestroy()" + "\n    mDigitalClock: " + mDigitalClock);
        }

        try {
            mDigitalClock.setLive(false);
        } catch (NullPointerException exception) {
            Log.e(TAG, "AHHH!!!", exception);
        }
    }
}
