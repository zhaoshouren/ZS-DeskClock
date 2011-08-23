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

package com.zhaoshouren.android.apps.deskclock.ui.widget;

import static com.zhaoshouren.android.apps.deskclock.DeskClock.DEVELOPER_MODE;
import static com.zhaoshouren.android.apps.deskclock.DeskClock.TAG;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.text.TextUtils;
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
    private final boolean mIsInEditMode = isInEditMode();

    private final DigitalClock mDigitalClock;

    public DigitalClockLayout(final Context context) {
        this(context, null);
    }
    
    // TODO: refactor to leverage Attibuteset for configuration/customization
    public DigitalClockLayout(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        
        if (!mIsInEditMode) {
            mDigitalClock = new DigitalClock(context) {
                @Override
                protected void updateTimeView(final FormattedTime formattedTime) {
                    if (isShown()) {
                        if (mTimeTextView != null) {
                            mTimeTextView.setText(formattedTime.formattedTime);
                        }
    
                        if (mTimeTextView != null) {
                            if (!TextUtils.isEmpty(formattedTime.formattedAmPm)) {
                                mAmPmTextView.setText(formattedTime.formattedAmPm);
                                mAmPmTextView.setVisibility(VISIBLE);
                            } else {
                                mAmPmTextView.setVisibility(GONE);
                            }
                        }
                    }
                }
    
                @Override
                protected void updateDateView(final FormattedTime formattedTime) {
                    if (isShown()) {
                        if (mDateTextView != null) {
                            mDateTextView.setText(formattedTime.formattedDate);
                        }
                    }
                }
            };     
        } else {
            mDigitalClock = null;
        }         
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // TODO Auto-generated method stub
    }
    
    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        // TODO Auto-generated method stub
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (DEVELOPER_MODE) {
            Log.d(TAG, "DigitalClockLayout.onAttachedToWindow(): id: " + getId());
        }

        if (mAttached) {
            return;
        }
        mAttached = true;
        
        if (!mIsInEditMode) {
            if (mLive) {
                mDigitalClock.setLive(true);
            }
    
            mDigitalClock.updateTime();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (DEVELOPER_MODE) {
            Log.d(TAG, "DigitalClockLayout.onDetachedFromWindow(): id: " + getId());
        }

        if (!mAttached) {
            return;
        }
        mAttached = false;
        
        if (mLive && !mIsInEditMode) {
            mDigitalClock.setLive(false);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mTimeTextView = (TextView) findViewById(VIEW_ID_TIME);
        if (mTimeTextView != null) {
            mTimeTextView.setTypeface(TYPEFACE_TIME_DEFAULT, TYPEFACE_TIME_DEFAULT_STYLE);
            if (mIsInEditMode) {
                mTimeTextView.setText("7:00");
            }
        }
        mAmPmTextView = (TextView) findViewById(VIEW_ID_AM_PM);
        if (mAmPmTextView != null) {            
             mAmPmTextView.setTypeface(TYPEFACE_TIME_DEFAULT, TYPEFACE_TIME_DEFAULT_STYLE);
             if (mIsInEditMode) {
                 mAmPmTextView.setText("PM");
             }
        }
        mDateTextView = (TextView) findViewById(VIEW_ID_DATE);
        if (mDateTextView != null) {
            mDateTextView.setTypeface(TYPEFACE_DATE_DEFAULT, TYPEFACE_DATE_DEFAULT_STYLE);
            if (mIsInEditMode) {
                mDateTextView.setText("Monday, July 11");
            }
        }
    }

    public void setLive(final boolean live) {
        mLive = live;
        mDigitalClock.setLive(live);
    }

    public void updateTime(final Time time) {
        mDigitalClock.updateTime(time);
    }

    public void setTypeface(final Typeface typeface, final int style) {
        setTimeTypeface(typeface, style);
        setDateTypeface(typeface, style);
    }
    
    public void setTimeTypeface(final Typeface typeface, final int style) {
        if (mTimeTextView != null) {
            mTimeTextView.setTypeface(typeface, style);
        }
        if (mAmPmTextView != null) {
            mAmPmTextView.setTypeface(typeface, style);
        }
    }
    
    public void setDateTypeface(final Typeface typeface, final int style) {
        if (mDateTextView != null) {
            mDateTextView.setTypeface(typeface, style);
        }
    }
    
    public void setTypeface(final Typeface typeface) {
        setTimeTypeface(typeface);
        setDateTypeface(typeface);
    }
    
    public void setTimeTypeface(final Typeface typeface) {
        if (mTimeTextView != null) {
            mTimeTextView.setTypeface(typeface);
        }
        if (mAmPmTextView != null) {
            mAmPmTextView.setTypeface(typeface);
        }
    }
    
    public void setDateTypeface(final Typeface typeface) {
        if (mDateTextView != null) {
            mDateTextView.setTypeface(typeface);
        }
    }
    
    public void refresh() {
        mDigitalClock.updateTime();
    }
}
