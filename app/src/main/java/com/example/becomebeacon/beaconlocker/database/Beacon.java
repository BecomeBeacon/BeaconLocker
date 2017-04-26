package com.example.becomebeacon.beaconlocker.database;


/**
 * Created by gwmail on 2017-04-26.
 */

class Beacon extends BeaconOnDB {

    private String UUID, islost;
    private String latatude, longitude;

    Beacon() {
        super();
        islost = "0";
    }

    String getUUID() {
        return UUID;
    }

    void setUUID(String UUID) {
        this.UUID = UUID;
    }


}
