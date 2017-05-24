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

import com.example.becomebeacon.beaconlocker.R;
import com.google.android.gms.maps.MapFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegLostDataActivity extends AppCompatActivity {
    private FirebaseDatabase dbSession;
    private LostDevInfo devInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState)      {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_lost_data);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        dbSession
                .getReference("beacon/")
                .child("tempDevAddr")
                .child("islost")
                .setValue("true"); // isLost 속성값 변경

        dbSession
                .getReference("beacon/")
                .child("tempDevAddr")
                .child("ifFar")
                .setValue("true"); // isFar 속성값 변경

        dbSession.getReference("lost_items/")
                .child("tempDevAddr")
                .setValue(devInfo); // lost_items에 분실물 라벨 추가

        // 분실물 등록이 완료되었습니다 메세지
        // map fragment 추가
        MapFragment mMapFragment = MapFragment.newInstance();
        android.app.FragmentTransaction fragmentTransaction =
                getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.miniMap, mMapFragment);
        fragmentTransaction.commit();
    }
}
