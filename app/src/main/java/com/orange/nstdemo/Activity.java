package com.orange.nstdemo;

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
    private static final String FORMATTER = "dd/MM/yyyy hh:mm:ss.SSS";

    ListView listView;
    NetworkStatsAdapter adapter;
    List<Pair<NetworkStats.Bucket>> buckets;
    static Activity resumed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity);
        setTitle(getDate(getInstallationTime()));
    }

    @Override
    protected void onStart() {
        super.onStart();
        listView = (ListView) findViewById(R.id.list);
        if (buckets == null) buckets = new ArrayList<>();
        adapter = new NetworkStatsAdapter(this, buckets);
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (requestCode == PERMISSION_ID && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
            addNewQuery();
            addNewQuery();
            adapter.notifyDataSetChanged();
            addNewAlarm();
        }
    }

    void addNewAlarm() {
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 5000, pendingIntent);
    }

    void addNewQuery() {
        NetworkStatsManager netStatsMgr = getSystemService(NetworkStatsManager.class);
        String id = getSystemService(TelephonyManager.class).getSubscriberId();
        final long start = getInstallationTime();
        final long end = System.currentTimeMillis();
        try {
            NetworkStats.Bucket mobile = netStatsMgr.querySummaryForUser(ConnectivityManager.TYPE_MOBILE, id, start, end);
            NetworkStats.Bucket wifi = netStatsMgr.querySummaryForUser(ConnectivityManager.TYPE_WIFI, "", start, end);
            buckets.add(0, new Pair<>(mobile, wifi));
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
        }
    }

    static String getDate(long milliSeconds) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(FORMATTER);
        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    private long getInstallationTime() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).lastUpdateTime;
        } catch (PackageManager.NameNotFoundException e) {
            return Long.MAX_VALUE;
        }
    }
}
