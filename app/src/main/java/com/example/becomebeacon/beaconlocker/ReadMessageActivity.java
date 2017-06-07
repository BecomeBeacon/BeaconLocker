package com.example.becomebeacon.beaconlocker;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Iterator;

import static com.example.becomebeacon.beaconlocker.BeaconList.msgMap;

public class ReadMessageActivity extends AppCompatActivity {
    private String mMessageKey;
    private int mMessageIndex;
    private FirebaseUser mUser = LoginActivity.getUser();
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mDatabaseRef = mDatabase.getReference("users/"+mUser.getUid()+"/message/");

    private ArrayList<FindMessage> msgList;
    //Layout 멤버변수
    TextView myMessageView;
    Button goUpperMessage;
    Button goLowerMessage;
    Button deleteMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_message);

        //툴바 세팅
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_additem);
        setSupportActionBar(toolbar);

        toolbar.setTitle(R.string.app_name);
        toolbar.setSubtitle("메세지 함");

        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setSubtitleTextColor(ContextCompat.getColor(ReadMessageActivity.this, R.color.colorSubtitle));

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent intent=getIntent();
        mMessageKey = intent.getStringExtra("MyMessageKey");

        int notiNum=intent.getIntExtra("NOTI",-1);

        if(notiNum!=-1) {
            Log.d("NOTIC","noti : "+notiNum);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notiNum);
        }

        int op=1;
        if(mMessageKey==null)
            op=0;
        mMessageIndex=initMsg(op);
        initUI();
        initListeners();
        displayMyMessage();
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public int initMsg(int op)
    {
        int idx = 0;
        msgList = new ArrayList<FindMessage>(BeaconList.msgMap.values());

        Iterator<FindMessage> iter=msgList.iterator();

        while(iter.hasNext())
        {
            FindMessage fm=iter.next();
            if(!BeaconList.mItemMap.containsKey(fm.devAddress))
            {
                iter.remove();
            }
            else if(fm.isPoint)
            {
                iter.remove();
            }
        }

        if(op==1) {

            for (int i = 0; i < msgList.size(); i++) {
                if (mMessageKey.equals(msgList.get(i).keyValue)) {
                    idx = i;

                }

            }
        }
        else if(op==0)
        {
            idx=msgList.size()-1;
        }

        return idx;
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
                makeMessage();
                Toast.makeText(ReadMessageActivity.this, "상위 메세지가 없습니다", Toast.LENGTH_SHORT).show();
            } else if (mMessageIndex >= msgList.size()) {
                mMessageIndex = msgList.size() - 1;
                makeMessage();
                Toast.makeText(ReadMessageActivity.this, "하위 메세지가 없습니다", Toast.LENGTH_SHORT).show();
            } else {
                makeMessage();
            }
        }
        catch (Exception e) {
            e.getStackTrace();
            Toast.makeText(ReadMessageActivity.this, "메세지가 없습니다", Toast.LENGTH_SHORT).show();
        }
    }

    private void makeMessage() {
        String str = msgList.get(mMessageIndex).devAddress;
        String nickname =BeaconList.mItemMap.get(str).getNickname();
        str = nickname + "에 대한 메세지\n" + msgList.get(mMessageIndex).message;

        myMessageView.setText(str);
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
        //파베에서 삭제
        Log.d("RMA", "RMA LOG" + msgList.get(mMessageIndex).keyValue);
        Log.d("RMA", "RMA Reference" + mDatabaseRef.child(msgList.get(mMessageIndex).keyValue).toString());
        mDatabaseRef.child(msgList.get(mMessageIndex).keyValue).removeValue();

        //내부에서 삭제
        BeaconList.msgMap.remove(msgList.get(mMessageIndex).keyValue);
        msgList.remove(mMessageIndex--);

        displayMyMessage();
        Toast.makeText(ReadMessageActivity.this, "메세지가 삭제됐습니다.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        msgList.clear();

    }
}
