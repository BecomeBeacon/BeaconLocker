package com.example.becomebeacon.beaconlocker;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by 함상혁입니다 on 2017-05-24.
 */

public class Notifications {
    static int cntNoti=0;
    static public HashMap<String,Integer> notifications=new HashMap();

    static public void clear()
    {
        notifications.clear();
    }
}
