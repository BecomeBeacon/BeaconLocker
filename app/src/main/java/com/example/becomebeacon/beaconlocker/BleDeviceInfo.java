package com.example.becomebeacon.beaconlocker;

/**
 * Created by 함상혁입니다 on 2017-04-24.
 */

import android.bluetooth.BluetoothDevice;

public class BleDeviceInfo {

    public static int TIME_OUT = 20;

    public boolean isCheckLocation;

    public BluetoothDevice btDevice;   // Bluetooth Device
    public String proximityUuid;       // UUID
    public String devName;             // Device Name
    public String devAddress;          // Device Address
    public int timeout;                // defatlt: 10; decrease per second

    public double limitDistance;
    public int major;                  // Major
    public int minor;                  // Minor
    public int measuredPower;          // Measured Power
    public int txPower;                // Tx Power
    public int rssi;                   // RSSI
    public double distance;            // Distance
    public double distance2;            // Distance

    //Device Info
    public String hwVersion;           // H/W Version
    public String fwVersion;           // Firmware Version
    public KalmanFilter rssiKalmanFileter;

    //User info
    public String nickname;
    public String picture;

    //Coordination
    public String latitude;
    public String longitude;

    public boolean isFar;
    public boolean isLost;

    //Constructor
    public BleDeviceInfo() {
        isCheckLocation=false;
        this.proximityUuid = "";
        this.devName = "";
        this.devAddress = "";
        this.isFar=false;
        this.isLost=false;

        this.major = 0;
        this.minor = 0;
        this.measuredPower = 0;
        this.txPower = 0;
        this.rssi = 0;
        this.distance = 0;
        this.distance2 = 0;
        this.limitDistance=Values.basicLimitDistance;

        this.hwVersion = "";
        this.fwVersion = "";

        this.rssiKalmanFileter = new KalmanFilter(0);

        this.nickname = "";
        this.picture = "";

        this.latitude = "";
        this.longitude = "";
    }

    //Constructor
    public BleDeviceInfo(String devAddress, String nickname) {
        isCheckLocation=false;
        this.proximityUuid = "";
        this.devName = "";
        this.devAddress = devAddress;
        this.limitDistance=Values.basicLimitDistance;

        this.major = 0;
        this.minor = 0;
        this.measuredPower = 0;
        this.txPower = 0;
        this.rssi = 0;
        this.distance = 0;
        this.distance2 = 0;

        this.hwVersion = "";
        this.fwVersion = "";

        this.rssiKalmanFileter = new KalmanFilter(0);

        this.nickname = nickname;
        this.picture = "";

        this.latitude = "";
        this.longitude = "";
    }


    //BluetoothDevice를 추가한 생성자
    //Constructor with Parms
    public BleDeviceInfo(BluetoothDevice device, String proximityUuid, String devName,
                         String devAddress, int major, int minor, int mPower, int rssi,
                         int txPower, double distance)
    {
        isCheckLocation=false;
        this.btDevice = device;
        this.proximityUuid = proximityUuid;
        this.devName = devName;
        this.devAddress = devAddress;
        this.major = major;
        this.minor = minor;
        this.measuredPower = mPower;
        this.txPower = txPower;
        this.isFar=false;
        this.isLost=false;

        this.rssi = rssi;
        this.distance = distance;
        this.timeout = TIME_OUT;
        this.limitDistance=Values.basicLimitDistance;

        this.rssiKalmanFileter = new KalmanFilter(this.rssi);

        this.nickname = "";
        this.picture = "";

        this.latitude = "";
        this.longitude = "";
    }

    // Measured Power 제외, 거리 1개
    public BleDeviceInfo(String proximityUuid, String devName,
                         String devAddress, int major, int minor, int txPower, int rssi, double distance)
    {
        isCheckLocation=false;
        this.proximityUuid = proximityUuid;
        this.devName = devName;
        this.devAddress = devAddress;
        this.major = major;
        this.minor = minor;
        //this.measuredPower = mPower;
        this.txPower = txPower;

        this.rssi = rssi;
        this.distance = distance;
        this.timeout = TIME_OUT;

        this.rssiKalmanFileter = new KalmanFilter(this.rssi);

        this.nickname = "";
        this.picture = "";

        this.latitude = "";
        this.longitude = "";
    }

    // Measured Power를 제외한 생성자
    public BleDeviceInfo(String proximityUuid, String devName,
                         String devAddress, int major, int minor, int txPower, int rssi, double distance, double distance2)
    {

        isCheckLocation=false;
        this.proximityUuid = proximityUuid;
        this.devName = devName;
        this.devAddress = devAddress;
        this.major = major;
        this.minor = minor;
        //this.measuredPower = mPower;
        this.txPower = txPower;

        this.rssi = rssi;
        this.distance = distance;
        this.distance2 = distance2;
        this.timeout = TIME_OUT;

        this.rssiKalmanFileter = new KalmanFilter(this.rssi);
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

    public int getMajor()
    {
        return this.major;
    }

    public void setMajor(int major)
    {
        this.major = major;
    }

    public int getMinor()
    {
        return this.minor;
    }

    public void setMinor(int minor)
    {
        this.minor = minor;
    }

    public String getMeasuredPower()
    {
        return String.valueOf(this.measuredPower);
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

    public String getHwVersion()
    {
        return this.hwVersion;
    }

    public String getFwVersion()
    {
        return this.fwVersion;
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

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setLimitDistance(int d)
    {
        this.limitDistance=d;
    }

    /*----------------------------------------------------------*/
    /*
        거리 계산
     */

    public static void setLimitTime(int t)
    {
        TIME_OUT=t;
    }

    public double estimateDistance(int rssiValue, int txPower)
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
    public boolean equals(BleDeviceInfo b) {
        if(b.devAddress.equals(this.devAddress))
            return true;
        else
            return false;
    }

    public BeaconOnDB toDB() {
        BeaconOnDB beaconOnDB = new BeaconOnDB(getNickname());

        return beaconOnDB;
    }

    public void setCoordinate(String lati,String longi)
    {
        latitude=lati;
        longitude=longi;
    }

}
