package com.zhaoshouren.android.apps.clock.receiver;

import static com.zhaoshouren.android.apps.clock.DeskClock.TAG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class TestBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.i(TAG, intent.getAction());
    }
}
