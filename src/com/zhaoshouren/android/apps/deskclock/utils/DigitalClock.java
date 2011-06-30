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

package com.zhaoshouren.android.apps.deskclock.utils;

import static com.zhaoshouren.android.apps.deskclock.DeskClock.DEVELOPER_MODE;
import static com.zhaoshouren.android.apps.deskclock.DeskClock.TAG;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.text.format.Time;
import android.util.Log;

public abstract class DigitalClock {
    private final FormattedTime mFormattedTime;
    private final Context mContext;
    private final ContentResolver mContentResolver;
    private final Handler mHandler;
    private boolean mLive = false;
    
    private static final IntentFilter sIntentFilter = new IntentFilter();
    static {
        sIntentFilter.addAction(Intent.ACTION_TIME_TICK);
        sIntentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        sIntentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        sIntentFilter.addAction(Intent.ACTION_DATE_CHANGED);
        sIntentFilter.addAction(Intent.ACTION_USER_PRESENT);
        sIntentFilter.addAction(Intent.ACTION_SCREEN_ON);
        sIntentFilter.addAction(Intent.ACTION_SCREEN_OFF);
    }

    public DigitalClock(final Context context) {
        this(context, new Handler());
    }

    public DigitalClock(final Context context, final Handler handler) {
        mContext = context;
        mContentResolver = context.getContentResolver();
        mHandler = handler;
        
        mFormattedTime = new FormattedTime(context);
    }

    private final BroadcastReceiver mBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (!mLive) {
                context.unregisterReceiver(mBroadCastReceiver);
                if (DEVELOPER_MODE) {
                    Log.d(TAG, "DigitalClock" 
                            + "> mBroadcastReceiver.onReceive(Context, Intent): Unregistering mBroadcastReceiver");
                }
            } else {
                final String action = intent.getAction();
                if (action == Intent.ACTION_TIMEZONE_CHANGED) {
                    mFormattedTime.switchTimezone(intent.getStringExtra("time-zone"));
                } 
    
                // Post a runnable to avoid blocking the broadcast.
                mHandler.post(new Runnable() {
                    public void run() {
                        updateTime(action != Intent.ACTION_TIME_TICK);
                    }
                });
            }
        }
    };

    private class FormatChangeObserver extends ContentObserver {
        public FormatChangeObserver() {
            super(mHandler);
        }

        @Override
        public void onChange(final boolean selfChange) {
            if (!mLive) {
                mContentResolver.unregisterContentObserver(mFormatChangeObserver);
            }

            updateTime();
        }
    }

    protected final ContentObserver mFormatChangeObserver = new FormatChangeObserver();

    public void updateTime(final Time time) {
        mFormattedTime.set(time);
        updateTime();
    }

    protected void updateTime(boolean updateDate) {
        if (mLive) {
            mFormattedTime.setToNow();
        }

        updateTimeView(mFormattedTime);
        if (updateDate) {
            updateDateView(mFormattedTime);
        }
    }
    
    public void updateTime() {
        updateTime(true);
    }

    public void setLive(final boolean live) {
        mLive = live;
        updateTime();
        if (mLive) {
            mContext.registerReceiver(mBroadCastReceiver, sIntentFilter, null, mHandler);
            mContentResolver.registerContentObserver(Settings.System.CONTENT_URI,
                    true, mFormatChangeObserver);
        }
    }
    
    @Override
    public String toString() {
        return "{ mLive: " + mLive + ", mContext: " + mContext + ", mDigitalClockTime: (" + mFormattedTime + ") }";     
    }
    
    

    protected abstract void updateTimeView(FormattedTime formattedTime);
    protected abstract void updateDateView(FormattedTime formattedTime);
}
