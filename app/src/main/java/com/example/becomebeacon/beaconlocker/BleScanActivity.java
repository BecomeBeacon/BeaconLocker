package com.example.becomebeacon.beaconlocker;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by 함상혁입니다 on 2017-04-27.
 */

public class BleScanActivity extends AppCompatActivity {

    public static String BEACON_UUID;       // changsu
    public static  Boolean saveRSSI;
    private static final long SCAN_PERIOD = 1000;       // 10초동안 SCAN 과정을 수행함
    private static final int REQUEST_ENABLE_BT = 1;
    private static final String TAG = "SCAN";
    private static final boolean IS_DEBUG = true;
    private static final long TIMEOUT_LIMIT = 20;
    private static final long TIMEOUT_PERIOD = 1000;
    //private static final boolean USING_WINI = true; // TI CC2541 사용: true
    private BluetoothService mBtService;

    // Socket 관련 상수
    private static final int SERVER_PORT = 6789;
    //private static final String SERVER_IP = "192.168.123.13";
    //private static final String SERVER_IP = "155.230.90.196";
    private static final String DEVICE_ADDR = "D0:39:72:A3:E1:2E";
    private static final int MAJOR = 100;
    private static final int MINOR = 2500;

    final static int MSG_RECEIVED_MSG = 0x100;

    /*
        Class Instance Variables
     */
    private BleDeviceInfo mBleDeviceInfo;
    private BleDeviceListAdapter mBleDeviceListAdapter;
    private BleUtils mBleUtils;

    /*
        Member Variables
     */
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<BleDeviceInfo> mArrayListBleDevice;   // scan 후 검색된 pebBle 장비를 저장하는 array list
    private HashMap<String, BleDeviceInfo> mItemMap;
    private BleDeviceInfo mMaxRssiBeacon;
    //private Handler mHandler;
    boolean mScanning;

    /*
        Widgets
     */
    ListView mBleListView;

    SharedPreferences setting;

    private Handler mHandler= new Handler()
    {
        public void handleMessage(Message msg)
        {
            if(mScanning)
            {
                mScanning = false;
                mBtService.getBtAdapter().stopLeScan(mBtService.mLeScanCallback);
            }

            mScanning = true;
            mBtService.getBtAdapter().startLeScan(mBtService.mLeScanCallback);
            mHandler.sendEmptyMessageDelayed(0, SCAN_PERIOD);
        }
    };

    private Handler mTimeOut = new Handler(){
        public void handleMessage(Message msg){
            Log.i("TAG","TIMEOUT UPDATE");

            int maxRssi = 0;
            int maxIndex = -1;

            //timeout counter update
            for (int i= 0 ; i < mArrayListBleDevice.size() ; i++){
                mArrayListBleDevice.get(i).timeout--;
                if(mArrayListBleDevice.get(i).timeout == 0){
                    mItemMap.remove(mArrayListBleDevice.get(i).devAddress);
                    mArrayListBleDevice.remove(i);
                }
                else{
                    if(mArrayListBleDevice.get(i).rssi > maxRssi || maxRssi == 0)
                    {
                        maxRssi = mArrayListBleDevice.get(i).rssi;
                        maxIndex = i;
                    }
                }
            }
            //TextView text_max_dev = (TextView)findViewById(R.id.text_max_dev);

            if(maxIndex == -1) {
                //text_max_dev.setText("No Dev");
            }
            else{
                //text_max_dev.setText(maxIndex+1 +"th    "
                //        + "major: " + mArrayListBleDevice.get(maxIndex).major + "  "
                //        + "minor: " + mArrayListBleDevice.get(maxIndex).minor + "  "
                //        + mArrayListBleDevice.get(maxIndex).getRssi() +"dbm");
            }
            mTimeOut.sendEmptyMessageDelayed(0,TIMEOUT_PERIOD);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mArrayListBleDevice = new ArrayList<BleDeviceInfo>();

        Log.d("sss","mbts assigned");
        mBtService=new BluetoothService(this,mHandler);
    }

    protected void onResume() {
        super.onResume();

        //BEACON_UUID = getBeaconUuid(setting);

        //saveRSSI = setting.getBoolean("saveRSSI", true);

        mBtService.checkBluetooth();
        //scanBleDevice(true);            // BLE 장치 검색\
        mHandler.sendEmptyMessageDelayed(0, SCAN_PERIOD);
        mTimeOut.sendEmptyMessageDelayed(0, TIMEOUT_PERIOD);


    }

    public String getBeaconUuid(SharedPreferences pref)
    {
        String uuid = "";

        uuid = pref.getString("keyUUID", BluetoothUuid.WINI_UUID.toString());

        /*
        if(USING_WINI) {
            uuid = pref.getString("keyUUID", BluetoothUuid.WINI_UUID.toString());
            //uuid = BluetoothUuid.WINI_UUID.toString();
        }
        else {

            uuid = pref.getString("keyUUID", BluetoothUuid.WIZTURN_PROXIMITY_UUID.toString());
            //uuid = BluetoothUuid.WIZTURN_PROXIMITY_UUID.toString();
        }
        */

        return uuid;
    }






}
