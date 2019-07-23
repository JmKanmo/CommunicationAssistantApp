package com.kakao.sdk.sample;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;


public class MainFragment extends Fragment {
    Button mSendButton;
    ListView listView;// 채팅방 목록
    EditText editText;// 채팅방 이름 입력
    Button button;// 채팅방 생성 버튼

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle   savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_device, container, false);
        mSendButton = rootView.findViewById(R.id.button_send);
        return rootView;
    }


}