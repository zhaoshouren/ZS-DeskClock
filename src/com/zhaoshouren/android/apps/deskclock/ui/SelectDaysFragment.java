package com.zhaoshouren.android.apps.deskclock.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;

import java.util.List;
import java.util.zip.Inflater;

import com.zhaoshouren.android.apps.deskclock.util.Alarm;
import com.zhaoshouren.android.apps.deskclock.util.Days;
import com.zhaoshouren.android.apps.deskclock.R;

import static com.zhaoshouren.android.apps.deskclock.util.Days.CALENDAR_DAY_STRING_VALUES;


public class SelectDaysFragment extends DialogFragment {
    
    private static final String[] DAYS = Days.getDays(false);
       
    private class SelectDaysAdapter extends BaseAdapter {
        
        private final LayoutInflater mLayoutInflater;
        private final Days mDays;
        
        public SelectDaysAdapter(final Context context, final LayoutInflater layoutInfater, final Days days) {
            mLayoutInflater = layoutInfater;
            mDays = days;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int arg0, View arg1, ViewGroup arg2) {
            // TODO Auto-generated method stub
            return null;
        }
       
        
    }
    
    public static SelectDaysFragment newInstance(int title) {
        SelectDaysFragment fragment = new SelectDaysFragment();
        Bundle args = new Bundle();
        args.putInt("title", title);
        fragment.setArguments(args);
        return fragment;
    }
    
    Alarm mAlarm;
    ListView mListView;
    SelectDaysAdapter mSelectDaysAdapter;
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        
        final Intent intent = getActivity().getIntent();
        intent.getParcelableExtra(Alarm.KEY_PARCEL);
        
        final LayoutInflater layoutInflator = getLayoutInflater(savedInstanceState);
        
        View view = layoutInflator.inflate(R.layout.select_days, null);
        
        mSelectDaysAdapter = new SelectDaysAdapter(getActivity(), layoutInflator, null) ;
        
        
        ListView listView = (ListView) view.findViewById(R.id.daysList);
        listView.setAdapter(mSelectDaysAdapter);
        
        
        return super.onCreateDialog(savedInstanceState);
    }

}
