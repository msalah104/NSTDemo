package com.orange.nstdemo;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

class NetworkStatsAdapter extends ArrayAdapter<NetworkStatsBucket> {

    //private static final String TAG = new Object(){}.getClass().getEnclosingClass().getSimpleName();
    private static final ForegroundColorSpan RED = new ForegroundColorSpan(Color.RED);
    private static final String FORMATTER = "dd/MM/yyyy hh:mm:ss.SSS";

    NetworkStatsAdapter(Context context) {
        super(context, android.R.layout.simple_list_item_2, NetworkStatsBucket.getBuckets());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            convertView = layoutInflater.inflate(android.R.layout.simple_list_item_2, null);
        }

        NetworkStatsBucket networkStatsBucket0 = getItem(position);
        NetworkStatsBucket networkStatsBucket1 = getItem(position < getCount() - 1 ? position + 1 : position);

        if (networkStatsBucket0 == null) return convertView;

        TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
        String string = String.format(
            getContext().getResources().getString(R.string.query_summary),
            getCount() - position - 1,
            formatDate(networkStatsBucket0.getMobileBucket().getEndTimeStamp()),
            networkStatsBucket0.getMobileBucket().getRxBytes(),
            networkStatsBucket0.getMobileBucket().getTxBytes(),
            networkStatsBucket0.getWifiBucket().getRxBytes(),
            networkStatsBucket0.getWifiBucket().getTxBytes());
        SpannableString span = new SpannableString(string);
        int i = 0;
        i = colorize(string, span, i, networkStatsBucket0.getMobileBucket().getRxBytes(), networkStatsBucket1.getMobileBucket().getRxBytes());
        i = colorize(string, span, i, networkStatsBucket0.getMobileBucket().getTxBytes(), networkStatsBucket1.getMobileBucket().getTxBytes());
        i = colorize(string, span, i, networkStatsBucket0.getWifiBucket().getRxBytes(), networkStatsBucket1.getWifiBucket().getRxBytes());
        i = colorize(string, span, i, networkStatsBucket0.getWifiBucket().getTxBytes(), networkStatsBucket1.getWifiBucket().getTxBytes());
        textView.setText(span);
        return convertView;
    }

    private int colorize(String string, SpannableString span, final int start, final long newBytes, final long oldBytes) {
        int i = string.indexOf(':', start);
        int j = string.indexOf(' ', i);
        if (newBytes < oldBytes) span.setSpan(RED, i + 1, j, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return j;
    }

    static String formatDate(long milliSeconds) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(FORMATTER);
        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

}
