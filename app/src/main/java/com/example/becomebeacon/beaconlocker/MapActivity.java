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

public class MapActivity extends FragmentActivity
        implements OnMapReadyCallback {

    GoogleMap googleMap;
    private GpsInfo gps;
    double lat;
    double lon;
    double inlat;
    double inlon;

    //맵 개체 생성
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        inlat = Double.valueOf(intent.getExtras().getString("LAT"));
        inlon = Double.valueOf(intent.getExtras().getString("LON"));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Log.d("MAP","lat : "+inlat+" long : "+inlon);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }
    public void getCurrentLocation()
    {
        gps = new GpsInfo(MapActivity.this,MapActivity.this);
        if(gps.isGetLocation())
        {
            lat = gps.lat;
            lon = gps.lon;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        this.googleMap = googleMap;
        LatLng LOST;
        if(inlat == 0 && inlon == 0) {
            getCurrentLocation();
            LOST = new LatLng(lat, lon);
        }
        else
        {
            LOST = new LatLng(inlat, inlon);
        }
        //좌표값 세팅

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(LOST)); // 지정 좌표로 카메라 무브
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(18)); // 0~20(1:세계,5:대륙,10:도시,15:거리)
        onAddMarker();
        addCircle(10);
    }
    public void onAddMarker()
    {
        LatLng LOST;
        if(inlat == 0 && inlon == 0) {
            LOST = new LatLng(lat, lon);
        }
        else
        {
            LOST = new LatLng(inlat, inlon);
        }
        //마커 옵션(분실물 정보, 분실 시각) 왜안되냐 도대체가
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(LOST);
        markerOptions.title("분실물");
        markerOptions.snippet("2017/4/22");

        //마커추가
        this.googleMap.addMarker(markerOptions);

        //정보창 클릭 리스너
        googleMap.setOnInfoWindowClickListener(infoWindowClickListener);
        /*
        //마커 클릭 리스너
        this.googleMap.setOnMarkerClickListener(markerClickListener);
        */

    }


    public void addCircle(int distance)
    {
        LatLng LOST;
        if(inlat == 0 && inlon == 0) {
            LOST = new LatLng(lat, lon);
        }
        else
        {
            LOST = new LatLng(inlat, inlon);
        }
        CircleOptions circle1KM = new CircleOptions().center(LOST) //원점
                .radius(distance)      //반지름 단위 : m 추후 디스턴스 따라 결정?
                .strokeWidth(0f)  //선너비 0f : 선없음
                .fillColor(Color.parseColor("#880000ff")); //배경색

        this.googleMap.addCircle(circle1KM);

    }
    GoogleMap.OnInfoWindowClickListener infoWindowClickListener = new GoogleMap.OnInfoWindowClickListener() {
        @Override
        public void onInfoWindowClick(Marker marker) {
            String markerId = marker.getId();
            String distan = "10m";
            Toast.makeText(MapActivity.this, "최초 디바이스와의 거리 : "+distan, Toast.LENGTH_SHORT).show();
        }
    };

    /*
    //마커 클릭 리스너
    GoogleMap.OnMarkerClickListener markerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            String markerId = marker.getId();
            //선택한 타겟위치
            LatLng location = marker.getPosition();
            Toast.makeText(MapActivity.this, "마커 클릭 Marker ID : "+markerId+"("+location.latitude+" "+location.longitude+")", Toast.LENGTH_SHORT).show();

            return false;
        }
    };
    */


}


