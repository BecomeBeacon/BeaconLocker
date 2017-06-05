package com.example.becomebeacon.beaconlocker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ReadMessageActivity extends AppCompatActivity {
    private int mMessageIndex;

    //Layout 멤버변수
    TextView myMessageView;
    Button goUpperMessage;
    Button goLowerMessage;
    Button deleteMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_message);

        Intent intent=getIntent();
        mMessageIndex = intent.getIntExtra("MyMessageIndex",-1);

        initUI();
        initListeners();
        displayMyMessage();
    }

    private void initUI() {
        myMessageView = (TextView)findViewById(R.id.myMessageView);
        goUpperMessage = (Button)findViewById(R.id.button_goUpperMessage);
        goLowerMessage = (Button)findViewById(R.id.button_goLowerMessage);
        deleteMessage = (Button)findViewById(R.id.button_deleteMessage);
    }

    private void initListeners() {
        goUpperMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGoUpperMessage();
            }
        });
        goLowerMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGoLowerMessagege();
            }
        });
        deleteMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDeleteMessage();
            }
        });
    }

    private void displayMyMessage() {
        try {
            if (mMessageIndex < 0) {
                mMessageIndex = 0;
                myMessageView.setText(BeaconList.msgList.get(mMessageIndex).message);
                Toast.makeText(ReadMessageActivity.this, "상위 메세지가 없습니다", Toast.LENGTH_LONG).show();
            } else if (mMessageIndex >= BeaconList.msgList.size()) {
                mMessageIndex = BeaconList.msgList.size() - 1;
                myMessageView.setText(BeaconList.msgList.get(mMessageIndex).message);
                Toast.makeText(ReadMessageActivity.this, "하위 메세지가 없습니다", Toast.LENGTH_LONG).show();
            } else {
                myMessageView.setText(BeaconList.msgList.get(mMessageIndex).message);
            }
        }
        catch (Exception e) {
            e.getStackTrace();
            Toast.makeText(ReadMessageActivity.this, "메세지가 없습니다", Toast.LENGTH_LONG).show();
        }
    }

    private void setGoUpperMessage() {
        mMessageIndex--;
        displayMyMessage();
    }

    private void setGoLowerMessagege() {
        mMessageIndex++;
        displayMyMessage();
    }

    private void setDeleteMessage() {
        BeaconList.msgList.remove(mMessageIndex--);
        //TODO:파베에서 삭제
        //
        
        displayMyMessage();
        Toast.makeText(ReadMessageActivity.this, "메세지가 삭제됐습니다.", Toast.LENGTH_LONG).show();
    }
}
