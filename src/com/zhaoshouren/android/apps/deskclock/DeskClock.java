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

package com.zhaoshouren.android.apps.deskclock;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

public class DeskClock {
    public static final boolean DEVELOPER_MODE = true; // Set to false for production
    public static final String TAG = "ZS.DeskClock";
    public static final String FORMAT_DATE_TIME = "%l:%M:%S %p";

    private static PowerManager.WakeLock sWakeLock;

    public static void acquireWakeLock(final Context context) {
        if (DEVELOPER_MODE) {
            Log.d(TAG, "acquireWakeLock(): Acquiring CPU wake lock");
        }
        if (sWakeLock != null) {
            return;
        }

        sWakeLock =
                ((PowerManager) context.getSystemService(Context.POWER_SERVICE)).newWakeLock(
                        PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP
                                | PowerManager.ON_AFTER_RELEASE, TAG);
        sWakeLock.acquire();
    }

    public static void releaseWakeLock() {
        if (DEVELOPER_MODE) {
            Log.d(TAG, "releaseWakeLock(): Releasing CPU wake lock");
        }
        if (sWakeLock != null) {
            sWakeLock.release();
            sWakeLock = null;
        }
    }
}
