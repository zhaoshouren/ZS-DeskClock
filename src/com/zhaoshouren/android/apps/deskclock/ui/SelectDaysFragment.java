package com.zhaoshouren.android.apps.deskclock.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;

import com.zhaoshouren.android.apps.deskclock.R;
import com.zhaoshouren.android.apps.deskclock.util.Days;


public class SelectDaysFragment extends DialogFragment {
    
    public static interface SelectDaysCallback {
        public void setDays(int days);
    }
          
    private static class SelectDaysAdapter extends BaseAdapter {
        
        private static final String[] DAYS = Days.getDays(false);
        private final LayoutInflater mLayoutInflater;
        private final Days mDays;
        private final FragmentActivity mFragmentActivity;
        
        public SelectDaysAdapter(final FragmentActivity fragmentActivity, final LayoutInflater layoutInfater, final int days) {
            mFragmentActivity = fragmentActivity;
            mLayoutInflater = layoutInfater;
            mDays = new Days(days);
        }

        @Override
        public int getCount() {
            return DAYS.length;
        }

        @Override
        public Object getItem(int position) {
            return DAYS[position];
        }

        @Override
        public long getItemId(int position) {
            return (long) position;
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
            checkedTextView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    final CheckedTextView checkedTextView = (CheckedTextView) view;
                    mDays.set(checkedTextView.getId(), checkedTextView.isChecked());
                    ((SetAlarmActivity) mFragmentActivity).setDays(mDays.selected);
                }
            });
            
            return convertView;
        }
    }
    
    public static SelectDaysFragment newInstance(int days) {
        SelectDaysFragment fragment = new SelectDaysFragment();
        Bundle args = new Bundle();
        args.putInt("days", days);
        fragment.setArguments(args);
        return fragment;
    }
    
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        
//        final Intent intent = getActivity().getIntent();
//        intent.getParcelableExtra(Alarm.KEY_PARCEL);
        
        final LayoutInflater layoutInflator = getLayoutInflater(savedInstanceState);
        
        View view = layoutInflator.inflate(R.layout.select_days, null);
        
        ListView listView = (ListView) view.findViewById(R.id.daysList);
        listView.setAdapter(new SelectDaysAdapter(getActivity(), layoutInflator, getArguments().getInt("days")));
        
        return super.onCreateDialog(savedInstanceState);
    }
}
