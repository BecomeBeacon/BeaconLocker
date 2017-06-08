package com.example.becomebeacon.beaconlocker;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by 함상혁입니다 on 2017-05-13.
 */

public class BeaconList {
    static public HashMap<String, BleDeviceInfo> scannedMap=new HashMap<String,BleDeviceInfo>();
    //myItem
    static public HashMap<String, BleDeviceInfo> mItemMap=new HashMap<String,BleDeviceInfo>();
    static public HashMap<String, BleDeviceInfo> lostMap=new HashMap<String,BleDeviceInfo>();
    static public HashMap<String,FindMessage> msgMap=new HashMap<String,FindMessage>(); //Key 값은 Message Key값
    static public HashMap<String, String> rewardMap=new HashMap<>(); //Key값은 MAC값 Value는 uid값




    static public ArrayList<BleDeviceInfo> mArrayListBleDevice=new ArrayList<BleDeviceInfo>();    ;
    static public ArrayList<BleDeviceInfo> mAssignedItem=new ArrayList<BleDeviceInfo>();

    static public void refresh() throws Exception
    {
        scannedMap.clear();
        mItemMap.clear();
        lostMap.clear();
        mArrayListBleDevice.clear();
        mAssignedItem.clear();
        msgMap.clear();
        rewardMap.clear();

    }
}
