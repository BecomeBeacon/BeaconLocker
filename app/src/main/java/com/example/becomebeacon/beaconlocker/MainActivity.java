package com.example.becomebeacon.beaconlocker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
    private TextView mPoint;
    private GoogleApiClient mGoogleApiClient;
    private Intent bleService;

    private Toolbar toolbar;

    private boolean usingTracking;

    private BluetoothScan mBleScan;
    //private boolean mScanning=false;
    private boolean isScannig=false;


    private HashMap<String, BleDeviceInfo> scannedMap;
    //myItem
    private HashMap<String, BleDeviceInfo> mItemMap;

    private ArrayList<BleDeviceInfo> mArrayListBleDevice;
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

    private FloatingActionButton fab;



    private boolean mScan;

    private Handler mHandler= new Handler()
    {
        public void handleMessage(Message msg)
        {
            try {
                if(mBleScan.getMod()== Values.USE_SCAN) {

                    if(mScan) {
                        mBleScan.getBtAdapter().stopLeScan(mBleScan.mLeScanCallback);
                        mScan=false;

                        mHandler.sendEmptyMessageDelayed(0, Values.scanBreakTime);


                    }
                    else
                    {
                        mBleScan.getBtAdapter().startLeScan(mBleScan.mLeScanCallback);
                        mScan=true;

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
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10501", Toast.LENGTH_LONG).show();
                finish();
            }

        }
    };

    private Handler mTimeOut = new Handler(){
        public void handleMessage(Message msg){
            try {
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
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10502", Toast.LENGTH_LONG).show();
                finish();
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            mainProgressDialog = new ProgressDialog(this);
            mainProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mainProgressDialog.setMessage("목록을 불러오는 중...");
            mainProgressDialog.show();

            initUI();

        int result1 = new PermissionRequester.Builder(MainActivity.this)
                .setTitle("권한 요청")
                .setMessage("권한을 요청합니다.")
                .setPositiveButtonName("네")
                .setNegativeButtonName("아니요.")
                .create()
                .request(android.Manifest.permission.ACCESS_FINE_LOCATION, 1000 , new PermissionRequester.OnClickDenyButtonListener() {
                    @Override
                    public void onClick(Activity activity) {

                    }
                });

        if (result1 == PermissionRequester.ALREADY_GRANTED) {

            if (ActivityCompat.checkSelfPermission(MainActivity.this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            }
        }
        else if(result1 == PermissionRequester.NOT_SUPPORT_VERSION) {

        }
        else if(result1 == PermissionRequester.REQUEST_PERMISSION) {

        }


            //툴바 세팅
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            toolbar.setTitle(R.string.app_name);
            toolbar.setSubtitle("내 기기 목록");

            toolbar.setTitleTextColor(Color.WHITE);
            toolbar.setSubtitleTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorSubtitle));

            if(getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            Notifications.clear();
            BeaconList.refresh();

            mBleUtils=new BleUtils();
            mBluetoothAdapter= BluetoothAdapter.getDefaultAdapter();

            GetMainActivity.setMA(this);

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

            mAuth=LoginActivity.getAuth();
            mUser=LoginActivity.getUser();

            mHandler.sendEmptyMessageDelayed(0, CEHCK_PERIOD);
            mTimeOut.sendEmptyMessageDelayed(0, TIMEOUT_PERIOD);

            fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //ble 검색 및 추가
                    if(mBleScan.getMod()== Values.USE_NOTHING) {

                        toolbar.setSubtitle("주변의 미등록 비콘 목록");
                        fab.setImageResource(R.drawable.fab_my);

                        myBeacons.setVisibility(View.GONE);
                        scannedBeacons.setVisibility(VISIBLE);
                        emptyListText.setVisibility(View.GONE);
                        mBleScan.changeMod(Values.USE_SCAN);
                        mBleScan.checkBluetooth();


                    }else if(mBleScan.getMod()== Values.USE_SCAN)
                    {
                        toolbar.setSubtitle("내 기기 목록");
                        fab.setImageResource(R.drawable.fab_scan);
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


            View headerLayout = navigationView.getHeaderView(0);
            mEmail=(TextView)headerLayout.findViewById(R.id.slide_user_email);
            mName=(TextView)headerLayout.findViewById(R.id.slide_user_name);
            mPoint=(TextView)headerLayout.findViewById(R.id.PointView);


            if (mEmail != null&&mUser!=null) {

                mEmail.setText(mUser.getEmail());
            }

            if (mName != null&&mUser!=null) {

                mName.setText(mUser.getDisplayName());
            }

            mBleScan =new BluetoothScan(this,mBleDeviceListAdapter,mBeaconsListAdapter);
            bleService= new Intent(this,BleService.class);
            startService(bleService);

            if(Values.useBLE)
                mBleScan.checkBluetooth();
            //이미지 파일 썩션
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10500", Toast.LENGTH_LONG).show();
            finish();
        }

    }

    public void setPoint(int p)
    {
        mPoint.setText(BleService.myPoint+"");
    }

    private void initUI() {
        try {
            fab = (FloatingActionButton) findViewById(R.id.fab);
            myBeacons=(ListView)findViewById(R.id.ble_list);
            scannedBeacons=(ListView)findViewById(R.id.scan_list);
            emptyListText=(TextView)findViewById(R.id.text_have_no_ble);
        } catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10503", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    public void stopBleService()
    {
        try {
            stopService(bleService);
        } catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10504", Toast.LENGTH_LONG).show();
            finish();
        }
    }


    @Override
    protected void onStart() {
        try {
            super.onStart();

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
        } catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10505", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    protected void onResume() {
        try {
            super.onResume();

            if(mItemMap.isEmpty())
            {
                myBeacons.setVisibility(View.GONE);
                scannedBeacons.setVisibility(View.GONE);
                emptyListText.setVisibility(VISIBLE);

            }
        } catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10506", Toast.LENGTH_LONG).show();
            finish();
        }


    }

    //Slide Back
    @Override
    public void onBackPressed() {
        try {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        } catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10507", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        try {
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


                //Memory 비우기
                mHandler.removeMessages(0);
                mTimeOut.removeMessages(0);
                stopBleService();
                PictureList.clear();

                //새 인텐트 불러오기
                signOut();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();

            }
            //Slide Close
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        } catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10508", Toast.LENGTH_LONG).show();
            finish();
        }
        return true;
    }


    private void signOut()
    {
        try {
            mAuth.signOut();
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            // ...
                            Toast.makeText(getApplicationContext(),"Logged Out",Toast.LENGTH_SHORT).show();

                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10509", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void updateUI(FirebaseUser user) {
        try {
            if (user != null) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);

            } else {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10511", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onDestroy() {

        mBleScan.end();
        mHandler.removeMessages(0);
        mTimeOut.removeMessages(0);
        //BeaconList.refresh();
        //stopService(bleService);
        super.onDestroy();

            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10510", Toast.LENGTH_LONG).show();
            finish();
        }
    }








}
