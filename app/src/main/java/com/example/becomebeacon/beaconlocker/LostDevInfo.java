package com.example.becomebeacon.beaconlocker;

import java.text.SimpleDateFormat;

/**
 * Created by heeseung on 2017-05-23.
 */

public class LostDevInfo {
    private double longitude;
    private double latitude;
    private String lostDate;
    private String devAddr;
    private String uid;
    private String userName;
    private String nickNameOfThing;
    private String pictureLink;
    private String pictureUri;

    public LostDevInfo() {
        longitude = 0;
        latitude = 0;
        lostDate = "";
        devAddr = "";
        nickNameOfThing= "";
        this.uid = LoginActivity.getUser().getUid();
        this.userName = LoginActivity.getUser().getDisplayName();
        pictureLink="null";
        pictureUri = "";
    }

    public LostDevInfo(BleDeviceInfo bdi) {
        longitude = bdi.longitude;
        latitude = bdi.latitude;
        SimpleDateFormat CurDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        lostDate = bdi.lastDate;
        devAddr = bdi.devAddress;
        nickNameOfThing= bdi.nickname;
        this.uid = bdi.uid;
        this.userName = bdi.userName;
        pictureLink=bdi.pictureLink;
    }

    public String getPictureLink(){return pictureLink;};

    public void setPictureLink(String pictureLink){this.pictureLink = pictureLink;}

    public String getNickNameOfThing(){return nickNameOfThing;}

    public void setNickNameOfThing(String nick){nickNameOfThing = nick;}

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getLostDate() {
        return lostDate;
    }

    public void setLostDate(String lostDate) {
        this.lostDate = lostDate;
    }

    public String getDevAddr() {
        return devAddr;
    }

    public void setDevAddr(String devAddr) {
        this.devAddr = devAddr;
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

    public String getPictureUri() {
        return pictureUri;
    }

    public void setPictureUri(String pictureUri) {
        this.pictureUri = pictureUri;
    }
}
