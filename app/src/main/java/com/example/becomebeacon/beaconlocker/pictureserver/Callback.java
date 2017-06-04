package com.example.becomebeacon.beaconlocker.pictureserver;

import android.content.Intent;

import java.io.Serializable;

/**
 * Created by gwmail on 2017-06-03.
 */

public interface Callback extends Serializable {
    void callBackMethod(Object obj);
}
