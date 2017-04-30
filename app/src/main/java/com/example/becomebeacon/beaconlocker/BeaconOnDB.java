package com.example.becomebeacon.beaconlocker.database;

/**
 * Created by GW on 2017-04-26.
 */

public class BeaconOnDB {
    private String nickname, picture, islost;

    public BeaconOnDB() {
        islost = "0";
        nickname = "";
        picture = "";
    }

    public BeaconOnDB(String nickname) {
        islost = "0";
        this.nickname = nickname;
        picture = "";
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getIslost() {
        return islost;
    }

    public void setIslost(String islost) {
        this.islost = islost;
    }

}
