package com.orang.nstdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = new Object(){}.getClass().getEnclosingClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, new Object(){}.getClass().getEnclosingMethod().getName());
        Helper helper = Helper.getSharedInstance(context);
        if (Activity.resumed != null) Activity.resumed.addNewAlarm();
        helper.addNewQuery();
    }
}
