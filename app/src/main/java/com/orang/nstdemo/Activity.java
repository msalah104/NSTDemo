package com.orang.nstdemo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class  Activity extends android.app.Activity {

    private static final String TAG = new Object(){}.getClass().getEnclosingClass().getSimpleName();

    private static final int PERMISSION_ID = 1;
    private static final String DATA_USAGE_LIST = "data_usage_list";
    private static final String STRING_SPLITTER = "#";

    Helper helper;
    ListView listView;
    ArrayAdapter<String> adapter;
    List<String> records;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity);
        helper = Helper.getSharedInstance(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        helper.setActivity(this);
        listView = (ListView) findViewById(R.id.list);
        records = getListOfRecords();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, records);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[] {Manifest.permission.READ_PHONE_STATE}, PERMISSION_ID );
        }
    }

    @Override
    protected void onStop() {
        helper.setActivity(null);
        updateData(records);
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (requestCode == PERMISSION_ID && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                this.startActivityForResult(intent, PERMISSION_ID);

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Assuming user has granted the uage permission
        if (requestCode == PERMISSION_ID) {
            helper.setActivity(this);
            helper.addNewQuery();
            helper.addNewAlarm();
        }
    }

    List<String> getListOfRecords() {
        SharedPreferences pref = getSharedPreferences(TAG, Context.MODE_PRIVATE);
        String stringRecords = pref.getString(DATA_USAGE_LIST, "");
        if (!stringRecords.isEmpty()){
            return new ArrayList<>(Arrays.asList(stringRecords.split(STRING_SPLITTER)));
        } else  {
            return new ArrayList<>();
        }
    }

    private void updateData(List<String> records) {
        StringBuilder builder = new StringBuilder();
        for (String record: records) builder.append(record).append(STRING_SPLITTER);
        SharedPreferences pref = getSharedPreferences(TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(DATA_USAGE_LIST, builder.toString());
        editor.apply();
    }
}
