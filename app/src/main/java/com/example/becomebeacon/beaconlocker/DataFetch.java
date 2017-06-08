package com.example.becomebeacon.beaconlocker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

/**
 * Created by GW on 2017-05-02.
 */

public class DataFetch {
    public static FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mUserAddressRef;
    private FirebaseUser mUser;



    private ArrayList<BleDeviceInfo> myBleInfo;
    private HashMap<String, BleDeviceInfo> myItemMap;

    public Bitmap bitmapImage;
    public FirebaseStorage storage = FirebaseStorage.getInstance();
    public DataFetch(ArrayList<BleDeviceInfo> mAssignedItem, HashMap<String, BleDeviceInfo> mItemMap) {
        myBleInfo = mAssignedItem;
        myItemMap = mItemMap;
    }

    public void displayBeacons() throws Exception{
    // users/$Uid/beacons/"Address"
        mUser= LoginActivity.getUser();

        if(mUser!=null)
            mUserAddressRef = mDatabase.getReference("users/" + mUser.getUid() + "/beacons");

        mUserAddressRef
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot addressSnapshot : dataSnapshot.getChildren()) {
                            BeaconOnUser myBeaconOnUser = addressSnapshot.getValue(BeaconOnUser.class);
                            Log.v("Test_Print_ADDR", myBeaconOnUser.address);

                            try {

                                findBeaconByAddress(myBeaconOnUser.address);
                            }catch(Exception e)
                            {

                            }
                        }
                        GetMainActivity.getMainActity().mainProgressDialog.dismiss();
                        try {
                            BleService.mContext.addDBListener();
                        }catch(Exception e)
                        {

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public void findBeaconByAddress(String address) throws Exception{
        // beacon/address/"beaconOnDB"
        DatabaseReference beaconInfoRef = mDatabase.getReference("beacon/");

        final String myAddress = address;


        beaconInfoRef.child(address)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final BleDeviceInfo bleDeviceInfo;
                        bleDeviceInfo = dataSnapshot.getValue(BleDeviceInfo.class);


                        if(bleDeviceInfo!=null) {

                            if(bleDeviceInfo.getPictureUri() != null)
                            {
                                try {
                                    StorageReference storageRef = storage.getReference().child(bleDeviceInfo.getPictureUri());
                                    // Storage 에서 다운받아 저장시킬 임시파일
                                    final File imageFile = File.createTempFile("images", "jpg");
                                    storageRef.getFile(imageFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                            // Success Case
                                            bitmapImage = BitmapFactory.decodeFile(imageFile.getPath());

                                            PictureList.pictures.put(bleDeviceInfo.devAddress,bitmapImage);
                                            //mImage.setImageBitmap(bitmapImage);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Fail Case
                                            e.printStackTrace();
                                        }
                                    });
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            ////////

                            if(myItemMap.containsKey(myAddress)) {
                                myItemMap.remove(myAddress);
                                for (int i = 0; i < BeaconList.mAssignedItem.size(); i++) {

                                    if (BeaconList.mAssignedItem.get(i).devAddress.equals(myAddress)) {
                                        BeaconList.mAssignedItem.remove(i);
                                    }
                                }
                            }
                            myBleInfo.add(bleDeviceInfo);
                            myItemMap.put(myAddress, bleDeviceInfo);

                        }

                    }
                   // }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public static FirebaseDatabase getDatabase()throws Exception
    {
        return mDatabase;
    }
}
