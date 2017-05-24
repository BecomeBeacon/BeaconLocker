package com.example.becomebeacon.beaconlocker;

/**
 * Created by heeseung on 2017-05-23.
 */

public class LostDevInfo {
    public double longitude;
    public double latitude;
    public int lostDate;
    public String devAddr;

    public void setLongetude(double lngTemp){
        longitude = lngTemp;
    }

    public void setLatitude(double latTemp){
        latitude = latTemp;
    }

    public void setLostDate(int dateTemp) {
        lostDate = dateTemp;
    }

    public void setDevAddr(String devTemp) {
        devAddr = devTemp;
    }
}
