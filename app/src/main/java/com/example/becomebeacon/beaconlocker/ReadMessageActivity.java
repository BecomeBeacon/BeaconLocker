package com.example.becomebeacon.beaconlocker;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

public class ReadMessageActivity extends AppCompatActivity {
    private String mMessageKey;
    private int mMessageIndex;
    private FirebaseUser mUser = LoginActivity.getUser();
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mDatabaseRef = mDatabase.getReference("users/"+mUser.getUid()+"/messages/");

    private ArrayList<FindMessage> msgList;
    //Layout 멤버변수
    TextView myMessageView;
    TextView myrealMessageView;
    Button goUpperMessage;
    Button goLowerMessage;
    Button deleteMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10700", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        try {
            switch(item.getItemId()) {
                case android.R.id.home:
                    finish();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10701", Toast.LENGTH_LONG).show();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public int initMsg(int op)
    {
        int idx = 0;
        try {
            idx = 0;
            msgList = new ArrayList<>(BeaconList.msgMap.values());
            Collections.sort(msgList);
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
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10702", Toast.LENGTH_LONG).show();
            finish();
        }

        return idx;
    }

    private void initUI() {
        try {
            myMessageView = (TextView)findViewById(R.id.myMessageView);
            myrealMessageView = (TextView)findViewById(R.id.myrealMessageView);
            goUpperMessage = (Button)findViewById(R.id.button_goUpperMessage);
            goLowerMessage = (Button)findViewById(R.id.button_goLowerMessage);
            deleteMessage = (Button)findViewById(R.id.button_deleteMessage);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10703", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initListeners() {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10704", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void displayMyMessage() {
        try {
            setUserName();
            if(msgList.size() == 0) {
                myrealMessageView.setText("메세지가 없습니다");
                Toast.makeText(ReadMessageActivity.this, "메세지가 없습니다", Toast.LENGTH_SHORT).show();
            }
            else {
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
                } catch (Exception e) {
                    e.getStackTrace();
                    Toast.makeText(ReadMessageActivity.this, "메세지가 없습니다", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10705", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void makeMessage() {
        try {
            String str = msgList.get(mMessageIndex).devAddress;
            String nickname =BeaconList.mItemMap.get(str).getNickname();
            str = nickname + "에 대한 메세지\n" + msgList.get(mMessageIndex).message;

            myrealMessageView.setText(str);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10706", Toast.LENGTH_LONG).show();
            finish();
        }
    }
    private void setUserName()
    {
        FindMessage message = msgList.get(mMessageIndex);
        String nickname =BeaconList.mItemMap.get(message.devAddress).getNickname();
        SimpleDateFormat CurDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = CurDateFormat.format(message.date);
        String str = nickname + "에 대한 메세지, " + date;
        myMessageView.setText(str);
    }


    private void setGoUpperMessage() {
        try {
            mMessageIndex--;
            displayMyMessage();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10707", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setGoLowerMessagege() {
        try {
            mMessageIndex++;
            displayMyMessage();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10708", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setDeleteMessage() {
    try {
        mDatabaseRef.child(msgList.get(mMessageIndex).keyValue).removeValue();

            //내부에서 삭제
            BeaconList.msgMap.remove(msgList.get(mMessageIndex).keyValue);
            msgList.remove(mMessageIndex--);

            Toast.makeText(ReadMessageActivity.this, "메세지가 삭제됐습니다.", Toast.LENGTH_SHORT).show();
            displayMyMessage();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10709", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onDestroy()
    {

        try {
            super.onDestroy();
            msgList.clear();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10710", Toast.LENGTH_LONG).show();
            finish();
        }

    }
}
