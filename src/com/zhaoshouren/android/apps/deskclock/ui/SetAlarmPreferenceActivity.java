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
import static com.zhaoshouren.android.apps.deskclock.DeskClock.TAG;
import static com.zhaoshouren.android.apps.deskclock.utils.Alarm.INVALID_ID;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

import com.zhaoshouren.android.apps.deskclock.R;
import com.zhaoshouren.android.apps.deskclock.utils.Alarm;
import com.zhaoshouren.android.apps.deskclock.utils.AlarmRingtonePreference;
import com.zhaoshouren.android.apps.deskclock.providers.AlarmContract;
import com.zhaoshouren.android.apps.deskclock.providers.AlarmContract.Toaster;
import com.zhaoshouren.android.apps.deskclock.utils.SelectedDays;
import com.zhaoshouren.android.apps.deskclock.utils.SelectedDaysPreference;

/**
 * Manages each alarm
 */
/**
 * @author Warren Chu
 * 
 */
public class SetAlarmPreferenceActivity extends PreferenceActivity implements
        TimePickerDialog.OnTimeSetListener, Preference.OnPreferenceChangeListener {

    // Alarm.NO_ALARM_ID = -1
    public static final int GET_NEXT_ALARM = -2;

    private static final Handler sHandler = new Handler();

    private EditTextPreference mLabelPreference;
    private CheckBoxPreference mEnabledPreference;
    private Preference mTimePreference;
    private AlarmRingtonePreference mRingtonePreference;
    private CheckBoxPreference mVibratePreference;
    private SelectedDaysPreference mSelectedDaysPreference;

    private Alarm mAlarm;

    /**
     * Set an alarm.
     */
    @Override
    protected void onCreate(final Bundle icicle) {
        super.onCreate(icicle);

        // Override the default content view.
        setContentView(R.layout.set_alarm_preference);

        addPreferencesFromResource(R.xml.alarm_preferences);

        // Get each preference so we can retrieve the value later.
        mLabelPreference = (EditTextPreference) findPreference("label");
        mEnabledPreference = (CheckBoxPreference) findPreference("enabled");
        mTimePreference = findPreference("time");
        mRingtonePreference = (AlarmRingtonePreference) findPreference("ringtone");
        mVibratePreference = (CheckBoxPreference) findPreference("vibrate");
        mSelectedDaysPreference = (SelectedDaysPreference) findPreference("selected_days");

        // Configure OnPreferenceChangeListeners
        mLabelPreference.setOnPreferenceChangeListener(this);
        mEnabledPreference.setOnPreferenceChangeListener(this);
        mRingtonePreference.setOnPreferenceChangeListener(this);
        mVibratePreference.setOnPreferenceChangeListener(this);
        mSelectedDaysPreference.setOnPreferenceChangeListener(this);

        final Intent intent = getIntent();
        final int alarmId = intent.getIntExtra(Alarm.KEY_ID, INVALID_ID);
        final byte[] rawData = intent.getByteArrayExtra(Alarm.KEY_RAW_DATA);
        if (DEVELOPER_MODE) {
            Log.d(TAG,
                    "SetAlarmPreferenceActivity.onCreate(): Alarm["
                            + alarmId
                            + (!TextUtils.isEmpty(mLabelPreference.getText()) ? "|"
                                    + mLabelPreference.getText() : "") + "]");
        }

        // Create an Alarm
        switch (alarmId) {
        case INVALID_ID:
            mAlarm = new Alarm(this);
            break;
        case GET_NEXT_ALARM:
            mAlarm = AlarmContract.getNextEnabledAlarm(this);
            if (mAlarm == null) {
                Log.e(TAG,
                        "SetAlarmPeferenceActivity.onCreate(): failed to get an Alarm from Alarms.getNextAlarm(Context)");
                finish();
                return;
            }
            break;
        default:
            if (rawData != null) {
                mAlarm = new Alarm(this, rawData);
                if (mAlarm == null) {
                    Log.e(TAG,
                            "SetAlarmPeferenceActivity.onCreate(): failed to get an Alarm from Alarm.createFromRawData(Context, byte[])");
                } else {
                    break;
                }
            }

            mAlarm = AlarmContract.getAlarm(this, alarmId);
            if (mAlarm == null) {
                Log.e(TAG,
                        "SetAlarmPeferenceActivity.onCreate(): failed to get an Alarm from Alarm.createFromRawData(Context, byte[])");
                finish();
                return;
            }
        }
        setPreferences();

        // We have to do this to get the save/cancel buttons to highlight on
        // their own.
        getListView().setItemsCanFocus(true);

        // Done Button (Save)
        ((Button) findViewById(R.id.alarm_save)).setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                Toaster.popAlarmToast(SetAlarmPreferenceActivity.this, mAlarm);
                saveAlarm();
                finish();
            }
        });

        // Revert Button
        final Button cancelButton = (Button) findViewById(R.id.alarm_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                finish();
            }
        });

        // Delete Button
        final Button deleteButton = (Button) findViewById(R.id.alarm_delete);
        if (alarmId == INVALID_ID) {
            deleteButton.setVisibility(View.GONE);
        } else {
            deleteButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(final View v) {
                    deleteAlarm();
                }
            });
        }

        // New Alarm, so show Time Picker Dialog
        if (alarmId == INVALID_ID) {
            showTimePicker(true);
        }
    }

    public boolean onPreferenceChange(final Preference preference, final Object newValue) {
        if (preference == mLabelPreference) {
            setLabelPreference(((String) newValue).trim());
        }
        return true;
    }

    /**
     * Set Preferences using values stored in mAlarm
     */
    private void setPreferences() {
        mTimePreference.setTitle(mAlarm.formattedTimeAmPm);
        setLabelPreference(mAlarm.label);
        mSelectedDaysPreference.setSelectedDays(mAlarm.selectedDays);
        mRingtonePreference.setAlert(mAlarm.ringtoneUri);
        mVibratePreference.setChecked(mAlarm.vibrate);
        mEnabledPreference.setChecked(mAlarm.enabled);
    }

    private void setLabelPreference(final String label) {
        mLabelPreference.setText(label);
        if (!TextUtils.isEmpty(label)) {
            mLabelPreference.setTitle(label);
        } else {
            mLabelPreference.setTitle(getString(android.R.string.untitled));
        }
    }

    @Override
    public boolean onPreferenceTreeClick(final PreferenceScreen preferenceScreen,
            final Preference preference) {
        if (preference == mTimePreference) {
            showTimePicker(false);
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
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
                            SetAlarmPreferenceActivity.this.finish();
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
                            SetAlarmPreferenceActivity.this.finish();
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

            mTimePreference.setTitle(mAlarm.formattedTimeAmPm);
        }
    }

    private void saveAlarm() {
        mAlarm.enabled = mEnabledPreference.isChecked();
        mAlarm.selectedDays = new SelectedDays(mSelectedDaysPreference.getSelectedDays());
        mAlarm.vibrate = mVibratePreference.isChecked();
        mAlarm.label = mLabelPreference.getText();
        mAlarm.ringtoneUri = mRingtonePreference.getAlert();
        mAlarm.normalize(true);

        sHandler.post(new Runnable() {
            public void run() {
                AlarmContract.saveAlarm(SetAlarmPreferenceActivity.this, mAlarm);
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
                                AlarmContract.deleteAlarm(SetAlarmPreferenceActivity.this, mAlarm.id);
                            }
                        });
                        finish();
                    }
                }).setNegativeButton(android.R.string.cancel, null).show();
    }
}
