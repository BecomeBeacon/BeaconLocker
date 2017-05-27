package com.example.becomebeacon.beaconlocker;

/**
 * Created by heeseung on 2017-05-23.
 */

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.example.becomebeacon.beaconlocker.R;
import com.google.android.gms.maps.MapFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegLostDataActivity extends AppCompatActivity {
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseRef;
    private LostDevInfo devInfo;
    String tempDevAddr;
    @Override
    protected void onCreate(Bundle savedInstanceState)      {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_lost_data);

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseRef = mDatabase.getReference();

        //BleDeviceInfo bleDeviceInfo = new BleDeviceInfo();

        devInfo = new LostDevInfo();
        tempDevAddr = "D5:0A:B9:FA:D0:E9";
        //tempDevAddr = bleDeviceInfo.getDevAddress();


        devInfo.setDevAddr(tempDevAddr);
        devInfo.setLatitude(35.886270);
        devInfo.setLongetude(128.610052);
        devInfo.setLostDate("20170520");

        mDatabase
                .getReference("beacon/" + tempDevAddr + "/")
                .child("isLost")
                .setValue(true); // isLost 속성값 변경

        mDatabase
                .getReference("beacon/" + tempDevAddr + "/")
                .child("isFar")
                .setValue(true); // isFar 속성값 변경

        mDatabase
                .getReference("lost_items/" + tempDevAddr + "/")
                .child("lastdate")
                .setValue(devInfo.getlostDate());
        mDatabase
                .getReference("lost_items/" + tempDevAddr + "/")
                .child("latitude")
                .setValue(devInfo.getLatitude());
        mDatabase
                .getReference("lost_items/" + tempDevAddr + "/")
                .child("longitude")
                .setValue(devInfo.getLongitude());

        // 분실물 등록이 완료되었습니다 메세지
        // map fragment 추가
        MapFragment mMapFragment = MapFragment.newInstance();
        android.app.FragmentTransaction fragmentTransaction =
                getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.miniMap, mMapFragment);
        fragmentTransaction.commit();

        exit_button_init();
    }
    //!!
    private void exit_button_init() {
        findViewById(R.id.rld_ExitBtn).setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                System.exit(0);
            }                                                          }
        );
    };
}