package com.example.becomebeacon.beaconlocker;

/**
 * Created by 함상혁입니다 on 2017-05-11.
 */

public class DeviceInfoStore {
    private static BleDeviceInfo bdi;

    static public BleDeviceInfo getBleInfo()throws Exception
    {
        return bdi;
    }
    static public void setBleInfo(BleDeviceInfo bd)
    {
        bdi=bd;
    }
}
