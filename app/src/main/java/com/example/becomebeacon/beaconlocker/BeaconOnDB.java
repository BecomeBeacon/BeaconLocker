package com.example.becomebeacon.beaconlocker;

/**
 * Created by GW on 2017-04-26.
 */

public class BeaconOnDB {
    public String nickname, picture, islost;

    public BeaconOnDB() throws Exception{
        islost = "0";
        nickname = "";
        picture = "";
    }

    public BeaconOnDB(String nickname) throws Exception{
        islost = "0";
        this.nickname = nickname;
        picture = "";
    }

    public String getNickname() throws Exception{
        return nickname;
    }

    public String getTitle() throws Exception{
        if( nickname != null ) {
            if(nickname.length() > 5) {
                return nickname.substring(0, 5) + "...";
            } else {
                return nickname;
            }
        }
        return null;
    }

}
