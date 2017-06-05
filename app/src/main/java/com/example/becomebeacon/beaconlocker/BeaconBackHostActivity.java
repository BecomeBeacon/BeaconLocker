package com.example.becomebeacon.beaconlocker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
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
import java.util.HashMap;


public class BeaconBackHostActivity extends AppCompatActivity {

    static private BeaconBackHostActivity mContext;
    private Button sendMessage;
    private TextView viewRssi;
    private EditText writeMessage;
    public  FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private FirebaseUser mUser;
    public Bitmap bitmapImage;
    public FirebaseStorage storage = FirebaseStorage.getInstance();

    String phoneNum;
    String inputMessage;
    BleDeviceInfo info;

    String mac;
    FindMessage fm;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_back_host);
        mContext=this;

        Intent intent = getIntent();
        mac = intent.getStringExtra("MAC");
        if(mac==null)
        {
            Uri uriData = getIntent().getData();
            mac = uriData.getQueryParameter("beaconID");
        }
        info = BeaconList.lostMap.get(mac);


        initUI();
        initListeners();
        viewImage();
        fm=new FindMessage();
        fm.devAddress = info.devAddress;
        mHandler.sendEmptyMessage(0);




    }

    private void initUI() {
        sendMessage=(Button)findViewById(R.id.sendMessageButton);
        viewRssi = (TextView)findViewById(R.id.rssiFlow);
        writeMessage = (EditText) findViewById(R.id.message);
    }

    private void initListeners() {

        sendMessage.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v)
            {
                inputMessage = writeMessage.getText().toString();
                fm.message = inputMessage;

                mDatabase.getReference("users/"+ info.getUid()).child("messages")
                        .push().setValue(fm);

                BeaconList.lostMap.get(info.devAddress).othersSendMsg=true;

                Toast.makeText(getApplicationContext(),"메시지 발송 완료",Toast.LENGTH_SHORT).show();
                //Notifications.notifications.remove(item.devAddress);
                finish();

            }
        });

    }

    private void viewImage()
    {
        if(info.getPictureUri() != null)
        {
            try {
                if (info.getPictureUri() == "null") {

                } else {
                    StorageReference storageRef = storage.getReference().child(info.getPictureUri());
                    // Storage 에서 다운받아 저장시킬 임시파일
                    final File imageFile = File.createTempFile("images", "jpg");
                    storageRef.getFile(imageFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            // Success Case
                            bitmapImage = BitmapFactory.decodeFile(imageFile.getPath());
                            //mImage.setImageBitmap(bitmapImage);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Fail Case
                            e.printStackTrace();
                        }
                    });
                }
                } catch(Exception e){
                    e.printStackTrace();
                }
        }
        ImageView image = (ImageView) findViewById(R.id.lost_device_image);
        image.setImageBitmap(bitmapImage);
    }

    private void viewRssiInUser()
    {

        viewRssi.setText(info.getDistance2()+"");
    }
    @Override
    public void onDestroy()
    {
        Log.d("BDA","BDA destroyed");
        mContext=null;
        super.onDestroy();
    }

    private Handler mHandler= new Handler()
    {
        public void handleMessage(Message msg)
        {
            viewRssiInUser();
            mHandler.sendEmptyMessageDelayed(0, 500);
        }
    };
}
