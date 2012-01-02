package com.zhaoshouren.android.apps.clock.ui;

import static com.zhaoshouren.android.apps.clock.DeskClock.DEVELOPER_MODE;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;




public class WeatherFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static interface OnWeatherLoadFinishedListener {
        public void onWeatherLoadFinished();
    }

    public static WeatherFragment newInstance() {
        WeatherFragment fragment = new WeatherFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    public static final String TAG = "ZS.WeatherFragment";
    private OnWeatherLoadFinishedListener mWeatherLoadFinishedListener;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        try {
            mWeatherLoadFinishedListener = (OnWeatherLoadFinishedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement WeatherFragment.OnWeatherLoadFinishedListener");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle savedInstanceState) {
        Loader<Cursor> cursorLoader = null;

        // TODO

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

        mWeatherLoadFinishedListener.onWeatherLoadFinished();
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> cursorLoader) {
        // Don't need to do anything since we have no open cursors
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLoaderManager().initLoader(0, savedInstanceState, this);
    }
}
