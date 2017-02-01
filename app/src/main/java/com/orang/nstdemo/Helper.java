package com.orang.nstdemo;

import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;

class Helper {

    private Context context;
    private Activity activity;
    private static Helper sharedInstance;

    private Helper(Context context) {
        this.context = context;
    }

    static Helper getSharedInstance(Context context) {
        if (sharedInstance == null) {
            sharedInstance = new Helper(context);
        }
        return sharedInstance;
    }

    void setActivity(Activity activity) {
        this.activity = activity;
    }


}