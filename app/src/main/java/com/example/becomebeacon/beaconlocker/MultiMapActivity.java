package com.example.becomebeacon.beaconlocker;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import com.google.android.gms.maps.model.CircleOptions;
import android.graphics.Color;
import android.util.Log;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;

public class MultiMapActivity extends FragmentActivity
        implements OnMapReadyCallback {

    GoogleMap googleMap;
    private GpsInfo gps;
    double lat;
    double lon;
    public  FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mUserAddressRef = mDatabase.getReference("/lost_items/");
    private FirebaseUser mUser;

    //맵 개체 생성
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mUserAddressRef = mDatabase.getReference("/lost_items/");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void getCurrentLocation() {
        gps = new GpsInfo(MultiMapActivity.this, MultiMapActivity.this);
        gps.getLocation();
        if (gps.isGetLocation()) {
            lat = gps.lat;
            lon = gps.lon;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        LatLng LOST;
        getCurrentLocation();
        Log.d("TAAG","lat : "+lat);
        Log.d("TAAG","lon : "+lon);
        LOST = new LatLng(lat, lon);

        //좌표값 세팅

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(LOST)); // 지정 좌표로 카메라 무브
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(16)); // 0~20(1:세계,5:대륙,10:도시,15:거리)
        FindLostItem();
        onAddMarker(lat,lon,"on");
    }

    public void onAddMarker(double latt, double lont,String date) {
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

    }


    public void addCircle(int distance,double latt,double lont) {
        LatLng LOST;
        LOST = new LatLng(latt, lont);

        CircleOptions circle1KM = new CircleOptions().center(LOST) //원점
                .radius(distance)      //반지름 단위 : m 추후 디스턴스 따라 결정?
                .strokeWidth(0f)  //선너비 0f : 선없음
                .fillColor(Color.parseColor("#880000ff")); //배경색

        this.googleMap.addCircle(circle1KM);

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
        if(rslt < 1000)
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
            int cnt1=0,cnt2=0;
            // users/$Uid/beacons/"Address"
            mUser= LoginActivity.getUser();


            Log.v("Test_Print_Uid", mUser.getUid());

            mUserAddressRef
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for(DataSnapshot addressSnapshot : dataSnapshot.getChildren()) {
                                GetLatLong getLatLong = addressSnapshot.getValue(GetLatLong.class);
                                Log.d("FUCK","lat : "+getLatLong.latitude);
                                Log.d("FUCK","lon : "+getLatLong.longitude);
                                if(calcDistance(getLatLong.latitude,getLatLong.longitude,lat,lon))
                                {
                                    Log.d("TTT","is in?");
                                    onAddMarker(getLatLong.latitude,getLatLong.longitude,getLatLong.lastdate);
                                    addCircle(10,getLatLong.latitude,getLatLong.longitude);
                                }
                                //Log.v("Test_Print_ADDR", myBeaconOnUser.address);

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });



    }

}




