package com.example.becomebeacon.beaconlocker;

/**
 * Created by Ryu on 2017-06-04.
 */

public class FindMessage {

    public String message;
    public String devAddress;
    public boolean isChecked;

    public FindMessage()
    {
        this.message ="";
        this.devAddress ="";
        isChecked=false;
    }


}
