package com.example.becomebeacon.beaconlocker;

import android.content.Intent;
import android.content.SharedPreferences;
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

/**
 * Created by 王楠 on 2017/5/13.
 */

public class SettingActivity extends AppCompatActivity {

    private BluetoothScan bs;
    private Switch scanOnOff;
    private Button GpsBotton;
    private TextView scanPeriod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        scanOnOff=(Switch)findViewById(R.id.scan_on_off);
        scanPeriod=(TextView)findViewById(R.id.scan_period);
        GpsBotton=(Button) findViewById(R.id.GpsBotton);

        bs=new BluetoothScan(null);



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

        GpsBotton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        }
        ) ;



        SharedPreferences pref = getSharedPreferences("pref", AppCompatActivity.MODE_PRIVATE); // Shared Preference를 불러옵니다.
        // 저장된 값들을 불러옵니다.
        int scanTime = pref.getInt("ScanPeriod", Values.scanBreakTime);
        Boolean useScan = pref.getBoolean("UseScan", true);


        scanOnOff.setChecked(useScan);
        scanPeriod.setText(""+scanTime/1000);


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

        editor.commit();



    }

}
