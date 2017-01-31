package com.orang.nstdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Helper helper = Helper.getSharedInstance(context);
        helper.addNewAlarm();
        helper.addNewQuery();

        Toast.makeText(context, "I'm running", Toast.LENGTH_SHORT).show();
    }
}
