package com.example.becomebeacon.beaconlocker;

import java.io.Serializable;

/**
 * Created by gwmail on 2017-06-03.
 */

public interface CallBack extends Serializable {
    void callBackMethod(Object obj);
}
