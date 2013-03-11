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

package com.zhaoshouren.android.apps.clock.receiver;

import static com.zhaoshouren.android.apps.clock.DeskClock.DEVELOPER_MODE;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;




public class DigitalClockAppWidgetProvider extends AppWidgetProvider {

    public static final String TAG = "ZS.DigitalClockAppWidgetProvider";

    private static final Intent ACTION_DIGITAL_CLOCK = new Intent(
            "com.zhaoshouren.android.apps.clock.DIGITAL_CLOCK");

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager,
            final int[] appWidgetIds) {
        ACTION_DIGITAL_CLOCK.putExtra("appWidgetIds", appWidgetIds);
        ACTION_DIGITAL_CLOCK.putExtra("packageName", context.getPackageName());

        context.startService(ACTION_DIGITAL_CLOCK);
    }

    @Override
    public void onEnabled(final Context context) {
        context.startService(ACTION_DIGITAL_CLOCK);
        if (DEVELOPER_MODE) {
            Log.d(TAG, "onEnabled()" + "\n    context: " + context
                    + "\n    startService(ACTION_DIGITAL_CLOCK)");
        }
    }

    @Override
    public void onDisabled(final Context context) {
        context.stopService(ACTION_DIGITAL_CLOCK);
        if (DEVELOPER_MODE) {
            Log.d(TAG, "onDisabled()" + "\n    context: " + context
                    + "\n    stopService(ACTION_DIGITAL_CLOCK)");
        }
    }
}
