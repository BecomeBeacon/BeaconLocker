package com.example.becomebeacon.beaconlocker;

import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapActivity extends FragmentActivity
        implements OnMapReadyCallback {

    //맵 개체 생성
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        //좌표값 세팅
        LatLng LOST = new LatLng(37.56, 126.97);
        //마커 옵션
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(LOST);
        markerOptions.title("분실물");
        markerOptions.snippet("2017/4/22");
        map.addMarker(markerOptions);

        map.moveCamera(CameraUpdateFactory.newLatLng(LOST)); // 지정 좌표로 카메라 무브
        map.animateCamera(CameraUpdateFactory.zoomTo(15)); // 0~20(1:세계,5:대륙,10:도시,15:거리)
    }

}