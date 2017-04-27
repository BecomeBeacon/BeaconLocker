package com.example.becomebeacon.beaconlocker.database;

/**
 * Created by gwmail on 2017-04-27.
 */

public class BeaconLost extends BeaconOnUser {
    private String latitude, longitude;

    public BeaconLost(String latitude, String longitude) {
        super();
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
