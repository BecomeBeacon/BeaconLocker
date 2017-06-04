package com.example.becomebeacon.beaconlocker;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


public class BeaconBackHostActivity extends AppCompatActivity {

    static private BeaconBackHostActivity mContext;
    private Button sendMessage;
    private TextView viewRssi;
    public  FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private FirebaseUser mUser;

    String phoneNum;
    BleDeviceInfo info;

    String mac;
    FindMessage FM;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_back_host);
        mContext=this;
        Intent intent = getIntent();
        mac = intent.getStringExtra("MAC");
        info = BeaconList.lostMap.get(mac);

        initUI();
        initListeners();

        mHandler.sendEmptyMessage(0);




    }

    private void initUI() {
        sendMessage=(Button)findViewById(R.id.sendMessageButton);
        viewRssi = (TextView)findViewById(R.id.rssiFlow);
    }

    private void initListeners() {

        sendMessage.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v)
            {
                TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(getApplicationContext().TELEPHONY_SERVICE);
                phoneNum = telephonyManager.getLine1Number();

                FM.setMessage(phoneNum);
                FM.setMessage("로 전화주세요");

                mDatabase.getReference("users/"+ info.getUid() + "/message/")
                        .push().setValue(FM);

                Toast.makeText(getApplicationContext(),"메시지 발송 완료",Toast.LENGTH_SHORT).show();
                //Notifications.notifications.remove(item.devAddress);

            }
        });

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
