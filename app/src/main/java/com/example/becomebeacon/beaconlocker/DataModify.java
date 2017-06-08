package com.example.becomebeacon.beaconlocker;

import android.util.Log;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gwmail on 2017-05-24.
 */

public class DataModify {
    public static FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    public static FirebaseStorage mStorage = FirebaseStorage.getInstance();
    private DatabaseReference mDatabaseRef;
    private StorageReference mStorageRef;
    private FirebaseUser mUser;

    private static final int LINK_BEACON = 0;
    private static final int LINK_LOSTITEM = 1;
    private static final int LINK_USER = 2;

    // whatChange = (1 = Beacon, 2 = lost item, 3 = User)
    public DataModify() throws Exception{
        mUser= LoginActivity.getUser();
        mDatabaseRef = mDatabase.getReference();
        mStorageRef = mStorage.getReference();
    }

    public void modifySwitch(int whatChange) throws Exception{
        switch(whatChange) {
            case LINK_BEACON:
                mDatabaseRef = mDatabase.getReference("beacon");
                break;
            case LINK_LOSTITEM:
                mDatabaseRef = mDatabase.getReference("lost_item");
                break;
            case LINK_USER:
                mDatabaseRef = mDatabase.getReference("users/"+mUser.getUid()+"/beacons");
                break;
        }
    }

    public void changeBeacon(BleDeviceInfo bleDeviceInfo) {
        Map<String, Object> childUpdates = new HashMap<>();
        try {
            childUpdates.put("/beacon/" + bleDeviceInfo.getDevAddress(), bleDeviceInfo);
        }catch(Exception e)
        {

        }
        mDatabaseRef.updateChildren(childUpdates);

    }

    public void deleteBeacon(BleDeviceInfo bleDeviceInfo){
        try {
            mStorageRef.child(bleDeviceInfo.getPictureUri()).delete();
        }
        catch (Exception e) {

        }
        mDatabaseRef.child("beacon/").child(bleDeviceInfo.devAddress).removeValue();

        mDatabaseRef.child("users").child(mUser.getUid()).child("beacons").child(bleDeviceInfo.devAddress).removeValue();
    }
}
