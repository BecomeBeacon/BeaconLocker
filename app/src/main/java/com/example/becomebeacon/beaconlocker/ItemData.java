package com.example.becomebeacon.beaconlocker;

/**
 * Created by GW on 2017-04-27.
 */

public class ItemData {

    private String nickname;
    private String islost;
    private int meter;

    public ItemData(String nickname, String islost, int meter) {
        this.nickname = nickname;
        this.islost = islost;
        this.meter = meter;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getIslost() {
        return islost;
    }

    public void setIslost(String islost) {
        this.islost = islost;
    }

    public int getMeter() {
        return meter;
    }

    public void setMeter(int meter) {
        this.meter = meter;
    }
}