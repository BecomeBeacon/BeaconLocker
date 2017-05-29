package com.example.becomebeacon.beaconlocker;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.example.becomebeacon.beaconlocker.database.DbOpenHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import static android.content.ContentValues.TAG;

/**
 * Created by heeseung on 2017-05-29.
 */

public class lostDevPullService extends Service {
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseRef;
    private DbOpenHelper dbOpenHelper;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseRef = mDatabase.getReference();

        dbOpenHelper = new DbOpenHelper(getApplicationContext());
        dbOpenHelper.open();
    }

    public void pullLostDevices(){

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot temp:dataSnapshot.getChildren()) {
                    dbOpenHelper.insert(temp.getKey(), Double.valueOf(temp.child("longitude").getKey()),
                            Double.valueOf(temp.child("latitude").getKey()), temp.child("lastdate").getKey());
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }
}
