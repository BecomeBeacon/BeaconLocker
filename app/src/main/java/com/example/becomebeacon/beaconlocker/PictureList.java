package com.example.becomebeacon.beaconlocker;

import android.graphics.Bitmap;

import java.util.HashMap;

/**
 * Created by Ryu on 2017-05-27.
 */

public class PictureList {
    static public HashMap<String,Bitmap> pictures=new HashMap<>();

    public static void clear()throws Exception
    {
        for(Bitmap bm:pictures.values())
        {
            bm.recycle();
        }
        pictures.clear();
    }
}
