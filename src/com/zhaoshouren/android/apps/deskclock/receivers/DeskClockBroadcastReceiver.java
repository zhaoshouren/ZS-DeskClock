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

package com.zhaoshouren.android.apps.deskclock.receivers;

import static com.zhaoshouren.android.apps.deskclock.DeskClock.DEVELOPER_MODE;
import static com.zhaoshouren.android.apps.deskclock.DeskClock.TAG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.zhaoshouren.android.apps.deskclock.ui.DeskClockActivity;

public class DeskClockBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (DEVELOPER_MODE) {
            Log.d(TAG, "DesktopBroadcastReceiver.onReceive()" 
                    + "\n    context: " + context
                    + "\n    intent: " + intent
                    + "\n    action: " + intent.getAction()
                    );
        }
        final String action = intent.getAction();

        if (action == Intent.ACTION_POWER_CONNECTED) {
            context.startActivity(new Intent(context, DeskClockActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_FROM_BACKGROUND));
        } else if (action == Intent.ACTION_POWER_DISCONNECTED) {
            context.startActivity(new Intent(Intent.ACTION_MAIN).addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_FROM_BACKGROUND).addCategory(
                    Intent.CATEGORY_HOME));
        }
    }
}
