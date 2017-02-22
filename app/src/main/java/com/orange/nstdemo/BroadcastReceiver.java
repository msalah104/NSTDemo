package com.orange.nstdemo;

import android.content.Context;
import android.content.Intent;

import java.util.function.BiConsumer;

public class BroadcastReceiver extends android.content.BroadcastReceiver {
    private final BiConsumer<Context, Intent> consumer;
    public BroadcastReceiver() {
        consumer = null;
    }
    public BroadcastReceiver(final BiConsumer<Context, Intent> onReceive) {
        consumer = onReceive;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (consumer == null) throw new UnsupportedOperationException("Not yet implemented");
        consumer.accept(context, intent);
    }
}
