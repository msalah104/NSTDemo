package com.orang.nstdemo;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

class Helper {

    static final String DATA_USAGE_LIST = "data_usage_list";
    static final String FORMATTER = "dd/MM/yyyy hh:mm:ss.SSS";
    static final String EMPTY_STRING = "";
    private static final String TAG = "MainActivity";
    private static final String STRING_SPLITTER = "#";


    NetworkStatsManager networkStatsManager;
    private PendingIntent pendingIntent;
    Context context;

    public Helper(Context context) {
        this.context = context;
    }

    void addNewQuery() {
        String newQuery = queryForDataUsage();
        String [] records = getListOfRecords();
        records = appendValue(records, newQuery);
        updateData(records);

        if (context instanceof MainActivity){
            ((MainActivity)context).adapter = new ArrayAdapter<String>(context,
                    android.R.layout.simple_list_item_1, android.R.id.text1, records);

            ((MainActivity)context).listView.setAdapter(((MainActivity)context).adapter);
        }
    }


    public String queryForDataUsage() {

        networkStatsManager = (NetworkStatsManager) context.getSystemService(
                Context.NETWORK_STATS_SERVICE);

        String mobile_id = getMobileSubscribeId();
        long endTime = System.currentTimeMillis();
        long startTime = getInstallationTime();
        try {
            NetworkStats.Bucket mobileUserQueryBucket = networkStatsManager.querySummaryForUser(ConnectivityManager.TYPE_MOBILE, mobile_id, startTime, endTime);
            NetworkStats.Bucket wifiUserQueryBucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_WIFI, EMPTY_STRING, startTime, endTime);

            String querySummary = context.getResources().getString(R.string.query_summary);
            querySummary = String.format(querySummary, mobileUserQueryBucket.getRxBytes(),
                    mobileUserQueryBucket.getTxBytes(),
                    wifiUserQueryBucket.getRxBytes(),
                    wifiUserQueryBucket.getTxBytes(),
                    getDate(startTime),
                    getDate(endTime));

            return querySummary;

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return "";
    }

    void updateData(String [] records) {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < records.length; i++) {
            sb.append(records[i]).append(STRING_SPLITTER);
        }

        SharedPreferences pref = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(DATA_USAGE_LIST, sb.toString());
        editor.commit();

    }

    String [] getListOfRecords () {
        SharedPreferences pref = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        String stringRecords = pref.getString(DATA_USAGE_LIST, EMPTY_STRING);
        if (!stringRecords.isEmpty()){
            String [] records = stringRecords.split(STRING_SPLITTER);
            return records;
        } else  {
            return new String[] {};
        }

    }

    public void addNewAlarm () {
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int interval = 5000;

        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
    }

    private String[] appendValue(String[] obj, String newObj) {
        ArrayList<String> temp = new ArrayList<String>(Arrays.asList(obj));
        temp.add(0, newObj);
        return temp.toArray(new String[temp.size()]);
    }

    public static String getDate(long milliSeconds)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(FORMATTER);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }


    public String getMobileSubscribeId() {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getSubscriberId();
    }

    public long getInstallationTime() {
        try {
            long time = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0)
                    .lastUpdateTime;
            return time;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

}


public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_ID = 1;

    Helper helper;
    ListView listView ;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE,
                    },
                    PERMISSION_ID);
        }

        helper = new Helper(this);

        listView = (ListView) findViewById(R.id.list);
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, helper.getListOfRecords());

        listView.setAdapter(adapter);
        helper.addNewAlarm();
    }

    @Override
    protected void onResume() {
        super.onResume();

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, helper.getListOfRecords());

        listView.setAdapter(adapter);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (requestCode == PERMISSION_ID && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                helper.addNewAlarm();
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                this.startActivity(intent);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);//Menu Resource, Menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                for(int i=0; i < 10 ; i++)
                    helper.addNewQuery();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
