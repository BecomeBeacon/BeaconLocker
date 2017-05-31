package com.example.becomebeacon.beaconlocker;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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

import com.estimote.sdk.Beacon;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    //private TextView et_Picture;
    //private EditText et_Islost;
    //private EditText et_LATITUDE;
    //private EditText et_LONGITUDE;

    //storage 관련 변수
    private Button btChoose;
    private Button btUpload;
    private ImageView ivPreview;

    private Uri filePath;

    private static final int CHOOSE_PICTURE = 0;
    private static final int TAKE_PICTURE = 1;
    private static final int CROP_SMALL_PICTURE = 2;
    private static Uri tempUri;
    private Bitmap mBitmap;
    private ProgressDialog progressDialog = null;


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
        //et_Picture = (TextView) findViewById(R.id.et_PICTURE);
        //et_LATITUDE = (EditText) findViewById(R.id.et_LATITUDE);
        //et_LONGITUDE = (EditText) findViewById(R.id.et_LONGITUDE);

        //mUserAddressRef = mDatabase.getReference("users/"+mUser.getUid()+"/beacons");
        //TODO :: 인증 실패시 조치
        //if(mFirebaseUser == null) {
        //startActivity(new Intent(MainActivity.this, LoginActivity.class));
        //finish();
        //return;
        //}
        if(et_Address!=null&&mBleDeviceInfo!=null) {
            Log.d("DSA","check 2");
            et_Address.setText(mBleDeviceInfo.devAddress);
        }



        //사진 선택
        btChoose = (Button) findViewById(R.id.btn_add_image);
        ivPreview = (ImageView) findViewById(R.id.iv_image);



        btChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //이미지를 선택
                showChoosePicDialog();
//                Intent intent = new Intent();
//                intent.setType("image/*");
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                startActivityForResult(Intent.createChooser(intent, "이미지를 선택하세요."), 0);
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

    private void initData() {
        et_Address.setText("");
        et_Nickname.setText("");
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


        BleDeviceInfo bleDeviceInfo = DeviceInfoStore.getBleInfo();
        BeaconOnUser beaconOnUser = new BeaconOnUser(bleDeviceInfo.getDevAddress());

        mUserAddressRef.child(bleDeviceInfo.getDevAddress()).setValue(beaconOnUser);

        //store beacon info to 'Beacon' DB in Uid order
        bleDeviceInfo.setNickname(et_Nickname.getText().toString());
        bleDeviceInfo.setLimitDistance(Double.valueOf(et_Limit_distance.getText().toString()));
        if (filePath != null) {
            bleDeviceInfo.setPictureUri(uploadFile());
        }
            mDatabase
                    .getReference("beacon/")
                    .child(bleDeviceInfo.getDevAddress())
                    .setValue(bleDeviceInfo)
                    .addOnSuccessListener(DataStoreActivity.this, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getApplicationContext(), "서버에 저장되었습니다.", Toast.LENGTH_LONG).show();
                            initData();
                            finish();
                        }
                    })
                    .addOnFailureListener(DataStoreActivity.this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "저장에 실패하였습니다.", Toast.LENGTH_LONG).show();
                        }
                    });
            BeaconList.scannedMap.remove(bleDeviceInfo.devAddress);
            Log.d("dataStoreActivity", "size : " + BeaconList.mArrayListBleDevice.size());
            for (int i = 0; i < BeaconList.mArrayListBleDevice.size(); i++) {
                if (BeaconList.mArrayListBleDevice.get(i).devAddress == bleDeviceInfo.devAddress) {
                    BeaconList.mArrayListBleDevice.remove(i);
                    Log.d("dataStoreActivity", "removed");

                }
            }
    }

//    //결과 처리
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        //request코드가 0이고 OK를 선택했고 data에 뭔가가 들어 있다면
//        if(requestCode == 0 && resultCode == RESULT_OK){
//            filePath = data.getData();
//            Log.d("TAG", "uri:" + String.valueOf(filePath));
//            try {
//                //Uri 파일을 Bitmap으로 만들어서 ImageView에 집어 넣는다.
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
//                ivPreview.setImageBitmap(bitmap);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    //upload the file
    private String uploadFile() {
        //업로드할 파일이 있으면 수행
        Log.v("Test","Filepath in uploadFile = " + String.valueOf(filePath));
        //업로드 진행 Dialog 보이기

        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("업로드중...");
        progressDialog.show();

        //storage
        FirebaseStorage storage = FirebaseStorage.getInstance();

        //Unique한 파일명을 만들자.
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMHH_mmss");
        Date now = new Date();
        String filename = formatter.format(now) + ".png";
        //storage 주소와 폴더 파일명을 지정해 준다.
        StorageReference storageRef = storage.getReferenceFromUrl("gs://beaconlocker-51c69.appspot.com/").child("beacon_images/" + filename);
        storageRef.putFile(filePath)
                //성공시
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        progressDialog.dismiss(); //업로드 진행 Dialog 상자 닫기
                        Toast.makeText(getApplicationContext(), "업로드 완료!", Toast.LENGTH_SHORT).show();
                    }
                })
                //실패시
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
//                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "업로드 실패!", Toast.LENGTH_SHORT).show();
                    }
                })
                //진행중
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
//                        @SuppressWarnings("VisibleForTests")
//                        double progress = (100 * taskSnapshot.getBytesTransferred()) /  taskSnapshot.getTotalByteCount();
//                        //dialog에 진행률을 퍼센트로 출력해 준다
//                        progressDialog.setMessage("Uploaded " + ((int) progress) + "% ...");
                    }
                });
        return "beacon_images/" + filename;
    }

    //TODO:: 모듈화 하기
    protected void showChoosePicDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DataStoreActivity.this);
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
                                .getExternalStorageDirectory(), "temp_image.png"));
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
    /**
     * 사진 마름질하다.
     */
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
        startActivityForResult(intent, CROP_SMALL_PICTURE);
    }
    /**
     * 사진 저장
     */
    protected void setImageToView(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            mBitmap = extras.getParcelable("data");
            //사진은 사각형
            ivPreview.setImageBitmap(mBitmap);//미리보기...
        }
    }

    private void imageToView(Uri uri) {
        try {
            //Uri 파일을 Bitmap으로 만들어서 ImageView에 집어 넣는다.
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            ivPreview.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}