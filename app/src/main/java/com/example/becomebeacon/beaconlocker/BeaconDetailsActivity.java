package com.example.becomebeacon.beaconlocker;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;

import static com.example.becomebeacon.beaconlocker.R.id.imageView;

/**
 * Created by 함상혁입니다 on 2017-05-14.
 */

public class BeaconDetailsActivity extends AppCompatActivity {

    private BleDeviceInfo item;
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



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_meter);
        mContext=this;



        item=DeviceInfoStore.getBleInfo();
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
        mImage= (ImageView) findViewById(imageView);
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
        disconnect.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v)
            {
                BeaconList.mItemMap.remove(item.devAddress);
                Log.d("BDA","size : "+BeaconList.mAssignedItem.size());
                for(int i=0;i<BeaconList.mAssignedItem.size();i++)
                {
                    if(BeaconList.mAssignedItem.get(i).devAddress==item.devAddress) {
                        BeaconList.mAssignedItem.remove(i);
                        Log.d("BDA","removed");
                        break;
                    }
                }
                dataModify.deleteBeacon(item);

                finish();
            }
        });

        main.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v)
            {
                item.nickname=nickName.getText().toString();
                item.limitDistance = Double.valueOf(limitDist.getText().toString());

                dataModify.changeBeacon(item);

                finish();
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
                if(item.isLost == true)
                {
                    mDatabase.getReference("beacon/"+ item.devAddress + "/")
                            .child("isLost")
                            .setValue(false);

                    mDatabase.getReference("lost_items/").child(item.devAddress).removeValue();

                    item.isLost = false;
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "잃어버린 물건이 아닙니다!!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onDestroy()
    {
        Log.d("BDA","BDA destroyed");
        mContext=null;
        super.onDestroy();
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
            switch (requestCode) {
                case TAKE_PICTURE:
                    cutImage(tempUri); // 사진 마름질하다.
                    break;
                case CHOOSE_PICTURE:
                    cutImage(data.getData());
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
            mImage.setImageBitmap(mBitmap);//미리보기...
        }
    }

    public void fetchPicture() {
        Bitmap bitmapImage=PictureList.pictures.get(item.devAddress);

        mImage.setImageBitmap(bitmapImage);
//        FirebaseStorage storage = FirebaseStorage.getInstance();
//        StorageReference storageRef = storage.getReferenceFromUrl("gs://beaconlocker-51c69.appspot.com/");
//        Log.v("Test_Uri1", "URI = " + item.pictureUri);
//        Toast.makeText(BeaconDetailsActivity.this,item.pictureUri,Toast.LENGTH_SHORT).show();
//        try {
//            storageRef = storage.getReferenceFromUrl("gs://beaconlocker-51c69.appspot.com/").child(item.pictureUri);
//        }
//        catch (Exception e) {
//            Toast.makeText(BeaconDetailsActivity.this,item.pictureUri,Toast.LENGTH_SHORT).show();
//            Log.v("Test_Uri2", "URI = " + item.pictureUri);
//        }
//
//        try {
//            // Storage 에서 다운받아 저장시킬 임시파일
//            final File imageFile = File.createTempFile("images", "jpg");
//            storageRef.getFile(imageFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
//                @Override
//                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
//                    // Success Case
//                    Bitmap bitmapImage = BitmapFactory.decodeFile(imageFile.getPath());
//                    mImage.setImageBitmap(bitmapImage);
//                    Toast.makeText(getApplicationContext(), "Success !!", Toast.LENGTH_LONG).show();
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    // Fail Case
//                    e.printStackTrace();
//                    Toast.makeText(getApplicationContext(), "Fail !!", Toast.LENGTH_LONG).show();
//                }
//            });
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }


}