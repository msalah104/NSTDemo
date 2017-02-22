package com.orange.nstdemo;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

public class  Activity extends android.app.ListActivity {

    private static final String TAG = new Object(){}.getClass().getEnclosingClass().getSimpleName();

    static List<Pair<NetworkStats.Bucket>> buckets; // Mobile * Wi-Fi Buckets
    Parcelable state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, new Object(){}.getClass().getEnclosingMethod().getName());
        super.onCreate(savedInstanceState);
        setTitle(NetworkStatsAdapter.formatDate(getInstallationTime(this)));
        Intent intent = new Intent(getApplicationContext(), BroadcastReceiver.class);
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        getSystemService(AlarmManager.class).setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 10000, pendingIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (state != null) {
            getListView().onRestoreInstanceState(state);
        } else {
            if (buckets == null) buckets = new ArrayList<>();
            setListAdapter(new NetworkStatsAdapter(this, buckets));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[] {Manifest.permission.READ_PHONE_STATE}, 0);
            return;
        }
        if (buckets.size() == 0) {
            addNewQuery(this);
            addNewQuery(this);
        }
        ((BaseAdapter) getListAdapter()).notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        state = getListView().onSaveInstanceState();
        super.onPause();
    }

    static void addNewQuery(final Context context) {
        NetworkStatsManager netStatsMgr = context.getSystemService(NetworkStatsManager.class);
        String id = context.getSystemService(TelephonyManager.class).getSubscriberId();
        final long start = getInstallationTime(context);
        final long end = System.currentTimeMillis();
        try {
            NetworkStats.Bucket mobile = netStatsMgr.querySummaryForUser(ConnectivityManager.TYPE_MOBILE, id, start, end);
            NetworkStats.Bucket wifi = netStatsMgr.querySummaryForUser(ConnectivityManager.TYPE_WIFI, "", start, end);
            buckets.add(0, new Pair<>(mobile, wifi));
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
        }
    }

    private static long getInstallationTime(final Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).lastUpdateTime;
        } catch (PackageManager.NameNotFoundException e) {
            return Long.MAX_VALUE;
        }
    }
}
