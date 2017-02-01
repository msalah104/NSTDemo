package com.orang.nstdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class  Activity extends android.app.Activity {

    private static final String TAG = new Object(){}.getClass().getEnclosingClass().getSimpleName();

    private static final int PERMISSION_ID = 1;

    Helper helper;
    ListView listView ;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity);

        helper = Helper.getSharedInstance(this);

        listView = (ListView) findViewById(R.id.list);
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, helper.getListOfRecords());

        listView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        helper.setActivity(this);

        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[] {Manifest.permission.READ_PHONE_STATE}, PERMISSION_ID );
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        helper.setActivity(null);
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
}
