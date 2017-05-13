package com.example.becomebeacon.beaconlocker.database;

import android.provider.BaseColumns;

/**
 * Created by gwmail on 2017-04-26.
 */

public final class DeviceDatabase {

    public static final class CreateDB implements BaseColumns {
        public static final String UUID = "uuid";
        public static final String NICKNAME = "nickname";
        public static final String PICTURE = "picture";
        public static final String ISLOST = "islost";
        public static final String _TABLENAME = "address";

        public static final String _CREATE =
                "create table "+_TABLENAME+"("
                        +_ID+" integer primary key autoincrement, "
                        +UUID+" text not null , "
                        +NICKNAME+" text not null , "
                        +PICTURE+" text not null , "
                        +ISLOST+" text not null );";
    }
}
