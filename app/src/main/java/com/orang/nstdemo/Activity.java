package com.orang.nstdemo;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class  Activity extends android.app.Activity {
    private static final String TAG = new Object(){}.getClass().getEnclosingClass().getSimpleName();

    private static final int PERMISSION_ID = 1;
    private static final String DATA_USAGE_LIST = "data_usage_list";
    private static final String STRING_SPLITTER = "#";
    private static final String FORMATTER = "dd/MM/yyyy hh:mm:ss.SSS";

    Helper helper;
    ListView listView;
    ArrayAdapter<String> adapter;
    List<String> records;
    static Activity resumed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity);
        helper = Helper.getSharedInstance(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        helper.setActivity(this);
        listView = (ListView) findViewById(R.id.list);
        records = getListOfRecords();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, records);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumed = this;
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[] {Manifest.permission.READ_PHONE_STATE}, PERMISSION_ID );
        }
    }

    @Override
    protected void onPause() {
        resumed = null;
        super.onResume();
    }

    @Override
    protected void onStop() {
        helper.setActivity(null);
        updateData(records);
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (requestCode == PERMISSION_ID && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                this.startActivityForResult(intent, PERMISSION_ID);

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Assuming user has granted the uage permission
        if (requestCode == PERMISSION_ID) {
            helper.setActivity(this);
            addNewQuery();
            addNewAlarm();
        }
    }

    List<String> getListOfRecords() {
        SharedPreferences pref = getSharedPreferences(TAG, Context.MODE_PRIVATE);
        String stringRecords = pref.getString(DATA_USAGE_LIST, "");
        if (!stringRecords.isEmpty()){
            return new ArrayList<>(Arrays.asList(stringRecords.split(STRING_SPLITTER)));
        } else  {
            return new ArrayList<>();
        }
    }

    private void updateData(List<String> records) {
        StringBuilder builder = new StringBuilder();
        for (String record: records) builder.append(record).append(STRING_SPLITTER);
        SharedPreferences pref = getSharedPreferences(TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(DATA_USAGE_LIST, builder.toString());
        editor.apply();
    }

    void addNewAlarm() {
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 5000, pendingIntent);
    }

    void addNewQuery() {
        String newQuery = queryForDataUsage();
        if (records == null) return;
        records.add(0, newQuery);
        adapter.notifyDataSetChanged();
    }

    private String queryForDataUsage() {
        NetworkStatsManager networkStatsManager = (NetworkStatsManager) getSystemService(Context.NETWORK_STATS_SERVICE);
        String mobile_id = getMobileSubscribeId();
        long endTime = System.currentTimeMillis();
        long startTime = getInstallationTime();
        try {
            NetworkStats.Bucket mobileBucket = networkStatsManager.querySummaryForUser(ConnectivityManager.TYPE_MOBILE, mobile_id, startTime, endTime);
            NetworkStats.Bucket wifiBucket = networkStatsManager.querySummaryForUser(ConnectivityManager.TYPE_WIFI, "", startTime, endTime);
            String querySummary = getResources().getString(R.string.query_summary);
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

    private static String getDate(long milliSeconds) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(FORMATTER);
        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    private String getMobileSubscribeId() {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getSubscriberId();
    }

    private long getInstallationTime() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).lastUpdateTime;
        } catch (PackageManager.NameNotFoundException e) {
            return Long.MAX_VALUE;
        }
    }


}
