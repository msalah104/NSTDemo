package com.orange.nstdemo;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.Process;
import android.provider.Settings;
import android.widget.BaseAdapter;

public class ListActivity extends android.app.ListActivity {
    private static final long INTERVAL =  5000 ;
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ((BaseAdapter) getListAdapter()).notifyDataSetChanged();
        }
    };
    private Parcelable state;
    static final String UPDATE = "update";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(NetworkStatsAdapter.formatDate(getInstallationTime(this)));
        Intent intent = new Intent(getApplicationContext(), IntentService.class);
        final PendingIntent pending = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        getSystemService(AlarmManager.class).setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), INTERVAL, pending);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (state != null) {
            getListView().onRestoreInstanceState(state);
        } else {
            if (NetworkStatsBucket.none()) NetworkStatsBucket.init(); // addNew() may lack permission here
            setListAdapter(new NetworkStatsAdapter(this));
        }
        registerReceiver(broadcastReceiver, new IntentFilter(UPDATE));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[] {Manifest.permission.READ_PHONE_STATE}, 0);
            return;
        }
        AppOpsManager appOpsManager = getSystemService(AppOpsManager.class);
        int packageUsageStats = appOpsManager.checkOpNoThrow("android:get_usage_stats", Process.myUid(), getPackageName());
        if (packageUsageStats != AppOpsManager.MODE_ALLOWED) {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            return;
        }
        if (NetworkStatsBucket.none()) {
            NetworkStatsBucket.addNew(this);
            NetworkStatsBucket.addNew(this);
        }
        ((BaseAdapter) getListAdapter()).notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        state = getListView().onSaveInstanceState();
        super.onPause();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

    static long getInstallationTime(final Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).lastUpdateTime;
        } catch (PackageManager.NameNotFoundException e) {
            return Long.MAX_VALUE;
        }
    }
}
