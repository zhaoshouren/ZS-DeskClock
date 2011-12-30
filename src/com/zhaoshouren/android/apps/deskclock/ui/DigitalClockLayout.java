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

package com.zhaoshouren.android.apps.deskclock.ui;

import static com.zhaoshouren.android.apps.deskclock.DeskClock.DEVELOPER_MODE;

import android.content.Context;
import android.graphics.Typeface;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhaoshouren.android.apps.deskclock.R;
import com.zhaoshouren.android.apps.deskclock.util.DigitalClock;
import com.zhaoshouren.android.apps.deskclock.util.FormattedTime;

/**
 * Displays the time
 */
public class DigitalClockLayout extends LinearLayout {

    private static final String TAG = "ZS.DigitalClockLayout";

    public static final Typeface TYPEFACE_TIME_DEFAULT = Typeface.DEFAULT_BOLD;
    public static final int TYPEFACE_TIME_DEFAULT_STYLE = Typeface.BOLD;
    public static final Typeface TYPEFACE_DATE_DEFAULT = Typeface.DEFAULT;
    public static final int TYPEFACE_DATE_DEFAULT_STYLE = Typeface.NORMAL;
    private static final int VIEW_ID_AM_PM = R.id.amPm;
    private static final int VIEW_ID_TIME = R.id.timeDisplay;
    private static final int VIEW_ID_DATE = R.id.date;
    private TextView mTimeTextView;
    private TextView mAmPmTextView;
    private TextView mDateTextView;
    private boolean mLive = true;
    private boolean mAttached;

    private final DigitalClock mDigitalClock;

    public DigitalClockLayout(final Context context) {
        this(context, null);
    }

    // TODO: refactor to leverage Attibuteset for configuration/customization
    public DigitalClockLayout(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        mDigitalClock = new DigitalClock(context) {
            @Override
            protected void updateTimeView(FormattedTime formattedTime) {
                if (isShown()) {
                    mTimeTextView.setText(formattedTime.formattedTime);

                    if (formattedTime.formattedAmPm.length() > 0) {
                        mAmPmTextView.setText(formattedTime.formattedAmPm);
                        mAmPmTextView.setVisibility(VISIBLE);
                    } else {
                        mAmPmTextView.setVisibility(GONE);
                    }
                }
            }

            @Override
            protected void updateDateView(FormattedTime formattedTime) {
                if (isShown()) {
                    if (mDateTextView != null) {
                        mDateTextView.setText(formattedTime.formattedDate);
                    }
                }
            }
        };
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (DEVELOPER_MODE) {
            Log.d(TAG, "onAttachedToWindow()" + "\n    getId() = " + getId());
        }

        if (mAttached) {
            return;
        }
        mAttached = true;

        if (mLive) {
            mDigitalClock.setLive(mLive);
        }

        mDigitalClock.updateTime();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (DEVELOPER_MODE) {
            Log.d(TAG, "onDetachedFromWindow()" + "\n    getId() = " + getId());
        }

        if (!mAttached) {
            return;
        }
        mAttached = false;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mTimeTextView = (TextView) findViewById(VIEW_ID_TIME);
        mTimeTextView.setTypeface(TYPEFACE_TIME_DEFAULT, TYPEFACE_TIME_DEFAULT_STYLE);
        mAmPmTextView = (TextView) findViewById(VIEW_ID_AM_PM);
        mAmPmTextView.setTypeface(TYPEFACE_TIME_DEFAULT, TYPEFACE_TIME_DEFAULT_STYLE);
        mDateTextView = (TextView) findViewById(VIEW_ID_DATE);
        if (mDateTextView != null) {
            mDateTextView.setTypeface(TYPEFACE_DATE_DEFAULT, TYPEFACE_DATE_DEFAULT_STYLE);
        }
    }

    public void setLive(final boolean live) {
        mLive = live;
        mDigitalClock.setLive(live);
    }

    public void updateTime(final Time time) {
        mDigitalClock.updateTime(time);
    }

    public void setTypeface(final Typeface tf) {
        mTimeTextView.setTypeface(tf);
        mAmPmTextView.setTypeface(tf);
        if (mDateTextView != null) {
            mDateTextView.setTypeface(tf);
        }
    }

    public void refresh() {
        mDigitalClock.updateTime();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        // TODO Auto-generated method stub
        super.onWindowVisibilityChanged(visibility);
        if (DEVELOPER_MODE) {
            Log.d(TAG, "onWindowVisibilityChanged(visibility)" + "\n    visibility: " + visibility);
        }
    }
}
