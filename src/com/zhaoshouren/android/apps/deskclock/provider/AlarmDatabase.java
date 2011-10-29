package com.zhaoshouren.android.apps.deskclock.provider;

import static com.zhaoshouren.android.apps.deskclock.DeskClock.DEVELOPER_MODE;
import static com.zhaoshouren.android.apps.deskclock.DeskClock.TAG;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.zhaoshouren.android.apps.deskclock.provider.AlarmContract.Alarms;

public class AlarmDatabase extends SQLiteOpenHelper {

    public interface AlarmTables {
        public static final String TABLE_ALARMS = "alarms";
    }

    public static class Sql implements AlarmTables {
        // Column order needs to correspond to INDEX_[Column] constant values
        public static final String[] SELECT = { Alarms._ID, Alarms.TIME, Alarms.SELECTED_DAYS,
                Alarms.ENABLED, Alarms.VIBRATE, Alarms.LABEL, Alarms.RINGTONE_URI };

        public static final String SORT_BY_TIME = Alarms.TIME + " ASC";
        public static final String SORT_BY_TIME_OF_DAY = Alarms.SORT + ", " + Alarms.TIME + " ASC";
        public static final String WHERE_ENABLED = Alarms.ENABLED + " = 1";
        public static final String WHERE_DISABLED = Alarms.ENABLED + " = 0";
        public static final String WHERE_ID_IN = Alarms._ID + " IN (?)";

        private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_ALARMS + " ("
                + Alarms._ID + " INTEGER PRIMARY KEY, " + Alarms.TIME + " INTEGER, "
                + Alarms.SELECTED_DAYS + " INTEGER, " + Alarms.ENABLED + " INTEGER, "
                + Alarms.VIBRATE + " INTEGER, " + Alarms.LABEL + " TEXT, " + Alarms.RINGTONE_URI
                + " TEXT, " + Alarms.SORT + " INTEGER);";

        private static final String INSERT_INTO_ALARMS = "INSERT INTO alarms (" + Alarms.TIME
                + ", " + Alarms.SELECTED_DAYS + ", " + Alarms.ENABLED + ", " + Alarms.VIBRATE
                + ", " + Alarms.LABEL + ", " + Alarms.RINGTONE_URI + ", " + Alarms.SORT + ") ";
    }

    private static final String DATABASE_NAME = "zhaoshouren.deskclock.db";

    private static final int DATABASE_VERSION = 1;

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
            Log.d(TAG, "AlarmContentProvider.onUpgrade(): Upgrading alarms database from version "
                    + oldVersion + " to " + currentVersion + ", which will destroy all old data");
        }

        // TODO migrate table rather than drop
        database.execSQL("DROP TABLE IF EXISTS alarms");
        onCreate(database);
    }
}
