package com.example.becomebeacon.beaconlocker.database;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.becomebeacon.beaconlocker.LoginActivity;
import com.example.becomebeacon.beaconlocker.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.example.becomebeacon.beaconlocker.LoginActivity;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by gwmail on 2017-04-26.
 */

public class DataStore extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseDatabase mDatabase;

    private EditText et_UUID;
    private EditText et_Nickname;
    private EditText et_Picture;
    private EditText et_Islost;
    //private EditText et_LATITUDE;
    //private EditText et_LONGITUDE;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        mAuth= LoginActivity.getAuth();
        mUser= LoginActivity.getUser();
        mDatabase = FirebaseDatabase.getInstance();

        et_UUID = (EditText) findViewById(R.id.et_UUID);
        et_Nickname = (EditText) findViewById(R.id.et_NICKNAME);
        et_Picture = (EditText) findViewById(R.id.et_PICTURE);
        //et_LATITUDE = (EditText) findViewById(R.id.et_LATITUDE);
        //et_LONGITUDE = (EditText) findViewById(R.id.et_LONGITUDE);

        //TODO :: 인증 실패시 조치
        //if(mFirebaseUser == null) {
            //startActivity(new Intent(MainActivity.this, LoginActivity.class));
            //finish();
            //return;
        //}

        Button button_clearForm = (Button) findViewById(R.id.button_clearForm);
        button_clearForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initData();
            }
        });

        Button button_saveForm = (Button) findViewById(R.id.button_saveForm);
        button_saveForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });
    }

    private void initData() {
        et_UUID.setText("");
        et_Nickname.setText("");
        et_Picture.setText("");
        et_Islost.setText("");
    }

    private void saveData() {
        Beacon beacon = new Beacon();
        beacon.setUUID(et_UUID.getText().toString());
        beacon.setNickname(et_Nickname.getText().toString());
        beacon.setPicture(et_Picture.getText().toString());

        mDatabase.getReference("users/" + mUser.getUid() + "/beacons/").push().setValue(beacon.getUUID());

        mDatabase.getReference("beacon/" + beacon.getUUID()).push().setValue("isLost", beacon.getIslost());
        mDatabase.getReference("beacon/" + beacon.getUUID()).push().setValue("Nickname",beacon.getNickname());
        mDatabase.getReference("beacon/" + beacon.getUUID()).push().setValue("Picture",beacon.getPicture());
    }
}
