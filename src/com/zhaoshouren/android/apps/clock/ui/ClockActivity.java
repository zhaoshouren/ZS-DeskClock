package com.zhaoshouren.android.apps.clock.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;

public class ClockActivity extends FragmentActivity implements OnClickListener, OnLongClickListener {

    public static final String TAG = "ZS.ClockActivity";
    
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onLongClick(View v) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub

    }

}
