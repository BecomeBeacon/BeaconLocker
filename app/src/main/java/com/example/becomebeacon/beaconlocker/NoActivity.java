package com.example.becomebeacon.beaconlocker;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

/**
 * Created by 함상혁입니다 on 2017-05-27.
 */

public class NoActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            Intent intent=getIntent();
            int noti=intent.getIntExtra("NOTI",-1);

            NotificationManager notificationManager = (NotificationManager)BleService.mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(noti);
            finish();
        } catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 11000", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}


//에러코드 공식
//첫 1자리
//        - 1 : Activity
//        - 2 : Service
//
//        둘째~셋째자리
//        - Activity, Service 순서
//
//        넷째~다섯째자리
//        - 예외처리 Method 순서 ( 0 = 전체 클래스 )
//
//        =================================================둘째~셋째자리 순서
//
//        Activity
//        1 : BeaconBackHostActivity
//        2 : BeaconDetailsActivity
//        3 : DataStoreActivity
//        4 : LoginActivity
//        5 : MainActivity
//        6 : MultiMapActivity
//        7 : ReadMessageActivity
//        8 : RegLostDataActivity
//        9 : SettingActivity
//        10 : NoActivity
//        Service
//        1 : BleService