package com.example.becomebeacon.beaconlocker.pictureserver;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.example.becomebeacon.beaconlocker.MainActivity;

import java.io.File;
import java.io.IOException;

/**
 * Created by gwmail on 2017-06-04.
 */

public class PicturePopup {
    private Context mContext;

    private static final int CHOOSE_PICTURE = 0;
    private static final int TAKE_PICTURE = 1;
    private static final int CROP_SMALL_PICTURE = 2;

    private Uri tempUri;
    private Uri filePath;

    public PicturePopup(Context context) {
        this.mContext = context;
    }

    public void showChoosePicDialog(final Callback choosePictureCallback, final Callback takePictureCallback) {
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
                        Uri tempUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "temp_image.png"));
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
//                cutImage(tempUri);        //사진 Cut 후 받아오기
                ImageToViewCallback.callBackMethod(tempUri);
                break;
            case CHOOSE_PICTURE:
//                cutImage(data.getData()); //사진 Cut 후 받아오기
                filePath = data.getData();
                ImageToViewCallback.callBackMethod(filePath);
                break;
//            case CROP_SMALL_PICTURE:
//                if (data != null) {
//                    setImageToView(data); // 사진은 미리보기
//                }
//                break;
        }
    }

        protected void cutImage(Uri uri) {
        if (uri == null) {
            Log.i("alanjet", "The uri is not exist.");
        }
        tempUri = uri;
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 설정
        intent.putExtra("crop", "true");
        // aspectX aspectY
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
//        // temp Uri 에 저장
//        Intent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
//        startActivityForResult(intent, CROP_SMALL_PICTURE);
    }

//    protected void setImageToView(Intent data, Callback ) {
//        Bundle extras = data.getExtras();
//        if (extras != null) {
//            mBitmap = extras.getParcelable("data");
//            //사진은 사각형
//            ivPreview.setImageBitmap(mBitmap);//미리보기...
//        }
//    }
}






