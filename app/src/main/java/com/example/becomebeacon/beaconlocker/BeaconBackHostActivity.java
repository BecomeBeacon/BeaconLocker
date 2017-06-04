package com.example.becomebeacon.beaconlocker;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;


public class BeaconBackHostActivity extends AppCompatActivity {


    static private BeaconBackHostActivity mContext;
    private Button getPhoneNumb;
    private TextView viewRssi;
    public  FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private FirebaseUser mUser;
    String phoneNum;
    BleDeviceInfo info;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_back_host);
        mContext=this;

        initUI();
        initListeners();
        mHandler.sendEmptyMessage(0);

    }

    private void initUI() {
        getPhoneNumb=(Button)findViewById(R.id.phoneNumbButton);
        viewRssi = (TextView)findViewById(R.id.rssiFlow);
    }

    private void initListeners() {

        getPhoneNumb.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v)
            {
                TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(getApplicationContext().TELEPHONY_SERVICE);
                phoneNum = telephonyManager.getLine1Number();
                //이폰넘버 어떻게 하실겁니까 ????????
            }
        });

    }
    private void viewRssiInUser()
    {
        viewRssi.setText(info.getRssi());
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
