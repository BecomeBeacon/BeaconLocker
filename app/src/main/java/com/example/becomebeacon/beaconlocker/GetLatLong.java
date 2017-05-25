package com.example.becomebeacon.beaconlocker;

/**
 * Created by Ryu on 2017-05-24.
 */

public class GetLatLong {
    public double latitude;
    public double longitude;
    public String lastdate;
    //Constructor
    public GetLatLong()
    {
        this.lastdate = "";
        this.longitude = 0;
        this.latitude = 0;
    }

    public GetLatLong(String lastdate, double longitude, double latitude)
    {
        this.lastdate = lastdate;
        this.longitude = longitude;
        this.latitude = latitude;
    }
}
