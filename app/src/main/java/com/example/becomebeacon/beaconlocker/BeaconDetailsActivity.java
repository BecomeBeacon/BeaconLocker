package com.example.becomebeacon.beaconlocker;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
    private TextView meter;
    private Button showMap;
    private Button findStuff;
    private EditText limitDist;
    private Button disconnect;
    private Button main;
    private Button changeImage;
    private Button lostButton;
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
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_item_meter);
            mContext=this;


            Intent intent=getIntent();
            String da=intent.getStringExtra("MAC");
            noti=intent.getIntExtra("NOTI",-1);

            item=BeaconList.mItemMap.get(da);

            //툴바 세팅
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_additem);
            setSupportActionBar(toolbar);

            toolbar.setTitle(R.string.app_name);
            String subtitle = "상세정보 : " + item.devAddress;
            toolbar.setSubtitle(subtitle);

            toolbar.setTitleTextColor(Color.WHITE);
            toolbar.setSubtitleTextColor(ContextCompat.getColor(BeaconDetailsActivity.this, R.color.colorSubtitle));

            if(getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            if(noti!=-1) {
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(noti);
                Notifications.notifications.remove(item.devAddress+Values.NOTI_I_FIND);

            }


            initUI();
            initListeners();

            nickName.setText(item.nickname);
            limitDist.setText(item.limitDistance+"");
//        address.setText(item.devAddress);

            meter.setText(String.format("%.2f",item.distance2));

            if(item.getPictureUri() != null) {
                fetchPicture();
            }
            else {
                //사진 없는 경우
            }
            dataModify = new DataModify();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10200", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initUI() {
        try {
            ivPreview= (ImageView) findViewById(R.id.iv_image);
            nickName=(EditText)findViewById(R.id.et_NICKNAME);
//        address=(TextView)findViewById(R.id.et_address);
            meter=(TextView)findViewById(R.id.meter);
            disconnect=(Button)findViewById(R.id.disconnect);

            changeImage=(Button)findViewById(R.id.changeImage);
            showMap=(Button)findViewById(R.id.showMap);
            limitDist=(EditText) findViewById(R.id.limit_distance);
            findStuff=(Button)findViewById(R.id.find);
            lostButton = (Button)findViewById(R.id.lostButton);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10201", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    //툴바세팅
    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        try {
            switch(item.getItemId()) {
                case android.R.id.home:
                    toMainMethod();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10202", Toast.LENGTH_LONG).show();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void toMainMethod() {
        try {
            item.nickname = nickName.getText().toString();
            item.limitDistance = Double.valueOf(limitDist.getText().toString());

            //사진이 수정됐으면
            if (filePath != null) {
                progressDialog = new ProgressDialog(BeaconDetailsActivity.this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setMessage("업로드중...");
                progressDialog.show();

                PictureDelete pictureDelete = new PictureDelete(new Callback() {
                    @Override
                    public void callBackMethod(Object obj) {
                        //기존 사진 삭제 성공 시
                        item = (BleDeviceInfo) obj;
                        PictureUpload pictureUpload = new PictureUpload(new Callback() {
                            @Override
                            public void callBackMethod(Object obj) {
                                //사진 재 업로드 성공 시

                                item = (BleDeviceInfo) obj;
                                dataModify.changeBeacon(item);
                                progressDialog.dismiss();
                                finish();
                            }
                        }, new Callback() {
                            @Override
                            public void callBackMethod(Object obj) {
                                //사진 재 업로드 실패 시
                                progressDialog.dismiss();
                            }
                        });

                        pictureUpload.uploadPicture(item, filePath);
                    }
                }, new Callback() {
                    @Override
                    public void callBackMethod(Object obj) {
                        //기존 사진 삭제 실패 시
                        progressDialog.dismiss();
                    }
                });

                pictureDelete.deletePicture(item);
            }
            //수정 없으면 다른 데이터만 수정
            else {
                dataModify.changeBeacon(item);
                finish();
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10203", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initListeners() {
        try {
            if(item.isLost==false)
            {
                findStuff.setEnabled(false);
                lostButton.setEnabled(true);
            }
            else
            {
                findStuff.setEnabled(true);
                lostButton.setEnabled(false);
            }
            disconnect.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v)
                {
                    BeaconList.mItemMap.remove(item.devAddress);

                    for (int i = 0; i < BeaconList.mAssignedItem.size(); i++) {

                        if (BeaconList.mAssignedItem.get(i).devAddress.equals(item.devAddress)) {
                            BeaconList.mAssignedItem.remove(i);
                        }

                    }

                    dataModify.deleteBeacon(item);

                    finish();
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
                    Intent intent = new Intent(getApplicationContext(), MultiMapActivity.class);

                    intent.putExtra("LAT",item.latitude);
                    intent.putExtra("LON",item.longitude);
                    intent.putExtra("DATE",item.lastDate);
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
                    if(BeaconList.rewardMap.containsKey(item.devAddress)) {
                        FindMessage fm=new FindMessage();
                        fm.isPoint=true;
                        fm.point=-Values.REWARD_POINT;
                        mDatabase.getReference("users/" + BeaconList.rewardMap.get(item.devAddress)).child("messages")
                                .push().setValue(fm);

                    }

                    item.isLost = false;
                    Toast.makeText(getApplicationContext(),"회수처리 하였습니다.",Toast.LENGTH_SHORT).show();
                    findStuff.setEnabled(false);
                    Notifications.notifications.remove(item.devAddress+Values.NOTI_I_FIND);
                }
            });

            lostButton.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v) {
                    //잃어버림 신고 버튼
                    Intent intent = new Intent(BeaconDetailsActivity.this, RegLostDataActivity.class);
                    intent.putExtra("MAC",item.devAddress);
                    startActivity(intent);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10204", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onDestroy()
    {
        try {
            super.onDestroy();

            mContext=null;

            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10205", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    public void fetchPicture() {
        try {
            Bitmap bitmapImage=PictureList.pictures.get(item.devAddress);

            ivPreview.setImageBitmap(bitmapImage);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10206", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    ////////////////사진 팝업 및 저장 관련 메소드 ////////////////////
    private void uploadMyPictureDialog() {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10207", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    //Picture Intent 생성 후 Result 처리
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if (resultCode == DataStoreActivity.RESULT_OK) {
                try {
                    switch (requestCode) {
                        case CHOOSE_PICTURE:
                        case TAKE_PICTURE:
                            //사진을 가져옴
                            picturePopup.pictureActivityForResult(requestCode, data, new Callback() {
                                @Override
                                public void callBackMethod(Object obj) {
                                    //중간처리 완료
                                    filePath = (Uri) obj;
                                    picturePopup.cutImage(new Callback() {
                                        @Override
                                        public void callBackMethod(Object obj) {
                                            //사진 크롭 완료
                                            startActivityForResult((Intent) obj, CROP_SMALL_PICTURE);
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
                catch (Exception e) {

                    e.printStackTrace();
                    Toast.makeText(BeaconDetailsActivity.this, "오류가 발생했습니다. 다른 방법으로 해주세요", Toast.LENGTH_SHORT).show();

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10208", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    static public BeaconDetailsActivity getBDA()
    {
        return mContext;
    }

    public void refreshDistance()
    {
        try {
            meter.setText(String.format("%.2f",item.distance2));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10209", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}