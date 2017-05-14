package com.example.becomebeacon.beaconlocker;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import static android.view.View.VISIBLE;

/**
 * Created by 함상혁입니다 on 2017-05-14.
 */

public class BeaconDetailsActivity extends AppCompatActivity {

    private BleDeviceInfo item;
    private TextView nickName;
    private TextView address;
    private TextView meter;
    static private BeaconDetailsActivity mContext;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_meter);
        mContext=this;

        item=DeviceInfoStore.getBleInfo();
        nickName=(TextView)findViewById(R.id.et_NICKNAME);
        address=(TextView)findViewById(R.id.et_Address);
        meter=(TextView)findViewById(R.id.meter);

        Log.d("BDA","item: "+item.nickname+", "+item.devAddress);
        nickName.setText(item.nickname);
        address.setText(item.devAddress);

        meter.setText(String.format("%.2f",item.distance2));




    }

    @Override
    public void onDestroy()
    {
        Log.d("BDA","BDA destroyed");
        mContext=null;
        super.onDestroy();
    }

    static public BeaconDetailsActivity getBDA()
    {
        return mContext;
    }

    public void refreshDistance()
    {
        meter.setText(String.format("%.2f",item.distance2));
    }


}
