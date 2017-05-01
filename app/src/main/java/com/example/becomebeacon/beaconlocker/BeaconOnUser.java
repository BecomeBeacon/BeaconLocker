package com.example.becomebeacon.beaconlocker;


import com.example.becomebeacon.beaconlocker.BeaconOnDB;

/**
 * Created by gwmail on 2017-04-26.
 */

public class BeaconOnUser  {
    public String address;

    public BeaconOnUser(String address) {
        this.address = address;
    }

    public BeaconOnUser() {
        address = "";
    }
}
