package com.zhaoshouren.android.apps.clock.ui;

import static com.zhaoshouren.android.apps.clock.DeskClock.DEVELOPER_MODE;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;

import com.zhaoshouren.android.apps.clock.R;
import com.zhaoshouren.android.apps.clock.util.Days;

public class SelectDaysDialogFragment extends DialogFragment {

    public static interface OnSelectDaysChangeListener {
        public void onSelectDaysChange(int selected);
    }

    protected static class SelectDaysAdapter extends BaseAdapter implements OnClickListener {

        private static final String[] DAYS = Days.getDays(false);
        private final LayoutInflater mLayoutInflater;
        private final Days mDays;

        public SelectDaysAdapter(final LayoutInflater layoutInfater, final int selected) {
            mLayoutInflater = layoutInfater;
            mDays = new Days(selected);
        }

        public int getSelected() {
            return mDays.selected;
        }

        @Override
        public int getCount() {
            return Days.DAY_STRING_VALUES.length;
        }

        @Override
        public Object getItem(int position) {
            if (DEVELOPER_MODE) {
                Log.d(TAG, "getItem(position)" + "\n    position: " + position + "\n    day: "
                        + DAYS[position]);
            }
            return DAYS[position];
        }

        @Override
        public long getItemId(int position) {
            return Days.DAY_VALUES[position];
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.select_days_list_item, null);
            }

            CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.day);
            if (checkBox != null) {
                final int dayValue = (int) getItemId(position);
                checkBox.setText((String) getItem(position));
                checkBox.setChecked(mDays.isSet(dayValue));
                checkBox.setId(dayValue);
                checkBox.setOnClickListener(this);
            }

            return convertView;
        }

        @Override
        public void onClick(View view) {
            final CheckBox checkBox = (CheckBox) view;
            mDays.set(checkBox.getId(), checkBox.isChecked());
        }
    }

    public static SelectDaysDialogFragment newInstance(int selected) {
        SelectDaysDialogFragment fragment = new SelectDaysDialogFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(Days.Keys.SELECTED, selected);
        fragment.setArguments(arguments);
        return fragment;
    }

    private static final String TAG = "SelectDaysDialogFragment";
    private static SelectDaysAdapter sSelectedDaysAdapter;
    private static OnSelectDaysChangeListener mOnSelectDaysListener;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        try {
            mOnSelectDaysListener = (OnSelectDaysChangeListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SelectDaysDialogFragment.OnSelectDaysChangeListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container,
            Bundle savedInstanceState) {
        getDialog().setTitle(R.string.alarm_repeat);

        sSelectedDaysAdapter =
                new SelectDaysAdapter(layoutInflater, getArguments().getInt(Days.Keys.SELECTED));

        View view = layoutInflater.inflate(R.layout.select_days_list, null);

        ListView listView = (ListView) view.findViewById(R.id.daysList);
        listView.setAdapter(sSelectedDaysAdapter);

        view.findViewById(R.id.ok).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final int selectedDays = sSelectedDaysAdapter.getSelected();
                if (selectedDays != getArguments().getInt(Days.Keys.SELECTED)) {
                    mOnSelectDaysListener.onSelectDaysChange(selectedDays);
                }
                dismiss();
            }
        });

        view.findViewById(R.id.cancel).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return view;
    };
}
