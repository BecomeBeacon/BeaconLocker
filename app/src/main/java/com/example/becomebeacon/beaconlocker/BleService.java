package com.example.becomebeacon.beaconlocker;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by 함상혁입니다 on 2017-05-09.
 */

public class BleService extends Service {


    BluetoothScan mBleScan;
    private ArrayList<BleDeviceInfo> mAssignedItem;
    boolean mScan;

    @Override
    public void onCreate()
    {
        super.onCreate();
        mBleScan =new BluetoothScan();
        mAssignedItem = GetMainActivity.getMainActity().mAssignedItem;
        mScan=false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        mBleScan.end();
        mHandler.removeMessages(0);
        super.onDestroy();

    }


    private Handler mHandler= new Handler()
    {
        public void handleMessage(Message msg)
        {
            Log.d("main","in handler");
            if(mBleScan.getMod()== Values.USE_TRACK) {

                if(mScan) {
                    mBleScan.getBtAdapter().stopLeScan(mBleScan.mLeScanCallback);
                    mScan=false;
                    Log.d("main","scan stop");
                    mHandler.sendEmptyMessageDelayed(0, Values.scanBreakTime);

                }
                else
                {
                    mBleScan.getBtAdapter().startLeScan(mBleScan.mLeScanCallback);
                    mScan=true;
                    Log.d("main","scan start");
                    mHandler.sendEmptyMessageDelayed(0, Values.scanTime);

                }
            }

        }
    };
}
