package com.example.becomebeacon.beaconlocker;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by 함상혁입니다 on 2017-04-24.
 */

public class BluetoothService {


    public static String BEACON_UUID;       // changsu
    public static  Boolean saveRSSI;
    private static final long SCAN_PERIOD = 1000;       // 10초동안 SCAN 과정을 수행함

    private static final long TIMEOUT_LIMIT = 20;
    private static final long TIMEOUT_PERIOD = 1000;
    //private static final boolean USING_WINI = true; // TI CC2541 사용: true

    private BleDeviceListAdapter mBleDeviceListAdapter;
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
    //private LogFile logFile;


    private BluetoothAdapter btAdapter;
    private boolean IS_DEBUG=true;

    private String TAG="BluetoothService";
    private Activity mActivity;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private BleUtils mBleUtils;

    private Handler mHandler;

    BluetoothService(Activity ma,  Handler h)
    {
        mActivity=ma;
        mHandler=h;
        mBleUtils=new BleUtils();

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

    public void getBleDeviceInfoFromLeScan(BluetoothDevice device, int rssi, byte[] scanRecord)
    {
        String devName;
        String devAddress;
        String scanRecordAsHex;     // 24byte
        String proximityUUID;       // 12 + 5 characters
        int major, minor;
        //int measuredPower;
        int txPower;                // changsu: 혼동을 없애기 위헤 measuredPower를 txPower로 변경함
        int rssiValue = rssi;

        devName = device.getName();
        if(devName == null)
            devName = "Unknown";

        devAddress = device.getAddress();
        if(devAddress == null)
            devAddress = "Unknown";

        if(!IS_DEBUG) {
            Log.d(TAG, "getBleDeviceInfoFromLeScan() : rssi: " + rssi +
                    ", addr: " + devName +
                    ", name: " + devAddress);
        }

        //이 비교부분을 위로 올리자.. 일치 하지않으면 뭐할 연산하나....
        scanRecordAsHex = mBleUtils.ByteArrayToString(scanRecord);

        //24byte
        proximityUUID = String.format("%s-%s-%s-%s-%s",
                scanRecordAsHex.substring(18, 26),
                scanRecordAsHex.substring(26, 30),
                scanRecordAsHex.substring(30, 34),
                scanRecordAsHex.substring(34, 38),
                scanRecordAsHex.substring(38, 50));


        major = mBleUtils.byteToInt(scanRecord[25], scanRecord[26]);
        minor = mBleUtils.byteToInt(scanRecord[27], scanRecord[28]);

        txPower = scanRecord[29];

        //Log.d(TAG, "proximityUUID: " + proximityUUID);

       // if(proximityUUID.equals(BEACON_UUID) || proximityUUID.equals(BluetoothUuid.WIZTURN_PROXIMITY_UUID.toString()) || proximityUUID.equals(BluetoothUuid.WINI_UUID.toString()) )
        {
            try {
                Log.d(TAG, "Found Pebble UUID: " + proximityUUID);

                double distance = mBleUtils.getDistance(rssiValue, txPower);
                double distance2 = mBleUtils.getDistance_20150515(rssiValue, txPower);

                Log.d(TAG, "dev name: " + devName +
                        ", addr: " + devAddress +
                        ", major: " + major +
                        ", minor: " + minor +
                        ", rssi: " + rssi +
                        ", txPower: " + txPower +
                        ", distance: " + distance +
                        ", distance2: " + distance2);

                BleDeviceInfo item = new BleDeviceInfo(proximityUUID, devName, devAddress, major, minor,
                        txPower, rssiValue, distance, distance2);


                //updateBleDeviceList(item);


            }catch(Exception ex)
            {
                Log.e("Error", "Exception: " + ex.getMessage());
            }

        }
    }

    public BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            mScanning = true;
            getBleDeviceInfoFromLeScan(device, rssi, scanRecord);
                    /*
                        Exception 방지를 위해 runOnUiThread()에서 notifyDataSetChanged()를 호출함
                        - Only the original thread that created a view hierarchy can touch its views
                     */
            mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    //mBleDeviceListAdapter.notifyDataSetChanged();

                }
            });

        }
    };



    public BluetoothAdapter getBtAdapter()
    {
        return btAdapter;
    }



}












