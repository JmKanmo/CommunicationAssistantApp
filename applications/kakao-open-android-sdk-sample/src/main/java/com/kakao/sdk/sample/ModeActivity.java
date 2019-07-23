package com.kakao.sdk.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.kakao.friends.request.FriendsRequest;
import com.kakao.sdk.sample.common.BaseActivity;

public class ModeActivity extends BaseActivity {

    public static RequestQueue mRequestQue;
    // private ImageView device;
    private ImageView mode1;
    private ImageView mode2;
    private ImageView mode3;
    // private ImageView logout;
    private String chat_user_name = FirebaseAuth.getInstance().getCurrentUser().getEmail().split("@")[0];
    private DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference(chat_user_name).child("log");
    private boolean isdevice = false;


    protected void onCreate(Bundle savedInstanceState) {
        Log.d("프로세스", "모드엑티비티실행");
        FindAddressClick(chat_user_name);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modeselect);

        mRequestQue = Volley.newRequestQueue(this);
        //device = findViewById(R.id.device);
        mode1 = findViewById(R.id.mode1);
        mode2 = findViewById(R.id.mode2);
        mode3 = findViewById(R.id.mode3);

        FirebaseMessaging.getInstance().subscribeToTopic(chat_user_name);

        //logout = findViewById(R.id.logout_button);

        /*
        device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ChatActivity_device.class);
                startActivity(intent);
            }
        });
        */

        mode1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //       ChatData chatData = new ChatData();
                //     String temp = FirebaseDatabase.getInstance().getReference().push().toString();
                //    DatabaseReference ref = reference1.child(temp.split("/")[temp.split("/").length - 1]);
                //   chatData.setMsg("");
                //  chatData.setFuntcion("일상생활");
                // chatData.setUser_name(chat_user_name);
                //chatData.setUser_phoneNumber(MainActivity.PhoneNumber);
                // chatData.setUser_room("");
                //chatData.setDatabaseReference(temp);
                // ref.setValue(chatData);
                //if (isdevice == false) {
                //   ref.removeValue();
                //}
                Intent intent = new Intent(getApplicationContext(), ChatActivity_mode1.class);
                startActivity(intent);
            }
        });
        mode2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ChatActivity_device.class);
                startActivity(intent);
            }
        });

        mode3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), KakaoTalkFriendListActivity.class);
                String[] friendType = {FriendsRequest.FriendType.KAKAO_TALK.name()};
                intent.putExtra(FriendsMainActivity.EXTRA_KEY_SERVICE_TYPE, friendType);
                startActivity(intent);


                //  Intent intent = new Intent(getApplicationContext(), RoomActivity_dial.class);
                //   intent.putExtra("device", isdevice);
                //  startActivity(intent);
            }
        });

        /*
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        */
    }

    @Override
    protected void onStart() {
        Log.d("TAG_ModeActivity", "mode_onStart");
        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().get("device") != null) {
                isdevice = (boolean) getIntent().getExtras().get("device");
                if (isdevice) {
                    Log.d("TAG_ModeActivity", "디바이스가 있습니다.");
                    // device.setEnabled(false);
                } else {
                    Log.d("TAG_ModeActivity", "디바이스가 없습니다.");
                }
            }
        }
        super.onStart();
    }

    private String FindAddressClick(String id) {

        final String[] info = new String[1];


        FirebaseDatabase.getInstance().getReference("유저목록").orderByPriority().addListenerForSingleValueEvent(new ValueEventListener() {

            String temp;
            String deaf;
            String nickname;
            String userId;
            String phoneNumber;


            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Log.d("TTTT", dataSnapshot.getValue().toString());


                for (String str : dataSnapshot.getValue().toString().split(",")) {


                    if (str.split("=")[0].equals(" msg")) {
                        deaf = str.split("=")[1];
                    }

                    if (str.split("=")[0].equals(" user_room")) {
                        nickname = str.split("=")[1];
                    }

                    if (str.split("=")[0].equals(" user_name")) {
                        userId = str.split("=")[1];
                    }

                    if (str.split("=")[0].equals(" user_phoneNumber")) {
                        phoneNumber = str.split("=")[1];
                    }

                    if (str.split("=")[0].equals(" function")) {

                        // Log.d("프로세스", "여기 잘 돌아가나요? == > " + userId);
                        //  Log.d("프로세스", "여기 잘 돌아가나요? == > " + id);

                        if (userId.equals(id)) {
                            temp = nickname + "&" + phoneNumber + "&" + deaf;
                            info[0] = temp;
                        }

                        deaf = "";
                        nickname = "";
                        userId = "";
                        phoneNumber = "";

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        return info[0];
    }
}
