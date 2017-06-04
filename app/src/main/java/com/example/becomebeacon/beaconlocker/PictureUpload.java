package com.example.becomebeacon.beaconlocker;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by gwmail on 2017-06-02.
 */

public class PictureUpload {
    private FirebaseStorage mStorage;
    private FirebaseDatabase mDatabase;
    private String mPicUri;
    private BleDeviceInfo mBleDeviceInfo;

    private CallBack successCallback;
    private CallBack failCallback;

    public PictureUpload(CallBack successCallback, CallBack failCallback) {
        mStorage = FirebaseStorage.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        this.successCallback = successCallback;
        this.failCallback = failCallback;
    }

    public void uploadPicture(BleDeviceInfo bleDeviceInfo, Uri filePath) {
        //Log : Upload 할 filepath
        Log.v("PictureUpload","Filepath in uploadFile = " + String.valueOf(filePath));
        mBleDeviceInfo = bleDeviceInfo;

        //Unique한 파일명을 만들자.
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMHH_mmss");
        Date now = new Date();
        String filename = formatter.format(now) + ".png";

        //Picture URI 를 저장
        mPicUri = "beacon_images/" + filename;


        //storage 주소와 폴더 파일명을 지정해 준다.
        StorageReference storageRef = mStorage.getReference().child(mPicUri);
        storageRef.putFile(filePath)
                //성공시
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.v("PictureUpload", "uploadPicture Success");
                        mBleDeviceInfo.setPictureUri(mPicUri);
                        getUriFromFirebase();
                    }
                })
                //실패시
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.v("PictureUpload", "uploadPicture Fail");
                        failCallback.callBackMethod(e);
                    }
                })
                //진행중
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                    }
                });
    }

    private void getUriFromFirebase() {
        StorageReference storageRef = mStorage.getReference().child(mPicUri);
        storageRef.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.v("PictureUpload", "getUriFromFirebase Success");
                        mBleDeviceInfo.setPictureLink(uri.toString());
                        successCallback.callBackMethod(mBleDeviceInfo);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.v("PictureUpload", "getUriFromFirebase Fail");
                        failCallback.callBackMethod(e);
                    }
                });
    }
}
