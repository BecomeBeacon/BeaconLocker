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

    private BluetoothAdapter btAdapter;

    private String TAG="BluetoothService";
    private Activity mActivity;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private Handler mHandler;

    BluetoothService(Activity ma,  Handler h)
    {
        mActivity=ma;
        mHandler=h;

        btAdapter = BluetoothAdapter.getDefaultAdapter();

    }

    public boolean getDeviceState() {
        Log.d(TAG, "Check the Bluetooth support");
        if(btAdapter == null) { Log.d(TAG, "Bluetooth is not available");
            return false;
        } else {
            Log.d(TAG, "Bluetooth is available");
            return true;
        }
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK)
                {
                    // 확인 눌렀을 때
                    // Next Step
                } else {
                    // 취소 눌렀을 때
                    Log.d(TAG, "Bluetooth is not enabled");
                }
                break;
        }


    }

    public void enableBluetooth()
    {
        Log.i(TAG, "Check the enabled Bluetooth");
        if(btAdapter.isEnabled())
        {
            // 기기의 블루투스 상태가 On인 경우
            Log.d(TAG, "Bluetooth Enable Now");
            // Next Step
        } else {
            // 기기의 블루투스 상태가 Off인 경우
            Log.d(TAG, "Bluetooth Enable Request");
            Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mActivity.startActivityForResult(i, REQUEST_ENABLE_BT);
        }
    }

    public void checkBluetooth()
    {
        if(getDeviceState()) {
            // 블루투스가 지원 가능한 기기일 때
            enableBluetooth();
        } else {
            Log.d(TAG,"bluetooth not supported");
        }
    }



}












