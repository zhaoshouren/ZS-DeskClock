/*
 * Copyright (C) 2011 Warren Chu
 * Copyright (C) 2008 The Android Open Source Project
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

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.RingtonePreference;
import android.util.AttributeSet;

import com.zhaoshouren.android.apps.deskclock.R;

/**
 * The RingtonePreference does not have a way to get/set the current ringtone so we override
 * onSaveRingtone and onRestoreRingtone to get the same behavior.
 */
public class AlarmRingtonePreference extends RingtonePreference {
    private Uri mAlertUri;

    public AlarmRingtonePreference(final Context context, final AttributeSet attributeset) {
        super(context, attributeset);
    }

    @Override
    protected void onSaveRingtone(final Uri ringtoneUri) {
        setAlert(ringtoneUri);
    }

    @Override
    protected Uri onRestoreRingtone() {
        return RingtoneManager.isDefault(mAlertUri) ? RingtoneManager.getActualDefaultRingtoneUri(
                getContext(), RingtoneManager.TYPE_ALARM) : mAlertUri;
    }

    public void setAlert(final Uri alertUri) {
        mAlertUri = alertUri;
        if (alertUri != null) {
            final Ringtone ringtone = RingtoneManager.getRingtone(getContext(), alertUri);
            if (ringtone != null) {
                setTitle(ringtone.getTitle(getContext()));
                return;
            }
        } 
        setTitle(R.string.silent_alarm_summary);
    }

    public Uri getAlert() {
        return mAlertUri;
    }
}
