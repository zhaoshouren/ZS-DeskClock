package com.zhaoshouren.android.apps.clock.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;

import com.zhaoshouren.android.apps.clock.util.Days;
import com.zhaoshouren.android.apps.clock.R;

public class SelectDaysListFragment extends ListFragment {

    public static interface OnSelectDaysChangeListener {
        public void onSelectDaysChange(int selected);
    }

    public static SelectDaysListFragment newInstance(int days) {
        SelectDaysListFragment fragment = new SelectDaysListFragment();
        Bundle arguments = new Bundle();
        arguments.putInt("days", days);
        fragment.setArguments(arguments);
        return fragment;
    }

    private static class SelectDaysAdapter extends BaseAdapter implements View.OnClickListener {

        private static final String[] DAYS = Days.getDays(false);
        private final LayoutInflater mLayoutInflater;
        private final Days mDays;
        private final OnSelectDaysChangeListener mOnSelectDaysChangeListener;

        private SelectDaysAdapter(OnSelectDaysChangeListener onSelectDaysChangeListener,
                final LayoutInflater layoutInfater, final int selected) {
            mOnSelectDaysChangeListener = onSelectDaysChangeListener;
            mLayoutInflater = layoutInfater;
            mDays = new Days(selected);
        }

        @Override
        public int getCount() {
            return Days.DAY_VALUES.length;
        }

        @Override
        public Object getItem(int position) {
            return DAYS[position];
        }

        @Override
        public long getItemId(int position) {
            return Days.DAY_VALUES[position];
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.day_list_item, null);
            }

            CheckedTextView checkedTextView = (CheckedTextView) convertView.findViewById(R.id.day);
            checkedTextView.setText((String) getItem(position));
            checkedTextView.setChecked(mDays.isSet(position));
            checkedTextView.setId(position);
            checkedTextView.setOnClickListener(this);

            return convertView;
        }

        @Override
        public void onClick(View view) {
            final CheckedTextView checkedTextView = (CheckedTextView) view;
            mDays.set(checkedTextView.getId(), checkedTextView.isChecked());
            mOnSelectDaysChangeListener.onSelectDaysChange(mDays.selected);
        }
    }

    private OnSelectDaysChangeListener mOnSelectDaysChangeListener;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        setListAdapter(new SelectDaysAdapter(mOnSelectDaysChangeListener,
                getLayoutInflater(savedInstanceState), getArguments().getInt("days")));
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mOnSelectDaysChangeListener = (OnSelectDaysChangeListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SelectDaysListFragment.OnSelectDaysChangeListener");
        }
    };

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        ((CheckedTextView) view.findViewById(R.id.day)).performClick();
    }
}
