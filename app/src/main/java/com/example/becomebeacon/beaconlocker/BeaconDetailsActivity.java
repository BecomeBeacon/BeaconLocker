package com.example.becomebeacon.beaconlocker;

import android.app.NotificationManager;
import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.becomebeacon.beaconlocker.pictureserver.Callback;
import com.example.becomebeacon.beaconlocker.pictureserver.PictureDelete;
import com.example.becomebeacon.beaconlocker.pictureserver.PictureUpload;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.IOException;

/**
 * Created by 함상혁입니다 on 2017-05-14.
 */

public class BeaconDetailsActivity extends AppCompatActivity {

    private BleDeviceInfo item;
    private int noti;
    private EditText nickName;
    private TextView address;
    private TextView meter;
    private Button showMap;
    private Button findStuff;
    private EditText limitDist;
    private Button disconnect;
    private Button main;
    private Button changeImage;
    static private BeaconDetailsActivity mContext;
    private ImageView mImage;
    private Bitmap mBitmap;
    protected static final int CHOOSE_PICTURE = 0;
    protected static final int TAKE_PICTURE = 1;
    protected static Uri tempUri;
    private static final int CROP_SMALL_PICTURE = 2;
    private DataModify dataModify;

    public  FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private FirebaseUser mUser;

    private Uri filePath = null;
    private ProgressDialog progressDialog = null;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_meter);
        mContext=this;

        Intent intent=getIntent();
        String da=intent.getStringExtra("MAC");
        noti=intent.getIntExtra("NOTI",-1);

        item=BeaconList.mItemMap.get(da);

        if(noti!=-1) {
            Log.d("NOTIC","noti : "+noti);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(noti);
            Notifications.notifications.remove(item.devAddress);

        }



        initUI();
        initListeners();

        Log.d("BDA","item: "+item.nickname+", "+item.devAddress+", "+item.pictureUri);
        nickName.setText(item.nickname);
        limitDist.setText(item.limitDistance+"");
        address.setText(item.devAddress);

        meter.setText(String.format("%.2f",item.distance2));

        if(item.getPictureUri() != null) {
            fetchPicture();
        }
        else {
            //사진 없는 경우
        }
        dataModify = new DataModify();
    }

    private void initUI() {
        mImage= (ImageView) findViewById(R.id.iv_image);
        nickName=(EditText)findViewById(R.id.et_NICKNAME);
        address=(TextView)findViewById(R.id.et_address);
        meter=(TextView)findViewById(R.id.meter);
        disconnect=(Button)findViewById(R.id.disconnect);
        main=(Button)findViewById(R.id.toMain);
        changeImage=(Button)findViewById(R.id.changeImage);
        showMap=(Button)findViewById(R.id.showMap);
        limitDist=(EditText) findViewById(R.id.limit_distance);
        findStuff=(Button)findViewById(R.id.find);
    }

    private void initListeners() {
        if(item.isLost==false)
        {
            findStuff.setEnabled(false);
        }
        else
        {
            findStuff.setEnabled(true);
        }
        disconnect.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v)
            {
                BeaconList.mItemMap.remove(item.devAddress);
                Log.d("BDA","size : "+BeaconList.mAssignedItem.size());

                for (int i = 0; i < BeaconList.mAssignedItem.size(); i++) {
                    Log.d("BDA", "Compare " +BeaconList.mAssignedItem.get(i).devAddress+", "+item.devAddress);
                    if (BeaconList.mAssignedItem.get(i).devAddress.equals(item.devAddress)) {
                        BeaconList.mAssignedItem.remove(i);
                        Log.d("BDA", "removed");


                    }
                    else
                    {
                        Log.d("BDA", "not same");
                    }
                }
                Log.d("BDA","Array : "+BeaconList.mAssignedItem);
                dataModify.deleteBeacon(item);

                finish();
            }
        });

        main.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v)
            {
                item.nickname=nickName.getText().toString();
                item.limitDistance = Double.valueOf(limitDist.getText().toString());

                //사진이 수정됐으면
                if(filePath != null) {
                    progressDialog = new ProgressDialog(BeaconDetailsActivity.this);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setMessage("업로드중...");
                    progressDialog.show();

                    Log.d("PictureModify", "Picture Modify");
                    PictureDelete pictureDelete = new PictureDelete(new Callback() {
                        @Override
                        public void callBackMethod(Object obj) {
                            //기존 사진 삭제 성공 시
                            item = (BleDeviceInfo)obj;
                            PictureUpload pictureUpload = new PictureUpload(new Callback() {
                                @Override
                                public void callBackMethod(Object obj) {
                                    //사진 재 업로드 성공 시
                                    Log.d("PictureModify", "Picture Re-Upload Success");
                                    item = (BleDeviceInfo)obj;
                                    dataModify.changeBeacon(item);
                                    progressDialog.dismiss();
                                    finish();
                                }
                            }, new Callback() {
                                @Override
                                public void callBackMethod(Object obj) {
                                    //사진 재 업로드 실패 시
                                    Log.d("PictureModify", "Picture Re-Upload Fail");
                                    progressDialog.dismiss();
                                }
                            });

                            pictureUpload.uploadPicture(item, filePath);
                        }
                    }, new Callback() {
                        @Override
                        public void callBackMethod(Object obj) {
                            //기존 사진 삭제 실패 시
                            Log.d("PictureModify", "Picture Delete Fail");
                            progressDialog.dismiss();
                        }
                    });

                    pictureDelete.deletePicture(item);
                }
                //수정 없으면 다른 데이터만 수정
                else {
                    Log.d("BDA","else if(filePath != null)");
                    dataModify.changeBeacon(item);
                    finish();
                }
            }
        });

        changeImage.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v)
            {
                //이부분 새로운 사진어쩌구 쓴다
                showChoosePicDialog();
            }
        });

        showMap.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v)
            {
                Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                Log.d("BDA","lat : "+item.latitude+" long : "+item.longitude);
                intent.putExtra("LAT",item.latitude);
                intent.putExtra("LON",item.longitude);
                startActivity(intent);
            }
        });
        findStuff.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View v)
            {

                mDatabase.getReference("beacon/"+ item.devAddress + "/")
                            .child("isLost")
                            .setValue(false);

                mDatabase.getReference("lost_items/").child(item.devAddress).removeValue();

                item.isLost = false;
                Toast.makeText(getApplicationContext(),"찾음 ㅅㄱ",Toast.LENGTH_SHORT).show();
                findStuff.setEnabled(false);
                Notifications.notifications.remove(item.devAddress);
            }
        });
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.d("BDA","BDA destroyed");
        mContext=null;

        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    static public BeaconDetailsActivity getBDA()
    {
        return mContext;
    }

    public void refreshDistance()
    {
        meter.setText(String.format("%.2f",item.distance2));
    }

    protected void showChoosePicDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(BeaconDetailsActivity.this);
        builder.setTitle("사진선택");
        String[] items = { "사진 선택하기", "카메라" };
        builder.setNegativeButton("취소", null);
        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case CHOOSE_PICTURE: // 사진 선택
                        Intent openAlbumIntent = new Intent(
                                Intent.ACTION_GET_CONTENT);
                        openAlbumIntent.setType("image/*");
                        //startActivityForResult사용한다.
                        startActivityForResult(openAlbumIntent, CHOOSE_PICTURE);
                        break;
                    case TAKE_PICTURE: // 카메라
                        Intent openCameraIntent = new Intent(
                                MediaStore.ACTION_IMAGE_CAPTURE);
                        tempUri = Uri.fromFile(new File(Environment
                                .getExternalStorageDirectory(), "temp_image.jpg"));
                        // 카메라 찍은사진은 SD에 저장
                        openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
                        startActivityForResult(openCameraIntent, TAKE_PICTURE);
                        break;
                }
            }
        });
        builder.show();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == MainActivity.RESULT_OK) {
            Log.d("BeaconDetailsAct", "버튼 클릭");
            int result = new PermissionRequester.Builder(BeaconDetailsActivity.this)
                    .setTitle("권한 요청")
                    .setMessage("권한을 요청합니다.")
                    .setPositiveButtonName("네")
                    .setNegativeButtonName("아니요.")
                    .create()
                    .request(Manifest.permission.CALL_PHONE, 1000 , new PermissionRequester.OnClickDenyButtonListener() {
                        @Override
                        public void onClick(Activity activity) {
                            Log.d("RESULT", "취소함.");
                        }
                    });

            if (result == PermissionRequester.ALREADY_GRANTED) {
                Log.d("RESULT", "권한이 이미 존재함.");
                if (ActivityCompat.checkSelfPermission(BeaconDetailsActivity.this,
                        Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                }
            }
            else if(result == PermissionRequester.NOT_SUPPORT_VERSION)
                Log.d("RESULT", "마쉬멜로우 이상 버젼 아님.");
            else if(result == PermissionRequester.REQUEST_PERMISSION) {
                Log.d("RESULT", "요청함. 응답을 기다림.");
                switch (requestCode) {
                    case TAKE_PICTURE:
                        //cutImage(tempUri); // 사진 마름질하다.
                        filePath = tempUri;
                        imageToView(filePath);
                        Log.v("Test", "filepath = " + filePath);
                        break;
                    case CHOOSE_PICTURE:
                        //cutImage(data.getData());
                        filePath = data.getData();
                        imageToView(filePath);
                        Log.v("Test", "filepath = " + filePath);
                        break;
                    case CROP_SMALL_PICTURE:
                        if (data != null) {
                            setImageToView(data); // 사진은 미리보기
                        }
                        break;
                }
            }
        }
    }
    /**
     * 사진 마름질하다.
     */
//    protected void cutImage(Uri uri) {
//        if (uri == null) {
//            Log.i("alanjet", "The uri is not exist.");
//        }
//        tempUri = uri;
//        Intent intent = new Intent("com.android.camera.action.CROP");
//        intent.setDataAndType(uri, "image/*");
//        // 설정
//        intent.putExtra("crop", "true");
//        // aspectX aspectY
//        intent.putExtra("aspectX", 1);
//        intent.putExtra("aspectY", 1);
//        // outputX outputY
//        intent.putExtra("outputX", 150);
//        intent.putExtra("outputY", 150);
//        intent.putExtra("return-data", true);
//        startActivityForResult(intent, CROP_SMALL_PICTURE);
//    }
    /**
     * 사진 저장
     */
    protected void setImageToView(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            mBitmap = extras.getParcelable("data");
            //사진은 사각형
            mImage.setImageBitmap(mBitmap);//미리보기...
        }
    }
    private void imageToView(Uri uri) {
        try {
            //Uri 파일을 Bitmap으로 만들어서 ImageView에 집어 넣는다.
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            mImage.setImageBitmap(bitmap);
            PictureList.pictures.remove(item.devAddress);
            PictureList.pictures.put(item.devAddress,bitmap);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void fetchPicture() {
        Bitmap bitmapImage=PictureList.pictures.get(item.devAddress);

        mImage.setImageBitmap(bitmapImage);
    }
}