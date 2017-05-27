package com.example.becomebeacon.beaconlocker;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by 함상혁입니다 on 2017-05-27.
 */

public class NoActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent=getIntent();
        int noti=intent.getIntExtra("NOTI",-1);
        Log.d("NO","noti get "+noti);
        NotificationManager notificationManager = (NotificationManager)BleService.mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(noti);
        finish();
    }
}
