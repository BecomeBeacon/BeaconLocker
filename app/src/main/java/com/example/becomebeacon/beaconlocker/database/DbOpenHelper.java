package com.example.becomebeacon.beaconlocker.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by gwmail on 2017-04-26.
 */

public class DbOpenHelper {

    private static final String DATABASE_NAME = "lostDevices.db";
    private static final int DATABASE_VERSION = 1;
    public static SQLiteDatabase mDB;
    private DatabaseHelper mDBHelper;
    private Context mCtx;

    private class DatabaseHelper extends SQLiteOpenHelper {

        // 생성자
        public DatabaseHelper(Context context, String name,
                              SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        // 최초 DB를 만들때 한번만 호출된다.
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DeviceDatabase.CreateDB._CREATE);

        }

        // 버전이 업데이트 되었을 경우 DB를 다시 만들어 준다.
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS "+DeviceDatabase.CreateDB._TABLENAME);
            onCreate(db);
        }
    }

    public DbOpenHelper(Context context){
        this.mCtx = context;
    }

    public DbOpenHelper open() throws SQLException {
        mDBHelper = new DatabaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
        mDB = mDBHelper.getWritableDatabase();
        return this;
    }   // getWritableDatabase() = DB사용권한 부여

    public void close(){
        mDB.close();
    }

    public void insert(String devAddr, double latitude, double longitude, String lostDate) {
        // 읽고 쓰기가 가능하게 DB 열기
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        // DB에 입력한 값으로 행 추가
        db.execSQL("INSERT INTO lostDevices VALUES(null, '" + devAddr + "', " + latitude + ", '" + longitude + ", '" + lostDate + "');");
        db.close();
    }
}