package com.orange.nstdemo;

import android.content.Intent;
import android.util.Log;

public class IntentService extends android.app.IntentService {
    private static final String TAG = new Object(){}.getClass().getEnclosingClass().getSimpleName();
    public IntentService() {
        super(TAG);
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, new Object(){}.getClass().getEnclosingMethod().getName());
        NetworkStatsBucket.addNew(this);
        sendBroadcast(new Intent("foo"));
    }
}
