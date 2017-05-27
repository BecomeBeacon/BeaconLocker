package com.example.becomebeacon.beaconlocker;

/**
 * Created by heeseung on 2017-05-23.
 */

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegLostDataActivity extends AppCompatActivity {
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseRef;
    private LostDevInfo devInfo;
    String tempDevAddr;
    private GoogleMap googleMap;
    private MapView mapView;
    @Override
    protected void onCreate(Bundle savedInstanceState)  {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_lost_data);

        Intent notiIntent=getIntent();
        int noti=notiIntent.getIntExtra("NOTI",-1);

        if(noti!=-1) {
            NotificationManager notificationManager = (NotificationManager) BleService.mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(noti);
        }

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseRef = mDatabase.getReference();



        Intent intent=getIntent();
        String mac=intent.getStringExtra("MAC");
        BleDeviceInfo bleDeviceInfo = BeaconList.mItemMap.get(mac);

        devInfo = new LostDevInfo();
        //테스트 tempDevAddr = "D5:0A:B9:FA:D0:E9";
        tempDevAddr = bleDeviceInfo.getDevAddress();


        devInfo.setDevAddr(tempDevAddr);
        devInfo.setLatitude(Double.valueOf(bleDeviceInfo.latitude));
        devInfo.setLongetude(Double.valueOf(bleDeviceInfo.longitude));
        devInfo.setLostDate("20170520");

        Log.d("RLDA","devInfo : "+devInfo.getDevAddr()+" "+devInfo.getLatitude()+" "+devInfo.getLongitude());

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

        // map fragment 추가

        android.app.FragmentTransaction fragmentTransaction =
                getFragmentManager().beginTransaction();


        fragmentTransaction.commit();



        LatLng lostPos = new LatLng(devInfo.getLatitude(),devInfo.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(lostPos);
        markerOptions.title("분실 발생 위치");
        markerOptions.snippet("등록 완료");


        exit_button_init();
    }
    //!!
    private void exit_button_init() {
        findViewById(R.id.rld_ExitBtn).setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                finish();
            }                                                          }
        );
    };
}