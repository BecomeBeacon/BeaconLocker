package com.example.becomebeacon.beaconlocker;

/**
 * Created by gwmail on 2017-04-26.
 */

public class BeaconOnUser  {
    public String address;

    public BeaconOnUser(String address)  {
        this.address = address;
    }

    public BeaconOnUser() throws Exception {
        address = "";
    }
}
