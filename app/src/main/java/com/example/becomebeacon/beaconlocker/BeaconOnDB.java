package com.example.becomebeacon.beaconlocker;

/**
 * Created by GW on 2017-04-26.
 */

public class BeaconOnDB {
    public String nickname, picture, islost;

    public BeaconOnDB() {
        islost = "0";
        nickname = "";
        picture = "";
    }

    public BeaconOnDB(String nickname) {
        islost = "0";
        this.nickname = nickname;
        picture = "";
    }

    public String getNickname() {
        return nickname;
    }

    public String getTitle() {
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
