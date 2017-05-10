package com.example.becomebeacon.beaconlocker;

/**
 * Created by 함상혁입니다 on 2017-05-09.
 */

public class GetMainActivity {
    static private MainActivity ma;

    public static void setMA(MainActivity a)
    {
        ma=a;
    }

    public static MainActivity getMainActity()
    {
        return ma;
    }

}
