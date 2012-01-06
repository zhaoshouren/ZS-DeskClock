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

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.zhaoshouren.android.apps.clock.R;
import com.zhaoshouren.android.apps.clock.util.Action;

import java.util.Arrays;

public class AnalogClockAppWidgetProvider extends AppWidgetProvider {

    private static final String TAG = "ZS.AnalogClockAppWidgetProvider";

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager,
            final int[] appWidgetIds) {
        final RemoteViews remoteViews =
                new RemoteViews(context.getPackageName(), R.layout.appwidget_analog);

        if (DEVELOPER_MODE) {
            Log.d(TAG,
                    "onUpdate()" + "\n    context: " + context + "\n    appWidgetIds: "
                            + Arrays.toString(appWidgetIds) + "\n    remoteViews.getLayoutId(): "
                            + remoteViews.getLayoutId() + "\n    R.id.appwidget_analog: "
                            + R.id.appwidget_analog);
        }

        remoteViews.setOnClickPendingIntent(R.id.appwidget_analog, PendingIntent.getActivity(
                context, 0, new Intent(Action.LIST_ALARMS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                PendingIntent.FLAG_CANCEL_CURRENT));

        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }
}
