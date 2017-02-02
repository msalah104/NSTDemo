package com.orange.nstdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = new Object(){}.getClass().getEnclosingClass().getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, new Object(){}.getClass().getEnclosingMethod().getName());
        if (Activity.resumed == null) return;
        Activity.resumed.addNewQuery();
        Activity.resumed.adapter.notifyDataSetChanged();
        Activity.resumed.addNewAlarm();
    }
}
