package com.orange.nstdemo;

import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

class NetworkStatsBucket {
    private static final String TAG = new Object(){}.getClass().getEnclosingClass().getSimpleName();
    private static List<NetworkStatsBucket> buckets; // Mobile * Wi-Fi Buckets
    private final NetworkStats.Bucket mobile, wifi;
    private NetworkStatsBucket(final NetworkStats.Bucket mobile, final NetworkStats.Bucket wifi) {
        this.mobile = mobile;
        this.wifi = wifi;
    }
    NetworkStats.Bucket getMobileBucket() { return mobile; }
    NetworkStats.Bucket getWifiBucket() { return wifi; }
    static List<NetworkStatsBucket> getBuckets() { return buckets; }
    static void addNew(final Context context) {
        Log.d(TAG, new Object(){}.getClass().getEnclosingMethod().getName());
        NetworkStatsManager netStatsMgr = context.getSystemService(NetworkStatsManager.class);
        String id = context.getSystemService(TelephonyManager.class).getSubscriberId();
        final long start = Activity.getInstallationTime(context);
        final long end = System.currentTimeMillis();
        try {
            NetworkStats.Bucket mobile = netStatsMgr.querySummaryForUser(ConnectivityManager.TYPE_MOBILE, id, start, end);
            NetworkStats.Bucket wifi = netStatsMgr.querySummaryForUser(ConnectivityManager.TYPE_WIFI, "", start, end);
            buckets.add(0, new NetworkStatsBucket(mobile, wifi));
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
        }
    }
    static boolean none() { return buckets == null || buckets.size() == 0; }
    static void init() { buckets = new ArrayList<>(); }
}
