/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kakao.sdk.sample;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kakao.sdk.sample.activities.SampleActivityBase;
import com.kakao.sdk.sample.common.ChatData;
import com.kakao.sdk.sample.common.widget.KakaoToast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.kakao.sdk.sample.BluetoothChatFragment.mChatService;


/**
 * A simple launcher activity containing a summary sample description, sample log and a custom
 * {@link android.support.v4.app.Fragment} which can display a view.
 * <p>
 * For devices with displays with a width of 720dp or greater, the sample log is always visible,
 * on other devices it's visibility is controlled by an item on the Action Bar.
 */
public class ChatActivity_device extends SampleActivityBase {


    ChatData chatData;
    static LinearLayout button_layout;

    static Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Log.d("TAG", "돌아가니?");
            // 원래 하려던 동작 (UI변경 작업 등)
            if (mSendButton != null) {
                if (BluetoothChatFragment.device_on) {
                    button_layout.setVisibility(View.GONE);
                } else {
                    button_layout.setVisibility(View.VISIBLE);
                    mSendButton.setVisibility(mSendButton.VISIBLE);
                    mSendButton.setEnabled(true);
                    mSendButton.setText("디바이스 활성화");
                    mSendButton.setTextColor(Color.WHITE);
                }
            }
            //BluetoothChatFragment.device_on = false;
        }
    };


    public static Activity activity;

    static Button mSendButton;

    static String chat_user_name = FirebaseAuth.getInstance().getCurrentUser().getEmail().split("@")[0];
    static String chat_room_name;

    String temp;
    String deaf;
    String nickname;
    String userId;
    String phoneNumber;

    private DatabaseReference reference = FirebaseDatabase.getInstance().getReference("원거리 소통");
    private DatabaseReference user_reference = FirebaseDatabase.getInstance().getReference("유저목록");
    private ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.d("TTTT",dataSnapshot.getKey());
            Log.d("TTTT",dataSnapshot.getValue().toString());
            get_kakao_info(dataSnapshot,chat_user_name);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };



    static DatabaseReference ref;
    static DatabaseReference room_exit_ref;


    String[] perMissionList = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_PRIVILEGED
    };

    // Whether the Log Fragment is currently shown
    private boolean mLogShown;

    public final Context context = this;

    static ListView listView;// 채팅방 목록
    static EditText editText;// 채팅방 이름 입력
    static Button button;// 채팅방 생성 버튼


    static RoomListAdapter adapter;
    static ArrayList<RoomsItem> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        user_reference.orderByPriority().addListenerForSingleValueEvent(valueEventListener);

//        Log.d("TATA",deaf);
//        Log.d("TATA",nickname);
//        Log.d("TATA",userId);
//        Log.d("TATA",phoneNumber);



        button_layout = findViewById(R.id.button_layout);
        mSendButton = findViewById(R.id.button_send);
        listView = findViewById(R.id.list);
        editText = findViewById(R.id.editText);
        button = findViewById(R.id.button);

        adapter = new RoomListAdapter(this, R.layout.rooms_item, data);
        listView.setAdapter(adapter);

        activity = this;

        Message msg = handler.obtainMessage();
        handler.sendMessage(msg);

        //Log.d("프로세스", msg.toString());

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        BluetoothChatFragment fragment = new BluetoothChatFragment();
        transaction.replace(R.id.sample_content_fragment, fragment);
        transaction.commit();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        for (String permission : perMissionList) {
            int check = checkCallingOrSelfPermission(permission);
            if (check == PackageManager.PERMISSION_DENIED) {
                requestPermissions(perMissionList, 0);
            }
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
                    if (BluetoothChatFragment.device_on) {

                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put(chat_user_name + "&" + editText.getText().toString() +"&"+ nickname, null);
                        reference.updateChildren(map);

                        chat_room_name = editText.getText().toString();
                        chatData = new ChatData();
                        String temp = FirebaseDatabase.getInstance().getReference().push().toString();
                        ref = reference.child(chat_user_name + "&" + chat_room_name + "&" + nickname);
                        chatData.setMsg(nickname);
                        chatData.setFuntcion("create_room");
                        chatData.setUser_name(chat_user_name);
                        chatData.setUser_phoneNumber(MainActivity.PhoneNumber);
                        chatData.setUser_room(chat_room_name);
                        chatData.setDatabaseReference(temp);
                        ref.child(temp.split("/")[temp.split("/").length - 1]).setValue(chatData);
                        room_exit_ref = ref;
                        sendMessage("방 생성" + "&" + chat_user_name + "&" + editText.getText().toString() + "&" + nickname + "&" + room_exit_ref.toString()); //하드웨어가 알수있어
                        Intent intent = new Intent(getApplicationContext(), ChatActivity_mode2.class);
                        startActivity(intent);
                        KakaoToast.makeToast(getApplicationContext(), "방을 생성하셨습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        KakaoToast.makeToast(getApplicationContext(), "연결된 기기의 네트워크 상태를 확인해주세요.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    KakaoToast.makeToast(getApplicationContext(), "연결된 기기없이는 방을 생성할 수 없습니다.", Toast.LENGTH_SHORT).show();

                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //Log.d("프로세스", "onCreateOptionsMenu");

        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //Log.d("프로세스", "onCreateOptionsMenu");
        MenuItem logToggle = menu.findItem(R.id.menu_toggle_log);
        logToggle.setVisible(findViewById(R.id.sample_output) instanceof ViewAnimator);
        logToggle.setTitle(mLogShown ? R.string.sample_hide_log : R.string.sample_show_log);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       // Log.d("프로세스", "onOptionsItemSelected");

        switch (item.getItemId()) {
            case R.id.menu_toggle_log:
                mLogShown = !mLogShown;
                ViewAnimator output = (ViewAnimator) findViewById(R.id.sample_output);
                if (mLogShown) {
                    output.setDisplayedChild(1);
                } else {
                    output.setDisplayedChild(0);
                }
                supportInvalidateOptionsMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendMessage(String message) {
       // Log.d("프로세스", "sendMessage");

        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(getApplicationContext(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);
            // Reset out string buffer to zero and clear the edit text field
            BluetoothChatFragment.mOutStringBuffer.setLength(0);
        }
    }

    @Override
    protected void onDestroy() {
        Log.d("프로세스", "메인엑티비티onDestroy");
        //Log.d("프로세스", "메인액티비티종료");
        if (mChatService != null) {
           // Log.d("프로세스", "연결끊기");
            mChatService.stop();
            mChatService = null;
        }
        super.onDestroy();

    }


    void get_kakao_info(DataSnapshot dataSnapshot,String chat_user_name) {

        String deaf_temp = "";
        String nickname_temp = "";
        String userId_temp = "";
        String phoneNumber_temp = "";


        for (String str : dataSnapshot.getValue().toString().split(",")) {

            if (str.split("=")[0].equals(" msg")) {
                deaf_temp = str.split("=")[1];
            }

            if (str.split("=")[0].equals(" user_room")) {
                nickname_temp = str.split("=")[1];
            }

            if (str.split("=")[0].equals(" user_name")) {
                userId_temp = str.split("=")[1];
            }

            if (str.split("=")[0].equals(" user_phoneNumber")) {
                phoneNumber_temp = str.split("=")[1];
            }

            if (str.split("=")[0].equals(" function")) {



                if (userId_temp.equals(chat_user_name)){
                    deaf = deaf_temp;
                    nickname= nickname_temp;
                    userId= userId_temp;
                    phoneNumber = phoneNumber_temp;
                }
            }
        }
    }

}