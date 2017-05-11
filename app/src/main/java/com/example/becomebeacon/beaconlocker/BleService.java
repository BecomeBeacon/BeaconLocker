package com.example.becomebeacon.beaconlocker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by 함상혁입니다 on 2017-05-09.
 */

public class BleService extends Service {


    BluetoothScan mBleScan;
    private ArrayList<BleDeviceInfo> mAssignedItem;
    boolean mScan;
    NotificationManager Notifi_M;
    Notification Notifi ;

    MainActivity mActivity;


    @Override
    public void onCreate()
    {
        super.onCreate();
        mActivity=GetMainActivity.getMainActity();
        Notifi_M = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        mBleScan =new BluetoothScan(this);

        mAssignedItem = mActivity.mAssignedItem;
        mScan=false;
        mHandler.sendEmptyMessage(0);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        mBleScan.end();
        mHandler.removeMessages(0);
        super.onDestroy();

    }


    private Handler mHandler= new Handler()
    {
        public void handleMessage(Message msg)
        {
            Log.d("SERVICE","in handler");
            if(mBleScan.getMod()== Values.USE_TRACK) {

                if(mScan) {
                    mBleScan.getBtAdapter().stopLeScan(mBleScan.mLeScanCallback);
                    mScan=false;
                    Log.d("SERVICE","scan stop");
                    mHandler.sendEmptyMessageDelayed(0, Values.scanBreakTime);

                }
                else
                {
                    mBleScan.getBtAdapter().startLeScan(mBleScan.mLeScanCallback);
                    mScan=true;
                    Log.d("SERVICE","scan start");
                    mHandler.sendEmptyMessageDelayed(0, Values.scanTime);

                }
            }

        }
    };

    public void pushNotification()
    {
//        Intent intent = new Intent(BleService.this, MainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(BleService.this, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);
//
//        Notifi = new Notification.Builder(BleService.this.getApplicationContext())
//                .setContentTitle("Content Title")
//                .setContentText("Content Text")
//                .setSmallIcon(R.drawable.main_logo)
//                .setTicker("알림!!!")
//                .setContentIntent(pendingIntent)
//                .build();
//
//        //소리추가
//        Notifi.defaults = Notification.DEFAULT_SOUND;
//
//        //알림 소리를 한번만 내도록
//        Notifi.flags = Notification.FLAG_ONLY_ALERT_ONCE;
//
//        //확인하면 자동으로 알림이 제거 되도록
//        Notifi.flags = Notification.FLAG_AUTO_CANCEL;
//
//
//        Notifi_M.notify( 777 , Notifi);

        NotificationCompat.Builder mBuilder = createNotification();

        //커스텀 화면 만들기
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.push_alarm);
        remoteViews.setImageViewResource(R.id.img, R.mipmap.ic_launcher);
        remoteViews.setTextViewText(R.id.title, "Title");
        remoteViews.setTextViewText(R.id.message, "message");

        //노티피케이션에 커스텀 뷰 장착
        mBuilder.setContent(remoteViews);
        mBuilder.setContentIntent(createPendingIntent());

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());




        //토스트 띄우기
        Toast.makeText(BleService.this, "뜸?", Toast.LENGTH_LONG).show();
    }

    private PendingIntent createPendingIntent(){
        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        return stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
    }




    private NotificationCompat.Builder createNotification(){
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(icon)
                .setContentTitle("StatusBar Title")
                .setContentText("StatusBar subTitle")
                .setSmallIcon(R.mipmap.ic_launcher/*스와이프 전 아이콘*/)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_ALL);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            builder.setCategory(Notification.CATEGORY_MESSAGE)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        return builder;
    }


}
