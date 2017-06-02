package com.example.becomebeacon.beaconlocker;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by 함상혁입니다 on 2017-04-27.
 */

public class MyBeaconsListAdapter extends BaseAdapter {
    private Context mContext;
    LayoutInflater mInflater;
    int mLayout;
    private boolean isScanning = false;
    private ArrayList<BleDeviceInfo> mBleDeviceInfoArrayList;
    private Bitmap mBitmap;
    public FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mUserAddressRef;
    private FirebaseUser mUser;
    private ImageView mImage;
    Bitmap bitmapImage;
    // 검색된 BLE 장치가 중복 추가되는 부분을 방지하기 위해 HashMap을 사용
    // String: Device Address(key값)
    private HashMap<String, BleDeviceInfo> mHashBleMap = new HashMap<String, BleDeviceInfo>();


    public MyBeaconsListAdapter(Context context, int layout, ArrayList<BleDeviceInfo> arBleList,
                                HashMap<String, BleDeviceInfo> hashBleMap)
    {
        mContext = context;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mBleDeviceInfoArrayList = arBleList;
        mLayout = layout;
        mHashBleMap = hashBleMap;
    }

    public synchronized void addOrUpdateItem(BleDeviceInfo info)
    {
        if(mHashBleMap.containsKey(info.getDevAddress()))
        {
            mHashBleMap.get(info.getDevAddress()).setRssi(info.getRssi());
        }
        else
        {
            mBleDeviceInfoArrayList.add(info);
            mHashBleMap.put(info.getDevAddress(), info);
        }

        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return mBleDeviceInfoArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return mBleDeviceInfoArrayList.get(position);
    }

    public void addBleDeviceItem(BleDeviceInfo item)
    {
        if(!mBleDeviceInfoArrayList.contains(item))
            mBleDeviceInfoArrayList.add(item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        FirebaseStorage storage = FirebaseStorage.getInstance();

        if(convertView == null)
        {
            convertView = mInflater.inflate(mLayout, parent, false);
        }



        //TextView txtUuid = (TextView)convertView.findViewById(R.id.text_uuid);
        //txtUuid.setText("UUID: " + mBleDeviceInfoArrayList.get(position).proximityUuid);
        if(PictureList.pictures.containsKey(mBleDeviceInfoArrayList.get(position).devAddress)) {
            ImageView image = (ImageView) convertView.findViewById(R.id.device_image);
            image.setImageBitmap(PictureList.pictures.get(mBleDeviceInfoArrayList.get(position).devAddress));
        }

        TextView txtBdName = (TextView)convertView.findViewById(R.id.text_bd_name);
        txtBdName.setText("Device Name: " + mBleDeviceInfoArrayList.get(position).nickname);

        TextView txtBdAddress = (TextView)convertView.findViewById(R.id.text_bd_address);
        txtBdAddress.setText("Dev Address: " + mBleDeviceInfoArrayList.get(position).devAddress);

        /*TextView txtMajor = (TextView)convertView.findViewById(R.id.text_major);
        txtMajor.setText("Major: " + String.valueOf(mBleDeviceInfoArrayList.get(position).major));

        TextView txtMinor = (TextView)convertView.findViewById(R.id.text_minor);
        txtMinor.setText("Minor: " + String.valueOf(mBleDeviceInfoArrayList.get(position).minor));

        TextView txtRssi = (TextView)convertView.findViewById(R.id.text_rssi);
        txtRssi.setText("RSSI: " + String.valueOf(mBleDeviceInfoArrayList.get(position).rssi) + " dbm");

        TextView txtTxPower = (TextView)convertView.findViewById(R.id.text_txpower);
        //txtTxPower.setText("Tx Power: " + String.valueOf(mBleDeviceInfoArrayList.get(position).measuredPower) + " dbm");
        txtTxPower.setText("Tx Power: " + String.valueOf(mBleDeviceInfoArrayList.get(position).txPower) + " dbm");      // changsu
        */
        TextView txtDistance = (TextView)convertView.findViewById(R.id.text_distance);
        txtDistance.setText("Distance: "// + String.valueOf(mBleDeviceInfoArrayList.get(position).distance) + " m ("
                + String.format("%.2f", mBleDeviceInfoArrayList.get(position).distance2) + "m");

        //TextView txtTimeout = (TextView)convertView.findViewById(R.id.text_timeout);
        //txtTimeout.setText("Timeout: " + String.valueOf(mBleDeviceInfoArrayList.get(position).timeout));

        Button btnConnect = (Button)convertView.findViewById(R.id.button_connect);
        //btnConnect.setVisibility(View.GONE);
        btnConnect.setText("Detail");
        btnConnect.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v)
            {


                Activity mActi=GetMainActivity.getMainActity();
                Intent intent = new Intent(mActi, BeaconDetailsActivity.class);
                intent.putExtra("MAC",mBleDeviceInfoArrayList.get(pos).devAddress);
                mActi.startActivity(intent);



            }
        });
//
//
        return convertView;
    }



}