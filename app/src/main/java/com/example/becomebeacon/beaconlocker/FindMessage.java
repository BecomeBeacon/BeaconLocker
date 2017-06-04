package com.example.becomebeacon.beaconlocker;

/**
 * Created by Ryu on 2017-06-04.
 */

public class FindMessage {

    public String Message;
    public String PhoneNumb;

    public FindMessage()
    {
        this.Message ="";
        this.PhoneNumb ="";
    }

    public void setMessage(String m)
    {
        this.Message = m;
    }
    public void setPhoneNumb(String m)
    {
        this.PhoneNumb = m;
    }


}
