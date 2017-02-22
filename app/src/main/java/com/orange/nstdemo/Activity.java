package com.orange.nstdemo;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.BaseAdapter;

public class  Activity extends android.app.ListActivity {

    private static final String TAG = new Object(){}.getClass().getEnclosingClass().getSimpleName();

    Parcelable state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, new Object(){}.getClass().getEnclosingMethod().getName());
        super.onCreate(savedInstanceState);
        setTitle(NetworkStatsAdapter.formatDate(getInstallationTime(this)));
        Intent intent = new Intent(getApplicationContext(), BroadcastReceiver.class);
        final PendingIntent pending = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        getSystemService(AlarmManager.class).setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 10000, pending);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (state != null) {
            getListView().onRestoreInstanceState(state);
        } else {
            if (NetworkStatsBucket.none()) NetworkStatsBucket.init();
            setListAdapter(new NetworkStatsAdapter(this));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[] {Manifest.permission.READ_PHONE_STATE}, 0);
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
    
    static long getInstallationTime(final Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).lastUpdateTime;
        } catch (PackageManager.NameNotFoundException e) {
            return Long.MAX_VALUE;
        }
    }
}
