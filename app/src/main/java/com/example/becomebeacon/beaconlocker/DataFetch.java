package com.example.becomebeacon.beaconlocker;

import android.util.Log;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by GW on 2017-05-02.
 */

public class DataFetch {
    public static FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mUserAddressRef;
    private FirebaseUser mUser;

    private ArrayList<BleDeviceInfo> myBleInfo;

    public void displayBeacons() {
    // users/$Uid/beacons/"Address"
        mUser= LoginActivity.getUser();

        mUserAddressRef = mDatabase.getReference("users/"+mUser.getUid()+"/beacons");

        myBleInfo = new ArrayList<BleDeviceInfo>();

        Log.v("Testing Print Uid", mUser.getUid());

        mUserAddressRef
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot addressSnapshot : dataSnapshot.getChildren()) {
                            BeaconOnUser myBeaconOnUser = addressSnapshot.getValue(BeaconOnUser.class);
                            Log.v("Test Print ADDR", myBeaconOnUser.address);

                            findBeaconByAddress(myBeaconOnUser.address);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    public void findBeaconByAddress(String address) {
        // beacon/address/"beaconOnDB"
        DatabaseReference beaconInfoRef = mDatabase.getReference("beacon/");

        beaconInfoRef.child(address)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        BeaconOnDB beaconOnDB = dataSnapshot.getValue(BeaconOnDB.class);
                        Log.v("Test Print nick", beaconOnDB.nickname);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public static FirebaseDatabase getDatabase()
    {
        return mDatabase;
    }
}
