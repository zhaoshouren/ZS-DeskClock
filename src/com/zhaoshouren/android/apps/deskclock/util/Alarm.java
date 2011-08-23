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

package com.zhaoshouren.android.apps.deskclock.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.text.format.Time;

import com.zhaoshouren.android.apps.deskclock.R;
import com.zhaoshouren.android.apps.deskclock.provider.AlarmDatabase.AlarmColumns;

public class Alarm extends FormattedTime implements Parcelable, AlarmColumns{
    public static final int INVALID_ID = -1;
    public static final String KEY_ID = "intent.extra.com.zhaoshouren.android.apps.deskclock.utils.Alarm.id";
    /**
     * Pass to Intent.putExtra(...) when sending Parcel from Alarm.writeToParcel(Parcel)
     */
    public static final String KEY_PARCEL =
            "intent.extra.com.zhaoshouren.android.apps.deskclock.utils.Alarm.Parcel";
    /**
     * Pass to Intent.putExtra(...) when sending byte[] from Alarm.toRawData(); Send raw data
     * instead of Parcel in cases where the Intent extras are being inflated for manipulation by a
     * remote service which may trigger a ClassNotFoundException
     */
    public static final String KEY_RAW_DATA =
            "intent.extra.com.zhaoshouren.android.apps.deskclock.utils.Alarm.raw_data";
    /**
     * Pass to Intent.putExtra(...) when sending Alarm.id.
     */
    public static final Parcelable.Creator<Alarm> CREATOR = new Parcelable.Creator<Alarm>() {
        public Alarm createFromParcel(final Parcel parcel) {
            return new Alarm(parcel);
        }

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

    // TODO refactor
    public Alarm(final Context context, final Cursor cursor) {
        super(context);
                
        set(cursor.getLong(INDEX_TIME));
        normalize(true);
        
        id = cursor.getInt(INDEX_ID);
        enabled = cursor.getInt(INDEX_ENABLED) == 1;
        days = new Days(cursor.getInt(INDEX_SELECTED_DAYS));
        vibrate = cursor.getInt(INDEX_VIBRATE) == 1;
        label = cursor.getString(INDEX_LABEL);
        final String alert = cursor.getString(INDEX_RINGTONE_URI);

        if (alert == Alarm.SILENT) {
            ringtoneUri = Uri.EMPTY;
        } else {
            ringtoneUri =
                    TextUtils.isEmpty(alert) ? RingtoneManager
                            .getDefaultUri(RingtoneManager.TYPE_ALARM) : Uri.parse(alert);
        }
    }
    
    public Alarm(final Alarm alarm) {
        super(); 
        set(alarm);
    }

    private Alarm(final Parcel parcel) {
        super();
        readFromParcel(parcel);
    }

    public Alarm(final Context context, final byte[] rawData) {
        super(context);
        
        final Parcel parcel = Parcel.obtain();
        parcel.unmarshall(rawData, 0, rawData.length);  
        
        readFromParcel(parcel);
    }

    public int describeContents() {
        return 0;
    }

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
    
    private void readFromParcel(final Parcel parcel) {
        parcel.setDataPosition(0);
        id = parcel.readInt();
        enabled = parcel.readInt() == 1;
        days = new Days(parcel.readInt());
        vibrate = parcel.readInt() == 1;
        label = parcel.readString();
        ringtoneUri = (Uri) parcel.readParcelable(null);
        set(parcel.readLong());
        normalize(true);
    }

    /**
     * Convert Alarm to byte[] for passing with Intents, etc. Use toContentVAlues() for storing alarms in persistent storage
     * @return byte[] representation of Alarm
     */
    public byte[] toRawData() {
        final Parcel parcel = Parcel.obtain();
        writeToParcel(parcel, 0);
        return parcel.marshall();
    }

    /**
     * Create ContentValues for Alarm
     * @return ContentValues representation of Alarm minus ID
     */
    public ContentValues toContentValues() {
        final ContentValues contentValues = new ContentValues(7);
        contentValues.put(ALARM_ENABLED, enabled);
        contentValues.put(ALARM_TIME, toMillis(true));
        contentValues.put(ALARM_SELECTED_DAYS, days.toInt());
        contentValues.put(ALARM_VIBRATE, vibrate);
        contentValues.put(ALARM_LABEL, label);
        contentValues.put(ALARM_RINGTONE_URI, ringtoneUri == null || ringtoneUri.equals(Uri.EMPTY)
                ? SILENT : ringtoneUri.toString());
        contentValues.put(ALARM_SORT, hour * 100 + minute);

        return contentValues;
    }

    /**
     * Retrieve label for Alarm
     * @return
     */
    public String getTickerText(final Context context) {
        return TextUtils.isEmpty(label) ? context.getString(R.string.default_alarm_ticker_text) : label;
    }
              
    public void updateScheduledTime() {
        final Time time = new Time();
        time.setToNow();
        
        if (compare(this, time) <= 0) {
            time.monthDay += 1;
        }
        
        time.hour = hour;
        time.minute = minute;
        time.second = 0;
        time.normalize(true);
        
        if (days.selected != Days.NO_DAYS_SELECTED) {
            time.monthDay += days.getDaysTillNext(time);
        }
        
        set(time.toMillis(true));
        normalize(true);
    } 
    
    public void set(Alarm alarm) {
        id = INVALID_ID;
        enabled = alarm.enabled;
        vibrate = alarm.vibrate;
        label = alarm.label;
        ringtoneUri = alarm.ringtoneUri;
        days.selected = alarm.days.selected;
        
        super.set(alarm);
    }
}
