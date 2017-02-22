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

        NetworkStatsBucket newBucket = getItem(position);
        NetworkStatsBucket oldBucket = getItem(position < getCount() - 1 ? position + 1 : position);

        if (newBucket == null) return convertView;

        TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
        String newString = String.format(
            getContext().getResources().getString(R.string.query_summary),
            getCount() - position - 1,
            formatDate(newBucket.getMobile().getEndTimeStamp()),
            newBucket.getMobile().getRxBytes(),
            newBucket.getMobile().getTxBytes(),
            newBucket.getWifi().getRxBytes(),
            newBucket.getWifi().getTxBytes());
        SpannableString newSpan = new SpannableString(newString);
        int i = 0;
        i = colorize(newString, newSpan, i, newBucket.getMobile().getRxBytes(), oldBucket.getMobile().getRxBytes());
        i = colorize(newString, newSpan, i, newBucket.getMobile().getTxBytes(), oldBucket.getMobile().getTxBytes());
        i = colorize(newString, newSpan, i, newBucket.getWifi().getRxBytes(), oldBucket.getWifi().getRxBytes());
        i = colorize(newString, newSpan, i, newBucket.getWifi().getTxBytes(), oldBucket.getWifi().getTxBytes());
        textView.setText(newSpan);
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
