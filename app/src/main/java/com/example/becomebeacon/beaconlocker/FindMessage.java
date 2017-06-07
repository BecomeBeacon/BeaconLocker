package com.example.becomebeacon.beaconlocker;

/**
 * Created by Ryu on 2017-06-04.
 */

public class FindMessage {

    public String message;
    public String devAddress;
    public boolean isChecked;
    public String keyValue;
    public boolean isPoint;
    public String sendUid;
    public int point;

    public FindMessage()
    {
        sendUid=LoginActivity.getUser().getUid();
        point=-1;
        isPoint=false;
        this.message ="";
        this.devAddress ="";
        isChecked=false;
        keyValue="";
    }

    public String toString()
    {
        return "message: "+message+" ischecked :"+isChecked;
    }

}
