package com.example.becomebeacon.beaconlocker;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.becomebeacon.beaconlocker.database.DbOpenHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import static android.content.ContentValues.TAG;

/**
 * Created by 함상혁입니다 on 2017-05-09.
 */

public class BleService extends Service {


    public static BleService mContext;
    private String TAG="BLESERVICE";
    BluetoothScan mBleScan;
    Location loc;

    private ArrayList<BleDeviceInfo> mAssignedItem;
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    boolean mScan;
    private GpsInfo gps;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseRef;
    private DbOpenHelper dbOpenHelper;

//    NotificationManager Notifi_M;
//    Notification Notifi ;





    @Override
    public void onCreate()
    {
        super.onCreate();

        mContext=this;
        Notifications.notifications=new HashMap<String,Integer>();
        if(isServiceRunningCheck()) {
            Log.d("BLESERVICE","already exist");
            stopSelf();
        }
        Log.d("BLESERVICE","service start");

        //Notifi_M = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        mBleScan =new BluetoothScan(this);
        Notifications.cntNoti=0;

        mAssignedItem = BeaconList.mAssignedItem;
        mScan=false;
        mHandler.sendEmptyMessage(0);
        mTimeOut.sendEmptyMessage(0);

        mDatabase = FirebaseDatabase.getInstance();

        dbOpenHelper = new DbOpenHelper(getApplicationContext());
        dbOpenHelper.open();

        dbOpenHelper.execSQL("CREATE TABLE IF NOT EXISTS lost_devices ( " +
                "devaddr VARCHAR(32) NOT NULL, " +
                "latitude DOUBLE NOT NULL, " +
                "longitude DOUBLE NOT NULL, " +
                "lastdate VARCHAR(32) NOT NULL, " +
                "PRIMARY KEY (devaddr));"
        );

        pullLostDevices();
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
        Log.d("Service","service destory");
        mBleScan.end();
        mHandler.removeMessages(0);
        mTimeOut.removeMessages(0);
        super.onDestroy();




    }


    private Handler mHandler= new Handler()
    {
        public void handleMessage(Message msg)
        {
            Log.d("SERVICE"," in handler");
            Log.d("PICTURES", "Picture = " + PictureList.pictures.toString());
            if(mBleScan.getMod()== Values.USE_TRACK) {
                Log.d("SERVICE"," in track");

                if(mScan) {

                    Log.d("SERVICE","useBLE is "+Values.useBLE);

                    Log.d("SERVICE", "scan stop "+Values.scanBreakTime);
                    mBleScan.getBtAdapter().stopLeScan(mBleScan.mLeScanCallback);

                    mScan = false;

                    mHandler.sendEmptyMessageDelayed(0, Values.scanBreakTime);


                }
                else
                {
                    Log.d(TAG,"gps : "+Values.useGPS);
                    if(Values.useGPS)
                    {
                        Log.d("SERVICE"," in gps");

                        //여기서 Values.latitude, Values.longitude에 현재 좌표 저장
                        gps = new GpsInfo(GetMainActivity.getMainActity(),GetMainActivity.getMainActity());
                        gps.getLocation();

                        Values.latitude = Double.toString(gps.lat);
                        Values.longitude = Double.toString(gps.lon);

                        Log.d(TAG,"lat : "+gps.lat+ " long : "+gps.lon);

                    }
                    if(Values.useBLE) {
                        Log.d("SERVICE", "scan start "+Values.scanTime );
                        mBleScan.getBtAdapter().startLeScan(mBleScan.mLeScanCallback);
                    }
                    mScan = true;

                    mHandler.sendEmptyMessageDelayed(0, Values.scanTime);


                }
            }

        }
    };

    private Handler mTimeOut= new Handler()
    {
        public void handleMessage(Message msg)
        {
            Log.d("SERVICE"," in Timeout");
            if(Values.useBLE) {

                for(int i=0;i<BeaconList.mAssignedItem.size();i++)
                {
                    BleDeviceInfo dbi=BeaconList.mAssignedItem.get(i);
                    dbi.timeout--;
                    if(dbi.timeout==0)
                    {
                        dbi.isFar=true;
                        pushNotification(dbi.nickname,dbi.devAddress);
                    }
                }
            }
            mTimeOut.sendEmptyMessageDelayed(0, 1000);



        }
    };



    public void pushNotification(String name,String devAddress)
    {

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, RegLostDataActivity.class);
        Intent intent2 = new Intent();

        intent.putExtra("NOTI",Notifications.cntNoti);
        intent2.putExtra("NOTI",Notifications.cntNoti);

        intent.putExtra("MAC",devAddress);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent nothingIntent = PendingIntent.getActivity(this, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);



        Notification.Builder builder = new Notification.Builder(this);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.small_main_logo));
        builder.setSmallIcon(R.drawable.main_logo);
        builder.setTicker("멀어짐");
        builder.setContentTitle(name + "이 멀어졌습니다");
        builder.setContentText("분실물로 등록할까요?");
        builder.setWhen(System.currentTimeMillis());
        //builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        builder.setVibrate(null);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);


        builder.addAction(R.drawable.yes, "네", pendingIntent);
        builder.addAction(R.drawable.no, "아니오",nothingIntent);
        Notification noti = builder.build();


        Notifications.notifications.put(devAddress,Notifications.cntNoti);
        Log.d("service","NotiNum is "+Notifications.cntNoti+" there is key "+Notifications.notifications.toString());

        notificationManager.notify(Notifications.cntNoti++, noti);


//        ////        //소리추가
//        noti.defaults = Notification.DEFAULT_SOUND;
//
//        //알림 소리를 한번만 내도록
//        noti.flags = Notification.FLAG_ONLY_ALERT_ONCE;
//
//        //확인하면 자동으로 알림이 제거 되도록
//        noti.flags = Notification.FLAG_AUTO_CANCEL;
//
//        //토스트 띄우기
//       Toast.makeText(BleService.this, "비컨 멀어짐", Toast.LENGTH_LONG).show();


    }

    public void pushFindNotification(String name,String devAddress)
    {

        Log.d("SERVICE","LostItem name : "+name+" ADRRESS : "+devAddress);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, RegLostDataActivity.class);
        Intent intent2 = new Intent();

        intent.putExtra("NOTI",Notifications.cntNoti);
        intent2.putExtra("NOTI",Notifications.cntNoti);

        intent.putExtra("MAC",devAddress);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent nothingIntent = PendingIntent.getActivity(this, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);



        Notification.Builder builder = new Notification.Builder(this);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.small_main_logo));
        builder.setSmallIcon(R.drawable.main_logo);
        builder.setTicker("감지됨");
        builder.setContentTitle(name + "이 감지되었습니다");
        builder.setContentText("분실물을 습득하셨나요?");
        builder.setWhen(System.currentTimeMillis());
        //builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        builder.setVibrate(null);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);


        builder.addAction(R.drawable.yes, "네", pendingIntent);
        builder.addAction(R.drawable.no, "아니오",nothingIntent);
        Notification noti = builder.build();


        Notifications.notifications.put(devAddress,Notifications.cntNoti);
        Log.d("service","NotiNum is "+Notifications.cntNoti+" there is key "+Notifications.notifications.toString());

        notificationManager.notify(Notifications.cntNoti++, noti);


//        ////        //소리추가
//        noti.defaults = Notification.DEFAULT_SOUND;
//
//        //알림 소리를 한번만 내도록
//        noti.flags = Notification.FLAG_ONLY_ALERT_ONCE;
//
//        //확인하면 자동으로 알림이 제거 되도록
//        noti.flags = Notification.FLAG_AUTO_CANCEL;
//
//        //토스트 띄우기
//       Toast.makeText(BleService.this, "비컨 멀어짐", Toast.LENGTH_LONG).show();


    }

//    public void pushNotification()
//    {
////        Intent intent = new Intent(BleService.this, MainActivity.class);
////        PendingIntent pendingIntent = PendingIntent.getActivity(BleService.this, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);
////
////        Notifi = new Notification.Builder(BleService.this.getApplicationContext())
////                .setContentTitle("Content Title")
////                .setContentText("Content Text")
////                .setSmallIcon(R.drawable.main_logo)
////                .setTicker("알림!!!")
////                .setContentIntent(pendingIntent)
////                .build();
////
////        //소리추가
////        Notifi.defaults = Notification.DEFAULT_SOUND;
////
////        //알림 소리를 한번만 내도록
////        Notifi.flags = Notification.FLAG_ONLY_ALERT_ONCE;
////
////        //확인하면 자동으로 알림이 제거 되도록
////        Notifi.flags = Notification.FLAG_AUTO_CANCEL;
////
////
////        Notifi_M.notify( 777 , Notifi);
//
//        NotificationCompat.Builder mBuilder = createNotification();
//
//        //커스텀 화면 만들기
//        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.push_alarm2);
//        //remoteViews.setImageViewResource(R.id.img, R.drawable.main_logo);
//        //remoteViews.setTextViewText(R.id.title, "Title");
//        remoteViews.setImageViewResource(R.id.noti_image, R.mipmap.ic_launcher);
//        remoteViews.setTextViewText(R.id.noti_text, "비컨이 멀어졌습니다.\n알고계신가요?");
//
//
//
//
//
//
//
//        //노티피케이션에 커스텀 뷰 장착
//        mBuilder.setContent(remoteViews);
//        mBuilder.setContentIntent(createPendingIntent());
//
//        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        mNotificationManager.notify(1, mBuilder.build());
//
//
//
//
//        //토스트 띄우기
//        Toast.makeText(BleService.this, "비컨 멀어짐", Toast.LENGTH_LONG).show();
//    }
//
//    private PendingIntent createPendingIntent(){
//        Intent resultIntent = new Intent(this, MainActivity.class);
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//        stackBuilder.addParentStack(MainActivity.class);
//        stackBuilder.addNextIntent(resultIntent);
//
//        return stackBuilder.getPendingIntent(
//                0,
//                PendingIntent.FLAG_UPDATE_CURRENT
//        );
//    }
//
//
//
//
//    private NotificationCompat.Builder createNotification(){
//        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
//        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setLargeIcon(icon)
//                .setContentTitle("StatusBar Title")
//                .setContentText("StatusBar subTitle")
//                .setSmallIcon(R.mipmap.ic_launcher/*스와이프 전 아이콘*/)
//                .setAutoCancel(true)
//                .setWhen(System.currentTimeMillis())
//                .setDefaults(Notification.DEFAULT_ALL);
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
//            builder.setCategory(Notification.CATEGORY_MESSAGE)
//                    .setPriority(Notification.PRIORITY_HIGH)
//                    .setVisibility(Notification.VISIBILITY_PUBLIC);
//        }
//        return builder;
//    }


    public boolean isServiceRunningCheck() {
        ActivityManager manager = (ActivityManager) this.getSystemService(Activity.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("BleService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void pullLostDevices() {

        mDatabaseRef = mDatabase.getReference("lost_items/");
        mDatabaseRef
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot tempSnapshot : dataSnapshot.getChildren()) {
                            LostDevInfo temp = new LostDevInfo();

                            temp.setDevAddr(tempSnapshot.getKey());
                            Log.d("SNAP","snapshot : "+tempSnapshot.toString());

                            temp.setLostDate(tempSnapshot.child("lastdate").getValue().toString());
                            temp.setLatitude(Double.valueOf(tempSnapshot.child("latitude").getValue().toString()));
                            temp.setLongetude(Double.valueOf(tempSnapshot.child("longitude").getValue().toString()));



                            Log.d("SNAP", temp.getDevAddr());
                            Log.d("SNAP", ""+temp.getLatitude());
                            Log.d("SNAP", ""+temp.getLongitude());



                            if(dbOpenHelper.uniqueTest(temp.getDevAddr())) {
                                dbOpenHelper.execSQL("INSERT INTO lost_devices VALUES('" + temp.getDevAddr() + "'," +
                                        temp.getLatitude() + "," +
                                        temp.getLongitude() + ", '" +
                                        temp.getLostDate() + "')"
                                );
                                Log.d("bleService", temp.getDevAddr());
                                Log.d("bleService", ""+temp.getLatitude());
                                Log.d("bleService", ""+temp.getLongitude());
                            }

                            Cursor cursor = dbOpenHelper.selectQuery("SELECT devaddr FROM lost_devices");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
//        mDatabaseRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for(DataSnapshot temp:dataSnapshot.getChildren()) {
//                    dbOpenHelper.insert(temp.getKey(), Double.valueOf(temp.child("longitude").getKey()),
//                            Double.valueOf(temp.child("latitude").getKey()), temp.child("lastdate").getKey());
//
//                    Log.d("LDPS", "temp.getKey = " + temp.getKey());
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                // Failed to read value
//                Log.w(TAG, "Failed to read value.", error.toException());
//
//            }
//        });
    }



}
