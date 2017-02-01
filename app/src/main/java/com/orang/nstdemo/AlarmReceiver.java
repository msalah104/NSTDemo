package com.orang.nstdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Helper helper = Helper.getSharedInstance(context);
        helper.addNewAlarm();
        helper.addNewQuery();
    }

}
