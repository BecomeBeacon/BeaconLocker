package com.example.becomebeacon.beaconlocker;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;


public class BeaconBackHostActivity extends AppCompatActivity {

    static private BeaconBackHostActivity mContext;
    private Button sendMessage;
    private TextView viewRssi;
    private EditText writeMessage;
    public  FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private FirebaseUser mUser;
    public Bitmap bitmapImage;
    public FirebaseStorage storage = FirebaseStorage.getInstance();
    private ImageView ivPreview;
    private TextView text_bd_name;

    String phoneNum;
    String inputMessage;
    BleDeviceInfo info;

    String mac;
    FindMessage fm;

    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_back_host);
            mContext=this;

            Intent intent = getIntent();
            mac = intent.getStringExtra("MAC");
            int noti= intent.getIntExtra("NOTI",-1);
            if(noti!=-1) {
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(noti);
                Notifications.notifications.remove(mac+Values.NOTI_I_FIND);

            }
            if(mac==null)
            {
                Uri uriData = getIntent().getData();
                mac = uriData.getQueryParameter("beaconID");
            }
            info = BeaconList.lostMap.get(mac);

        //툴바 세팅
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

            toolbar.setTitle(R.string.app_name);
            String subtitle = "상세정보 : " + info.devAddress;
            toolbar.setSubtitle(subtitle);

            toolbar.setTitleTextColor(Color.WHITE);
            toolbar.setSubtitleTextColor(ContextCompat.getColor(BeaconBackHostActivity.this, R.color.colorSubtitle));

            if(getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            initUI();
            initListeners();
            viewImage();
            fm=new FindMessage();
            fm.devAddress = info.devAddress;
            mHandler.sendEmptyMessage(0);
            text_bd_name.setText(info.getNickname());
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10100", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    //툴바세팅
    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item){
        try {
            switch(item.getItemId()) {
                case android.R.id.home:
                    finish();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10101", Toast.LENGTH_LONG).show();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initUI() {
        try {
            sendMessage=(Button)findViewById(R.id.sendMessageButton);
            viewRssi = (TextView)findViewById(R.id.rssiFlow);
            writeMessage = (EditText) findViewById(R.id.message);
            ivPreview = (ImageView) findViewById(R.id.lost_device_image);
            text_bd_name = (TextView) findViewById(R.id.text_bd_name);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10102", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initListeners() {

        try {
            sendMessage.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v)
                {
                    inputMessage = writeMessage.getText().toString();
                    fm.message = inputMessage;

                    mDatabase.getReference("users/"+ info.getUid()).child("messages")
                            .push().setValue(fm);

                    BeaconList.lostMap.get(info.devAddress).othersSendMsg=true;

                    Toast.makeText(getApplicationContext(),"메시지 발송 완료",Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10103", Toast.LENGTH_LONG).show();
            finish();
        }

    }

    private void viewImage() {
        try {
            if (info.getPictureUri() == "") {
                Log.d("BBHA", "if문");
            } else {
                Log.d("BBHA", "else문");
                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setMessage("사진을 불러오는 중...");
                progressDialog.show();

                try {
                    Log.d("BBHA", "getpictureUri = " + info.getPictureUri());
                    StorageReference storageRef = storage.getReference().child(info.getPictureUri());
                    // Storage 에서 다운받아 저장시킬 임시파일
                    final File imageFile = File.createTempFile("images", "jpg");
                    Log.d("BBHA", "2222");
                    storageRef.getFile(imageFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            // Success Case
                            bitmapImage = BitmapFactory.decodeFile(imageFile.getPath());
                            ivPreview.setImageBitmap(bitmapImage);
                            progressDialog.dismiss();
                            Log.d("BBHA", "3333");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Fail Case
                            e.printStackTrace();
                            progressDialog.dismiss();
                            Log.d("BBHA", "4444");
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "사진을 불러오지 못했습니다.", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                    Log.d("BBHA", "5555");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10104", Toast.LENGTH_LONG).show();
            finish();
        }
        Log.d("BBHA", "6666");
    }

    private void viewRssiInUser()
    {
        try {
            String str = info.getDistance2() + "";
            viewRssi.setText(str);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10105", Toast.LENGTH_LONG).show();
            finish();
        }
    }
    @Override
    public void onDestroy()
    {
        try {
            mContext=null;
            super.onDestroy();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10106", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private Handler mHandler= new Handler()
    {
        public void handleMessage(Message msg)
        {
            try {
                viewRssiInUser();
                mHandler.sendEmptyMessageDelayed(0, 500);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10107", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    };
}
