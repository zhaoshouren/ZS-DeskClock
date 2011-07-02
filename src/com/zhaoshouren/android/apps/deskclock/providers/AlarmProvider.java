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
package com.zhaoshouren.android.apps.deskclock.providers;

import static com.zhaoshouren.android.apps.deskclock.DeskClock.DEVELOPER_MODE;
import static com.zhaoshouren.android.apps.deskclock.DeskClock.TAG;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.zhaoshouren.android.apps.deskclock.providers.AlarmDatabase.AlarmColumns;
import com.zhaoshouren.android.apps.deskclock.providers.AlarmDatabase.AlarmTables;


public class AlarmProvider extends ContentProvider implements AlarmTables, AlarmColumns{
    
    public static final Uri CONTENT_URI = Uri
            .parse("content://com.zhaoshouren.android.apps.deskclock/alarm");
    

    private static final String TYPE_ALARM_ID = "vnd.android.cursor.item/alarms";
    private static final String TYPE_ALARM = "vnd.android.cursor.dir/alarms";
    
    private static final int ALARM = 1;
    private static final int ALARM_ID = 2;
    
    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);  
    static {
        URI_MATCHER.addURI("com.zhaoshouren.android.apps.deskclock", "alarm", ALARM);
        URI_MATCHER.addURI("com.zhaoshouren.android.apps.deskclock", "alarm/#", ALARM_ID);
    }
 
    private static AlarmDatabase sAlarmDatabase;
    private static ContentResolver sContentResolver;

    @Override
    public boolean onCreate() {
        final Context context = getContext();
        sAlarmDatabase = new AlarmDatabase(context);
        sContentResolver = context.getContentResolver();
        return true;
    }

    @Override
    public Cursor query(final Uri uri, final String[] projectionIn, final String selection,
            final String[] selectionArguments, final String sort) {
        final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch (URI_MATCHER.match(uri)) {
        case ALARM:
            queryBuilder.setTables(TABLE_ALARMS);
            break;
        case ALARM_ID:
            queryBuilder.setTables(TABLE_ALARMS);
            queryBuilder.appendWhere(_ID + " = " + uri.getPathSegments().get(1));
            break;
        default:
            throw new IllegalArgumentException("Unknown URL " + uri);
        }

        final Cursor cursor =
                queryBuilder.query(sAlarmDatabase.getReadableDatabase(), projectionIn, selection,
                        selectionArguments, null, null, sort);

        if (cursor == null) {
            if (DEVELOPER_MODE) {
                Log.d(TAG, "AlarmContentProvider.query(): No alarms found");
            }
        } else {
            cursor.setNotificationUri(sContentResolver, uri);
        }

        return cursor;
    }

    @Override
    public String getType(final Uri uri) {
        switch (URI_MATCHER.match(uri)) {
        case ALARM:
            return TYPE_ALARM;
        case ALARM_ID:
            return TYPE_ALARM_ID;
        default:
            throw new IllegalArgumentException("Unknown URI");
        }
    }

    @Override
    public int update(final Uri uri, final ContentValues contentValues, final String where,
            final String[] whereArguments) {
        int count ;

        switch (URI_MATCHER.match(uri)) {
        case ALARM_ID: {
            final long rowId = Long.parseLong(uri.getPathSegments().get(1));
            
            count =
                    sAlarmDatabase.getWritableDatabase().update(TABLE_ALARMS, contentValues,
                            _ID + " = " + rowId, null);
            
            if (DEVELOPER_MODE) {
                Log.d(TAG, "AlarmContentProvider.update()"
                        + "\n    uri: " + uri
                        + "\n    rowId: " + rowId 
                        + "\n    count: " + count);
            }
            break;
        }
        default: {
            throw new UnsupportedOperationException("Cannot update URL: " + uri);
        }
        }

        sContentResolver.notifyChange(uri, null);
        return count;
    }

    @Override
    public Uri insert(final Uri uri, final ContentValues initialValues) {
        if (URI_MATCHER.match(uri) != ALARM) {
            throw new IllegalArgumentException("Cannot insert into URL: " + uri);
        }

        final long id =
                sAlarmDatabase.getWritableDatabase().insert(TABLE_ALARMS, ALARM_LABEL,
                        new ContentValues(initialValues));

        if (id < 0) {
            throw new SQLException("Failed to insert row into " + uri);
        }

        if (DEVELOPER_MODE) {
            Log.d(TAG, "AlarmContentProvider.insert(): Added alarm"
                    + "\n    id: " + id
                    + "\n    uri: " + uri
                    + "\n    intialValues: " + initialValues);
        }

        final Uri newUri = ContentUris.withAppendedId(CONTENT_URI, id);
        sContentResolver.notifyChange(newUri, null);
        return newUri;
    }

    @Override
    public int delete(final Uri uri, String where, final String[] whereArguments) {
        final SQLiteDatabase database = sAlarmDatabase.getWritableDatabase();
        final int count;
        
        switch (URI_MATCHER.match(uri)) {
        case ALARM:
            count = database.delete(TABLE_ALARMS, where, whereArguments);
            break;
        case ALARM_ID:
            final String id = uri.getPathSegments().get(1);
            if (TextUtils.isEmpty(where)) {
                where = _ID + " = " + id;
            } else {
                where = _ID + " = " + id + " AND (" + where + ")";
            }
            count = database.delete(TABLE_ALARMS, where, whereArguments);
            break;
        default:
            throw new IllegalArgumentException("Cannot delete from URL: " + uri);
        }

        sContentResolver.notifyChange(uri, null);
        return count;
    }    
}
