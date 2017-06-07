package com.example.becomebeacon.beaconlocker;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.becomebeacon.beaconlocker.pictureserver.Callback;
import com.example.becomebeacon.beaconlocker.pictureserver.PicturePopup;
import com.example.becomebeacon.beaconlocker.pictureserver.PictureUpload;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;

import static com.example.becomebeacon.beaconlocker.pictureserver.PicturePopup.CHOOSE_PICTURE;
import static com.example.becomebeacon.beaconlocker.pictureserver.PicturePopup.CROP_SMALL_PICTURE;
import static com.example.becomebeacon.beaconlocker.pictureserver.PicturePopup.TAKE_PICTURE;

/**
 * Created by gwmail on 2017-04-26.
 */

/* 등록할 때 몇미터 이상 할건지 같이등록 */

public class DataStoreActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mUserAddressRef;
    private BleDeviceInfo mBleDeviceInfo;

    private TextView et_Address;
    private EditText et_Nickname;
    private EditText et_Limit_distance;

    //storage 관련 변수
    //private Button btChoose;
    private Button btUpload;
    private ImageView ivPreview;

    private Uri filePath;

    private static Uri tempUri;
    private Bitmap mBitmap;
    private ProgressDialog progressDialog = null;

    private PicturePopup picturePopup;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        mBleDeviceInfo=DeviceInfoStore.getBleInfo();

        //툴바 세팅
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_additem);
        setSupportActionBar(toolbar);

        toolbar.setTitle(R.string.app_name);
        toolbar.setSubtitle("BLE 등록");

        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setSubtitleTextColor(ContextCompat.getColor(DataStoreActivity.this, R.color.colorSubtitle));

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mAuth= LoginActivity.getAuth();
        mUser= LoginActivity.getUser();
        mDatabase = DataFetch.getDatabase();
        mUserAddressRef = mDatabase.getReference("users/"+mUser.getUid()+"/beacons");

        et_Address = (TextView) findViewById(R.id.et_address);
        et_Nickname = (EditText) findViewById(R.id.et_NICKNAME);
        et_Limit_distance = (EditText) findViewById(R.id.et_Limit_distance);

        if(et_Address!=null&&mBleDeviceInfo!=null) {
            Log.d("DSA","check 2");
            et_Address.setText(mBleDeviceInfo.devAddress);
        }
        //사진 선택
        //btChoose = (Button) findViewById(R.id.btn_add_image);
        ivPreview = (ImageView) findViewById(R.id.iv_image);



        ivPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadMyPictureDialog();
            }
        });
        Log.v("DSA","Filepath first = " + String.valueOf(filePath));

    }
    @Override
    public void onDestroy() {
        super.onDestroy();

        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_additem, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_bt1:
                saveData();
                break;
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveData() {
        if (et_Address.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Address 값이 없습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        } else if (et_Nickname.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Nickname 값이 없습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (et_Limit_distance.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), "한계거리 값이 없습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        //'users' 에 소지한 비콘 Address 넣기
        BeaconOnUser beaconOnUser = new BeaconOnUser(mBleDeviceInfo.getDevAddress());

        mUserAddressRef.child(mBleDeviceInfo.getDevAddress()).setValue(beaconOnUser);

        //store beacon info to 'Beacon' DB in Uid order
        mBleDeviceInfo.setNickname(et_Nickname.getText().toString());
        mBleDeviceInfo.setLimitDistance(Double.valueOf(et_Limit_distance.getText().toString()));

        //사진이 있는 경우
        if (filePath != null) {
            PictureUpload pictureUpload = new PictureUpload(new Callback() {
                @Override
                public void callBackMethod(Object obj) {
                    //Upload 성공시
                    Log.d("MODULE_TEST", "Upload Success");
                    mBleDeviceInfo = (BleDeviceInfo)obj;
                    databaseStore();
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "업로드 완료!", Toast.LENGTH_SHORT).show();
                }
            }, new Callback() {
                @Override
                public void callBackMethod(Object obj) {
                    //Upload 실패시
                    Log.d("MODULE_TEST", "Upload Fail");
                    Exception e = (Exception)obj;
                    e.getStackTrace();
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "사진 업로드에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            });

            progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("업로드중...");
            progressDialog.show();

            Log.d("MODULE_TEST", "do->upload");
            pictureUpload.uploadPicture(mBleDeviceInfo, filePath);
        }

        //사진 없는경우 바로 업로드
        else {
            databaseStore();
        }

        BeaconList.scannedMap.remove(mBleDeviceInfo.devAddress);
        Log.d("dataStoreActivity", "size : " + BeaconList.mArrayListBleDevice.size());
        for (int i = 0; i < BeaconList.mArrayListBleDevice.size(); i++) {
            if (BeaconList.mArrayListBleDevice.get(i).devAddress == mBleDeviceInfo.devAddress) {
                BeaconList.mArrayListBleDevice.remove(i);
            }
        }
    }

    private void databaseStore() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("DB 게시중...");
        progressDialog.show();

        DatabaseReference databaseReference = mDatabase.getReference("beacon/").child(mBleDeviceInfo.getDevAddress());

        try {
            databaseReference.setValue(mBleDeviceInfo)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getApplicationContext(), "서버에 저장되었습니다.", Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.getStackTrace();
                            Toast.makeText(getApplicationContext(), "DB 저장에 실패하였습니다. 다시 시도해주세요.", Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                    });
        }
        catch (Exception e) {
            e.getStackTrace();
        }
    }

    ////////////////사진 팝업 및 저장 관련 메소드 ////////////////////
    private void uploadMyPictureDialog() {
        //이미지를 선택
        picturePopup = new PicturePopup(DataStoreActivity.this);
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
                    Log.d("GALL","Uri = " + data.toString());
                    picturePopup.pictureActivityForResult(requestCode, data, new Callback() {
                        @Override
                        public void callBackMethod(Object obj) {
                            //중간처리 완료
                            Log.d("GALL","Uri = " + obj.toString());
                            filePath = (Uri)obj;
                            picturePopup.cutImage(new Callback() {
                                @Override
                                public void callBackMethod(Object obj) {
                                    //사진 크롭 완료
                                    Log.d("GALL","Uri = " + obj.toString());
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
}