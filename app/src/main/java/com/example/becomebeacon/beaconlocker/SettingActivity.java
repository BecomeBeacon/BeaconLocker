package com.example.becomebeacon.beaconlocker;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.example.becomebeacon.beaconlocker.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;

/**
 * Created by 王楠 on 2017/5/13.
 */

public class SettingActivity extends AppCompatActivity {

    public static SettingActivity mContext;
    private final int CHECK_GPS = 3232;
    private BluetoothScan bs;
    private Switch scanOnOff;
    private Switch gpsSwitch;
    private TextView scanPeriod;
    private GpsInfo Gps;
    SharedPreferences pref;

    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        //툴바 세팅
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_additem);
        setSupportActionBar(toolbar);

        toolbar.setTitle(R.string.app_name);
        toolbar.setSubtitle("세팅");

        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setSubtitleTextColor(ContextCompat.getColor(SettingActivity.this, R.color.colorSubtitle));

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Gps = new GpsInfo(this,this);
        mContext=this;

        pref = getSharedPreferences("pref", AppCompatActivity.MODE_PRIVATE);

        editor = pref.edit(); // Editor를 불러옵니다.

        scanOnOff=(Switch)findViewById(R.id.scan_on_off);
        scanPeriod=(TextView)findViewById(R.id.scan_period);
        gpsSwitch=(Switch) findViewById(R.id.GpsBotton);

        bs=new BluetoothScan(null);



        scanOnOff.setOnCheckedChangeListener(new myListener());
        gpsSwitch.setOnCheckedChangeListener(new myListener());
    }

    public void onResume()
    {
        Log.d("SETTING","on resume");
        super.onResume();
        int scanTime = pref.getInt("ScanPeriod", Values.scanBreakTime);
        Boolean useScan = pref.getBoolean("UseScan", true);
        Boolean useGPS = pref.getBoolean("UseGPS",true);

        if(!bs.isBleOn())
        {
            useScan=false;
        }

        if(!Gps.GpsEnabled())
        {
            Log.d("SETTING","gps isn't able");
            useGPS=false;
        }
        else
        {
            Log.d("SETTING","gps is able");
        }

        scanPeriod.setText(""+scanTime/1000);
        changeScan(useScan);
        changeGPS(useGPS);
        //gpsSwitch.setChecked(useGPS);



    }

    public void onStop()
    {
        super.onStop();


        Values.scanBreakTime=Integer.valueOf(scanPeriod.getText().toString())*1000;

        // 저장할 값들을 입력합니다.
        editor.putInt("ScanPeriod", (Integer.valueOf(scanPeriod.getText().toString()))*1000);
        editor.putBoolean("UseScan", scanOnOff.isChecked());
        editor.putBoolean("UseGPS",gpsSwitch.isChecked());

        editor.commit();



    }

    public void onDestroy()
    {
        super.onDestroy();

        Values.scanBreakTime=Integer.valueOf(scanPeriod.getText().toString())*1000;

        // 저장할 값들을 입력합니다.
        editor.putInt("ScanPeriod", (Integer.valueOf(scanPeriod.getText().toString()))*1000);
        editor.putBoolean("UseScan", scanOnOff.isChecked());
        editor.putBoolean("UseGPS",gpsSwitch.isChecked());

        editor.commit();

        mContext=null;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Values.CHECK_GPS) {
            Log.d("SETTING","in onActivityResult");
            if(Gps.locationManager==null)
            {
                Gps.locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            }
            if(!Gps.GpsEnabled())
            {
                Log.d("SETTING","1gps is "+Gps.isGPSEnabled);
                Values.useGPS=false;
                changeGPS(false);
                //gpsSwitch.setChecked(false);
            }
            else
            {
                Log.d("SETTING","2gps is "+Gps.isGPSEnabled);
                Values.useGPS=true;
                changeGPS(true);
                //.setChecked(true);
            }

        }


    }
    class myListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            if(buttonView==gpsSwitch) {
                if (isChecked) {
                    Log.d("SETTING", "in gpsSwitchListener");
//                if(!Gps.GpsEnabled()) {
//                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                    startActivityForResult(intent, CHECK_GPS);
//                }
                    if (!Gps.GpsEnabled()) {
                        Gps.showSettingsAlert();
                    }

                } else {
                    Values.useGPS = false;
                    changeGPS(false);
                }
            }
            else if(buttonView==scanOnOff)
            {
                if(isChecked)
                {
                    Values.useBLE=true;
                    editor.putBoolean("UseScan",true);
                    bs.checkBluetooth();
                }
                else
                {
                    Values.useBLE=false;
                    editor.putBoolean("UseScan",false);
                }
                editor.commit();
            }
        }
    }

    public void changeGPS(boolean op)
    {
        gpsSwitch.setOnCheckedChangeListener(null);
        gpsSwitch.setChecked(op);
        gpsSwitch.setOnCheckedChangeListener(new myListener());


        Values.useGPS=op;

        editor.putBoolean("UseGPS",op);
        editor.commit();

    }

    public void changeScan(boolean op)
    {
        scanOnOff.setOnCheckedChangeListener(null);
        scanOnOff.setChecked(op);
        scanOnOff.setOnCheckedChangeListener(new myListener());


        Values.useBLE=op;

        editor.putBoolean("UseScan",op);
        editor.commit();

    }

}
