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
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.zhaoshouren.android.apps.clock.R;
import com.zhaoshouren.android.apps.clock.provider.AlarmContract;
import com.zhaoshouren.android.apps.clock.util.Action;
import com.zhaoshouren.android.apps.clock.util.Alarm;

public class AlarmListActivityNew extends FragmentActivity implements OnItemClickListener, OnItemLongClickListener, OnClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static class AlarmListAdapter extends CursorAdapter {

        private class Views {
            private CheckBox alarmCheckBox;
            private View alarmInfo;
            private TextView timeTextView;
            private TextView amPmTextView;
            private TextView daysTextView;
            private TextView labelTextView;
        }

        private static LayoutInflater sLayoutInflater;

        public AlarmListAdapter(final Context context, final LayoutInflater layoutInflater,
                final Cursor cursor, final int flags) {
            super(context, cursor, flags);
            sLayoutInflater = layoutInflater;
        }

        @Override
        public void bindView(final View view, final Context context, final Cursor cursor) {
            final Alarm alarm = Alarm.getFrom(context, cursor);
            final Views views = (Views) view.getTag();

            // Set toggle state and assign OnClickListener
            views.alarmCheckBox.setChecked(alarm.enabled);
            views.alarmCheckBox.setTag(alarm);
            views.alarmCheckBox.setOnClickListener((OnClickListener) context);

            // Set the time
            views.timeTextView.setText(alarm.formattedTime);
            views.amPmTextView.setText(alarm.formattedAmPm);

            // Set the selected days
            views.daysTextView.setText(alarm.days.toString(context));

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
        public View newView(final Context context, final Cursor cursor, final ViewGroup parent) 
        {
            final View view = sLayoutInflater.inflate(R.layout.alarm_list_item, parent, false);
            final Views views = new Views();

            views.alarmCheckBox = (CheckBox) view.findViewById(R.id.alarm_on_off);
            views.alarmInfo = view.findViewById(R.id.alarm_info);
            views.timeTextView = (TextView) view.findViewById(R.id.time);
            views.amPmTextView = (TextView) view.findViewById(R.id.am_pm);
            views.daysTextView = (TextView) view.findViewById(R.id.days);
            views.labelTextView = (TextView) view.findViewById(R.id.label);

            view.setTag(views);

            return view;
        }
    }

    public static final String TAG = "ZS.AlarmListActivity";

    private static AlarmListAdapter sAlarmListAdapter;
    private static ListView sAlarmsListView;
    private static LoaderManager sLoaderManager;
    private static LayoutInflater sLayoutInflater;;

    private void toggleAlarm(final Alarm alarm) {
        alarm.toggle();
        AlarmContract.updateAlarm(this, alarm);
        if (alarm.enabled) {
            alarm.showToast(this);
        }
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
            toggleAlarm(Alarm.getFrom(this, (Cursor) sAlarmsListView.getAdapter().getItem(
                    adapterContextMenuInfo.position)));
            return true;
        case R.id.edit_alarm:
            startActivity(new Intent(Action.SET_ALARM).putExtra(Alarm.Keys.ID, alarmId));
            return true;
        }
        return super.onContextItemSelected(menuItem);
    }

    @Override
    protected void onCreate(final Bundle icicle) {
        super.onCreate(icicle);

        sLayoutInflater = getLayoutInflater();
        sLoaderManager = getSupportLoaderManager();
        sAlarmListAdapter = new AlarmListAdapter(this, sLayoutInflater, null, 0);

        sLoaderManager.initLoader(0, icicle, this);

        setContentView(R.layout.alarm_list);
        sAlarmsListView = (ListView) findViewById(R.id.alarm_list);
        sAlarmsListView.setAdapter(sAlarmListAdapter);
        sAlarmsListView.setVerticalScrollBarEnabled(true);
        sAlarmsListView.setOnItemClickListener(this);
        sAlarmsListView.setOnCreateContextMenuListener(this);
        sAlarmsListView.setAddStatesFromChildren(true);
        
        findViewById(R.id.add_alarm).setOnClickListener(this);
        findViewById(R.id.desk_clock_button).setOnClickListener(this);
    }

    @Override
    public void onCreateContextMenu(final ContextMenu contextMenu, final View view,
            final ContextMenuInfo contextMenuInfo) {
        // Inflate the menu from xml.
        getMenuInflater().inflate(R.menu.context_menu, contextMenu);

        final Alarm alarm =
                Alarm.getFrom(this, (Cursor) sAlarmsListView.getAdapter().getItem(
                        ((AdapterContextMenuInfo) contextMenuInfo).position));

        // Inflate the custom view and set each TextView's text.
        final View contextMenuHeaderView =
                sLayoutInflater.inflate(R.layout.context_menu_header, null);

        final boolean isLabelEmpty = TextUtils.isEmpty(alarm.label);
        ((TextView) contextMenuHeaderView.findViewById(R.id.header_time)).setText(isLabelEmpty
                ? alarm.format(this.getString(alarm.is24HourFormat
                        ? Alarm.Format.WEEKDAY_HOUR_MINUTE_24
                        : Alarm.Format.WEEKDAY_HOUR_MINUTE_CAP_AM_PM)) : alarm.format(this
                        .getString(alarm.is24HourFormat
                                ? Alarm.Format.ABBREV_WEEKDAY_HOUR_MINUTE_24
                                : Alarm.Format.ABBREV_WEEKDAY_HOUR_MINUTE_CAP_AM_PM)));

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
        Alarm.cancelToast();
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> cursorLoader) {
        sAlarmListAdapter.swapCursor(null);
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> cursorLoader, final Cursor cursor) {
        sAlarmListAdapter.swapCursor(cursor);
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
            startActivity(new Intent(Action.SET_ALARM));
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        getSupportLoaderManager().restartLoader(0, null, this);
    }
    
    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int pos,
            final long id) {
        startActivity(new Intent(Action.SET_ALARM).putExtra(Alarm.Keys.ID, (int) id));

    }
    
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.alarm_on_off:
            toggleAlarm((Alarm) v.getTag());
            break;
        case R.id.add_alarm:
            startActivity(new Intent(Action.SET_ALARM));
            break;
        case R.id.desk_clock_button:
            startActivity(new Intent(Action.HOME));
            break;
        }
    }    
}
