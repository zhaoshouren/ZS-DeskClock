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

package com.zhaoshouren.android.apps.clock.ui;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.ListPreference;
import android.util.AttributeSet;

import com.zhaoshouren.android.apps.clock.util.Days;
import com.zhaoshouren.android.apps.clock.R;

public class DaysPreference extends ListPreference {

    public Days days = new Days();

    public DaysPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        setEntries(Days.getDays(false));
        setEntryValues(Days.CALENDAR_DAY_STRING_VALUES);
    }

    @Override
    protected void onDialogClosed(final boolean positiveResult) {
        if (positiveResult) {
            setTitle(getTitle(getContext()));
            callChangeListener(days);
        }
    }

    private String getTitle(final Context context) {
        return days.getCount() > 0 ? days.toString(context) : context.getText(R.string.never).toString();
    }

    @Override
    protected void onPrepareDialogBuilder(final Builder builder) {
        builder.setMultiChoiceItems(getEntries(), days.toBooleanArray(),
                new DialogInterface.OnMultiChoiceClickListener() {
                    public void onClick(final DialogInterface dialog, final int which,
                            final boolean isChecked) {
                        days.set(Days.DAY_VALUES[which], isChecked);
                    }
                });
    }

    public void setDays(int selectedDays) {
        days.selected = selectedDays;
        setTitle(getTitle(getContext()));
    }

    public Days getDays() {
        return days;
    }
}
