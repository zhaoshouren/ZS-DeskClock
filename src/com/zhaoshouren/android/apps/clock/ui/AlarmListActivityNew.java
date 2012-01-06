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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.zhaoshouren.android.apps.clock.R;
import com.zhaoshouren.android.apps.clock.provider.AlarmContract;
import com.zhaoshouren.android.apps.clock.util.Action;
import com.zhaoshouren.android.apps.clock.util.Alarm;
import com.zhaoshouren.android.apps.clock.util.FormattedTime;
import com.zhaoshouren.android.apps.clock.util.Toaster;

public class AlarmListActivityNew extends FragmentActivity implements OnItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private class AlarmTimeAdapter extends CursorAdapter {

        private class Views {
            private ToggleButton alarmToggleButton;
            private CheckBox alarmCheckBox;
            private TextView timeTextView;
            private TextView amPmTextView;
            private TextView daysTextView;
            private TextView labelTextView;
        }

        private final LayoutInflater mLayoutInflater;

        public AlarmTimeAdapter(final Context context, final LayoutInflater layoutInflater,
                final Cursor cursor, final int flags) {
            super(context, cursor, flags);
            mLayoutInflater = layoutInflater;
        }

        @Override
        public void bindView(final View view, final Context context, final Cursor cursor) {
            final Alarm alarm = new Alarm(context, cursor);
            final Views views = (Views) view.getTag();

            // Set toggle state and assign OnClickListener
            views.alarmCheckBox.setChecked(alarm.enabled);
            // views.alarmToggleButton.setChecked(alarm.enabled);
            // views.alarmToggleButton.setOnClickListener(new OnClickListener() {
            // @Override
            // public void onClick(final View v) {
            // toggleAlarm(alarm);
            // }
            // });

            // Set the time
            views.timeTextView.setText(alarm.formattedTime);
            views.amPmTextView.setText(alarm.formattedAmPm);

            // Set the selected days
            views.daysTextView.setText(alarm.days.toString(AlarmListActivityNew.this));

            // Set the label
            views.labelTextView.setText(alarm.label);

            // Set visibility
            views.amPmTextView.setVisibility(TextUtils.isEmpty(alarm.formattedAmPm) ? View.GONE
                    : View.VISIBLE);
            // views.labelTextView.setVisibility(TextUtils.isEmpty(alarm.label) ? View.GONE
            // : View.VISIBLE);
            views.daysTextView.setVisibility(alarm.days.toInt() == 0 ? View.GONE : View.VISIBLE);
        }

        @Override
        public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
            final View view = mLayoutInflater.inflate(R.layout.alarm_time, parent, false);
            final Views views = new Views();

            // views.alarmToggleButton = (ToggleButton) view.findViewById(R.id.alarm_on_off);
            views.alarmCheckBox = (CheckBox) view.findViewById(R.id.alarm_on_off);
            views.timeTextView = (TextView) view.findViewById(R.id.time);
            views.amPmTextView = (TextView) view.findViewById(R.id.am_pm);
            views.daysTextView = (TextView) view.findViewById(R.id.days);
            views.labelTextView = (TextView) view.findViewById(R.id.label);

            view.setTag(views);

            return view;
        }
    }

    private static final String TAG = "ZS.AlarmListActivity";

    private static AlarmTimeAdapter sAlarmTimeAdapter;
    private static ListView sAlarmsListView;
    private static LoaderManager sLoaderManager;
    private static LayoutInflater sLayoutInflater;;

    private void addNewAlarm() {
        startActivity(new Intent(Action.SET_ALARM));
    }

    @Override
    public boolean onContextItemSelected(final MenuItem menuItem) {
        final AdapterContextMenuInfo adapterContextMenuInfo =
                (AdapterContextMenuInfo) menuItem.getMenuInfo();
        final int alarmId = (int) adapterContextMenuInfo.id;

        if (DEVELOPER_MODE) {
            Log.d(TAG, "onContextItemSelected()" + "\n     alarmId: " + alarmId);
        }

        switch (menuItem.getItemId()) {
        case R.id.delete_alarm:
            new AlertDialog.Builder(this).setTitle(getString(R.string.delete_alarm))
                    .setMessage(getString(R.string.delete_alarm_confirm))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface d, final int w) {
                            AlarmContract.deleteAlarm(AlarmListActivityNew.this, alarmId);
                        }
                    }).setNegativeButton(android.R.string.cancel, null).show();
            return true;
        case R.id.enable_alarm:
            toggleAlarm(new Alarm(this, (Cursor) sAlarmsListView.getAdapter().getItem(
                    adapterContextMenuInfo.position)));
            return true;
        case R.id.edit_alarm:
            startActivity(new Intent(Action.SET_ALARM).putExtra(Alarm.Keys.ID, alarmId));
            return true;
        default:
            break;
        }
        return super.onContextItemSelected(menuItem);
    }

    @Override
    protected void onCreate(final Bundle icicle) {
        super.onCreate(icicle);

        sLayoutInflater = getLayoutInflater();
        sLoaderManager = getSupportLoaderManager();
        sAlarmTimeAdapter = new AlarmTimeAdapter(this, sLayoutInflater, null, 0);

        sLoaderManager.initLoader(0, icicle, this);

        setContentView(R.layout.alarm_list);
        sAlarmsListView = (ListView) findViewById(R.id.alarm_list);
        sAlarmsListView.setAdapter(sAlarmTimeAdapter);
        sAlarmsListView.setVerticalScrollBarEnabled(true);
        sAlarmsListView.setOnItemClickListener(this);
        sAlarmsListView.setOnCreateContextMenuListener(this);

        findViewById(R.id.add_alarm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                addNewAlarm();
            }
        });
        // // Make the entire view selected when focused.
        // addAlarmView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
        // @Override
        // public void onFocusChange(final View view, final boolean hasFocus) {
        // view.setSelected(hasFocus);
        // }
        // });

        findViewById(R.id.desk_clock_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                startActivity(new Intent(Action.HOME));
            }
        });
    }

    @Override
    public void onCreateContextMenu(final ContextMenu contextMenu, final View view,
            final ContextMenuInfo contextMenuInfo) {
        // Inflate the menu from xml.
        getMenuInflater().inflate(R.menu.context_menu, contextMenu);

        final Alarm alarm =
                new Alarm(this, (Cursor) sAlarmsListView.getAdapter().getItem(
                        ((AdapterContextMenuInfo) contextMenuInfo).position));

        // Inflate the custom view and set each TextView's text.
        final View contextMenuHeaderView =
                sLayoutInflater.inflate(R.layout.context_menu_header, null);

        final boolean isLabelEmpty = TextUtils.isEmpty(alarm.label);
        final boolean is24HourFormat = DateFormat.is24HourFormat(this);
        ((TextView) contextMenuHeaderView.findViewById(R.id.header_time)).setText(isLabelEmpty
                ? alarm.format(this.getString(is24HourFormat
                        ? FormattedTime.FORMAT_WEEKDAY_HOUR_MINUTE_24
                        : FormattedTime.FORMAT_WEEKDAY_HOUR_MINUTE_CAP_AM_PM)) : alarm.format(this
                        .getString(is24HourFormat
                                ? FormattedTime.FORMAT_ABBREV_WEEKDAY_HOUR_MINUTE_24
                                : FormattedTime.FORMAT_ABBREV_WEEKDAY_HOUR_MINUTE_CAP_AM_PM)));

        final TextView labelView = (TextView) contextMenuHeaderView.findViewById(R.id.header_label);
        if (isLabelEmpty) {
            labelView.setVisibility(View.GONE);
        } else {
            labelView.setText(alarm.label);
        }

        // Set the custom view on the menu.
        contextMenu.setHeaderView(contextMenuHeaderView);
        // Change the text based on the state of the alarm.
        if (alarm.enabled) {
            contextMenu.findItem(R.id.enable_alarm).setTitle(R.string.disable_alarm);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int loaderId, final Bundle icicle) {
        return AlarmContract.getAlarmsCursorLoader(this);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.alarm_list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sLoaderManager.destroyLoader(0);
        Toaster.cancelToast();
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int pos,
            final long id) {
        startActivity(new Intent(Action.SET_ALARM).putExtra(Alarm.Keys.ID, (int) id));
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> cursorLoader) {
        sAlarmTimeAdapter.swapCursor(null);
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> cursorLoader, final Cursor cursor) {
        sAlarmTimeAdapter.swapCursor(cursor);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem menuItem) {
        switch (menuItem.getItemId()) {
        case R.id.menu_item_settings:
            startActivity(new Intent(this, SettingsPreferenceActivity.class));
            return true;
        case R.id.menu_item_desk_clock:
            startActivity(new Intent(Action.HOME));
            return true;
        case R.id.menu_item_add_alarm:
            addNewAlarm();
            return true;
        default:
            break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        getSupportLoaderManager().restartLoader(0, null, this);
    }

    private void toggleAlarm(final Alarm alarm) {
        alarm.toggle();
        AlarmContract.saveAlarm(this, alarm);
        if (alarm.enabled) {
            Toaster.popAlarmToast(this, alarm);
        }
    }
}
