/*
 * Copyright (C) 2011 Warren Chu
 * Copyright (C) 2009 The Android Open Source Project
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

package com.zhaoshouren.android.apps.clock.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.text.format.Time;

import com.zhaoshouren.android.apps.clock.R;
import com.zhaoshouren.android.apps.clock.provider.AlarmContract.Alarms;

public class Alarm extends FormattedTime implements Parcelable {
    public static final int INVALID_ID = -1;
    public static final int GET_NEXT_ALARM = -2;

    public static class Keys {

        public static final String ID = "Alarm.Id";
        /**
         * Pass to Intent.putExtra(...) when sending Parcel from Alarm.writeToParcel(Parcel)
         */
        public static final String PARCELABLE = "Alarm.Parcelable";
        /**
         * Pass to Intent.putExtra(...) when sending byte[] from Alarm.toRawData(); Send raw data
         * instead of Parcel in cases where the Intent extras are being inflated for manipulation by
         * a remote service which may trigger a ClassNotFoundException
         */
        public static final String RAW_DATA = "Alarm.RawData";
    }

    /**
     * Pass to Intent.putExtra(...) when sending Alarm.id.
     */
    public static final Parcelable.Creator<Alarm> CREATOR = new Parcelable.Creator<Alarm>() {
        @Override
        public Alarm createFromParcel(final Parcel parcel) {
            return Alarm.readFrom(null, parcel);
        }

        @Override
        public Alarm[] newArray(final int size) {
            return new Alarm[size];
        }
    };

    private static final String SILENT = "silent";

    public int id;
    public boolean enabled;
    public Days days = new Days();
    public boolean vibrate;
    public String label;
    public Uri ringtoneUri;

    public Alarm(final Alarm alarm) {
        super();
        set(alarm);
    }

    public Alarm() {
        super();
        setToNow();
        updateScheduledTime();

        id = INVALID_ID;
        second = 0;
        enabled = true;
        vibrate = true;
        label = "";
        ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
    }

    public Alarm(final Context context) {
        super(context);
        setToNow();
        updateScheduledTime();

        id = INVALID_ID;
        second = 0;
        enabled = true;
        vibrate = true;
        label = "";
        ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
    }

    public static Alarm getFrom(Context context, Cursor cursor) {
        Alarm alarm = null;
        if (cursor.moveToFirst()) {
            alarm = new Alarm(context, cursor);
        }
        return alarm;
    }

    private Alarm(final Context context, final Cursor cursor) {
        super(context);

        set(cursor.getLong(cursor.getColumnIndex(Alarms.TIME)));
        normalize(true);

        id = cursor.getInt(cursor.getColumnIndex(Alarms._ID));
        enabled = cursor.getInt(cursor.getColumnIndex(Alarms.ENABLED)) == 1;
        days = new Days(cursor.getInt(cursor.getColumnIndex(Alarms.SELECTED_DAYS)));
        vibrate = cursor.getInt(cursor.getColumnIndex(Alarms.VIBRATE)) == 1;
        label = cursor.getString(cursor.getColumnIndex(Alarms.LABEL));
        final String alert = cursor.getString(cursor.getColumnIndex(Alarms.RINGTONE_URI));

        if (alert == Alarm.SILENT) {
            ringtoneUri = Uri.EMPTY;
        } else {
            ringtoneUri =
                    TextUtils.isEmpty(alert) ? RingtoneManager
                            .getDefaultUri(RingtoneManager.TYPE_ALARM) : Uri.parse(alert);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Retrieve label for Alarm
     * 
     * @return
     */
    public String getTickerText(final Context context) {
        return TextUtils.isEmpty(label) ? context.getString(R.string.default_alarm_ticker_text)
                : label;
    }

    private static Alarm readFrom(final Context context, final Parcel parcel) {
        try {
            Alarm alarm = new Alarm(context);

            parcel.setDataPosition(0);

            alarm.id = parcel.readInt();
            alarm.enabled = parcel.readInt() == 1;
            alarm.days = new Days(parcel.readInt());
            alarm.vibrate = parcel.readInt() == 1;
            alarm.label = parcel.readString();
            alarm.ringtoneUri = (Uri) parcel.readParcelable(null);
            alarm.set(parcel.readLong());
            alarm.normalize(true);

            return alarm;
        } catch (Exception exception) {
            // TODO: log exception
        }

        return null;
    }

    public static Alarm readFrom(final Context context, final byte[] rawData) {
        final Parcel parcel = Parcel.obtain();

        try {
            parcel.unmarshall(rawData, 0, rawData.length);

            return readFrom(context, parcel);
        } catch (Exception exception) {
            // TODO: log exception
        }

        return null;
    }

    public void set(final Alarm alarm) {
        super.set(alarm);

        id = INVALID_ID;
        enabled = alarm.enabled;
        vibrate = alarm.vibrate;
        label = alarm.label;
        ringtoneUri = alarm.ringtoneUri;
        days.selected = alarm.days.selected;

        if (enabled) {
            updateScheduledTime();
        }
    }

    /**
     * Create ContentValues for Alarm
     * 
     * @return ContentValues representation of Alarm minus ID
     */
    public ContentValues writeToContentValues() {
        final ContentValues contentValues = new ContentValues(7);
        contentValues.put(Alarms.ENABLED, enabled);
        contentValues.put(Alarms.TIME, toMillis(true));
        contentValues.put(Alarms.SELECTED_DAYS, days.toInt());
        contentValues.put(Alarms.VIBRATE, vibrate);
        contentValues.put(Alarms.LABEL, label);
        contentValues.put(Alarms.RINGTONE_URI, ringtoneUri == null || ringtoneUri.equals(Uri.EMPTY)
                ? SILENT : ringtoneUri.toString());
        contentValues.put(Alarms.SORT, hour * 100 + minute);

        return contentValues;
    }

    /**
     * Convert Alarm to byte[] for passing with Intents, etc. Use toContentVAlues() for storing
     * alarms in persistent storage
     * 
     * @return byte[] representation of Alarm
     */
    public byte[] writeToRawData() {
        final Parcel parcel = Parcel.obtain();
        writeToParcel(parcel, 0);
        return parcel.marshall();
    }

    public void updateScheduledTime() {
        final Time time = new Time();
        time.setToNow();

        monthDay += days.getDaysTillNext(this);
        normalize(true);

        if (compare(this, time) <= 0) {
            month = time.month;
            monthDay = time.monthDay;
            year = time.year;
            normalize(true);

            if (compare(this, time) <= 0) {
                monthDay += 1;
                normalize(true);

                int daysTillNext = days.getDaysTillNext(this);

                if (daysTillNext >= 0) {
                    monthDay += daysTillNext;
                    normalize(true);
                }
            }
        }
    }

    @Override
    public void writeToParcel(final Parcel parcel, final int flag) {
        parcel.writeInt(id);
        parcel.writeInt(enabled ? 1 : 0);
        parcel.writeInt(days.toInt());
        parcel.writeInt(vibrate ? 1 : 0);
        parcel.writeString(label);
        parcel.writeParcelable(ringtoneUri, flag);
        parcel.writeLong(toMillis(true));
        parcel.setDataPosition(0);
    }

    public void toggle() {
        enabled = !enabled;
    }
}
