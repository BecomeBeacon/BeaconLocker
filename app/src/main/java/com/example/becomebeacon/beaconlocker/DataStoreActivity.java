package com.example.becomebeacon.beaconlocker;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by gwmail on 2017-04-26.
 */

public class DataStoreActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private static FirebaseDatabase mDatabase;

    private TextView et_UUID;
    private EditText et_Nickname;
    //private TextView et_Picture;
    //private EditText et_Islost;
    //private EditText et_LATITUDE;
    //private EditText et_LONGITUDE;

    private DatabaseReference mUserUuidRef;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setTitle("BLE 등록");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mAuth= LoginActivity.getAuth();
        mUser= LoginActivity.getUser();
        mDatabase = FirebaseDatabase.getInstance();
        mUserUuidRef = mDatabase.getReference("users/"+mUser.getUid()+"beacons");

        et_UUID = (TextView) findViewById(R.id.et_UUID);
        et_Nickname = (EditText) findViewById(R.id.et_NICKNAME);
        //et_Picture = (TextView) findViewById(R.id.et_PICTURE);
        //et_LATITUDE = (EditText) findViewById(R.id.et_LATITUDE);
        //et_LONGITUDE = (EditText) findViewById(R.id.et_LONGITUDE);

        //TODO :: 인증 실패시 조치
        //if(mFirebaseUser == null) {
            //startActivity(new Intent(MainActivity.this, LoginActivity.class));
            //finish();
            //return;
        //}


        Button button_saveForm = (Button) findViewById(R.id.button_saveForm);
        button_saveForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });

        displayBeacons();
    }
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.list_menu, menu);
//
//        LayoutInflater inflator=(LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
//
//        Inflater ss = inflator.inflate(R.menu.list_menu,false);
//
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(android.view.MenuItem item) {
//
//        int id = item.getItemId();
//
//        if( id == R.id.newBle ){
//            saveData();
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    private void initData() {
        et_UUID.setText("");
        et_Nickname.setText("");
    }

    private void saveData() {
        if (et_UUID.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), "UUID 값이 없습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        } else if (et_Nickname.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Nickname 값이 없습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        //else if (et_Picture.getText().toString().isEmpty()) {
        //    Toast.makeText(getApplicationContext(), "Picture 값이 없습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
        //    return;
        //}

        //'users' 에 소지한 비콘 UUID 넣기
        BleDeviceInfo bleDeviceInfo = new BleDeviceInfo();
        bleDeviceInfo.setProximityUuid(et_UUID.getText().toString());
        BeaconOnUser beaconOnUser = new BeaconOnUser(bleDeviceInfo.getProximityUuid());

        mUserUuidRef.push().setValue(beaconOnUser);

        //store beacon info to 'Beacon' DB in Uid order
        bleDeviceInfo.setNickname(et_Nickname.getText().toString());
        bleDeviceInfo.setPicture("in develop");

        mDatabase
                .getReference("beacon/")
                .child(bleDeviceInfo.getProximityUuid())
                .setValue(bleDeviceInfo.toDB())
                .addOnSuccessListener(DataStoreActivity.this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "서버에 저장되었습니다.", Toast.LENGTH_LONG).show();
                        initData();
                        finish();
                    }
                })
                .addOnFailureListener(DataStoreActivity.this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "저장에 실패하였습니다.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void displayBeacons() {
        // users/$Uid/beacons/"UUID"

        Log.v("Testing Print Uid", mUser.getUid());

        mUserUuidRef
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot uuidSnapshot : dataSnapshot.getChildren()) {
                            BeaconOnUser myBeaconOnUser = uuidSnapshot.getValue(BeaconOnUser.class);
                            Log.v("Test Print UUID", myBeaconOnUser.Uuid);

                            findBeaconByUuid(myBeaconOnUser.Uuid);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    private void findBeaconByUuid(String Uuid) {
        // beacon/UUID/"beaconOnDB"
        DatabaseReference beaconInfoRef = mDatabase.getReference("beacon/");

        beaconInfoRef.child(Uuid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        BeaconOnDB beaconOnDB = dataSnapshot.getValue(BeaconOnDB.class);
                        Log.v("Test Print nick", beaconOnDB.nickname);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public static FirebaseDatabase getDatabase()
    {
        return mDatabase;
    }
}
