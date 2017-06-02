package com.example.becomebeacon.beaconlocker;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;
import android.content.SharedPreferences;
import android.widget.Toast;


import com.estimote.sdk.connection.internal.protocols.Operation;
import com.example.becomebeacon.beaconlocker.database.DbOpenHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


/**
 * Created by 함상혁입니다 on 2017-04-24.
 */

public class BluetoothScan {

    //private static final boolean USING_WINI = true; // TI CC2541 사용: true
    //private Handler mHandler;




    public static String BEACON_UUID;       // changsu
    public static  Boolean saveRSSI;
    private static final long SCAN_PERIOD = 1000;       // 10초동안 SCAN 과정을 수행함

    private static final long TIMEOUT_LIMIT = 2;
    private static final long TIMEOUT_PERIOD = 1000;
    //private static final boolean USING_WINI = true; // TI CC2541 사용: true

    private BleDeviceListAdapter mBleDeviceListAdapter;
    private MyBeaconsListAdapter mBeaconsListAdapter;



    /*
        Class Instance Variables
     */
    private BleDeviceInfo mBleDeviceInfo;

    /*
        Member Variables
     */
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<BleDeviceInfo> mArrayListBleDevice;   // scan 후 검색된 pebBle 장비를 저장하는 array list
    private ArrayList<BleDeviceInfo> mAssignedItem;

    private BleDeviceInfo mMaxRssiBeacon;
    //private Handler mHandler;
    boolean mScanning;
    private HashMap<String, BleDeviceInfo> mItemMap;
    private HashMap<String, BleDeviceInfo> mScannedMap;

    SharedPreferences pref;

    /*
        Widgets
     */
    ListView mBleListView;

    SharedPreferences setting;
    //private LogFile logFile;

    private boolean IS_DEBUG=true;


    private int mod;
    private String TAG="BluetoothScan";
    private MainActivity mActivity;
    public static final int REQUEST_CONNECT_DEVICE = 1;
    public static final int REQUEST_ENABLE_BT = 2;
    private BleUtils mBleUtils;


    BleService mBleService;


    BluetoothScan(BleService bleS )
    {
        mBleService=bleS;
        mActivity=GetMainActivity.getMainActity();
        //setting = getSharedPreferences;
        BEACON_UUID = getBeaconUuid();


        //saveRSSI = setting.getBoolean("saveRSSI", true);

        mItemMap = BeaconList.mItemMap;
        mAssignedItem=BeaconList.mAssignedItem;


        mBleUtils=new BleUtils();
        mod = Values.USE_TRACK;


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }


    BluetoothScan(MainActivity ma, BleDeviceListAdapter bdla, MyBeaconsListAdapter mbla)
    {

        mActivity=ma;
        mItemMap=BeaconList.mItemMap;
        mScannedMap=BeaconList.scannedMap;

        mArrayListBleDevice=BeaconList.mArrayListBleDevice;
        mAssignedItem=BeaconList.mAssignedItem;

        setting = PreferenceManager.getDefaultSharedPreferences(mActivity);
        BEACON_UUID = getBeaconUuid(setting);

        saveRSSI = setting.getBoolean("saveRSSI", true);

        mBleUtils=new BleUtils();
        mBleDeviceListAdapter=bdla;
        mBeaconsListAdapter=mbla;
        mod = Values.USE_NOTHING;

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    }

    public void end()
    {
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
    }


    protected void onDestory(){
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
    }

    public boolean getDeviceState() {
        Log.d(TAG, "Check the Bluetooth support");
        if(mBluetoothAdapter == null) { Log.d(TAG, "Bluetooth is not available");
            return false;
        } else {
            Log.d(TAG, "Bluetooth is available");
            return true;
        }
    }

    public boolean isBleOn()
    {
        return mBluetoothAdapter.isEnabled();
    }

    public void enableBluetooth()
    {
        Log.i(TAG, "Check the enabled Bluetooth");
        if(mBluetoothAdapter.isEnabled())
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

    public void getBleDeviceInfoFromLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        String devName;
        String devAddress;
        String scanRecordAsHex;     // 24byte
        String proximityUUID;       // 12 + 5 characters
        int major, minor;
        //int measuredPower;
        int txPower;                // changsu: 혼동을 없애기 위헤 measuredPower를 txPower로 변경함
        int rssiValue = rssi;

        devName = device.getName();
        if (devName == null)
            devName = "Unknown";

        devAddress = device.getAddress();
        if (devAddress == null)
            devAddress = "Unknown";

        if (!IS_DEBUG) {
//            Log.d(TAG, "getBleDeviceInfoFromLeScan() : rssi: " + rssi +
//                    ", addr: " + devName +
//                    ", name: " + devAddress);
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


        if (proximityUUID.equals(BEACON_UUID) || proximityUUID.equals(BluetoothUuid.WIZTURN_PROXIMITY_UUID.toString()) || proximityUUID.equals(BluetoothUuid.WINI_UUID.toString()))
        {
            try {
                //               Log.d(TAG, "Found Pebble UUID: " + proximityUUID);

                double distance = mBleUtils.getDistance(rssiValue, txPower);
                double distance2 = mBleUtils.getDistance_20150515(rssiValue, txPower);

//                Log.d(TAG, "dev name: " + devName +
//                        ", addr: " + devAddress +
//                        ", major: " + major +
//                        ", minor: " + minor +
//                        ", rssi: " + rssi +
//                        ", txPower: " + txPower +
//                        ", distance: " + distance +
//                        ", distance2: " + distance2);

                BleDeviceInfo item = new BleDeviceInfo(proximityUUID, devName, devAddress, major, minor,
                        txPower, rssiValue, distance, distance2);


                updateBleDeviceList(item);
                Log.d("SCAN","Scanned item LD is "+item.limitDistance);


            } catch (Exception ex) {
                Log.e("Error", "Exception: " + ex.getMessage());
            }
        }
    }

    public void updateBleDeviceList(BleDeviceInfo item)
    {
        int index = 0;
        boolean foundItem = false;
        int KalmanRSSI =0;
        /**
         * HashMap의 key값에 동일한 device address가 있는 경우: update 수행
         */

        if(mod== Values.USE_SCAN) { //스캔중일 경우
            //Log.d("SCAN","mitem : "+mArrayListBleDevice.toString());
            if (!mItemMap.containsKey(item.devAddress)) {
                if (mScannedMap.containsKey(item.devAddress)) {

//            mScannedMap.get(item.devAddress).rssi = item.rssi;
                    mScannedMap.get(item.devAddress).rssi = (int) mScannedMap.get(item.devAddress).rssiKalmanFileter.update(item.rssi);
                    KalmanRSSI = mScannedMap.get(item.devAddress).rssi;
//            mScannedMap.get(item.devAddress).distance = item.distance;
//            mScannedMap.get(item.devAddress).distance2 = item.distance2;
                    mScannedMap.get(item.devAddress).distance = mBleUtils.getDistance(KalmanRSSI, item.txPower);
                    mScannedMap.get(item.devAddress).distance2 = mBleUtils.getDistance_20150515(KalmanRSSI, item.txPower);

                    mScannedMap.get(item.devAddress).timeout = item.timeout;

//                    Log.d("Debug", "Major: " + item.major +
//                            ", Minor: " + item.minor +
//                            ", rssi: " + KalmanRSSI +
//                            ", distance: " + item.distance);
                } else {
                    /**
                     *  HashMap에 해당 item의 device address가 없는 경우, 추가함
                     *  key값: devAddress
                     */

                    mArrayListBleDevice.add(item);
                    mScannedMap.put(item.devAddress, item);

                }


            }
//        else if(mod==Values.USE_NOTHING)//자기비컨만 보는 메인화면
//        {
//
//            if (mItemMap.containsKey(item.devAddress)) {
//
//               // Log.d(TAG, "it's contain key");
////            mScannedMap.get(item.devAddress).rssi = item.rssi;
//                mItemMap.get(item.devAddress).rssi = (int) mItemMap.get(item.devAddress).rssiKalmanFileter.update(item.rssi);
//                KalmanRSSI = mItemMap.get(item.devAddress).rssi;
////            mScannedMap.get(item.devAddress).distance = item.distance;
////            mScannedMap.get(item.devAddress).distance2 = item.distance2;
//                mItemMap.get(item.devAddress).distance = mBleUtils.getDistance(KalmanRSSI, item.txPower);
//                mItemMap.get(item.devAddress).distance2 = mBleUtils.getDistance_20150515(KalmanRSSI, item.txPower);
//
//                mItemMap.get(item.devAddress).timeout = item.timeout;
//
////                Log.d("Debug", "Major: " + item.major +
////                        ", Minor: " + item.minor +
////                        ", rssi: " + KalmanRSSI +
////                        ", distance: " + item.distance);
//            }


//            if (saveRSSI) {
//            /*
//            logFile.recodeLogFile(
//                    "dev name: " + devName +
//                            ", addr: " + devAddress +
//                            ", major: " + major +
//                            ", minor: " + minor +
//                            ", rssi: " + rssi +
//                            ", txPower: " + txPower +
//                            ", distance: " + distance + "\n\n");
//
//            */
//                if (KalmanRSSI == 0)
//                    KalmanRSSI = item.rssi;
//
//            }

            mMaxRssiBeacon = getMaxRssiBeacon();
        }
        else if(mod== Values.USE_TRACK)
        {
            Log.d("SCAN","Tracking...: "+item.devAddress);
            Log.d("SCAN","mItem : "+mItemMap.toString());

            DbOpenHelper dbOpenHelper = new DbOpenHelper(BleService.mContext.getApplicationContext());

            dbOpenHelper.open();

            if(!dbOpenHelper.uniqueTest(item.devAddress)) //있을때 if문 작동함
            {
                BleDeviceInfo bdi;
                //내꺼
                if(mItemMap.containsKey(item.devAddress))
                {
                    bdi=mItemMap.get(item.devAddress);
                    if(bdi.isLost) {
                        Log.d("DATABASE", "Key" + item.devAddress + " LostItem ");
                        mBleService.pushFindNotification(bdi.nickname, bdi.devAddress);
                    }
                }
                else {
                    Log.d("DATABASE", "address :" + item.devAddress + "is Not mine : " + mItemMap);
                    //다른사람
                }

            }
            else
            {
                Log.d("DATABASE",item.devAddress+" is not in DB : ");
            }

            if(mItemMap.containsKey(item.devAddress)) {
                if (BeaconList.scannedMap.containsKey(item.devAddress)) {
                    Log.d("SCAN", "scanned map has my item");
                    BeaconList.scannedMap.remove(item.devAddress);

                    for (int i = 0; i < BeaconList.mArrayListBleDevice.size(); i++) {
                        if (BeaconList.mArrayListBleDevice.get(i).devAddress == item.devAddress) {
                            BeaconList.mArrayListBleDevice.remove(i);
                            Log.d("SCAN", "removed");

                        }
                    }
                }
                Log.d("SCAN", "Tracking.. contain1");
                BleDeviceInfo tItem = mItemMap.get(item.devAddress);


                tItem.rssi = (int) tItem.rssiKalmanFileter.update(item.rssi);
                KalmanRSSI = tItem.rssi;
                tItem.distance = mBleUtils.getDistance(KalmanRSSI, item.txPower);
                tItem.distance2 = mBleUtils.getDistance_20150515(KalmanRSSI, item.txPower);
                tItem.timeout = item.TIME_OUT;

                if (Values.useGPS) {
                    tItem.setCoordinate(Values.latitude, Values.longitude);
                    Log.d(TAG, "in useGps : lat : " + tItem.latitude + " long : " + tItem.longitude);
                    long now=System.currentTimeMillis();
                    tItem.lastDate=new Date(now);
                }

                Log.d("SCAN", tItem.devAddress + "dist : " + tItem.distance2 + " isfar? " + tItem.isFar);

//                if (tItem.isLost)
//                {
//                    Log.d("Notic","Key"+tItem.devAddress+" LostedItem ");
//                    mBleService.pushFindNotification(tItem.nickname,tItem.devAddress);
//
//
//                }
                Log.d("LOST",tItem.devAddress+" isLost : "+tItem.isLost);

                if(tItem.isLost)
                {
                    Log.d("SCAN", tItem.devAddress+" is lost");

                }
                else if(tItem.isLost!=true&&tItem.limitDistance<tItem.distance2&&tItem.isFar!=true) {
                //if(0.2<tItem.distance2) {
                    //멀다 팝업 띄운다

                    Log.d("Notic","Key"+tItem.devAddress+" too far : distance :"+tItem.distance2+" limit : "+tItem.limitDistance);

                    mBleService.pushNotification(tItem.nickname, tItem.devAddress);
                    Log.d("Notic","NotiNum : "+Notifications.notifications.get(tItem.devAddress));
                    mItemMap.get(tItem.devAddress).isFar = true;
                        //팝업 내용에따라 isLost 갱신

                }
                else if(tItem.limitDistance>tItem.distance2)
                {
                    if(Notifications.notifications.containsKey(tItem.devAddress))
                    {

                        NotificationManager notificationManager = (NotificationManager)BleService.mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.cancel(Notifications.notifications.get(tItem.devAddress));
                        Log.d("Notic","NotiNum is "+Notifications.notifications.get(tItem.devAddress)+" there is key "+Notifications.notifications.toString());

                        //Notifications.notifications.remove(tItem.devAddress);


                    }
                    else
                    {
                        Log.d("Notic","Key "+tItem.devAddress+"is not in noti "+Notifications.notifications.toString());
                    }
                    mItemMap.get(tItem.devAddress).isFar = false;
                }
                else{
                    Log.d("Notic","Key : "+tItem.devAddress+" is already far");
                }
            }
        }
    }





        // 가장 근거리의 Beacon 정보를 서버로 전송함
        //sendSocketMsg(mMaxRssiBeacon.devAddress, mMaxRssiBeacon.major, mMaxRssiBeacon.minor);


    public BleDeviceInfo getMaxRssiBeacon()
    {
        int pos = 0;
        int maxRssi = mArrayListBleDevice.get(0).rssi;

        for(int i = 1; i < mArrayListBleDevice.size(); i++)
        {
            if(maxRssi > mArrayListBleDevice.get(i).rssi)
            {
                pos = i;
                maxRssi = mArrayListBleDevice.get(i).rssi;
            }
        }

        return mArrayListBleDevice.get(pos);
    }

    public BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            mScanning = true;
            Log.d("SCAN","mod is "+mod);

            getBleDeviceInfoFromLeScan(device, rssi, scanRecord);
                    /*
                        Exception 방지를 위해 runOnUiThread()에서 notifyDataSetChanged()를 호출함
                        - Only the original thread that created a view hierarchy can touch its views
                     */
            if(mod==Values.USE_SCAN) {
                mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        if (mod == Values.USE_SCAN) {//스캔중일시

                            mBleDeviceListAdapter.notifyDataSetChanged();

                        } else {//자기 비컨만 표시

                            //mBeaconsListAdapter.notifyDataSetChanged();
                        }

                    }
                });
            }

        }
    };



    public BluetoothAdapter getBtAdapter()
    {
        return mBluetoothAdapter;
    }

    public void changeMod(int m)
    {
        if(m== Values.USE_SCAN)
        {
            mod= Values.USE_SCAN;
        } else if (m == Values.USE_TRACK){
           mod= Values.USE_TRACK;

        }
        else if(m== Values.USE_NOTHING) {
            mod = Values.USE_NOTHING;
        }
    }

    public int getMod()
    {
        return mod;
    }

    public String getBeaconUuid(SharedPreferences pref)
    {
        String uuid = "";

        //uuid = pref.getString("keyUUID", BluetoothUuid.WINI_UUID.toString());
        uuid=BluetoothUuid.WINI_UUID.toString();

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
    public String getBeaconUuid()
    {
        String uuid = "";

        //uuid = pref.getString("keyUUID", BluetoothUuid.WINI_UUID.toString());
        uuid=BluetoothUuid.WINI_UUID.toString();

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












