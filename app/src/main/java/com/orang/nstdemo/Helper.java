package com.orang.nstdemo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

class Helper {

    private static final String TAG = new Object(){}.getClass().getEnclosingClass().getSimpleName();

    private static final String FORMATTER = "dd/MM/yyyy hh:mm:ss.SSS";

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

    void addNewQuery() {
        String newQuery = queryForDataUsage();
        if (activity == null) return;
        if (activity.records == null) return;
        activity.records.add(0, newQuery);
        activity.adapter.notifyDataSetChanged();
    }

    private String queryForDataUsage() {
        NetworkStatsManager networkStatsManager = (NetworkStatsManager) context.getSystemService(Context.NETWORK_STATS_SERVICE);
        String mobile_id = getMobileSubscribeId();
        long endTime = System.currentTimeMillis();
        long startTime = getInstallationTime();
        try {
            NetworkStats.Bucket mobileBucket = networkStatsManager.querySummaryForUser(ConnectivityManager.TYPE_MOBILE, mobile_id, startTime, endTime);
            NetworkStats.Bucket wifiBucket = networkStatsManager.querySummaryForUser(ConnectivityManager.TYPE_WIFI, "", startTime, endTime);
            String querySummary = context.getResources().getString(R.string.query_summary);
            querySummary = String.format(querySummary,
                    getDate(startTime),
                    getDate(endTime),
                    mobileBucket.getRxBytes(),
                    mobileBucket.getTxBytes(),
                    wifiBucket.getRxBytes(),
                    wifiBucket.getTxBytes());
            return querySummary;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
        }
        return "";
    }
    
    void addNewAlarm() {
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 5000, pendingIntent);
    }

    private static String getDate(long milliSeconds) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(FORMATTER);
        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }


    private String getMobileSubscribeId() {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getSubscriberId();
    }

    private long getInstallationTime() {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).lastUpdateTime;
        } catch (PackageManager.NameNotFoundException e) {
            return Long.MAX_VALUE;
        }
    }

}