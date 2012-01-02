package com.zhaoshouren.android.apps.clock.ui;

import static com.zhaoshouren.android.apps.clock.DeskClock.DEVELOPER_MODE;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;



import com.zhaoshouren.android.apps.clock.provider.AlarmContract;
import com.zhaoshouren.android.apps.clock.util.Alarm;

public class AlarmFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static interface OnAlarmLoadFinishedListener {
        public void onAlarmLoadFinished(Alarm alarm);
    }

    public static AlarmFragment newInstance(final int id) {
        AlarmFragment fragment = new AlarmFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(Alarm.Keys.ID, id);
        fragment.setArguments(arguments);
        return fragment;
    }

    public static final int GET_NEXT_ALARM = -2;
    public static final String TAG = "ZS.AlarmFragment";
    private OnAlarmLoadFinishedListener mAlarmLoadFinishedListener;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        try {
            mAlarmLoadFinishedListener = (OnAlarmLoadFinishedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement AlarmFragment.OnAlarmLoadFinishedListener");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle savedInstanceState) {
        Loader<Cursor> cursorLoader = null;

        if (id == GET_NEXT_ALARM) {
            cursorLoader = AlarmContract.getAlarmsCursorLoader(getActivity(), true);
        } else if (id >= 0) {
            cursorLoader = AlarmContract.getAlarmCursorLoader(getActivity(), id);
        }

        if (DEVELOPER_MODE) {
            Log.d(TAG, "onCreateLoader()" + "\n   id = " + id + "\n   cursorLoader = "
                    + cursorLoader);
        }

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> cursorLoader, final Cursor cursor) {
        if (DEVELOPER_MODE) {
            Log.d(TAG, "cursor: " + cursor.getCount());
            Log.d(TAG, "activity: " + getActivity());
        }

        mAlarmLoadFinishedListener.onAlarmLoadFinished(AlarmContract
                .getAlarm(getActivity(), cursor));
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> cursorLoader) {
        // Don't need to do anything since we have no open cursors
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLoaderManager().initLoader(getArguments().getInt(Alarm.Keys.ID), savedInstanceState,
                this);
    }
}
