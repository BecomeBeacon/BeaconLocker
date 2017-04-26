package com.example.becomebeacon.beaconlocker.database;


/**
 * Created by gwmail on 2017-04-26.
 */

public class Beacon {

    private String UUID, nickname, picture, islost;
    private String latatude, longitude;

    public Beacon() {
        islost = "0";
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String NICKNAME) {
        this.nickname = nickname;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String PICTURE) {
        this.picture = picture;
    }

    public String getIslost() {
        return islost;
    }

    public void setIslost(String ISLOST) {
        this.islost = islost;
    }

    public String getLatatude() {
        return latatude;
    }

    public void setLatatude(String latatude) {
        this.latatude = latatude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
