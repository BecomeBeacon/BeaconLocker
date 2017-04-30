package com.example.becomebeacon.beaconlocker.database;


import com.example.becomebeacon.beaconlocker.BeaconOnDB;

/**
 * Created by gwmail on 2017-04-26.
 */

public class BeaconOnUser extends BeaconOnDB {
    private String UUID, Uid;

    public BeaconOnUser() {
        super();
        UUID = "";
        Uid = "";
    }

    public String getUUID() {
        return UUID;
    }

    void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public BeaconOnDB toDB() {
        BeaconOnDB beaconOnDB = new BeaconOnDB(getNickname());

        return beaconOnDB;
    }
}
