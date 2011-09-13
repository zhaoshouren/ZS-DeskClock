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

package com.zhaoshouren.android.apps.deskclock.ui;

import static com.zhaoshouren.android.apps.deskclock.DeskClock.TAG;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.zhaoshouren.android.apps.deskclock.util.Alarm;

/**
 * Full screen alarm alert: pops visible indicator and plays alarm tone. This activity shows the
 * alert as a dialog.
 */
public class AlarmAlertActivity extends AlarmAlertFullScreenActivity {
    // If we try to check the keyguard more than 5 times, just launch the full screen activity.
    private static final int MAX_KEYGUARD_CHECKS = 5;
    private static final IntentFilter sIntentFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(final Message message) {
            handleScreenOff((KeyguardManager) message.obj);
        }
    };
    
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            handleScreenOff((KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE));
        }
    };
    
    private int mKeyguardRetryCount;

    @Override
    protected void onCreate(final Bundle icicle) {
        super.onCreate(icicle);

        // Listen for the screen turning off so that when the screen comes back on, the user does
        // not need to unlock the phone to dismiss the alarm.
        registerReceiver(mBroadcastReceiver, sIntentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);

        // Remove any of the keyguard messages just in case
        mHandler.removeMessages(0);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private boolean checkRetryCount() {
        if (mKeyguardRetryCount++ >= MAX_KEYGUARD_CHECKS) {
            Log.e(TAG,
                    "AlarmAlert.checkRetryCount(): Tried to read keyguard status too many times, bailing...");
            return false;
        }
        return true;
    }

    private void handleScreenOff(final KeyguardManager keyguardManager) {
        if (!keyguardManager.inKeyguardRestrictedInputMode() && checkRetryCount()) {
            mHandler.sendMessageDelayed(
                    mHandler.obtainMessage(0, keyguardManager), 500);
        } else {
            // Launch the full screen activity but do not turn the screen on.
            startActivity(new Intent(this, AlarmAlertFullScreenActivity.class).putExtra(
                    Alarm.KEY_PARCELABLE, mAlarm).putExtra(SCREEN_OFF, true));
            finish();
        }
        
    }
}
