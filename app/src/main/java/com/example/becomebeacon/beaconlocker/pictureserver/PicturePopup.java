package com.example.becomebeacon.beaconlocker.pictureserver;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.example.becomebeacon.beaconlocker.PermissionRequester;

import java.io.File;

/**
 * Created by gwmail on 2017-06-04.
 */

public class PicturePopup {
    private Context mContext;

    public static final int CHOOSE_PICTURE = 0;
    public static final int TAKE_PICTURE = 1;
    public static final int CROP_SMALL_PICTURE = 2;

    private Uri tempUri;

    public PicturePopup(Context context) {
        this.mContext = context;
    }

    public void showChoosePicDialog(final Callback choosePictureCallback, final Callback takePictureCallback) {
        int result1 = new PermissionRequester.Builder((Activity) mContext)
                .setTitle("권한 요청")
                .setMessage("권한을 요청합니다.")
                .setPositiveButtonName("네")
                .setNegativeButtonName("아니요.")
                .create()
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE, 1000 , new PermissionRequester.OnClickDenyButtonListener() {
                    @Override
                    public void onClick(Activity activity) {
                        Log.d("RESULT", "취소함.");
                    }
                });

        if (result1 == PermissionRequester.ALREADY_GRANTED) {
            Log.d("RESULT", "권한이 이미 존재함.");
            if (ActivityCompat.checkSelfPermission( mContext,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            }
        }
        else if(result1 == PermissionRequester.NOT_SUPPORT_VERSION)
            Log.d("RESULT", "마쉬멜로우 이상 버젼 아님.");
        else if(result1 == PermissionRequester.REQUEST_PERMISSION) {
            Log.d("RESULT", "요청함. 응답을 기다림.");
            int result2 = new PermissionRequester.Builder((Activity) mContext)
                    .setTitle("권한 요청")
                    .setMessage("권한을 요청합니다.")
                    .setPositiveButtonName("네")
                    .setNegativeButtonName("아니요.")
                    .create()
                    .request(Manifest.permission.READ_EXTERNAL_STORAGE, 1000 , new PermissionRequester.OnClickDenyButtonListener() {
                        @Override
                        public void onClick(Activity activity) {
                            Log.d("RESULT", "취소함.");
                        }
                    });

            if (result2 == PermissionRequester.ALREADY_GRANTED) {
                Log.d("RESULT", "권한이 이미 존재함.");
                if (ActivityCompat.checkSelfPermission(mContext,
                        Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                }
            }
            else if(result2 == PermissionRequester.NOT_SUPPORT_VERSION)
                Log.d("RESULT", "마쉬멜로우 이상 버젼 아님.");
            else if(result2 == PermissionRequester.REQUEST_PERMISSION) {
                Log.d("RESULT", "요청함. 응답을 기다림.");

            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("사진선택");
        String[] items = { "사진 선택하기", "카메라" };
        builder.setNegativeButton("취소", null);
        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case CHOOSE_PICTURE: // 사진 선택
                        Intent openAlbumIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        openAlbumIntent.setType("image/*");
                        choosePictureCallback.callBackMethod(openAlbumIntent);
                        break;
                    case TAKE_PICTURE: // 카메라
                        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        tempUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "temp_image.png"));
                        // 카메라 찍은사진은 SD에 저장
                        openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
                        takePictureCallback.callBackMethod(openCameraIntent);
                        break;
                }
            }
        });
        builder.show();
    }

    public void pictureActivityForResult(int requestCode, Intent data, Callback ImageToViewCallback) {
        switch (requestCode) {
            case TAKE_PICTURE:
                ImageToViewCallback.callBackMethod(tempUri);
                break;
            case CHOOSE_PICTURE:
                tempUri = data.getData();
                ImageToViewCallback.callBackMethod(tempUri);
                break;
        }
    }

    public void cutImage(Callback cropSmallPictureCallback) {
        if (tempUri == null) {
            Log.i("alanjet", "The uri is not exist.");
        }
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(tempUri, "image/*");
        // 설정
        intent.putExtra("crop", "true");
        // aspectX aspectY
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        // temp Uri 에 저장
        intent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);

        cropSmallPictureCallback.callBackMethod(intent);
    }
}






