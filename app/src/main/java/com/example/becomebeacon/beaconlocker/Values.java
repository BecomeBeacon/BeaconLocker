package com.example.becomebeacon.beaconlocker;

/**
 * Created by 함상혁입니다 on 2017-04-27.
 */

public class Values {
    public static final int CHECK_GPS=111;
    public static final int USE_SCAN=22;
    public static final int USE_TRACK=33;
    public static final int USE_NOTHING=55;
    public static final int REWARD_POINT=100;
    public static final int NOTI_FAR=320;
    public static final int NOTI_I_FIND=4210;
    public static final int NOTI_OTHER_FIND=1520;

    public static int scanBreakTime=5000;
    public static int scanTime=2000;
    public static double basicLimitDistance = 1;
    public static boolean useBLE;
    public static boolean useGPS;
    public static double latitude;
    public static double longitude;
}
