package com.example.becomebeacon.beaconlocker;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

/**
 * Created by 함상혁입니다 on 2017-04-24.
 */

public class BluetoothService {

    private String TAG="BluetoothService";
    private Activity mActivity;
    private final int REQUEST_CONNECT_DEVICE=11111;

    BluetoothService(Activity ma)
    {
        mActivity=ma;
    }

    public void scanDevice()
    {
        Log.d(TAG, "Scan Device");
        Intent serverIntent = new Intent(mActivity, DeviceListActivity.class);
        mActivity.startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }



}
