package com.example.becomebeacon.beaconlocker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.widget.Switch;
import android.widget.TextView;

import com.example.becomebeacon.beaconlocker.R;

/**
 * Created by 王楠 on 2017/5/13.
 */

public class SettingActivity extends AppCompatActivity {

    private Switch scanOnOff;
    private TextView scanPeriod;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        scanOnOff=(Switch)findViewById(R.id.scan_on_off);
        scanPeriod=(TextView)findViewById(R.id.scan_period);


        SharedPreferences pref = getSharedPreferences("pref", AppCompatActivity.MODE_PRIVATE); // Shared Preference를 불러옵니다.


        // 저장된 값들을 불러옵니다.
        float scanTime = pref.getFloat("ScanPeriod", (float)5.0);
        Boolean useScan = pref.getBoolean("UseScan", true);

        scanOnOff.setChecked(useScan);
        scanPeriod.setText(""+scanTime);


    }

    public void onStop()
    {
        super.onStop();
        SharedPreferences pref = getSharedPreferences("pref", AppCompatActivity.MODE_PRIVATE);

        SharedPreferences.Editor editor = pref.edit(); // Editor를 불러옵니다.

        // 저장할 값들을 입력합니다.
        editor.putFloat("ScanPeriod", Float.valueOf(scanPeriod.getText().toString()));
        editor.putBoolean("UseScan1", scanOnOff.isChecked());

        editor.commit();



    }

}
