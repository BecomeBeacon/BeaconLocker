package com.example.becomebeacon.beaconlocker;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

public class GpsInfo extends Service implements LocationListener {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    public Context mContext;
    public Activity mActivity;

    // 현재 GPS 사용유무
    static boolean isGPSEnabled = false;

    // 네트워크 사용유무
    static boolean isNetworkEnabled = false;

    // GPS 상태값
    boolean isGetLocation = false;

    Location location;

    double lat; // 위도
    double lon; // 경도

    // 최소 GPS 정보 업데이트 거리 10미터
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;

    // 최소 GPS 정보 업데이트 시간 밀리세컨이므로 1분
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;

    protected LocationManager locationManager;

    public GpsInfo(Context context, Activity activity) {
        this.mContext = context;
        this.mActivity = activity;
        //getLocation();
        locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
    }

    public GpsInfo() {

    }



    public Location getLocation()throws Exception {

        try {
            //locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            GpsEnabled();
            NetworkEnabled();

            if (Build.VERSION.SDK_INT >= 23) {
                if (ContextCompat.checkSelfPermission(mContext,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // 이 권한을 필요한 이유를 설명해야하는가?

                    if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity,Manifest.permission.ACCESS_FINE_LOCATION))
                    {
                        // 다이어로그같은것을 띄워서 사용자에게 해당 권한이 필요한 이유에 대해 설명합니다
                        // 해당 설명이 끝난뒤 requestPermissions()함수를 호출하여 권한허가를 요청해야 합니다
                    }
                    else
                    {
                        ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                        // 필요한 권한과 요청 코드를 넣어서 권한허가요청에 대한 결과를 받아야 합니다
                    }

                }
            }
//            if(!isGPSEnabled)
//            {
//                showSettingsAlert();
//            }
            isGetLocation = true;

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES,this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES,this);

            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            lat = getLatitude();
            lon = getLongitude();

            if(lat==0 || lon ==0)
            {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                lat = getLatitude();
                lon = getLongitude();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    /**
     * GPS 종료
     * */
    public void stopUsingGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(GpsInfo.this);
        }
    }

    public boolean GpsEnabled()throws Exception
    {
        // GPS 정보 가져오기
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 현재 네트워크 상태 값 알아오기
        return isGPSEnabled;

    }
    public boolean NetworkEnabled()throws Exception
    {
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return isNetworkEnabled;
    }

    /**
     * 위도값을 가져옵니다.
     * */
    public double getLatitude()throws Exception{
        if(location != null) {
            lat = location.getLatitude();
        }
        return lat;
    }

    /**
     * 경도값을 가져옵니다.
     * */
    public double getLongitude()throws Exception{
        if(location != null) {
            lon = location.getLongitude();
        }
        return lon;
    }

    /**
     * GPS 나 wife 정보가 켜져있는지 확인합니다.
     * */
    public boolean isGetLocation()throws Exception {
        return this.isGetLocation;
    }

    /**
     * GPS 정보를 가져오지 못했을때
     * 설정값으로 갈지 물어보는 alert 창
     * */
    public void showSettingsAlert()throws Exception{
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        alertDialog.setTitle("GPS 사용유무셋팅");
        alertDialog.setMessage("GPS 셋팅이 되지 않았을수도 있습니다.\n 설정창으로 가시겠습니까?");

        // OK 를 누르게 되면 설정창으로 이동합니다.
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        mActivity.startActivityForResult(intent,Values.CHECK_GPS);
                    }
                });
        // Cancle 하면 종료 합니다.
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(mActivity==SettingActivity.mContext)
                        {
                            SettingActivity.mContext.changeGPS(false);
                        }
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }
/*
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                //권한 획득이 거부되면 결과 배열은 비어있게 됨
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //권한 획득이 허용되면 수행해야 할 작업이 표시됨
                    //일반적으로 작업을 처리할 메서드를 호출

                } else {

                    //권한 획득이 거부되면 수행해야 할 적업이 표시됨
                    //일반적으로 작업을 처리할 메서드를 호출
                }
                return;
            }
        }
    }
   */

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub

    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }
}
