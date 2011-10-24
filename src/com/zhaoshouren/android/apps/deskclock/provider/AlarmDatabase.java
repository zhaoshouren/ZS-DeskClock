package com.zhaoshouren.android.apps.deskclock.provider;

import static com.zhaoshouren.android.apps.deskclock.DeskClock.DEVELOPER_MODE;
import static com.zhaoshouren.android.apps.deskclock.DeskClock.TAG;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class AlarmDatabase extends SQLiteOpenHelper {
  
    private static final String DATABASE_NAME = "zhaoshouren.deskclock.db";
    private static final int DATABASE_VERSION = 1; 
    
    public interface AlarmTables {
        public static final String TABLE_ALARMS = "alarms";
    }
    
    public interface AlarmColumns extends BaseColumns {
        // Column names
        // _ID implemented in BaseColumns
        /**
         * Alarm time in UTC milliseconds from the epoch.
         * <P>
         * SQLite Type: INTEGER
         * </P>
         */
        public static final String ALARM_TIME = "time";
        /**
         * Selected days bitmask as integer
         * <P>
         * SQLite Type: INTEGER
         * </P>
         */
        public static final String ALARM_SELECTED_DAYS = "selected_days";
        /**
         * True if alarm is active
         * <P>
         * SQLite Type: INTEGER
         * </P>
         */
        public static final String ALARM_ENABLED = "enabled";
        /**
         * True if alarm should vibrate
         * <P>
         * SQLite Type: INTEGER
         * </P>
         */
        public static final String ALARM_VIBRATE = "vibrate";
        /**
         * Label for alarm
         * <P>
         * SQLite Type: TEXT
         * </P>
         */
        public static final String ALARM_LABEL = "label";
        /**
         * Audio alert to play when alarm triggers
         * <P>
         * SQLite Type: TEXT
         * </P>
         */
        public static final String ALARM_RINGTONE_URI = "ringtone_uri";
        /**
         * Calculated sort value derived from hour and minutes of alarm
         * <P>
         * SQLite Type: INTEGER
         * </P>
         */
        public static final String ALARM_SORT = "sort";

        // INDEX_[Column] values need to match corresponding order of columns in SELECT
        public static final int INDEX_ID = 0;
        public static final int INDEX_TIME = 1;
        public static final int INDEX_SELECTED_DAYS = 2;
        public static final int INDEX_ENABLED = 3;
        public static final int INDEX_VIBRATE = 4;
        public static final int INDEX_LABEL = 5;
        public static final int INDEX_RINGTONE_URI = 6;
        public static final int INDEX_SORT = 7;
    }
    
    public static class Sql implements AlarmTables, AlarmColumns {
        // Column order needs to correspond to INDEX_[Column] constant values
        public static final String[] SELECT = { 
                _ID, 
                ALARM_TIME, 
                ALARM_SELECTED_DAYS, 
                ALARM_ENABLED, 
                ALARM_VIBRATE,
                ALARM_LABEL, 
                ALARM_RINGTONE_URI 
                };
        
        public static final String SORT_BY_TIME = ALARM_TIME + " ASC";
        public static final String SORT_BY_TIME_OF_DAY = ALARM_SORT + ", " + ALARM_TIME + " ASC";
        public static final String WHERE_ENABLED = ALARM_ENABLED + " = 1";
        public static final String WHERE_DISABLED = ALARM_ENABLED + " = 0";
        public static final String WHERE_ID_IN = _ID + " IN (?)";       
    
        private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_ALARMS + " (" 
            + _ID + " INTEGER PRIMARY KEY, " 
            + ALARM_TIME + " INTEGER, " 
            + ALARM_SELECTED_DAYS + " INTEGER, " 
            + ALARM_ENABLED + " INTEGER, " 
            + ALARM_VIBRATE + " INTEGER, " 
            + ALARM_LABEL + " TEXT, " 
            + ALARM_RINGTONE_URI + " TEXT, "
            + ALARM_SORT + " INTEGER);";
            
        private static final String INSERT_INTO_ALARMS = "INSERT INTO alarms (" 
            + ALARM_TIME + ", " 
            + ALARM_SELECTED_DAYS + ", " 
            + ALARM_ENABLED + ", " 
            + ALARM_VIBRATE + ", " 
            + ALARM_LABEL + ", " 
            + ALARM_RINGTONE_URI + ", "
            + ALARM_SORT + ") ";
    }

    public AlarmDatabase(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase database) {
        database.execSQL(Sql.CREATE_TABLE);

        // insert default alarms
        database.execSQL(Sql.INSERT_INTO_ALARMS + "VALUES (23400000, 31, 0, 1, '', '', 630);");
        database.execSQL(Sql.INSERT_INTO_ALARMS + "VALUES (34200000, 96, 0, 1, '', '', 930);");
    }

    @Override
    public void onUpgrade(final SQLiteDatabase database, final int oldVersion,
            final int currentVersion) {
        if (DEVELOPER_MODE) {
            Log.d(TAG,
                    "AlarmContentProvider.onUpgrade(): Upgrading alarms database from version "
                            + oldVersion + " to " + currentVersion
                            + ", which will destroy all old data");
        }

        // TODO migrate table rather than drop
        database.execSQL("DROP TABLE IF EXISTS alarms");
        onCreate(database);
    }
}