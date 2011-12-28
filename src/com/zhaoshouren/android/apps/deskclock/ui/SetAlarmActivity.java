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

import static com.zhaoshouren.android.apps.deskclock.DeskClock.DEVELOPER_MODE;
import static com.zhaoshouren.android.apps.deskclock.util.Alarm.INVALID_ID;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import com.zhaoshouren.android.apps.deskclock.R;
import com.zhaoshouren.android.apps.deskclock.provider.AlarmContract;
import com.zhaoshouren.android.apps.deskclock.util.Alarm;
import com.zhaoshouren.android.apps.deskclock.util.Toaster;

/**
 * Manages each alarm
 */
public class SetAlarmActivity extends FragmentActivity implements
        AlarmFragment.OnAlarmLoadFinishedListener, TimePickerDialog.OnTimeSetListener,
        SelectDaysDialogFragment.OnSelectDaysChangeListener, View.OnClickListener {

    // Alarm.NO_ALARM_ID = -1
    public static final int GET_NEXT_ALARM = -2;
    public static final String TAG = "ZS.SetAlarmActivity";

    private static final Handler sHandler = new Handler();

    private Alarm mAlarm;

    private TextView mTime;
    private TextView mDays;
    private EditText mLabel;
    private Spinner mRingtone;
    private ToggleButton mVibrate;
    private ToggleButton mEnabled;

    /*
     * (non-Javadoc)
     * 
     * @see android.app.TimePickerDialog.OnTimeSetListener#onTimeSet(android.widget.TimePicker, int,
     * int)
     */
    @Override
    public void onTimeSet(final TimePicker view, final int hour, final int minute) {
        if (hour != mAlarm.hour || minute != mAlarm.minute) {
            mAlarm.hour = hour;
            mAlarm.minute = minute;
            mAlarm.normalize(true);

            mTime.setText(mAlarm.formattedTimeAmPm);
        }
    }

    private void deleteAlarm() {
        new AlertDialog.Builder(this).setTitle(getString(R.string.delete_alarm))
                .setMessage(getString(R.string.delete_alarm_confirm))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface d, final int w) {
                        sHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                AlarmContract.deleteAlarm(SetAlarmActivity.this, mAlarm.id);
                            }
                        });
                        finish();
                    }
                }).setNegativeButton(android.R.string.cancel, null).show();
    }

    private void saveAlarm() {
        mAlarm.enabled = mEnabled.isChecked();
        mAlarm.days.selected = (Integer) mDays.getTag();
        mAlarm.vibrate = mVibrate.isChecked();
        mAlarm.label = mLabel.getText().toString();
        mAlarm.ringtoneUri = (Uri) mRingtone.getSelectedItem();
        mAlarm.normalize(true);

        sHandler.post(new Runnable() {
            @Override
            public void run() {
                AlarmContract.saveAlarm(SetAlarmActivity.this, mAlarm);
            }
        });

        finish();
    }

    private void setLabelPreference(final String label) {
        mLabel.setText(label);
        if (!TextUtils.isEmpty(label)) {
            mLabel.setText(label);
        } else {
            mLabel.setText(getString(android.R.string.untitled));
        }
    }

    /**
     * Set Preferences using values stored in alarm
     * 
     * @param alarm
     */
    private void setContent(final Alarm alarm) {
        mTime.setText(alarm.formattedTimeAmPm);
        setLabelPreference(alarm.label);
        mDays.setText(alarm.days.toString(this));
        mDays.setTag(alarm.days.toInt());
        // mRingtone.setSelection(getRingtoneIndex(alarm.ringtoneUri));
        mVibrate.setChecked(alarm.vibrate);
        mEnabled.setChecked(alarm.enabled);
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
                    @Override
                    public void onBackPressed() {
                        if (finishOnCancelOrBackPressed) {
                            SetAlarmActivity.this.finish();
                        }
                        super.onBackPressed();
                    };
                };
        // TimePickerDialog does not support Dialog.OnCancelListener so attach an
        // TimePickerDialog.OnClickListener on the cancel button
        timePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                getString(android.R.string.cancel), new TimePickerDialog.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        if (finishOnCancelOrBackPressed) {
                            SetAlarmActivity.this.finish();
                        }
                    }
                });

        timePickerDialog.show();
    }

    /**
     * Set an alarm.
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        final int id = intent.getIntExtra(Alarm.Keys.ID, INVALID_ID);
        final Alarm alarm = intent.getParcelableExtra(Alarm.Keys.PARCELABLE);
        final byte[] rawData = intent.getByteArrayExtra(Alarm.Keys.RAW_DATA);

        if (DEVELOPER_MODE) {
            Log.d(TAG, "onCreate()" + "\n   id = " + id);
        }

        setContentView(R.layout.set_alarm);

        mTime = (TextView) findViewById(R.id.time);
        mDays = (TextView) findViewById(R.id.days);
        // Select Days ImageButton
        findViewById(R.id.btnDays).setOnClickListener(this);

        mLabel = (EditText) findViewById(R.id.label);
        mRingtone = (Spinner) findViewById(R.id.ringtone);
        mVibrate = (ToggleButton) findViewById(R.id.vibrate);
        mEnabled = (ToggleButton) findViewById(R.id.enabled);

        // Save Button
        final Button saveButton = (Button) findViewById(R.id.alarm_save);
        saveButton.setEnabled(false);
        saveButton.setOnClickListener(this);

        // Cancel Button
        findViewById(R.id.alarm_cancel).setOnClickListener(this);

        // Delete Button
        final Button deleteButton = (Button) findViewById(R.id.alarm_delete);
        if (id == INVALID_ID) {
            deleteButton.setVisibility(View.GONE);
        } else {
            deleteButton.setEnabled(false);
            deleteButton.setOnClickListener(this);
        }

        if (id == INVALID_ID) {
            onAlarmLoadFinished(new Alarm(this));
        } else if (alarm != null || rawData != null) {
            onAlarmLoadFinished(alarm != null ? alarm : new Alarm(this, rawData));
        } else if (savedInstanceState == null) {
            FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(AlarmFragment.newInstance(id), AlarmFragment.TAG);
            fragmentTransaction.commit();
        }

    }

    protected void showSelectDaysDialog() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment previousDialog =
                getSupportFragmentManager().findFragmentByTag("select-days-dialog");
        if (previousDialog != null) {
            fragmentTransaction.remove(previousDialog);
        }
        fragmentTransaction.addToBackStack(null);

        DialogFragment newFragment = SelectDaysDialogFragment.newInstance(mAlarm.days.selected);
        newFragment.show(fragmentTransaction, "select-days-dialog");
    }

    @Override
    public void onSelectDaysChange(int days) {
        mAlarm.days.selected = days;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.alarm_save:
            Toaster.popAlarmToast(SetAlarmActivity.this, mAlarm);
            saveAlarm();
            break;
        case R.id.alarm_delete:
            deleteAlarm();
            break;
        case R.id.btnDays:
            showSelectDaysDialog();
        }
    }

    @Override
    public void onAlarmLoadFinished(final Alarm alarm) {
        mAlarm = alarm != null ? alarm : new Alarm(this);

        setContent(mAlarm);

        if (mAlarm.id == INVALID_ID) {
            showTimePicker(true);
        }
    }
}
