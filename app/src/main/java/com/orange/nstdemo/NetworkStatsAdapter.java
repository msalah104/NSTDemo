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
import java.util.Date;
import java.util.Locale;

class NetworkStatsAdapter extends ArrayAdapter<NetworkStatsBucket> {

    private static final ForegroundColorSpan RED = new ForegroundColorSpan(Color.RED);

    NetworkStatsAdapter(final Context context) {
        super(context, android.R.layout.simple_list_item_2, NetworkStatsBucket.getBuckets());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            convertView = layoutInflater.inflate(android.R.layout.simple_list_item_2, null);
        }

        final NetworkStatsBucket last = getItem(position);
        final NetworkStatsBucket previous = getItem(position < getCount() - 1 ? position + 1 : position);

        if (last == null) return convertView;

        final String string = String.format(
            getContext().getResources().getString(R.string.query_summary),
            getCount() - position - 1,
            dateIso8601(last.getMobile().getEndTimeStamp()),
            last.getMobile().getRxBytes(),
            last.getMobile().getTxBytes(),
            last.getWifi().getRxBytes(),
            last.getWifi().getTxBytes());
        final SpannableString span = new SpannableString(string);
        int i = string.indexOf('.');
        i = colorize(string, span, i, last.getMobile().getRxBytes(), previous.getMobile().getRxBytes());
        i = colorize(string, span, i, last.getMobile().getTxBytes(), previous.getMobile().getTxBytes());
        i = colorize(string, span, i, last.getWifi().getRxBytes(), previous.getWifi().getRxBytes());
        i = colorize(string, span, i, last.getWifi().getTxBytes(), previous.getWifi().getTxBytes());
        ((TextView) convertView.findViewById(android.R.id.text1)).setText(span);

        return convertView;
    }

    private int colorize(final String string, final SpannableString span, final int start, final long last, final long previous) {
        final int i = string.indexOf(':', start) + 1;
        final int j = string.indexOf(' ', i);
        if (last < previous) {
            span.setSpan(CharacterStyle.wrap(RED), i, j, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return j;
    }

    static String dateIso8601(final long date) {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ", Locale.US).format(new Date(date));
    }

}
