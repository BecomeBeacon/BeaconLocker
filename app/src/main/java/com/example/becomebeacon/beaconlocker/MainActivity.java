package com.example.becomebeacon.beaconlocker;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.renderscript.RenderScript;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.becomebeacon.beaconlocker.database.DataStore;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private MainActivity mActivity;

    private LayoutInflater layoutInflater;





    private ListView myBeacons;
    private ListView scannedBeacons;
    private final int REQUEST_ENABLE_BT=9999;
    private BluetoothAdapter mBluetoothAdapter;
    private FirebaseUser mUser;
    private FirebaseAuth mAuth;
    private TextView mEmail;
    private TextView mName;
    private GoogleApiClient mGoogleApiClient;
    private BleDeviceListAdapter mBleDeviceListAdapter;
    private MyBeaconsListAdapter mBeaconsListAdapter;
    private HashMap<String, BleDeviceInfo> mItemMap;
    private BluetoothService mBleService;
    private boolean mScanning=false;
    private boolean isScannig=false;

    private static final long TIMEOUT_LIMIT = 20;
    private static final long TIMEOUT_PERIOD = 1000;
    private static final long SCAN_PERIOD = 1000;
    public ArrayList<BleDeviceInfo> mArrayListBleDevice;
    public ArrayList<BleDeviceInfo> mAssignedItem;


    private Handler mHandler= new Handler()
    {
        public void handleMessage(Message msg)
        {
            if(mScanning)
            {
                mScanning = false;
                mBleService.getBtAdapter().stopLeScan(mBleService.mLeScanCallback);
            }

            mScanning = true;
            mBleService.getBtAdapter().startLeScan(mBleService.mLeScanCallback);
            mHandler.sendEmptyMessageDelayed(0, SCAN_PERIOD);
        }
    };

    private Handler mTimeOut = new Handler(){
        public void handleMessage(Message msg){
            Log.i("TAG","TIMEOUT UPDATE");

            int maxRssi = 0;
            int maxIndex = -1;

            //timeout counter update
            for (int i= 0 ; i < mArrayListBleDevice.size() ; i++){
                mArrayListBleDevice.get(i).timeout--;
                if(mArrayListBleDevice.get(i).timeout == 0){
                    mItemMap.remove(mArrayListBleDevice.get(i).devAddress);
                    mArrayListBleDevice.remove(i);
                }
                else{
                    if(mArrayListBleDevice.get(i).rssi > maxRssi || maxRssi == 0)
                    {
                        maxRssi = mArrayListBleDevice.get(i).rssi;
                        maxIndex = i;
                    }
                }
            }
            //TextView text_max_dev = (TextView)findViewById(R.id.text_max_dev);

            if(maxIndex == -1) {
                //text_max_dev.setText("No Dev");
            }
            else{
                //text_max_dev.setText(maxIndex+1 +"th    "
                //        + "major: " + mArrayListBleDevice.get(maxIndex).major + "  "
                //        + "minor: " + mArrayListBleDevice.get(maxIndex).minor + "  "
                //        + mArrayListBleDevice.get(maxIndex).getRssi() +"dbm");
            }
            mTimeOut.sendEmptyMessageDelayed(0,TIMEOUT_PERIOD);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myBeacons=(ListView)findViewById(R.id.ble_list);
        scannedBeacons=(ListView)findViewById(R.id.scan_list);
        if(myBeacons==null||scannedBeacons==null)
        {
            Log.d("sss","cannot find listview");
        }
        mActivity=this;
        mArrayListBleDevice = new ArrayList<BleDeviceInfo>();
        mAssignedItem=new ArrayList<BleDeviceInfo>();
        mItemMap = new HashMap<String, BleDeviceInfo>();
        mBleDeviceListAdapter = new BleDeviceListAdapter(this, R.layout.activity_main_content,
                mArrayListBleDevice, mItemMap);
        mBeaconsListAdapter = new MyBeaconsListAdapter(this, R.layout.activity_main_content,
                mArrayListBleDevice, mItemMap);


        mBleService=new BluetoothService(this,mBleDeviceListAdapter,mBeaconsListAdapter);
        scannedBeacons = (ListView)findViewById(R.id.scan_list);
        scannedBeacons.setAdapter(mBleDeviceListAdapter);

        myBeacons=(ListView)findViewById(R.id.ble_list);
        myBeacons.setAdapter(mBeaconsListAdapter);

        mAuth=LoginActivity.getAuth();
        mUser=LoginActivity.getUser();
        mBleDeviceListAdapter=new BleDeviceListAdapter(this, R.layout.activity_main_content,
                mArrayListBleDevice, mItemMap);


        //Slide
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ble 검색 및 추가
                myBeacons.setVisibility(View.GONE);
                scannedBeacons.setVisibility(View.VISIBLE);
                mBleService.changeMod(Use.USE_SCAN);
                mHandler.sendEmptyMessageDelayed(0, SCAN_PERIOD);
                mTimeOut.sendEmptyMessageDelayed(0, TIMEOUT_PERIOD);


           }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
               this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);




        //bluetoothAdapter 얻기
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);


        //bluetooth 체크 후 비활성화시 팝업
//        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
//
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//        }
        mBleService.checkBluetooth();

        View headerLayout = navigationView.getHeaderView(0);
        mEmail=(TextView)headerLayout.findViewById(R.id.slide_user_email);
        mName=(TextView)headerLayout.findViewById(R.id.slide_user_name);



        if (mEmail != null) {

            mEmail.setText(mUser.getEmail());
        }

        if (mName != null) {

            mName.setText(mUser.getDisplayName());
        }










    }



    @Override
    protected void onStart() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onResume() {
        super.onResume();


        //BEACON_UUID = getBeaconUuid(setting);

        //saveRSSI = setting.getBoolean("saveRSSI", true);

        mBleService.checkBluetooth();

        if(isScannig) {
            //scanBleDevice(true);            // BLE 장치 검색\
            mHandler.sendEmptyMessageDelayed(0, SCAN_PERIOD);
            mTimeOut.sendEmptyMessageDelayed(0, TIMEOUT_PERIOD);
        }
        else
        {

        }


    }

    //Slide Back
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        int id = item.getItemId();

        if (id == R.id.nav_machine) {

        } else if (id == R.id.nav_laf) {

        } else if (id == R.id.nav_map) {
            Intent intent = new Intent(getApplicationContext(), MapActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_setting) {

        } else if (id == R.id.nav_logout) {
            signOut();
            finish();

        }
        //Slide Close
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void signOut()
    {
        mAuth.signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // ...
                        Toast.makeText(getApplicationContext(),"Logged Out",Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void updateUI(FirebaseUser user) {

        if (user != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);

        } else {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);

        }
    }

    public String getBeaconUuid(SharedPreferences pref)
    {
        String uuid = "";

        uuid = pref.getString("keyUUID", BluetoothUuid.WINI_UUID.toString());

        /*
        if(USING_WINI) {
            uuid = pref.getString("keyUUID", BluetoothUuid.WINI_UUID.toString());
            //uuid = BluetoothUuid.WINI_UUID.toString();
        }
        else {

            uuid = pref.getString("keyUUID", BluetoothUuid.WIZTURN_PROXIMITY_UUID.toString());
            //uuid = BluetoothUuid.WIZTURN_PROXIMITY_UUID.toString();
        }
        */

        return uuid;
    }

}
