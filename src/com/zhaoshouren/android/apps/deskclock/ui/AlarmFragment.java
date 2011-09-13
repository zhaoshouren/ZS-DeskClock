package com.zhaoshouren.android.apps.deskclock.ui;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.zhaoshouren.android.apps.deskclock.provider.AlarmContract;
import com.zhaoshouren.android.apps.deskclock.util.Alarm;

public class AlarmFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    
    public static interface OnAlarmLoadFinishedListener {
        public void onAlarmLoadFinished(Alarm alarm);
    }
    
    public static AlarmFragment newInstance(final int id) {
        AlarmFragment fragment = new AlarmFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(Alarm.KEY_ID, id);
        fragment.setArguments(arguments);
        return fragment;
    }
    
    public static final int GET_NEXT_ALARM = -2;
    public static final String TAG = "Fragment.Alarm";
    private OnAlarmLoadFinishedListener mAlarmLoadFinishedListener;
    
    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        try {
            mAlarmLoadFinishedListener = (OnAlarmLoadFinishedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement AlarmFragment.OnAlarmLoadFinishedListener");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle savedInstanceState) {
        if (id == GET_NEXT_ALARM) {
            return AlarmContract.getAlarmsCursorLoader(getActivity(), true);
        } else if (id >= 0) {
            return AlarmContract.getAlarmCursorLoader(getActivity(), id);
        }
        return null;
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> cursorLoader, final Cursor cursor) {
        final Alarm alarm = AlarmContract.getAlarm(getActivity(), cursor);    
        mAlarmLoadFinishedListener.onAlarmLoadFinished(alarm != null ? alarm : new Alarm(getActivity()));
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> cursorLoader) {}
    
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       
        getLoaderManager().initLoader(getArguments().getInt("id"), savedInstanceState, this);
    }
}
