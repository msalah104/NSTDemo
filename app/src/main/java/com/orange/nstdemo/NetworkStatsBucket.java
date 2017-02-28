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
    private static final String NOTIFICATION_TITLE = "NST_DEMO";
    private static final String NOTIFICATION_CONTENT = "Issue has been raised";
    private static final String NOTIFICATION_TICKER = "NST_DEMO: New Message Alert!";
    private static List<NetworkStatsBucket> buckets; // Mobile * Wi-Fi Buckets
    private final NetworkStats.Bucket mobile, wifi;

    private NetworkStatsBucket(final NetworkStats.Bucket mobile, final NetworkStats.Bucket wifi) {
        this.mobile = mobile;
        this.wifi = wifi;
    }

    NetworkStats.Bucket getMobile() {
        return mobile;
    }

    NetworkStats.Bucket getWifi() {
        return wifi;
    }

    static List<NetworkStatsBucket> getBuckets() {
        return buckets;
    }

    static void addNew(final Context context) {
        Log.i(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
        final long start = ListActivity.getInstallationTime(context);
        final long end = System.currentTimeMillis();
        NetworkStats.Bucket mobile = getMobileUsage(context, start, end);
        NetworkStats.Bucket wifi = getWifiUsage(context, start, end);
        buckets.add(0, new NetworkStatsBucket(mobile, wifi));
    }

    private static NetworkStats.Bucket getMobileUsage(final Context context, long start, long end) {
        String id = context.getSystemService(TelephonyManager.class).getSubscriberId();
        return getUsage(context, ConnectivityManager.TYPE_MOBILE, id, start, end);
    }

    private static NetworkStats.Bucket getWifiUsage(final Context context, long start, long end) {
        return getUsage(context, ConnectivityManager.TYPE_WIFI, "", start, end);
    }

    private static NetworkStats.Bucket getUsage(final Context context, final int networkType, String subscriberId, long start, long end) {
        final NetworkStatsManager netStatsMgr = context.getSystemService(NetworkStatsManager.class);
        try {
            return netStatsMgr.querySummaryForUser(networkType, subscriberId, start, end);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
        }
        return null;
    }

    static void notifyIfDecrease(final Context context) {
        NetworkStats.Bucket newMobileBucket = buckets.get(0).getMobile();
        NetworkStats.Bucket newWifiBucket = buckets.get(0).getWifi();
        NetworkStats.Bucket oldMobileBucket = buckets.get(1).getMobile();
        NetworkStats.Bucket oldWifiBucket = buckets.get(1).getWifi();
        if (oldMobileBucket.getRxBytes() <= newMobileBucket.getRxBytes() &&
            oldMobileBucket.getTxBytes() <= newMobileBucket.getTxBytes() &&
            oldWifiBucket.getRxBytes() <= newWifiBucket.getRxBytes() &&
            oldWifiBucket.getTxBytes() <= newWifiBucket.getTxBytes()) return;

        Intent notificationIntent = new Intent(context, ListActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(ListActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(context);

        Notification notification = builder.setContentTitle(context.getString(R.string.count_error))
                                           .setContentText(NetworkStatsAdapter.formatDate(newMobileBucket.getEndTimeStamp()))
                                           .setSmallIcon(R.mipmap.ic_launcher)
                                           .setContentIntent(pendingIntent).build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0,notification);

    }

    static boolean none() { return buckets == null || buckets.size() == 0; }
    static void init() { buckets = new ArrayList<>(); }
}
