package com.example.becomebeacon.beaconlocker.pictureserver;

import android.support.annotation.NonNull;

import com.example.becomebeacon.beaconlocker.BleDeviceInfo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

/**
 * Created by gwmail on 2017-06-03.
 */

public class PictureDelete {
    private FirebaseStorage mStorage;
    private FirebaseDatabase mDatabase;

    private Callback successCallback;
    private Callback failCallback;

    private BleDeviceInfo mBleDeviceInfo;

    public PictureDelete(Callback successCallback, Callback failCallback) {
        mStorage = FirebaseStorage.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        this.successCallback = successCallback;
        this.failCallback = failCallback;
    }

    public void deletePicture(BleDeviceInfo bleDeviceInfo){
        mBleDeviceInfo = bleDeviceInfo;
        if(mBleDeviceInfo.getPictureUri()!=null)
            mStorage.getReference().child(mBleDeviceInfo.getPictureUri()).delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            successCallback.callBackMethod(mBleDeviceInfo);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            failCallback.callBackMethod(e);
                        }
                    });
    }
}
