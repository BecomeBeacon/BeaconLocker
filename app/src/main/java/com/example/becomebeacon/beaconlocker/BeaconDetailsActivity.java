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
import com.example.becomebeacon.beaconlocker.pictureserver.PicturePopup;
import com.example.becomebeacon.beaconlocker.pictureserver.PictureUpload;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.IOException;

import static com.example.becomebeacon.beaconlocker.pictureserver.PicturePopup.CHOOSE_PICTURE;
import static com.example.becomebeacon.beaconlocker.pictureserver.PicturePopup.CROP_SMALL_PICTURE;
import static com.example.becomebeacon.beaconlocker.pictureserver.PicturePopup.TAKE_PICTURE;

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
    private ImageView ivPreview;
    private Bitmap mBitmap;
    protected static Uri tempUri;
    private DataModify dataModify;

    public  FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private FirebaseUser mUser;

    private Uri filePath = null;
    private ProgressDialog progressDialog = null;

    private PicturePopup picturePopup;



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
        ivPreview= (ImageView) findViewById(R.id.iv_image);
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
                uploadMyPictureDialog();
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

    public void fetchPicture() {
        Bitmap bitmapImage=PictureList.pictures.get(item.devAddress);

        ivPreview.setImageBitmap(bitmapImage);
    }

    ////////////////사진 팝업 및 저장 관련 메소드 ////////////////////
    private void uploadMyPictureDialog() {
        //이미지를 선택
        picturePopup = new PicturePopup(BeaconDetailsActivity.this);
        picturePopup.showChoosePicDialog(new Callback() {
            @Override
            public void callBackMethod(Object obj) {
                //사진 선택
                startActivityForResult((Intent)obj, CHOOSE_PICTURE);
            }
        }, new Callback() {
            @Override
            public void callBackMethod(Object obj) {
                //사진 촬영
                startActivityForResult((Intent)obj, TAKE_PICTURE);
            }
        });
    }

    //Picture Intent 생성 후 Result 처리
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == DataStoreActivity.RESULT_OK) {
            switch (requestCode) {
                case CHOOSE_PICTURE:
                case TAKE_PICTURE:
                    //사진을 가져옴
                    picturePopup.pictureActivityForResult(requestCode, data, new Callback() {
                        @Override
                        public void callBackMethod(Object obj) {
                            //중간처리 완료
                            filePath = (Uri)obj;
                            picturePopup.cutImage(new Callback() {
                                @Override
                                public void callBackMethod(Object obj) {
                                    //사진 크롭 완료
                                    startActivityForResult((Intent)obj, CROP_SMALL_PICTURE);
                                }
                            });
                        }
                    });
                    break;
                case CROP_SMALL_PICTURE:
                    try {
                        //Uri 파일을 Bitmap으로 만들어서 ImageView에 집어 넣는다.
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                        ivPreview.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
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
}