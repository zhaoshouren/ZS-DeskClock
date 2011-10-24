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

package com.zhaoshouren.android.apps.deskclock.util;


import static com.zhaoshouren.android.apps.deskclock.DeskClock.DEVELOPER_MODE;
import static com.zhaoshouren.android.apps.deskclock.DeskClock.TAG;

import android.content.BroadcastReceiver;
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
    private final Handler mHandler;
    private boolean mLive = false;
    private boolean mStopped = false;
    private boolean mRegistered = false; 
    
    private static final IntentFilter sIntentFilter = new IntentFilter();
    static {
        sIntentFilter.addAction(Intent.ACTION_TIME_TICK);
        
        sIntentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        sIntentFilter.addAction(Intent.ACTION_DATE_CHANGED);
        
        sIntentFilter.addAction(Intent.ACTION_USER_PRESENT);
        sIntentFilter.addAction(Intent.ACTION_SCREEN_ON);
        sIntentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        
        sIntentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        sIntentFilter.addAction(Intent.ACTION_LOCALE_CHANGED);
    }

    private final BroadcastReceiver mBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            
            mHandler.post(new Runnable() {
                public void run() {
                    final String action = intent.getAction();
                    boolean updateDate = true;             
                    
                    if (action == Intent.ACTION_TIME_TICK) {
                    	updateDate = false;
                    } else if (action == Intent.ACTION_SCREEN_OFF) {
                        mStopped = true;
                        updateDate = false;
                    } else if (action == Intent.ACTION_USER_PRESENT || action == Intent.ACTION_SCREEN_ON) {
                        mStopped = false;
                    } else if (action == Intent.ACTION_TIME_CHANGED) {
                        mFormattedTime.switchTimezone(intent.getStringExtra("time-zone"));
                    } 
                    
                    if (!mStopped) {
                        updateTime(updateDate);
                    }
                }
            });    
        }
    };

    private final ContentObserver mContentObserver;

    public DigitalClock(final Context context) {
        this(context, new Handler());
    }

    public DigitalClock(final Context context, final Handler handler) {
        mContext = context;
        mHandler = handler; 
        mFormattedTime = new FormattedTime(context);
        
        mContentObserver = new ContentObserver(mHandler) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                mFormattedTime.setFormats(mContext);            
                updateTime(true);
            }
        };
    }
    
    public void updateTime() {
        updateTime(true);
    }
    
    public void updateTime(final Time time) {
        mFormattedTime.set(time);
        updateTime(true);
    }

    protected void updateTime(boolean updateDate) {
        if (mLive) {
            mFormattedTime.setToNow();
        }
        
        if (DEVELOPER_MODE) {
            Log.d(TAG, mFormattedTime.formattedTimeAmPm);
        }

        updateTimeView(mFormattedTime);
        if (updateDate) {
            updateDateView(mFormattedTime);
        }
    }
    
    
    public void setLive(final boolean live) {
        mLive = live;
        updateTime();
        if (mLive && !mRegistered) {
            mRegistered = true;
            mContext.registerReceiver(mBroadCastReceiver, sIntentFilter, null, mHandler);
            mContext.getContentResolver().registerContentObserver(Settings.System.CONTENT_URI,
                    true, mContentObserver);
        } else if (!mLive && mRegistered) {
            mContext.unregisterReceiver(mBroadCastReceiver);
            mContext.getContentResolver().unregisterContentObserver(mContentObserver);
        }
    }
    
    @Override
    public String toString() {
        return "{ mLive: " + mLive + ", mContext: " + mContext + ", mDigitalClockTime: (" + mFormattedTime + ") }";     
    }

    protected abstract void updateTimeView(FormattedTime formattedTime);
    protected abstract void updateDateView(FormattedTime formattedTime);
}
