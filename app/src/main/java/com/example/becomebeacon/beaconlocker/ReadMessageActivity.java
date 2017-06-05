package com.example.becomebeacon.beaconlocker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class ReadMessageActivity extends AppCompatActivity {
    private int mMessageIndex;

    //Layout 멤버변수
    TextView myMessageView;
    Button goUpperMessage;
    Button goLowerMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_message);

        Intent intent=getIntent();
        mMessageIndex = intent.getIntExtra("MyMessageIndex",-1);

        initUI();
    }

    private void initUI() {
        TextView myMessageView = (TextView)findViewById(R.id.myMessageView);
        Button goUpperMessage = (Button)findViewById(R.id.button_goUpperMessage);
        Button goLowerMessage = (Button)findViewById(R.id.button_goLowerMessage);
    }


}
