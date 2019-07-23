package com.kakao.sdk.sample;

/**
 * Created by Elizabeth on 2016-05-24.
 */
public class RoomsItem {
    private int profile; // 사진
    private String room_name; // 방이름
    private String room_master_name; // 방장닉네임
    private String room_master_id; // 방장ID


    public int getProfile() {
        return profile;
    }

    public String getRoom_name() {
        return room_name;
    }

    public String getRoom_master_name() {
        return room_master_name;
    }

    public String getRoom_master_id() {
        return room_master_id;
    }


    public RoomsItem(int profile, String room_name, String room_master_name, String room_master_id ) {
        this.profile = profile;
        this.room_name = room_name;
        this.room_master_name = room_master_name;
        this.room_master_id = room_master_id;
    }
}