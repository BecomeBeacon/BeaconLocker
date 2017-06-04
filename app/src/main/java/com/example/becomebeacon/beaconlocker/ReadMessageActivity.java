package com.example.becomebeacon.beaconlocker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ReadMessageActivity extends AppCompatActivity {
    private int mMessageIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_message);

        Intent intent=getIntent();
        mMessageIndex = intent.getIntExtra("MyMessageIndex",-1);
    }

    private void initUI() {
        
    }
}
