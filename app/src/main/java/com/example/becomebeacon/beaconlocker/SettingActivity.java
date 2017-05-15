package com.example.becomebeacon.beaconlocker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
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

    private final int CHECK_GPS = 3232;
    private BluetoothScan bs;
    private Switch scanOnOff;
    private Switch gpsSwitch;
    private TextView scanPeriod;
    private GpsInfo Gps;
    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Gps = new GpsInfo(this,this);



        bs=new BluetoothScan(null);

        scanOnOff=(Switch)findViewById(R.id.scan_on_off);
        scanPeriod=(TextView)findViewById(R.id.scan_period);
        gpsSwitch=(Switch) findViewById(R.id.GpsBotton);


        scanOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    bs.checkBluetooth();
                    Values.useBLE=true;
                }
                else
                {
                    Values.useBLE=false;
                }
            }
        });

        gpsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    if(Gps.GpsEnabled() || Gps.NetworkEnabled()) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, CHECK_GPS);
                    }

                }
                else
                {
                    Values.useGPS=false;
                }
            }
        });




        pref = getSharedPreferences("pref", AppCompatActivity.MODE_PRIVATE); // Shared Preference를 불러옵니다.
        // 저장된 값들을 불러옵니다.



    }

    public void onResume()
    {
        super.onResume();
        int scanTime = pref.getInt("ScanPeriod", Values.scanBreakTime);
        Boolean useScan = pref.getBoolean("UseScan", true);
        Boolean useGPS = pref.getBoolean("UseGPS",true);


        scanOnOff.setChecked(useScan);
        scanPeriod.setText(""+scanTime/1000);
        gpsSwitch.setChecked(useGPS);
    }

    public void onStop()
    {
        super.onStop();
        SharedPreferences pref = getSharedPreferences("pref", AppCompatActivity.MODE_PRIVATE);

        SharedPreferences.Editor editor = pref.edit(); // Editor를 불러옵니다.

        Values.scanBreakTime=Integer.valueOf(scanPeriod.getText().toString())*1000;


        // 저장할 값들을 입력합니다.
        editor.putInt("ScanPeriod", (Integer.valueOf(scanPeriod.getText().toString()))*1000);
        editor.putBoolean("UseScan", scanOnOff.isChecked());
        editor.putBoolean("UseGPS",gpsSwitch.isChecked());

        editor.commit();



    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CHECK_GPS) {
            if(Gps.locationManager==null)
            {
                Gps.locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            }
            if(!Gps.locationManager.isProviderEnabled(Gps.locationManager.GPS_PROVIDER))
            {
                Values.useGPS=false;
                gpsSwitch.setChecked(false);
            }
            else
            {
                Values.useGPS=true;
                gpsSwitch.setChecked(true);
            }

        }

    }

}
