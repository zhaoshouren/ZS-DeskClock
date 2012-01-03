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

package com.zhaoshouren.android.apps.clock.ui;

import static com.zhaoshouren.android.apps.clock.DeskClock.DEVELOPER_MODE;
import static com.zhaoshouren.android.apps.clock.util.Alarm.INVALID_ID;

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

import com.zhaoshouren.android.apps.clock.R;
import com.zhaoshouren.android.apps.clock.provider.AlarmContract;
import com.zhaoshouren.android.apps.clock.util.Alarm;
import com.zhaoshouren.android.apps.clock.util.Alarm.Keys;
import com.zhaoshouren.android.apps.clock.util.Days;
import com.zhaoshouren.android.apps.clock.util.Toaster;

/**
 * Manages each alarm
 */
public class SetAlarmActivity extends FragmentActivity implements
        AlarmFragment.OnAlarmLoadFinishedListener, TimePickerDialog.OnTimeSetListener,
        SelectDaysDialogFragment.OnSelectDaysChangeListener, View.OnClickListener {

    private static final String TAG = "ZS.SetAlarmActivity";
    private static final String TAG_SELECT_DAYS_DIALOG_FRAGMENT = "ZS.SelectDaysDialogFragment";

    private Alarm mAlarm;

    private TextView mTime;
    private TextView mDays;
    private EditText mLabel;
    private Spinner mRingtone;
    private ToggleButton mVibrate;
    private ToggleButton mEnabled;
    private Button mSave;

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
            mSave.setEnabled(true);
        }
    }

    private void deleteAlarm() {
        new AlertDialog.Builder(this).setTitle(getString(R.string.delete_alarm))
                .setMessage(getString(R.string.delete_alarm_confirm))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface d, final int w) {
                        new Handler().post(new Runnable() {
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

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                AlarmContract.saveAlarm(SetAlarmActivity.this, mAlarm);
            }
        });

        finish();
    }

    /**
     * Update views using values stored in mAlarm
     */
    private void updateViews() {
        // Time
        mTime.setText(mAlarm.formattedTimeAmPm);

        // Repeat Schedule
        switch (mAlarm.days.selected) {
        case Days.NO_DAYS_SELECTED:
            mDays.setText(R.string.repeat_never);
            break;
        case Days.EVERY_DAY_SELECTED:
            mDays.setText(R.string.repeat_every_day);
            break;
        default:
            mDays.setText(mAlarm.days.toString(this));
        }
        mDays.setTag(mAlarm.days.toInt());

        // Label
        final String label = mAlarm.label;
        if (!TextUtils.isEmpty(label)) {
            mLabel.setText(label);
        } else {
            mLabel.setHint(getString(android.R.string.untitled));
        }

        // Ringtone
        // mRingtone.setSelection(getRingtoneIndex(alarm.ringtoneUri));

        // Vibrate
        mVibrate.setChecked(mAlarm.vibrate);

        // Enabled
        mEnabled.setChecked(mAlarm.enabled);
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
        final int id = intent.getIntExtra(Keys.ID, INVALID_ID);
        final Alarm alarm = intent.getParcelableExtra(Keys.PARCELABLE);
        final byte[] rawData = intent.getByteArrayExtra(Keys.RAW_DATA);

        if (DEVELOPER_MODE) {
            Log.d(TAG, "onCreate()" + "\n   id = " + id);
        }

        setContentView(R.layout.set_alarm);

        mTime = (TextView) findViewById(R.id.time);
        mTime.setOnClickListener(this);
        findViewById(R.id.btn_time).setOnClickListener(this);

        mDays = (TextView) findViewById(R.id.days);
        mDays.setOnClickListener(this);
        // Select Days ImageButton
        findViewById(R.id.btn_days).setOnClickListener(this);

        mLabel = (EditText) findViewById(R.id.label);
        mRingtone = (Spinner) findViewById(R.id.ringtone);
        mVibrate = (ToggleButton) findViewById(R.id.btn_vibrate);
        mVibrate.setOnClickListener(this);
        findViewById(R.id.enabled).setOnClickListener(this);
        mEnabled = (ToggleButton) findViewById(R.id.btn_enabled);
        mEnabled.setOnClickListener(this);
        findViewById(R.id.vibrate).setOnClickListener(this);

        // Save Button
        mSave = (Button) findViewById(R.id.alarm_save);
        mSave.setOnClickListener(this);

        // Cancel Button
        findViewById(R.id.alarm_cancel).setOnClickListener(this);

        // Delete Button
        final Button deleteButton = (Button) findViewById(R.id.alarm_delete);
        deleteButton.setOnClickListener(this);

        if (id == INVALID_ID) {
            deleteButton.setVisibility(View.GONE);
            onAlarmLoadFinished(new Alarm(this));
        } else if (alarm != null) {
            onAlarmLoadFinished(alarm);
        } else if (rawData != null) {
            onAlarmLoadFinished(new Alarm(this, rawData));
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
                getSupportFragmentManager().findFragmentByTag(TAG_SELECT_DAYS_DIALOG_FRAGMENT);
        if (previousDialog != null) {
            fragmentTransaction.remove(previousDialog);
        }
        fragmentTransaction.addToBackStack(null);

        DialogFragment newFragment = SelectDaysDialogFragment.newInstance(mAlarm.days.selected);

        newFragment.show(fragmentTransaction, TAG_SELECT_DAYS_DIALOG_FRAGMENT);
    }

    @Override
    public void onSelectDaysChange(int days) {
        mAlarm.days.selected = days;
        updateViews();
        mSave.setEnabled(true);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        // Save Button
        case R.id.alarm_save:
            Toaster.popAlarmToast(SetAlarmActivity.this, mAlarm);
            saveAlarm();
            finish();
            break;
        // Delete Button
        case R.id.alarm_delete:
            deleteAlarm();
            finish();
            break;
        // Repeat Schedule
        case R.id.btn_days:
        case R.id.days:
            showSelectDaysDialog();
            break;
        // Cancel Button
        case R.id.alarm_cancel:
            finish();
            break;
        // Time
        case R.id.btn_time:
        case R.id.time:
            showTimePicker(false);
            break;
        // Enabled
        case R.id.enabled:
            mEnabled.performClick();
        case R.id.btn_enabled:
            mSave.setEnabled(true);
            break;
        // Vibrate
        case R.id.vibrate:
            mVibrate.performClick();
        case R.id.btn_vibrate:
            mSave.setEnabled(true);
            break;
        }
    }

    @Override
    public void onAlarmLoadFinished(final Alarm alarm) {
        mAlarm = alarm;

        updateViews();
        mSave.setEnabled(false);

        if (mAlarm.id == INVALID_ID) {
            mTime.setText("");
            mAlarm.hour = 0;
            mAlarm.minute = 0;
            showTimePicker(true);
        }
    }
}
