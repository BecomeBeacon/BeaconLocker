package com.example.becomebeacon.beaconlocker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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
        try {
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
        } catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10900", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    public void onResume()
    {
        try {
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
            useGPS=false;
        }
        else
        {

        }

            scanPeriod.setText(""+scanTime/1000);
            changeScan(useScan);
            changeGPS(useGPS);
            //gpsSwitch.setChecked(useGPS);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10901", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    public void onStop()
    {
        try {
            super.onStop();


            Values.scanBreakTime=Integer.valueOf(scanPeriod.getText().toString())*1000;

            // 저장할 값들을 입력합니다.
            editor.putInt("ScanPeriod", (Integer.valueOf(scanPeriod.getText().toString()))*1000);
            editor.putBoolean("UseScan", scanOnOff.isChecked());
            editor.putBoolean("UseGPS",gpsSwitch.isChecked());

            editor.commit();
        } catch (NumberFormatException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10902", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    public void onDestroy()
    {
        try {
            super.onDestroy();

            Values.scanBreakTime=Integer.valueOf(scanPeriod.getText().toString())*1000;

            // 저장할 값들을 입력합니다.
            editor.putInt("ScanPeriod", (Integer.valueOf(scanPeriod.getText().toString()))*1000);
            editor.putBoolean("UseScan", scanOnOff.isChecked());
            editor.putBoolean("UseGPS",gpsSwitch.isChecked());

            editor.commit();

            mContext=null;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10903", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        try {
            switch(item.getItemId()) {
                case android.R.id.home:
                    finish();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10904", Toast.LENGTH_LONG).show();
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        try {
            super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Values.CHECK_GPS) {

            if(Gps.locationManager==null)
            {
                Gps.locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            }
            if(!Gps.GpsEnabled())
            {

                Values.useGPS=false;
                changeGPS(false);
                //gpsSwitch.setChecked(false);
            }
            else
            {

                Values.useGPS=true;
                changeGPS(true);
                //.setChecked(true);
            }

            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10905", Toast.LENGTH_LONG).show();
            finish();
        }
    }
    class myListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            if(buttonView==gpsSwitch) {
                if (isChecked) {

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
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10906", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    public void changeGPS(boolean op)
    {
        try {
            gpsSwitch.setOnCheckedChangeListener(null);
            gpsSwitch.setChecked(op);
            gpsSwitch.setOnCheckedChangeListener(new myListener());


            Values.useGPS=op;

            editor.putBoolean("UseGPS",op);
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10907", Toast.LENGTH_LONG).show();
            finish();
        }

    }

    public void changeScan(boolean op)
    {
        try {
            scanOnOff.setOnCheckedChangeListener(null);
            scanOnOff.setChecked(op);
            scanOnOff.setOnCheckedChangeListener(new myListener());


            Values.useBLE=op;

            editor.putBoolean("UseScan",op);
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10908", Toast.LENGTH_LONG).show();
            finish();
        }

    }

}
