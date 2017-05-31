package com.example.becomebeacon.beaconlocker.database;

import android.provider.BaseColumns;

/**
 * Created by heeseung on 2017-05-30.
 */

public final class DataBases {

    public static final class CreateDB implements BaseColumns {
        public static final String DEVADDR = "devaddr";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String LASTDATE = "lastdate";
        public static final String _TABLENAME = "lost_devices";
        public static final String _CREATE =
                "create table IF NOT EXISTS "+ _TABLENAME +"("
                        +DEVADDR+" text not null , "
                        +LATITUDE+" text not null , "
                        +LONGITUDE+" text not null , "
                        +LASTDATE+" text not null );";
    }
}