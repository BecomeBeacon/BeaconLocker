package com.example.becomebeacon.beaconlocker.database;

/**
 * Created by gwmail on 2017-04-27.
 */

public class BeaconLost {
    private String latitude, longitude;

    public BeaconLost(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
