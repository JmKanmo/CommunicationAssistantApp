package com.kakao.sdk.sample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Elizabeth on 2016-05-24.
 */
public class RoomListAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private ArrayList<RoomsItem> data; //Item 목록을 담을 배열
    private int layout;

    public RoomListAdapter(Context context, int layout, ArrayList<RoomsItem> data) {
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.data = data;
        this.layout = layout;
    }

    @Override
    public int getCount() { //리스트 안 Item의 개수를 센다.
        return data.size();
    }

    @Override
    public String getItem(int position) {
        return data.get(position).getRoom_name();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(layout, parent, false);
        }
        RoomsItem RoomsItem = data.get(position);

        //이미지 파일 연동
        ImageView profile = (ImageView) convertView.findViewById(R.id.profile);
        profile.setImageResource(RoomsItem.getProfile());

        //이름 등 정보 연동
        TextView info = (TextView) convertView.findViewById(R.id.info);
        info.setText(RoomsItem.getRoom_name());

        //전화번호 연동
        TextView phone = (TextView) convertView.findViewById(R.id.phone);
        phone.setText(RoomsItem.getRoom_master_name());


        return convertView;
    }
}