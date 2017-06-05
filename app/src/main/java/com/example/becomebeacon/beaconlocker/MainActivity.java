package com.example.becomebeacon.beaconlocker;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private MainActivity mActivity;

    private LayoutInflater layoutInflater;





    private ListView myBeacons;
    private ListView scannedBeacons;
    private TextView emptyListText;
    private final int REQUEST_ENABLE_BT=9999;
    private BluetoothAdapter mBluetoothAdapter;
    private FirebaseUser mUser;
    private FirebaseAuth mAuth;
    private TextView mEmail;
    private TextView mName;
    private GoogleApiClient mGoogleApiClient;
    private Intent bleService;

    private boolean usingTracking;

    private BluetoothScan mBleScan;
    //private boolean mScanning=false;
    private boolean isScannig=false;


    private HashMap<String, BleDeviceInfo> scannedMap;
    //myItem
    private HashMap<String, BleDeviceInfo> mItemMap;

    private ArrayList<BleDeviceInfo> mArrayListBleDevice;    ;
    private ArrayList<BleDeviceInfo> mAssignedItem;

    //myItem


    public ArrayList<BleDeviceInfo> mMyBleDeviceList;

    private BleUtils mBleUtils;

    public static String BEACON_UUID;       // changsu
    public static  Boolean saveRSSI;
    private static final long CEHCK_PERIOD = 1000;       // 10초동안 SCAN 과정을 수행함

    private static final long TIMEOUT_LIMIT = 20;
    private static final long TIMEOUT_PERIOD = 1000;
    //private static final boolean USING_WINI = true; // TI CC2541 사용: true

    private BleDeviceListAdapter mBleDeviceListAdapter;
    private MyBeaconsListAdapter mBeaconsListAdapter;

    public ProgressDialog mainProgressDialog = null;



    private boolean mScan;

    private Handler mHandler= new Handler()
    {
        public void handleMessage(Message msg)
        {
            if(mBleScan.getMod()== Values.USE_SCAN) {

                if(mScan) {
                    mBleScan.getBtAdapter().stopLeScan(mBleScan.mLeScanCallback);
                    mScan=false;
                    Log.d("main","scan stop");
                    Log.d("main","scan break time ; "+Values.scanBreakTime);
                    mHandler.sendEmptyMessageDelayed(0, Values.scanBreakTime);


                }
                else
                {
                    mBleScan.getBtAdapter().startLeScan(mBleScan.mLeScanCallback);
                    mScan=true;
                    Log.d("main","scan start");
                    Log.d("main","scan time ; "+Values.scanTime);
                    mHandler.sendEmptyMessageDelayed(0, Values.scanTime);


                }
            }
            else if(mBleScan.getMod()== Values.USE_NOTHING)
            {
                if(mScan)
                {
                    mScan=false;
                    mBleScan.getBtAdapter().stopLeScan(mBleScan.mLeScanCallback);
                }
                mBeaconsListAdapter.notifyDataSetChanged();
                if(mItemMap.isEmpty()) {
                    emptyListText.setVisibility(View.VISIBLE);
                    myBeacons.setVisibility(View.GONE);
                }
                else {
                    emptyListText.setVisibility(View.GONE);
                    myBeacons.setVisibility(View.VISIBLE);
                }
                if(BeaconDetailsActivity.getBDA()!=null)
                {
                    BeaconDetailsActivity.getBDA().refreshDistance();
                }


                mHandler.sendEmptyMessageDelayed(0, CEHCK_PERIOD);
            }

        }
    };

    private Handler mTimeOut = new Handler(){
        public void handleMessage(Message msg){
            //Log.i("TAG","TIMEOUT UPDATE");

            HashMap<String, BleDeviceInfo> tMap;
            ArrayList<BleDeviceInfo> tArray;
            int mod= mBleScan.getMod();



            int maxRssi = 0;
            int maxIndex = -1;


            if(mod== Values.USE_SCAN) {
                tMap = scannedMap;
                tArray = mArrayListBleDevice;


                //timeout counter update
                for (int i = 0; i < tArray.size(); i++) {
                    tArray.get(i).timeout--;
                    if (tArray.get(i).timeout == 0) {
                        tMap.remove(tArray.get(i).devAddress);
                        tArray.remove(i);
                    } else {
                        if (tArray.get(i).rssi > maxRssi || maxRssi == 0) {
                            maxRssi = tArray.get(i).rssi;
                            maxIndex = i;
                        }
                    }
                }
                //TextView text_max_dev = (TextView)findViewById(R.id.text_max_dev);

                if (maxIndex == -1) {
                    //text_max_dev.setText("No Dev");
                } else {
                    //text_max_dev.setText(maxIndex+1 +"th    "
                    //        + "major: " + mArrayListBleDevice.get(maxIndex).major + "  "
                    //        + "minor: " + mArrayListBleDevice.get(maxIndex).minor + "  "
                    //        + mArrayListBleDevice.get(maxIndex).getRssi() +"dbm");
                }
            }

            mTimeOut.sendEmptyMessageDelayed(0, TIMEOUT_PERIOD);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainProgressDialog = new ProgressDialog(this);
        mainProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mainProgressDialog.setMessage("목록을 불러오는 중...");
        mainProgressDialog.show();

        BeaconList.refresh();

        mBleUtils=new BleUtils();
        mBluetoothAdapter= BluetoothAdapter.getDefaultAdapter();



        GetMainActivity.setMA(this);

        bleService= new Intent(this,BleService.class);
        startService(bleService);

        myBeacons=(ListView)findViewById(R.id.ble_list);
        scannedBeacons=(ListView)findViewById(R.id.scan_list);
        emptyListText=(TextView)findViewById(R.id.text_have_no_ble);
        if(myBeacons==null||scannedBeacons==null)
        {
            Log.d("sss","cannot find listview");
        }
        mActivity=this;
        mArrayListBleDevice = BeaconList.mArrayListBleDevice;
        mAssignedItem=BeaconList.mAssignedItem;
        scannedMap = BeaconList.scannedMap;
        mItemMap = BeaconList.mItemMap;
        mBleDeviceListAdapter = new BleDeviceListAdapter(this, R.layout.ble_device_row,
                mArrayListBleDevice, scannedMap,mAssignedItem, mItemMap);
        mBeaconsListAdapter = new MyBeaconsListAdapter(this, R.layout.ble_device_row,
                mAssignedItem, mItemMap);

        SharedPreferences pref = getSharedPreferences("pref", AppCompatActivity.MODE_PRIVATE); // Shared Preference를 불러옵니다.
        // 저장된 값들을 불러옵니다.
        int scanTime = pref.getInt("ScanPeriod", Values.scanBreakTime);
        Boolean useScan = pref.getBoolean("UseScan", true);
        Boolean useGps = pref.getBoolean("UseGPS",false);



        Values.scanBreakTime=scanTime;
        Values.useBLE=useScan;
        Values.useGPS=useGps;



        usingTracking=true;
        mScan=false;

        scannedBeacons = (ListView)findViewById(R.id.scan_list);
        scannedBeacons.setAdapter(mBleDeviceListAdapter);

        myBeacons=(ListView)findViewById(R.id.ble_list);
        myBeacons.setAdapter(mBeaconsListAdapter);

        mBleScan =new BluetoothScan(this,mBleDeviceListAdapter,mBeaconsListAdapter);

        mAuth=LoginActivity.getAuth();
        mUser=LoginActivity.getUser();



        mHandler.sendEmptyMessageDelayed(0, CEHCK_PERIOD);
        mTimeOut.sendEmptyMessageDelayed(0, TIMEOUT_PERIOD);



        //Slide
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ble 검색 및 추가
                if(mBleScan.getMod()== Values.USE_NOTHING) {

                    myBeacons.setVisibility(View.GONE);
                    scannedBeacons.setVisibility(VISIBLE);
                    emptyListText.setVisibility(View.GONE);
                    mBleScan.changeMod(Values.USE_SCAN);
                    mBleScan.checkBluetooth();


                }else if(mBleScan.getMod()== Values.USE_SCAN)
                {
                    if(mItemMap.isEmpty())
                    {
                        emptyListText.setVisibility(VISIBLE);
                        myBeacons.setVisibility(View.GONE);

                    }
                    else
                    {
                        emptyListText.setVisibility(View.GONE);
                        myBeacons.setVisibility(VISIBLE);
                    }

                    scannedBeacons.setVisibility(View.GONE);
                    mBleScan.changeMod(Values.USE_NOTHING);
                }


                Log.d("main","scan mod changed "+mBleScan.getMod());

           }
        });

//        //TODO : fab - test용 버튼 (db저장메뉴)
//        FloatingActionButton fab_test = (FloatingActionButton) findViewById(R.id.fab_test);
//        fab_test.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                stopBleService();
//            }
//        });

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
        if(Values.useBLE)
            mBleScan.checkBluetooth();

        View headerLayout = navigationView.getHeaderView(0);
        mEmail=(TextView)headerLayout.findViewById(R.id.slide_user_email);
        mName=(TextView)headerLayout.findViewById(R.id.slide_user_name);



        if (mEmail != null&&mUser!=null) {

            mEmail.setText(mUser.getEmail());
        }

        if (mName != null&&mUser!=null) {

            mName.setText(mUser.getDisplayName());
        }

        //이미지 파일 썩션










    }


    public void stopBleService()
    {
        stopService(bleService);
    }


    @Override
    protected void onStart() {
        //displayBeacons();
        super.onStart();
        Log.v("Testing Print", "onStart");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();



        //My Data List 갱신
        DataFetch dataFetch = new DataFetch(mAssignedItem, mItemMap);
        dataFetch.displayBeacons();

        ProgressDialog asyncDialog = new ProgressDialog(
                MainActivity.this);

        //Log.v("mAssignedItem1 Addr", mAssignedItem.get(0).devAddress);
        Log.v("Test_Print", "Test1");

//        mItemMap = new HashMap<String, BleDeviceInfo>();
//        for(int i = 0; i < mAssignedItem.size(); i++) {
//            mItemMap.put(mAssignedItem.get(i).devAddress, mAssignedItem.get(i));
//        }

        //Log.v("mAssignedItem2 Addr", mAssignedItem.get(0).devAddress);
        Log.v("Test_Print", "Test2");

        //Log.v("mItemMap Addr", mItemMap.get("EC:08:81:F9:2A:D3").devAddress);
        //Log.v("mItemMap nick", mItemMap.get("EC:08:81:F9:2A:D3").getNickname());
    }

    protected void onResume() {
        super.onResume();

        //BEACON_UUID = getBeaconUuid(setting);

        if(mItemMap.isEmpty())
        {
            myBeacons.setVisibility(View.GONE);
            scannedBeacons.setVisibility(View.GONE);
            emptyListText.setVisibility(VISIBLE);

        }
        //saveRSSI = setting.getBoolean("saveRSSI", true);




//        if(isScannig) {
//            //scanBleDevice(true);            // BLE 장치 검색\
//            mHandler.sendEmptyMessageDelayed(0, SCAN_PERIOD);
//            mTimeOut.sendEmptyMessageDelayed(0, TIMEOUT_PERIOD);
//        }
//        else
//        {
//
//        }


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

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        int id = item.getItemId();

        if (id == R.id.nav_machine) {
            //내 메세지함 실행
            Intent intent = new Intent(this, ReadMessageActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_laf) {
            double lat, lng;

            GpsInfo gpsCoordi= new GpsInfo(GetMainActivity.getMainActity(),GetMainActivity.getMainActity());
            gpsCoordi.getLocation();

            lat = gpsCoordi.lat;
            lng = gpsCoordi.lon;
            Log.d("gpsgps","lat : "+gpsCoordi.lat);
            Log.d("gpsgps","lon : "+gpsCoordi.lon);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse( "https://beaconlocker-51c69.firebaseapp.com/?lat=" + lat + "&lng=" + lng  ));
            //Intent intent = new Intent(getApplicationContext(), RegLostDataActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_map) {
            Intent intent = new Intent(this, MultiMapActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_setting) {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_logout) {
            signOut();

            //Memory 비우기
            mHandler.removeMessages(0);
            mTimeOut.removeMessages(0);
            stopBleService();
            PictureList.clear();

            //새 인텐트 불러오기
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
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

//    private void displayBeacons(){
//        Log.v("Testing Print Uid", mUser.getUid());
//        DatabaseReference userUuidRef = mDatabase.getReference("users");
//
//        userUuidRef
//                .addChildEventListener(new ChildEventListener() {
//                    @Override
//                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                        Log.v("Testing Print", "ChildAdded");
//                        //get UUID
//                        for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
//                            String Uuid = (String) messageSnapshot.getValue();
//                            Log.v("Testing Print Uuid", Uuid);
//                        }
//                    }
//
//                    @Override
//                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//                    }
//
//                    @Override
//                    public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//                    }
//
//                    @Override
//                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
//    }

    @Override
    public void onDestroy() {
        Log.d("Main","main destory");
        mBleScan.end();
        mHandler.removeMessages(0);
        mTimeOut.removeMessages(0);
        //BeaconList.refresh();
        //stopService(bleService);
        super.onDestroy();

    }








}
