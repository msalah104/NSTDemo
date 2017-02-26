package com.orange.nstdemo;

import android.content.Intent;

public class IntentService extends android.app.IntentService {
    private static final String TAG = new Object(){}.getClass().getEnclosingClass().getSimpleName();
    public IntentService() {
        super(TAG);
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        if (NetworkStatsBucket.none()) {
            NetworkStatsBucket.init();
        }

        NetworkStatsBucket.updateLog(this);
        sendBroadcast(new Intent(ListActivity.UPDATE));
    }
}
