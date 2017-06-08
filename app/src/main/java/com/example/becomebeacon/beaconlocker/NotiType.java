package com.example.becomebeacon.beaconlocker;

/**
 * Created by 함상혁입니다 on 2017-06-07.
 */

public class NotiType {
    String devAddress;
    int op;

    public NotiType(String add,int op)
    {
        devAddress=add;
        this.op=op;
    }

    public String toString()
    {
        return "MAC :"+devAddress+ " op : "+op;
    }

    @Override
    public boolean equals(Object nt)
    {
        boolean b;
        NotiType noti=(NotiType)nt;
        if(noti.devAddress.equals(devAddress)&&noti.op==op)
        {

            b=true;
        }
        else
        {

            b=false;
        }


        return b;
    }
}
