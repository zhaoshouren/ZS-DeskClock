package com.zhaoshouren.android.apps.deskclock.ui;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;

import com.zhaoshouren.android.apps.deskclock.R;
import com.zhaoshouren.android.apps.deskclock.util.Days;


public class SelectDaysDialogFragment extends DialogFragment implements View.OnClickListener, SelectDaysListFragment.OnSelectDaysChangeListener {
    
    public static interface OnSelectDaysChangeListener {
        public void onSelectDaysChange(int selected);
    }
          
    protected static class SelectDaysAdapter extends BaseAdapter {
        
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
                }
            });
            
            return convertView;
        }
    }
    
    public static SelectDaysDialogFragment newInstance(int days) {
        SelectDaysDialogFragment fragment = new SelectDaysDialogFragment();
        Bundle arguments = new Bundle();
        arguments.putInt("days", days);
        fragment.setArguments(arguments);
        return fragment;
    }
    
    private Days mDays;
    
    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        View view = layoutInflater.inflate(R.layout.select_days, null);
        
        ListView listView = (ListView) view.findViewById(R.id.daysList);
        listView.setAdapter(new SelectDaysAdapter(layoutInflater, getArguments().getInt("days")));
        
        return view;
    };
    
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.ok) {
            ((OnSelectDaysChangeListener) getActivity()).onSelectDaysChange(mDays.selected);
        }
        dismiss();
    }

    @Override
    public void onSelectDaysChange(int selected) {
        mDays.selected = selected;
    }
}
