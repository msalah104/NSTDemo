package com.orange.nstdemo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

class NetworkStatsBucket {
    private static final String TAG = new Object(){}.getClass().getEnclosingClass().getSimpleName();
    private static List<NetworkStatsBucket> buckets;
    private final NetworkStats.Bucket mobile, wifi;

    private NetworkStatsBucket(final Context context) {
        final long start = ListActivity.getInstallationTime(context);
        final long end = System.currentTimeMillis();
        this.mobile =  queryMobileSummaryForUser(context, start, end);
        this.wifi = queryWifiSummaryForUser(context, start, end);
    }
    NetworkStats.Bucket getMobile() { return mobile; }
    NetworkStats.Bucket getWifi() { return wifi; }

    static List<NetworkStatsBucket> getBuckets() { return buckets; }

    static void addNew(final Context context) {
        Log.i(TAG, new Object(){}.getClass().getEnclosingMethod().getName());
        buckets.add(0, new NetworkStatsBucket(context));
    }

    private static NetworkStats.Bucket queryMobileSummaryForUser(final Context context, final long start, final long end) {
        final String id = context.getSystemService(TelephonyManager.class).getSubscriberId();
        return querySummaryForUser(context, ConnectivityManager.TYPE_MOBILE, id, start, end);
    }

    private static NetworkStats.Bucket queryWifiSummaryForUser(final Context context, final long start, final long end) {
        return querySummaryForUser(context, ConnectivityManager.TYPE_WIFI, "", start, end);
    }

    private static NetworkStats.Bucket querySummaryForUser(final Context context, final int networkType, String subscriberId, final long start, final long end) {
        final NetworkStatsManager netStatsMgr = context.getSystemService(NetworkStatsManager.class);
        try {
            return netStatsMgr.querySummaryForUser(networkType, subscriberId, start, end);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
        }
        return null;
    }

    static void notifyIfDecrease(final Context context) {
        final NetworkStats.Bucket lastMobileBucket = buckets.get(0).getMobile();
        final NetworkStats.Bucket lastWifiBucket = buckets.get(0).getWifi();
        final NetworkStats.Bucket previousMobileBucket = buckets.get(1).getMobile();
        final NetworkStats.Bucket previousWifiBucket = buckets.get(1).getWifi();
        if (previousMobileBucket.getRxBytes() <= lastMobileBucket.getRxBytes() &&
            previousMobileBucket.getTxBytes() <= lastMobileBucket.getTxBytes() &&
            previousWifiBucket.getRxBytes() <= lastWifiBucket.getRxBytes() &&
            previousWifiBucket.getTxBytes() <= lastWifiBucket.getTxBytes()) return;

        final PendingIntent pending = TaskStackBuilder.create(context)
            .addParentStack(ListActivity.class)
            .addNextIntent(new Intent(context, ListActivity.class))
            .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        final Notification notification = new Notification.Builder(context)
            .setContentTitle(context.getString(R.string.count_error))
            .setContentText(NetworkStatsAdapter.dateIso8601(lastMobileBucket.getEndTimeStamp()))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pending)
            .build();
        context.getSystemService(NotificationManager.class).notify(0, notification);

    }

    static boolean none() { return buckets == null || buckets.size() == 0; }
    static void init() { buckets = new ArrayList<>(); }
}
