package com.example.becomebeacon.beaconlocker;

/**
 * Created by 함상혁입니다 on 2017-04-24.
 */

import android.bluetooth.BluetoothDevice;

public class BleDeviceInfo {

    public static int TIME_OUT = 20;

    public boolean isCheckLocation;

    public boolean othersSendMsg;
    public BluetoothDevice btDevice;   // Bluetooth Device
    public String proximityUuid;       // UUID
    public String devName;             // Device Name
    public String devAddress;          // Device Address
    public int timeout;                // defatlt: 10; decrease per second

    public double limitDistance;
    public int measuredPower;          // Measured Power
    public int txPower;                // Tx Power
    public int rssi;                   // RSSI
    public double distance;            // Distance
    public double distance2;            // Distance

    //Device Info
    public KalmanFilter rssiKalmanFileter;
    public String lastDate;

    //User info
    public String nickname;
    public String pictureUri;

    //Coordination
    public double latitude;
    public double longitude;

    public boolean isFar;
    public boolean isLost;

    public String uid;
    public String userName;

    public String pictureLink;

    //Constructor
    public BleDeviceInfo() {
        othersSendMsg=false;
        isCheckLocation=false;
        this.proximityUuid = "";
        this.devName = "";
        this.devAddress = "";
        this.isFar=false;
        this.isLost=false;

        this.measuredPower = 0;
        this.txPower = 0;
        this.rssi = 0;
        this.distance = 0;
        this.distance2 = 0;
        this.limitDistance=Values.basicLimitDistance;

        this.rssiKalmanFileter = new KalmanFilter(0);

        this.nickname = "";
        this.pictureUri = "";

        this.latitude = 0;
        this.longitude = 0;
        if(LoginActivity.getUser()!=null) {
            this.uid = LoginActivity.getUser().getUid();
            this.userName = LoginActivity.getUser().getDisplayName();
        }
        lastDate="";

        this.pictureLink = null;
    }

    public BleDeviceInfo (LostDevInfo ldi)
    {

        this();
        nickname=ldi.getNickNameOfThing();
        devAddress=ldi.getDevAddr();
        latitude=ldi.getLatitude();
        longitude=ldi.getLongitude();
        userName=ldi.getUserName();
        uid=ldi.getUid();
        lastDate=ldi.getLostDate();
        pictureLink=ldi.getPictureLink();
        pictureUri=ldi.getPictureUri();

    }

    //Constructor
    public BleDeviceInfo(String devAddress, String nickname) {
        othersSendMsg=false;
        isCheckLocation=false;
        this.proximityUuid = "";
        this.devName = "";
        this.devAddress = devAddress;
        this.limitDistance=Values.basicLimitDistance;
        this.isFar=false;
        this.measuredPower = 0;
        this.txPower = 0;
        this.rssi = 0;
        this.distance = 0;
        this.distance2 = 0;
        lastDate="";

        this.rssiKalmanFileter = new KalmanFilter(0);

        this.nickname = nickname;
        this.pictureUri = "";

        this.latitude = 0;
        this.longitude = 0;

        this.uid = LoginActivity.getUser().getUid();
        this.userName = LoginActivity.getUser().getDisplayName();

        this.pictureLink = null;
    }


    //BluetoothDevice를 추가한 생성자
    //Constructor with Parms
    public BleDeviceInfo(BluetoothDevice device, String proximityUuid, String devName,
                         String devAddress, int major, int minor, int mPower, int rssi,
                         int txPower, double distance)
    {
        othersSendMsg=false;
        isCheckLocation=false;
        this.btDevice = device;
        this.proximityUuid = proximityUuid;
        this.devName = devName;
        this.devAddress = devAddress;
        this.measuredPower = mPower;
        this.txPower = txPower;
        this.isFar=false;
        this.isLost=false;

        lastDate="";
        this.rssi = rssi;
        this.distance = distance;
        this.timeout = TIME_OUT;
        this.limitDistance=Values.basicLimitDistance;

        this.rssiKalmanFileter = new KalmanFilter(this.rssi);

        this.nickname = "";
        this.pictureUri = "";

        this.latitude = 0;
        this.longitude = 0;

        this.uid = LoginActivity.getUser().getUid();
        this.userName = LoginActivity.getUser().getDisplayName();

        this.pictureLink = null;
    }

    // Measured Power 제외, 거리 1개
    public BleDeviceInfo(String proximityUuid, String devName,
                         String devAddress, int major, int minor, int txPower, int rssi, double distance)
    {
        othersSendMsg=false;
        isCheckLocation=false;
        this.isFar=false;
        this.proximityUuid = proximityUuid;
        this.devName = devName;
        this.devAddress = devAddress;
        lastDate="";
        //this.measuredPower = mPower;
        this.txPower = txPower;
        this.limitDistance=Values.basicLimitDistance;

        this.rssi = rssi;
        this.distance = distance;
        this.timeout = TIME_OUT;

        this.rssiKalmanFileter = new KalmanFilter(this.rssi);

        this.nickname = "";
        this.pictureUri = "";

        this.latitude = 0;
        this.longitude = 0;

        this.uid = LoginActivity.getUser().getUid();
        this.userName = LoginActivity.getUser().getDisplayName();

        this.pictureLink = null;
    }

    // Measured Power를 제외한 생성자
    public BleDeviceInfo(String proximityUuid, String devName,
                         String devAddress, int major, int minor, int txPower, int rssi, double distance, double distance2)
    {
        othersSendMsg=false;
        lastDate="";
        this.isFar=false;
        isCheckLocation=false;
        this.proximityUuid = proximityUuid;
        this.devName = devName;
        this.devAddress = devAddress;
        //this.measuredPower = mPower;
        this.txPower = txPower;
        this.limitDistance=Values.basicLimitDistance;

        this.latitude = 0;
        this.longitude = 0;

        this.rssi = rssi;
        this.distance = distance;
        this.distance2 = distance2;
        this.timeout = TIME_OUT;

        this.rssiKalmanFileter = new KalmanFilter(this.rssi);

        this.uid = LoginActivity.getUser().getUid();
        this.userName = LoginActivity.getUser().getDisplayName();

        this.pictureLink = null;
    }

    /*
        아래의 get, set 함수는 사용하지 않음: 추후 사용을 위해 남겨둠
     */
    /*----------------------------------------------------------*/
    public String getProximityUuid()
    {
        return this.proximityUuid;
    }
    public void setProximityUuid(String uuid)
    {
        this.proximityUuid = uuid;
    }

    public String getDeviceName()
    {
        return this.devName;
    }

    public void setDeviceName(String deviceName)
    {
        this.devName = devName;
    }

    public String getDevAddress()
    {
        return this.devAddress;
    }

    public void setDevAddress(String deviceAddr)
    {
        this.devAddress = deviceAddr;
    }

    public int getMeasuredPower()
    {
        return measuredPower;
        //return String.valueOf(this.measuredPower);
    }

    public void setMeasuredPower(int mPower)
    {
        this.measuredPower = mPower;
    }

    public int getRssi()
    {
        return this.rssi;
    }

    public void setRssi(int rssi)
    {
        this.rssi = rssi;
    }

    public int getTxPower()
    {
        return this.txPower;
    }

    public void setTxPower(int power)
    {
        this.txPower = power;
    }

    public double getDistance()
    {
        return this.distance;
    }

    public void setDistance(double distance)
    {
        this.distance = distance;
    }

    public double getDistance2()
    {
        return this.distance2;
    }

    public void setDistance2(double distance2)
    {
        this.distance2 = distance2;
    }

    public int getTimeout(){ return this.timeout; }

    public void setTimeout(int timeout){ this.timeout = timeout; }

    public String getPictureUri() {
        return pictureUri;
    }

    public void setPictureUri(String picture) {
        this.pictureUri = picture;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setLimitDistance(double d)
    {
        this.limitDistance=d;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPictureLink() {
        return pictureLink;
    }

    public void setPictureLink(String pictureLink) {
        this.pictureLink = pictureLink;
    }

    /*----------------------------------------------------------*/
    /*
        거리 계산
     */

    public static void setLimitTime(int t) throws Exception
    {
        TIME_OUT=t;
    }

    public double estimateDistance(int rssiValue, int txPower) throws Exception
    {
        if(txPower == 0)
        {
            txPower = -1;
        }

        if(rssiValue == 0)
        {
            rssiValue = 0;
        }

        this.distance = Math.pow(10, ((double)txPower - rssiValue) / (10 * 2));

        return distance;
    }

    /*
        BleDeviceInfo의 devAddress만 비교하기 위함
     */
    public boolean equals(BleDeviceInfo b)throws Exception {
        if(b.devAddress.equals(this.devAddress))
            return true;
        else
            return false;
    }

    public void setCoordinate(double lati,double longi)throws Exception
    {
        latitude=lati;
        longitude=longi;
    }

}
