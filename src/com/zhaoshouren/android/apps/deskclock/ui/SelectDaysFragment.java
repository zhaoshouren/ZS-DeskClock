package com.zhaoshouren.android.apps.deskclock.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;

import java.util.List;

import com.zhaoshouren.android.apps.deskclock.utils.Alarm;
import com.zhaoshouren.android.apps.deskclock.R;
import com.zhaoshouren.android.apps.deskclock.utils.SelectedDays;
import static com.zhaoshouren.android.apps.deskclock.utils.SelectedDays.CALENDAR_DAY_STRING_VALUES;


public class SelectDaysFragment extends DialogFragment {
    
    private static final String[] DAYS = SelectedDays.getDays(false);
    
    private class SelectDaysAdapter extends ArrayAdapter<String> {

        public SelectDaysAdapter(Context context, int textViewResourceId, String[] objects) {
            super(context, textViewResourceId, objects);
            
        }
        
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            ((CheckBox) convertView.findViewById(R.id.checkBox1)).setText();
//            return super.getView(position, convertView, parent);
//        }
        
        
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
    ArrayAdapter<String> mArrayAdapter;
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        
        final Intent intent = getActivity().getIntent();
        intent.getParcelableExtra(Alarm.KEY_PARCEL);
        
        View view = getLayoutInflater(savedInstanceState).inflate(R.layout.select_days, null);
        
        mArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.day_list_item, R.id.dayCheckBox, DAYS) ;
        
        
        ListView listView = (ListView) view.findViewById(R.id.selectedDaysList);
        listView.setAdapter(mArrayAdapter);
        
        
        return super.onCreateDialog(savedInstanceState);
    }

}
