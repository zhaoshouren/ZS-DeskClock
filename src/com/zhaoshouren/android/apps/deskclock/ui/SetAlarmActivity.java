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
import static com.zhaoshouren.android.apps.deskclock.util.Alarm.INVALID_ID;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import com.zhaoshouren.android.apps.deskclock.R;
import com.zhaoshouren.android.apps.deskclock.provider.AlarmContract;
import com.zhaoshouren.android.apps.deskclock.provider.AlarmContract.Toaster;
import com.zhaoshouren.android.apps.deskclock.util.Alarm;
import com.zhaoshouren.android.apps.deskclock.util.Days;

/**
 * Manages each alarm
 */
public class SetAlarmActivity extends FragmentActivity implements
        TimePickerDialog.OnTimeSetListener, LoaderManager.LoaderCallbacks<Cursor> {

    // Alarm.NO_ALARM_ID = -1
    public static final int GET_NEXT_ALARM = -2;

    private static final Handler sHandler = new Handler();

    private Alarm mAlarm;
//    private EditTextPreference mLabelPreference;
//    private CheckBoxPreference mEnabledPreference;
//    private Preference mTimePreference;
//    private AlarmRingtonePreference mRingtonePreference;
//    private CheckBoxPreference mVibratePreference;
//    private DaysPreference mDaysPreference;
    
    private TextView mTime;
    private TextView mDays;
    private ImageButton mDaysButton;
    private EditText mLabel;
    private Spinner mRingtone;
    private ToggleButton mVibrate;
    private ToggleButton mEnabled;
    
    
    private Button mSaveButton;
    private Button mDeleteButton;

    private LoaderManager mLoaderManager;
    

    /**
     * Set an alarm.
     */
    @Override
    protected void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        
        mLoaderManager = getSupportLoaderManager();

        // Override the default content view.
        setContentView(R.layout.set_alarm);
                
        mTime = (TextView) findViewById(R.id.time);
        mDays = (TextView) findViewById(R.id.days);
        mDaysButton = (ImageButton) findViewById(R.id.btnDays);
        mLabel = (EditText) findViewById(R.id.label);
        mRingtone = (Spinner) findViewById(R.id.ringtone);
        mVibrate = (ToggleButton) findViewById(R.id.vibrate);
        mEnabled = (ToggleButton) findViewById(R.id.enabled);
        

        final Intent intent = getIntent();
        final int alarmId = intent.getIntExtra(Alarm.KEY_ID, INVALID_ID);
        
        // Save Button
        mSaveButton = (Button) findViewById(R.id.alarm_save);
        mSaveButton.setEnabled(false);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                Toaster.popAlarmToast(SetAlarmActivity.this, mAlarm);
                saveAlarm();
                finish();
            }
        });

        // Cancel Button
        final Button cancelButton = (Button) findViewById(R.id.alarm_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                finish();
            }
        });

        // Delete Button
        mDeleteButton = (Button) findViewById(R.id.alarm_delete);
        if (alarmId == INVALID_ID) {
            mDeleteButton.setVisibility(View.GONE);
        } else {
            mDeleteButton.setEnabled(false);
            mDeleteButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(final View v) {
                    deleteAlarm();
                }
            });
        }
        
        // get alarm or initialize loader
        switch (alarmId) {
        case INVALID_ID:
            mAlarm = new Alarm(this);
            break;
        case GET_NEXT_ALARM:
            //mAlarm = AlarmContract.getNextEnabledAlarm(this);
            mLoaderManager.initLoader(GET_NEXT_ALARM, null, this);
            return;
        default:
            final byte[] rawData = intent.getByteArrayExtra(Alarm.KEY_RAW_DATA);
            if (rawData != null) {
                mAlarm = new Alarm(this, rawData);
                if (mAlarm != null) {
                    break;                    
                } else {
                    Log.e(TAG,
                            "SetAlarmPeferenceActivity.onCreate(): failed to get an Alarm from Alarm.createFromRawData(Context, byte[])");
                    //
                    mLoaderManager.initLoader(alarmId, null, this);
                    return;
                }
            }
            
        }
        setPreferences(mAlarm);
                
        // New Alarm, so show Time Picker Dialog
        if (alarmId == INVALID_ID) {
            showTimePicker(true);
        }   
    }

    /**
     * Set Preferences using values stored in alarm
     * @param alarm
     */
    private void setPreferences(final Alarm alarm) {
        if (alarm != null) {
            mTime.setText(alarm.formattedTimeAmPm);
            setLabelPreference(alarm.label);
            mDays.setText(alarm.days.toString(this));
            mDays.setTag(alarm.days.toInt());
            mRingtone.setSelection(getRingtoneIndex(alarm.ringtoneUri));
            mVibrate.setChecked(alarm.vibrate);
            mEnabled.setChecked(alarm.enabled);
        }
    }

    private void setLabelPreference(final String label) {
        mLabel.setText(label);
        if (!TextUtils.isEmpty(label)) {
            mLabel.setText(label);
        } else {
            mLabel.setText(getString(android.R.string.untitled));
        }
    }
    
    private int getRingtoneIndex(final Uri ringtone) {
        //TODO: configure adapter
        return 0;
    }

    /**
     * Display a Time Picker Dialog
     * 
     * @param finishOnCancelOrBackPressed
     *            call finish() when cancel or back button is pressed rather than closing dialog
     */
    private void showTimePicker(final boolean finishOnCancelOrBackPressed) {
        final TimePickerDialog timePickerDialog =
                new TimePickerDialog(this, this, mAlarm.hour, mAlarm.minute,
                        DateFormat.is24HourFormat(this)) {
                    public void onBackPressed() {
                        if (finishOnCancelOrBackPressed) {
                            SetAlarmActivity.this.finish();
                        }
                        super.onBackPressed();
                    };
                };
        // TimePickerDialog does not support Dialog.OnCancelListener so attach an
        // TimePickerDialog.OnClickListener on the cancel button
        timePickerDialog.setButton(TimePickerDialog.BUTTON_NEGATIVE,
                getString(android.R.string.cancel), new TimePickerDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (finishOnCancelOrBackPressed) {
                            SetAlarmActivity.this.finish();
                        }
                    }
                });

        timePickerDialog.show();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.TimePickerDialog.OnTimeSetListener#onTimeSet(android.widget.TimePicker, int,
     * int)
     */
    public void onTimeSet(final TimePicker view, final int hour, final int minute) {
        if (hour != mAlarm.hour || minute != mAlarm.minute) {
            mAlarm.hour = hour;
            mAlarm.minute = minute;
            mAlarm.normalize(true);

            mTime.setText(mAlarm.formattedTimeAmPm);
        }
    }

    private void saveAlarm() {
        mAlarm.enabled = mEnabled.isChecked();
        mAlarm.days.selected = (Integer) mDays.getTag();
        mAlarm.vibrate = mVibrate.isChecked();
        mAlarm.label = mLabel.getText().toString();
        mAlarm.ringtoneUri = (Uri) mRingtone.getSelectedItem();
        mAlarm.normalize(true);

        sHandler.post(new Runnable() {
            public void run() {
                AlarmContract.saveAlarm(SetAlarmActivity.this, mAlarm);
            }
        });
    }

    private void deleteAlarm() {
        new AlertDialog.Builder(this).setTitle(getString(R.string.delete_alarm))
                .setMessage(getString(R.string.delete_alarm_confirm))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface d, final int w) {
                        sHandler.post(new Runnable() {
                            public void run() {
                                AlarmContract.deleteAlarm(SetAlarmActivity.this, mAlarm.id);
                            }
                        });
                        finish();
                    }
                }).setNegativeButton(android.R.string.cancel, null).show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle icicle) {
        if (id == GET_NEXT_ALARM) {
            return AlarmContract.getEnabledAlarmsCursorLoader(this);
        } else if (id >= 0){
            return AlarmContract.getAlarmCursorLoader(this, id);
        }
        return null;
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> cursorLoader, final Cursor cursor) {
        mAlarm = AlarmContract.getAlarm(this, cursor);
        if (mAlarm == null) {
            
        }
        setPreferences(mAlarm);
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> cursorLoader) {}
}
