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
import static com.zhaoshouren.android.apps.deskclock.DeskClock.TAG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.zhaoshouren.android.apps.deskclock.providers.AlarmContract;

public class AlarmInitBroadcastReceiver extends BroadcastReceiver {

    /**
     * Filtered Intents:
     *  android.intent.action.BOOT_COMPLETED
     *  android.intent.action.TIME_SET
     *  android.intent.action.TIMEZONE_CHANGED
     *  android.intent.action.LOCALE_CHANGED
     */
    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();
        
        if (DEVELOPER_MODE) {
            Log.d(TAG, "AlarmInitBroadcastReceiver.onReceive()"
                    + "\n    action: " + action);
        }

        if (context.getContentResolver() == null) {
            Log.e(TAG,
                    "AlarmInitBroadcastReceiver.onReceive(): FAILURE - unable to get content resolver; alarms inactive.");
            return;
        }
        
        if (action == Intent.ACTION_BOOT_COMPLETED) {
            AlarmContract.saveSnoozeAlert(context, -1, -1);
            AlarmContract.disableExpiredAlarms(context);
        }
        AlarmContract.setNextSnoozeAlert(context);
    }
}
