package com.example.becomebeacon.beaconlocker;

/**
 * Created by 함상혁입니다 on 2017-04-27.
 */

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by changsu on 2015-03-23.
 */

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class BleDeviceListAdapter extends BaseAdapter {
    private Context mContext;
    LayoutInflater mInflater;
    int mLayout;
    private boolean isScanning = false;
    //private ArrayList<BluetoothDevice> mBleDeviceArrayList;
    private ArrayList<BleDeviceInfo> mBleDeviceInfoArrayList;
    private ArrayList<BleDeviceInfo> mAssignedArrayList;

    // 검색된 BLE 장치가 중복 추가되는 부분을 방지하기 위해 HashMap을 사용
    // String: Device Address(key값)
    private HashMap<String, BleDeviceInfo> mHashBleMap = new HashMap<String, BleDeviceInfo>();
    private HashMap<String, BleDeviceInfo> mAssignedBleMap = new HashMap<String, BleDeviceInfo>();

    public BleDeviceListAdapter(Context context, int layout, ArrayList<BleDeviceInfo> arBleList,
                                HashMap<String, BleDeviceInfo> hashBleMap,ArrayList<BleDeviceInfo> mBleList,HashMap<String, BleDeviceInfo> mBleMap)
    {
        mContext = context;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mBleDeviceInfoArrayList = arBleList;
        mAssignedArrayList=mBleList;

        mLayout = layout;
        mHashBleMap = hashBleMap;
        mAssignedBleMap=mBleMap;
    }

    public synchronized void addOrUpdateItem(BleDeviceInfo info) throws Exception
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


        if(convertView == null)
        {
            convertView = mInflater.inflate(mLayout, parent, false);
        }


        //TextView txtUuid = (TextView)convertView.findViewById(R.id.text_uuid);
        //txtUuid.setText("UUID: " + mBleDeviceInfoArrayList.get(position).proximityUuid);

        TextView txtBdName = (TextView)convertView.findViewById(R.id.text_bd_name);
        txtBdName.setText("Device Name: " + mBleDeviceInfoArrayList.get(position).devName);

        TextView txtBdAddress = (TextView)convertView.findViewById(R.id.text_bd_address);
        txtBdAddress.setText("Dev Address: " + mBleDeviceInfoArrayList.get(position).devAddress);

        //TextView txtMajor = (TextView)convertView.findViewById(R.id.text_major);
        //txtMajor.setText("Major: " + String.valueOf(mBleDeviceInfoArrayList.get(position).major));

        //TextView txtMinor = (TextView)convertView.findViewById(R.id.text_minor);
        //txtMinor.setText("Minor: " + String.valueOf(mBleDeviceInfoArrayList.get(position).minor));

        //TextView txtRssi = (TextView)convertView.findViewById(R.id.text_rssi);
        //txtRssi.setText("RSSI: " + String.valueOf(mBleDeviceInfoArrayList.get(position).rssi) + " dbm");

        //TextView txtTxPower = (TextView)convertView.findViewById(R.id.text_txpower);
        //txtTxPower.setText("Tx Power: " + String.valueOf(mBleDeviceInfoArrayList.get(position).measuredPower) + " dbm");
        //txtTxPower.setText("Tx Power: " + String.valueOf(mBleDeviceInfoArrayList.get(position).txPower) + " dbm");      // changsu

        TextView txtDistance = (TextView)convertView.findViewById(R.id.text_distance);
        txtDistance.setText("Distance: " //+ String.format("%.2f",String.valueOf(mBleDeviceInfoArrayList.get(position).distance))+ " m ("
                + String.format("%.2f", mBleDeviceInfoArrayList.get(position).distance2) +"m");

        //TextView txtTimeout = (TextView)convertView.findViewById(R.id.text_timeout);
        //txtTimeout.setText("Timeout: " + String.valueOf(mBleDeviceInfoArrayList.get(position).timeout));

        Button btnConnect = (Button)convertView.findViewById(R.id.button_connect);
        btnConnect.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v)
            {

                if(BeaconList.mItemMap.containsKey(mBleDeviceInfoArrayList.get(pos).devAddress))
                {
                    Toast.makeText(GetMainActivity.getMainActity(), "이미 등록된 비컨", Toast.LENGTH_LONG).show();

                }
                else {
                    if (!mAssignedBleMap.containsKey(mBleDeviceInfoArrayList.get(pos).devAddress)) {
//                    mAssignedArrayList.add(mBleDeviceInfoArrayList.get(pos));
//                    mAssignedBleMap.put(mBleDeviceInfoArrayList.get(pos).getDevAddress(), mBleDeviceInfoArrayList.get(pos));
//                    //mHashBleMap.get(mBleDeviceInfoArrayList.get(pos).devAddress).setTimeout(1);
//                    mHashBleMap.remove(mBleDeviceInfoArrayList.get(pos).devAddress);
//                    mBleDeviceInfoArrayList.remove(pos);
//


                        DeviceInfoStore.setBleInfo(mBleDeviceInfoArrayList.get(pos));

                        Activity mActi = GetMainActivity.getMainActity();
                        Intent intent = new Intent(mActi, DataStoreActivity.class);
                        mActi.startActivity(intent);

                        notifyDataSetChanged();
                    }
                }

            }
        });
//
//
        return convertView;
    }


//    public void addDevice(BluetoothDevice device)
//    {
//        if(!mBleDeviceArrayList.contains(device))
//        {
//            mBleDeviceArrayList.add(device);
//        }
//    }

//    public BluetoothDevice getDevice(int position)
//    {
//        return mBleDeviceArrayList.get(position);
//    }
//
//    public int getBleDeviceCount()
//    {
//        return mBleDeviceArrayList.size();
//    }
//
//    public Object getBleDeviceItem(int i)
//    {
//        return mBleDeviceArrayList.get(i);
//    }

    /*  BleDeviceScanActivity에서 최대 RSSI Beacon을 계산함
    public BleDeviceInfo getMaxRssiBeacon()
    {
        int pos = 0;
        int maxRssi = mBleDeviceInfoArrayList.get(0).rssi;
        for(int i = 1; i  < mBleDeviceInfoArrayList.size() ; i++)
        {
            if(maxRssi < mBleDeviceInfoArrayList.get(pos).rssi)
            {
                maxRssi = mBleDeviceInfoArrayList.get(pos).rssi;
                pos = i;
            }
        }
        return mBleDeviceInfoArrayList.get(pos);
    }
    */

}