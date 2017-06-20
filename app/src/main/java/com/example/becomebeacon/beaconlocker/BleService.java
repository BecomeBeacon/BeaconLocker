package com.example.becomebeacon.beaconlocker;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by 함상혁입니다 on 2017-05-09.
 */

public class BleService extends Service {

    public boolean checkZeroPoint;
    public static BleService mContext;
    private String TAG="BLESERVICE";
    private BluetoothScan mBleScan;
    private Location loc;
    public static int myPoint ;
    private String myPointKey=null;

    private String FIND_OTHERS_NOTI;
    private final String FIND_NOTI_SUB="분실물을 습득하셨나요?";

    private ArrayList<BleDeviceInfo> mAssignedItem;
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    boolean mScan;
    private GpsInfo gps;
    private DatabaseReference lostBeaconInfoRef;
    private DatabaseReference messageInfoRef;

    private FirebaseDatabase mDatabase;

    @Override
    public void onCreate()
    {
        super.onCreate();
        mDatabase = FirebaseDatabase.getInstance();

        checkZeroPoint=true;

        lostBeaconInfoRef = mDatabase.getReference("lost_items/");
        messageInfoRef = mDatabase.getReference("users/"+LoginActivity.getUser().getUid()+"/messages");

        mContext=this;
        Notifications.clear();

        try {
            if(isServiceRunningCheck()) {

                stopSelf();
            }
            mBleScan =new BluetoothScan(this);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 20104", Toast.LENGTH_LONG).show();
            stopSelf();
        }


        //Notifi_M = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        Notifications.cntNoti=0;

        mAssignedItem = BeaconList.mAssignedItem;
        mScan=false;
        mHandler.sendEmptyMessage(0);
        mTimeOut.sendEmptyMessage(0);
        myPoint=0;

        mDatabase = FirebaseDatabase.getInstance();

        /* DB 비활성화
        dbOpenHelper = new DbOpenHelper(getApplicationContext());
        dbOpenHelper.open();


        pullLostDevices();
        */
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public void addDBListener() throws Exception
    {
        lostBeaconInfoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot addressSnapshot : dataSnapshot.getChildren()) {
                    LostDevInfo lostItem = addressSnapshot.getValue(LostDevInfo.class);
                    if(BeaconList.lostMap.containsKey(lostItem.getDevAddr())) //갱신됨
                    {
                        BeaconList.lostMap.remove(lostItem.getDevAddr());
                        BeaconList.lostMap.put(lostItem.getDevAddr(),new BleDeviceInfo(lostItem));
                    }
                    else //신규 추가
                    {
                        BeaconList.lostMap.put(lostItem.getDevAddr(),new BleDeviceInfo(lostItem));
                    }


                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        messageInfoRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {
                    int addTotal=0;

                    for(DataSnapshot addressSnapshot : dataSnapshot.getChildren()) {
                        FindMessage msg=addressSnapshot.getValue(FindMessage.class);
                        msg.keyValue=addressSnapshot.getKey();


                        if(msg.isPoint)
                        {
                            if(msg.point<0) {//point 주는 msg는 음수로 온다
                                addTotal -= msg.point;

                                messageInfoRef.child(msg.keyValue).removeValue();


                            }
                            else {
                                checkZeroPoint = false;
                                if(myPoint<msg.point)
                                    myPoint = msg.point;
                                myPointKey=addressSnapshot.getKey();
                            }
                            msg.isChecked=true;



                            //Point msg는 받고 삭제해야한다




                        }
                        else
                        {
                            if(msg.isChecked==false) {
                                if (BeaconList.mItemMap.containsKey(msg.devAddress)) {
                                    if (!BeaconList.msgMap.containsKey(addressSnapshot.getKey())) {
                                        BeaconList.msgMap.put(addressSnapshot.getKey(), msg);
                                        pushMsgNotification(BeaconList.mItemMap.get(msg.devAddress), msg);
                                    }
                                }

                                msg.isChecked = true;

                                messageInfoRef.child(addressSnapshot.getKey()).setValue(msg);

                                if(BeaconList.rewardMap.containsKey(msg.devAddress))
                                {
                                    BeaconList.rewardMap.remove(msg.devAddress);
                                }
                                BeaconList.rewardMap.put(msg.devAddress,msg.sendUid);
                            }
                            else
                            {
                                if (!BeaconList.msgMap.containsKey(addressSnapshot.getKey())) {
                                    BeaconList.msgMap.put(addressSnapshot.getKey(), msg);
                                }
                            }





                        }

                        //DB에 ischeck를 체크해줘야함
                    }

                    if(checkZeroPoint)
                    {
                        FindMessage fm=new FindMessage();
                        fm.isPoint=true;
                        fm.point=addTotal;

                        messageInfoRef.push().setValue(fm);
                        checkZeroPoint=false;
                        myPoint=addTotal;

                    }
                    else
                    {

                        myPoint+=addTotal;
                        GetMainActivity.getMainActity().setPoint(myPoint);

                        //내 점수 setText 해줘야함 ㄹㅇ루
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 20103", Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {

        mHandler.removeMessages(0);
        mTimeOut.removeMessages(0);

        try {
            mBleScan.end();
        }catch(Exception e)
        {

        }

        super.onDestroy();

        if(myPointKey!=null)
            messageInfoRef.child(myPointKey).child("point").setValue(myPoint);

    }


    private Handler mHandler= new Handler()
    {
        public void handleMessage(Message msg)
        {

            try {
                if(mBleScan.getMod()== Values.USE_TRACK) {


                    if(mScan) {

                        mBleScan.getBtAdapter().stopLeScan(mBleScan.mLeScanCallback);

                        mScan = false;

                        mHandler.sendEmptyMessageDelayed(0, Values.scanBreakTime);


                    }
                    else
                    {
                        if(Values.useGPS)
                        {


                            //여기서 Values.latitude, Values.longitude에 현재 좌표 저장
                            gps = new GpsInfo(GetMainActivity.getMainActity(),GetMainActivity.getMainActity());
                            gps.getLocation();

                            Values.latitude = gps.lat;
                            Values.longitude = gps.lon;


                        }
                        if(Values.useBLE) {

                            mBleScan.getBtAdapter().startLeScan(mBleScan.mLeScanCallback);
                        }
                        mScan = true;

                        mHandler.sendEmptyMessageDelayed(0, Values.scanTime);


                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 20101", Toast.LENGTH_LONG).show();
                mHandler.removeMessages(0);
            }

        }
    };

    private Handler mTimeOut= new Handler()
    {
        public void handleMessage(Message msg)
        {

            try {
                if(Values.useBLE) {

                    for(int i=0;i<BeaconList.mAssignedItem.size();i++)
                    {
                        BleDeviceInfo dbi=BeaconList.mAssignedItem.get(i);
                        dbi.timeout--;
                        if(dbi.isLost!=true&&dbi.timeout==0)
                        {
                            dbi.isFar=true;
                            pushNotification(dbi);
                        }
                    }
                }
                mTimeOut.sendEmptyMessageDelayed(0, 1000);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 20102", Toast.LENGTH_LONG).show();
                mHandler.removeMessages(0);
            }


        }
    };



    public void pushNotification(BleDeviceInfo bdi) throws Exception
    {

        if(Notifications.notifications.containsKey(bdi.devAddress+Values.NOTI_FAR))
        {
            return;
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, RegLostDataActivity.class);
        Intent intent2 = new Intent();

        intent.putExtra("NOTI",Notifications.cntNoti);
        intent2.putExtra("NOTI",Notifications.cntNoti);

        intent.putExtra("MAC",bdi.devAddress);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent nothingIntent = PendingIntent.getActivity(this, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);



        Notification.Builder builder = new Notification.Builder(this);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.small_main_logo));
        builder.setSmallIcon(R.drawable.small_main_logo);
        builder.setTicker("멀어짐");
        builder.setContentTitle(bdi.nickname + "이 멀어졌습니다");
        builder.setContentText("분실물로 등록할까요?");
        builder.setWhen(System.currentTimeMillis());
        //builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        builder.setVibrate(null);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);


        builder.addAction(R.drawable.yes, "네", pendingIntent);
        builder.addAction(R.drawable.no, "아니오",nothingIntent);
        Notification noti = builder.build();



        Notifications.notifications.put(bdi.devAddress+Values.NOTI_FAR,Notifications.cntNoti);

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

    public void pushMsgNotification(BleDeviceInfo bdi, FindMessage msg) throws  Exception
    {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, ReadMessageActivity.class);

        BeaconList.msgMap.put(msg.keyValue,msg);
        String messageKey = msg.keyValue;

        intent.putExtra("NOTI",Notifications.cntNoti);
        intent.putExtra("MyMessageKey",messageKey);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.small_main_logo));
        builder.setSmallIcon(R.drawable.small_main_logo);
        builder.setTicker("누군가가" + bdi.getNickname() + "를 찾았습니다.");
        builder.setContentTitle("누군가가" + bdi.getNickname() + "를 찾았습니다.");
        builder.setContentText("메세지 : " + msg.message);
        builder.setWhen(System.currentTimeMillis());
        //builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        builder.setVibrate(null);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);


        Notification noti = builder.build();

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

    public void pushFindNotification(LostDevInfo ldi,int op) throws Exception
    {
        if(Notifications.notifications.containsKey(ldi.getDevAddr()+Values.NOTI_I_FIND))
        {
            return;
        }else
        {

        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent ,intent2;

        if(op == 1) {
            FIND_OTHERS_NOTI="당신의 "+ldi.getNickNameOfThing() + "이 감지되었습니다";
            intent = new Intent(this, BeaconDetailsActivity.class);
        }
        else if(op==0)
        {
            FIND_OTHERS_NOTI=ldi.getUserName()+"님의 "+ldi.getNickNameOfThing()+"이 감지되었습니다";
            intent = new Intent(this, BeaconBackHostActivity.class);
        }
        else
        {
            FIND_OTHERS_NOTI="ERROR";
            intent = new Intent();
        }
        intent2 = new Intent();

        intent.putExtra("NOTI", Notifications.cntNoti);
        intent2.putExtra("NOTI", Notifications.cntNoti);

        intent.putExtra("MAC", ldi.getDevAddr());

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent nothingIntent = PendingIntent.getActivity(this, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);


        Notification.Builder builder = new Notification.Builder(this);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.small_main_logo));
        builder.setSmallIcon(R.drawable.small_main_logo);
        builder.setTicker("감지됨");
        builder.setContentTitle(FIND_OTHERS_NOTI);
        builder.setContentText(FIND_NOTI_SUB);
        builder.setWhen(System.currentTimeMillis());
        //builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        builder.setVibrate(null);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);


        builder.addAction(R.drawable.yes, "네", pendingIntent);
        builder.addAction(R.drawable.no, "아니오", nothingIntent);
        Notification noti = builder.build();


        Notifications.notifications.put(ldi.getDevAddr()+Values.NOTI_I_FIND, Notifications.cntNoti);

        notificationManager.notify(Notifications.cntNoti++, noti);




    }


    public boolean isServiceRunningCheck() throws Exception{
        ActivityManager manager = (ActivityManager) this.getSystemService(Activity.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("BleService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void findStuff(BleDeviceInfo item)
    {

        mDatabase.getReference("lost_items/").child(item.devAddress).removeValue();

        mDatabase.getReference("beacon/"+ item.devAddress + "/")
                .child("isLost")
                .setValue(false);
        if(BeaconList.lostMap.containsKey(item.devAddress))
        {
            BeaconList.lostMap.remove(item.devAddress);
        }


        item.isLost = false;
    }


}
