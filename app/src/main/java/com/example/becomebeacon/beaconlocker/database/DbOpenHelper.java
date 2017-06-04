package com.example.becomebeacon.beaconlocker.database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by heeseung on 2017-05-30.
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
            db.execSQL(DataBases.CreateDB._CREATE);
        }

        // 버전이 업데이트 되었을 경우 DB를 다시 만들어 준다.
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS "+DataBases.CreateDB._TABLENAME);
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
    }

    public void close(){
        mDB.close();
    }

    public void execSQL(String query) {
        mDB.execSQL(query);
    }

    public boolean uniqueTest(String devAddr) {
        if (mDB.rawQuery("SELECT * FROM lost_devices WHERE devaddr = ?", new String[] {devAddr}) == null)
            return true;
        else
            return false;
    }

    public void dropAndCreateTable()
    {
        mDB.execSQL("DROP TABLE IF EXISTS lostDevices.lost_devices");
        mDB.execSQL("CREATE TABLE IF NOT EXISTS lost_devices ( " +
                "devaddr VARCHAR(32) NOT NULL, " +
                "latitude DOUBLE NOT NULL, " +
                "longitude DOUBLE NOT NULL, " +
                "lastdate VARCHAR(32) NOT NULL, " +
                "PRIMARY KEY (devaddr));"
        );
    }

    public Cursor selectQuery(String query){
        return mDB.rawQuery(query, null);
    }
}