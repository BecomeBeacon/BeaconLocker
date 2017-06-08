package com.example.becomebeacon.beaconlocker;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MultiMapActivity extends FragmentActivity
        implements OnMapReadyCallback {

    public GoogleMap googleMap;
    private GpsInfo gps;
    double lat;
    double lon;
    double inlat=0;
    double inlon=0;
    String lData;
    public  FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mUserAddressRef = mDatabase.getReference("/lost_items/");
    private FirebaseUser mUser;
    Marker m;

    //MarkerOptions myPlace = new MarkerOptions();
    MarkerOptions markerOptions = new MarkerOptions();


    //맵 개체 생성
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            Intent intent = getIntent();
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_map);
            mUserAddressRef = mDatabase.getReference("/lost_items/");

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            inlat = intent.getDoubleExtra("LAT",0);
            inlon = intent.getDoubleExtra("LON",0);
            lData = intent.getStringExtra("DATE");
        } catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10600", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    public void getCurrentLocation() {
        try {
            gps = new GpsInfo(MultiMapActivity.this, MultiMapActivity.this);
            gps.getLocation();
            if (gps.isGetLocation()) {
                lat = gps.lat;
                lon = gps.lon;
            }
        } catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10601", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        try {
            this.googleMap = googleMap;
            LatLng LOST;
            getCurrentLocation();
            LOST = new LatLng(lat, lon);
            onAddMyMarker(lat,lon);
            mHandler.sendEmptyMessage(0);
            //좌표값 세팅
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(LOST)); // 지정 좌표로 카메라 무브
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(16)); // 0~20(1:세계,5:대륙,10:도시,15:거리)
            if(inlat==0) {
                FindLostItem();
            }
            else
            {
                onAddMarker(inlat,inlon,lData);
            }
        } catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10602", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onDestroy() {
        try {
            Log.d("Service","service destory");
            mHandler.removeMessages(0);
            super.onDestroy();
        } catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10603", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private Handler mHandler= new Handler()
    {
        public void handleMessage(Message msg)
        {
            try {
                m.remove();
                Making();
                mHandler.sendEmptyMessageDelayed(0, 2000);
            } catch (Exception e) {
                e.printStackTrace();

                Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10604", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    };
    public void Making()
    {
        try {
            LatLng myLocation;
            getCurrentLocation();
            myLocation = new LatLng(lat,lon);
            //  m = this.googleMap.addMarker(markerOptions.position(myLocation).
            //          icon(BitmapDescriptorFactory.defaultMarker(200f)).title("현재 위치"));
            m = this.googleMap.addMarker(markerOptions.position(myLocation).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker15)).title("현재 위치"));
        } catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10605", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    public void onAddMyMarker(double latt,double lont)
    {
        try {
            LatLng LOST;

            LOST = new LatLng(latt, lont);

            //마커 옵션(분실물 정보, 분실 시각) 왜안되냐 도대체가
        /*
        myPlace.position(LOST);
        myPlace.icon(BitmapDescriptorFactory.defaultMarker(200f));
        myPlace.title("현재 위치");
        */
            //마커추가
            //m = this.googleMap.addMarker(markerOptions.position(LOST).
            //      icon(BitmapDescriptorFactory.defaultMarker(200f)).title("현재 위치"));
            m = this.googleMap.addMarker(markerOptions.position(LOST).icon(BitmapDescriptorFactory.fromResource(R.drawable.hos)).title("현재 위치"));
            //정보창 클릭 리스너
            googleMap.setOnInfoWindowClickListener(infoWindowClickListener);
        /*
        //마커 클릭 리스너
        this.googleMap.setOnMarkerClickListener(markerClickListener);
        */
        } catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10606", Toast.LENGTH_LONG).show();
            finish();
        }
    }
    public void onAddMarker(double latt, double lont,String date) {
        try {
            LatLng LOST;

            LOST = new LatLng(latt, lont);

            //마커 옵션(분실물 정보, 분실 시각) 왜안되냐 도대체가
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(LOST);
            markerOptions.title("분실물");
            markerOptions.snippet(date);


            //마커추가
            this.googleMap.addMarker(markerOptions);
            //정보창 클릭 리스너
            googleMap.setOnInfoWindowClickListener(infoWindowClickListener);
        /*
        //마커 클릭 리스너
        this.googleMap.setOnMarkerClickListener(markerClickListener);
        */
        } catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10607", Toast.LENGTH_LONG).show();
            finish();
        }
    }


    public void addCircle(int distance,double latt,double lont) {
        try {
            LatLng LOST;
            LOST = new LatLng(latt, lont);

            CircleOptions circle1KM = new CircleOptions().center(LOST) //원점
                    .radius(distance)      //반지름 단위 : m 추후 디스턴스 따라 결정?
                    .strokeWidth(0f)  //선너비 0f : 선없음
                    .fillColor(Color.parseColor("#880000ff")); //배경색

            this.googleMap.addCircle(circle1KM);
        } catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10608", Toast.LENGTH_LONG).show();
            finish();
        }

    }
    //두 좌표간의 거리 계산
    public static boolean calcDistance(double lat1, double lon1, double lat2, double lon2){
        double EARTH_R, Rad, radLat1, radLat2, radDist;
        double distance, ret;
        EARTH_R = 6371000.0;
        Rad = Math.PI/180;
        radLat1 = Rad * lat1;
        radLat2 = Rad * lat2;
        radDist = Rad * (lon1 - lon2);

        distance = Math.sin(radLat1) * Math.sin(radLat2);
        distance = distance + Math.cos(radLat1) * Math.cos(radLat2) * Math.cos(radDist);
        ret = EARTH_R * Math.acos(distance);
        double rslt = Math.round(Math.round(ret) / 1000);
        Log.d("Calcdis","Result ? : "+rslt);
        if(rslt < 1)
        {
            return true;
        }
        else
        {
            return false;
        }


    }

    GoogleMap.OnInfoWindowClickListener infoWindowClickListener = new GoogleMap.OnInfoWindowClickListener() {
        @Override
        public void onInfoWindowClick(Marker marker) {
            String markerId = marker.getId();
            String distan = "10m";
            Toast.makeText(MultiMapActivity.this, "최초 디바이스와의 거리 : "+distan, Toast.LENGTH_SHORT).show();
        }
    };


    public void FindLostItem() {
        try {
            // users/$Uid/beacons/"Address"
            mUser= LoginActivity.getUser();


            Log.v("Test_Print_Uid", mUser.getUid());

            mUserAddressRef
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for(DataSnapshot addressSnapshot : dataSnapshot.getChildren()) {
                                LostDevInfo lostDevInfo = addressSnapshot.getValue(LostDevInfo.class);
                                if(calcDistance(lostDevInfo.getLatitude(),lostDevInfo.getLongitude(),lat,lon))
                                {
                                    onAddMarker(lostDevInfo.getLatitude(),lostDevInfo.getLongitude(),lostDevInfo.getLostDate());
                                    addCircle(10,lostDevInfo.getLatitude(),lostDevInfo.getLongitude());
                                }
                                //Log.v("Test_Print_ADDR", myBeaconOnUser.address);

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10609", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}




