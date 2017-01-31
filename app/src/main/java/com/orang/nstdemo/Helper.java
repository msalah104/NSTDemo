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
import android.widget.ArrayAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

class Helper {

    static final String DATA_USAGE_LIST = "data_usage_list";
    static final String FORMATTER = "dd/MM/yyyy hh:mm:ss.SSS";
    static final String EMPTY_STRING = "";
    private static final String TAG = "MainActivity";
    private static final String STRING_SPLITTER = "#";


    NetworkStatsManager networkStatsManager;
    private PendingIntent pendingIntent;
    Context context;

    public Helper(Context context) {
        this.context = context;
    }

    void addNewQuery() {
        String newQuery = queryForDataUsage();
        String [] records = getListOfRecords();
        records = appendValue(records, newQuery);
        updateData(records);

        if (context instanceof Activity){
            ((Activity)context).adapter = new ArrayAdapter<String>(context,
                                                                       android.R.layout.simple_list_item_1, android.R.id.text1, records);

            ((Activity)context).listView.setAdapter(((Activity)context).adapter);
        }
    }


    public String queryForDataUsage() {

        networkStatsManager = (NetworkStatsManager) context.getSystemService(
                Context.NETWORK_STATS_SERVICE);

        String mobile_id = getMobileSubscribeId();
        long endTime = System.currentTimeMillis();
        long startTime = getInstallationTime();
        try {
            NetworkStats.Bucket mobileUserQueryBucket = networkStatsManager.querySummaryForUser(ConnectivityManager.TYPE_MOBILE, mobile_id, startTime, endTime);
            NetworkStats.Bucket wifiUserQueryBucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_WIFI, EMPTY_STRING, startTime, endTime);

            String querySummary = context.getResources().getString(R.string.query_summary);
            querySummary = String.format(querySummary, mobileUserQueryBucket.getRxBytes(),
                    mobileUserQueryBucket.getTxBytes(),
                    wifiUserQueryBucket.getRxBytes(),
                    wifiUserQueryBucket.getTxBytes(),
                    getDate(startTime),
                    getDate(endTime));

            return querySummary;

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return "";
    }

    void updateData(String [] records) {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < records.length; i++) {
            sb.append(records[i]).append(STRING_SPLITTER);
        }

        SharedPreferences pref = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(DATA_USAGE_LIST, sb.toString());
        editor.commit();

    }

    String [] getListOfRecords () {
        SharedPreferences pref = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        String stringRecords = pref.getString(DATA_USAGE_LIST, EMPTY_STRING);
        if (!stringRecords.isEmpty()){
            String [] records = stringRecords.split(STRING_SPLITTER);
            return records;
        } else  {
            return new String[] {};
        }

    }

    public void addNewAlarm () {
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int interval = 5000;

        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
    }

    private String[] appendValue(String[] obj, String newObj) {
        ArrayList<String> temp = new ArrayList<String>(Arrays.asList(obj));
        temp.add(0, newObj);
        return temp.toArray(new String[temp.size()]);
    }

    public static String getDate(long milliSeconds)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(FORMATTER);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }


    public String getMobileSubscribeId() {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getSubscriberId();
    }

    public long getInstallationTime() {
        try {
            long time = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0)
                    .lastUpdateTime;
            return time;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

}