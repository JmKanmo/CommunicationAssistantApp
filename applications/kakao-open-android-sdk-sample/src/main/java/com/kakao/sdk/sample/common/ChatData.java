package com.kakao.sdk.sample.common;

import android.util.Log;

/**
 * Created by KPlo on 2018. 11. 3..
 */

public class ChatData {

    private String user_name;
    private String user_room;
    private String msg;
    private String user_phoneNumber;
    private String function;
    private String databaseReference;


    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public void setUser_phoneNumber(String user_phoneNumber) {
        this.user_phoneNumber = user_phoneNumber;
    }

    public String getUser_phoneNumber(){
        return user_phoneNumber;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_room(String user_room) {
        this.user_room = user_room;
    }

    public String getUser_room() {
        return user_room;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getFunction() {
        return function;
    }

    public void setFuntcion(String function) {
        this.function = function;
    }

    public String getDatabaseReference() {
        return databaseReference;
    }

    public void setDatabaseReference(String databaseReference) {
        this.databaseReference = databaseReference;
    }
}
