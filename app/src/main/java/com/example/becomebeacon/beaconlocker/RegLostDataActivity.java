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
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegLostDataActivity extends AppCompatActivity implements OnMapReadyCallback {
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseRef;
    private LostDevInfo devInfo;
    LatLng lostPos;
    CameraPosition cp;

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
        //devInfo.setDevAddr(bleDeviceInfo.getDevAddress());
        //devInfo.setLatitude(Double.valueOf(bleDeviceInfo.latitude));
        //devInfo.setLongetude(Double.valueOf(bleDeviceInfo.longitude));
        devInfo.setDevAddr("EE:EE:EE:EE:EE:EE");
        devInfo.setLatitude(35.885661);
        devInfo.setLongetude(128.609486);
        devInfo.setLostDate("20170520");

        Log.d("RLDA","devInfo : "+devInfo.getDevAddr()+" "+devInfo.getLatitude()+" "+devInfo.getLongitude());

        mDatabase
                .getReference("beacon/" + devInfo.getDevAddr() + "/")
                .child("isLost")
                .setValue(true); // isLost 속성값 변경

        mDatabase
                .getReference("beacon/" + devInfo.getDevAddr() + "/")
                .child("isFar")
                .setValue(true); // isFar 속성값 변경

        mDatabase
                .getReference("lost_items/" + devInfo.getDevAddr() + "/")
                .child("lastdate")
                .setValue(devInfo.getLostDate());
        mDatabase
                .getReference("lost_items/" + devInfo.getDevAddr() + "/")
                .child("latitude")
                .setValue(devInfo.getLatitude());
        mDatabase
                .getReference("lost_items/" + devInfo.getDevAddr() + "/")
                .child("longitude")
                .setValue(devInfo.getLongitude());

        lostPos = new LatLng(devInfo.getLatitude(),devInfo.getLongitude());
        cp = new CameraPosition.Builder().target((lostPos)).zoom(17).build();

        MapFragment mMapFragment = MapFragment.newInstance(new GoogleMapOptions().camera(cp));
        android.app.FragmentTransaction fragmentTransaction =
                getFragmentManager().beginTransaction();

        fragmentTransaction.replace(R.id.miniMap, mMapFragment);
        fragmentTransaction.commit();
        mMapFragment.getMapAsync(this);

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

    @Override
    public void onMapReady(GoogleMap map) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(lostPos);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
        map.addMarker(markerOptions);
        map.getUiSettings().setScrollGesturesEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(false);
        map.getUiSettings().setZoomGesturesEnabled(false);
    }
}