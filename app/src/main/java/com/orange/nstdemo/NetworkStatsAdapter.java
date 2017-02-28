package com.orange.nstdemo;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
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

        NetworkStatsBucket lastBucket = getItem(position);
        NetworkStatsBucket previousBucket = getItem(position < getCount() - 1 ? position + 1 : position);

        if (lastBucket == null) return convertView;

        TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
        String string = String.format(
            getContext().getResources().getString(R.string.query_summary),
            getCount() - position - 1,
            formatDate(lastBucket.getMobile().getEndTimeStamp()),
            lastBucket.getMobile().getRxBytes(),
            lastBucket.getMobile().getTxBytes(),
            lastBucket.getWifi().getRxBytes(),
            lastBucket.getWifi().getTxBytes());
        SpannableString span = new SpannableString(string);
        int i = string.indexOf('.');
        i = colorize(string, span, i, lastBucket.getMobile().getRxBytes(), previousBucket.getMobile().getRxBytes());
        i = colorize(string, span, i, lastBucket.getMobile().getTxBytes(), previousBucket.getMobile().getTxBytes());
        i = colorize(string, span, i, lastBucket.getWifi().getRxBytes(), previousBucket.getWifi().getRxBytes());
        i = colorize(string, span, i, lastBucket.getWifi().getTxBytes(), previousBucket.getWifi().getTxBytes());
        textView.setText(span);

        return convertView;
    }

    private int colorize(String string, SpannableString span, final int start, final long newBytes, final long oldBytes) {
        int i = string.indexOf(':', start) + 1;
        int j = string.indexOf(' ', i);
        if (newBytes < oldBytes) span.setSpan(CharacterStyle.wrap(RED), i, j, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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
