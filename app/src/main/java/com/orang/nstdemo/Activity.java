package com.orang.nstdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class Activity extends AppCompatActivity {

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

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_NETWORK_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_NETWORK_STATE,
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
