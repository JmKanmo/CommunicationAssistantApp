package com.kakao.sdk.sample;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kakao.sdk.sample.common.ChatData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ChatActivity_mode3_select extends AppCompatActivity implements TextView.OnEditorActionListener {

    final Context context = this;
    Vibrator vibrator;
    private boolean isStop = false;

    final String[] info = new String[1];

    TextView target_name;

    static boolean received;
    static String sender;
    private ImageView dial_accept_btn;
    private ImageView dial_refuse_btn;

    private ImageView dial_cancel_btn;

    private TextView textView;

    private String chat_room;
    private String chat_user;
    private String chat_message;
    private String chat_function;
    private String chat_databaseReference;
    private String chat_phoneNumber;
    private String chat_isdeaf = "";
    private String chat_myName = "";

    private String chat_user_name = FirebaseAuth.getInstance().getCurrentUser().getEmail().split("@")[0];

    DatabaseReference reference = FirebaseDatabase.getInstance().getReference(chat_user_name).child("dial");

    MediaPlayer mediaPlayer;

    private String URL = "https://fcm.googleapis.com/fcm/send";

    private Timer timer;
    private long firstTime;
    private long diff;

    class CustomTimer extends TimerTask {

        CustomTimer() {
            firstTime = System.currentTimeMillis();
        }

        @Override
        public void run() {
            long curTime = System.currentTimeMillis();
            diff = (curTime - firstTime) / 1000;
            Log.d("TAG", diff + "초");

            if (diff > 30) {
                if (received) {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    isStop = true;
                    timer.cancel();

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference(sender).child("dial");
                    ChatData chatData = new ChatData();
                    String temp = reference.push().toString();
                    DatabaseReference ref = reference.child(temp.split("/")[temp.split("/").length - 1]);
                    chatData.setMsg("");
                    chatData.setFuntcion("dial_refuse");
                    chatData.setUser_name(chat_user_name);
                    chatData.setUser_phoneNumber(MainActivity.PhoneNumber);
                    chatData.setUser_room("");

                    chatData.setDatabaseReference(temp);
                    ref.setValue(chatData);
                    ref.removeValue();
                    finish();
                } else {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            final Toast toast = Toast.makeText(context, "상대방이 전화를 받지않습니다.", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    });
                    finish();
                    timer.cancel();
                    isStop = true;
                    reference.removeEventListener(childEventListener);
                    sendNotification(KakaoTalkFriendListActivity.chat_target_name, chat_myName);
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference(KakaoTalkFriendListActivity.chat_target_name).child("dial");
                    ChatData chatData = new ChatData();
                    String temp = reference.push().toString();
                    DatabaseReference ref = reference.child(temp.split("/")[temp.split("/").length - 1]);
                    chatData.setMsg("");
                    chatData.setFuntcion("dial_cancel");
                    chatData.setUser_name(chat_user_name);
                    chatData.setUser_phoneNumber(MainActivity.PhoneNumber);
                    chatData.setUser_room("");
                    chatData.setDatabaseReference(temp);
                    ref.setValue(chatData);
                    ref.removeValue();

                    FirebaseDatabase.getInstance().getReference("통화").child(chat_user_name + "&" + KakaoTalkFriendListActivity.chat_target_name).removeValue();


                }
                timer.cancel();
            }
        }
    }

    private ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            chatConversation(dataSnapshot);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.d("프로세스", "전화선택모드액티비티실행");

        reference.addChildEventListener(childEventListener);


        vibrator = (Vibrator) getSystemService(context.VIBRATOR_SERVICE);

        timer = new Timer();
        timer.schedule(new CustomTimer(), 1000, 1000);

        if (getIntent().getExtras() != null) {
            received = (boolean) getIntent().getExtras().get("received");
            sender = (String) getIntent().getExtras().get("sender");
            chat_isdeaf = (String) getIntent().getExtras().get("isdeaf");
            chat_myName = (String) getIntent().getExtras().get("myName");
        }

        if (received) {
            mediaPlayer = MediaPlayer.create(this, R.raw.kakaotog_boiseutog);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
            startVibrate();
            setContentView(R.layout.dial_reception);

            dial_accept_btn = findViewById(R.id.dial_accept_btn);
            dial_refuse_btn = findViewById(R.id.dial_refuse_btn);
            textView = findViewById(R.id.sender_name);

            FindAddressClick_received(sender);

            //textView.setText(info[0]);
            dial_accept_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    vibrator.cancel();
                    timer.cancel();
                    isStop = true;

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference(sender).child("dial");
                    ChatData chatData = new ChatData();
                    String temp = reference.push().toString();
                    DatabaseReference ref = reference.child(temp.split("/")[temp.split("/").length - 1]);
                    chatData.setMsg("");
                    chatData.setFuntcion("dial_accept");
                    chatData.setUser_name(chat_user_name);
                    chatData.setUser_phoneNumber(MainActivity.PhoneNumber);
                    chatData.setUser_room("");
                    chatData.setDatabaseReference(temp);
                    ref.setValue(chatData);
                    ref.removeValue();

                    if (MyService.deaf) {
                        finish();
                        Intent intent = new Intent(getApplicationContext(), ChatActivity_mode3_deaf.class);
                        // intent.putExtra("received", received);
                        // intent.putExtra("sender", sender);
                        startActivity(intent);
                    } else {
                        finish();
                        Intent intent = new Intent(getApplicationContext(), ChatActivity_mode3_nondeaf.class);
                        //intent.putExtra("received", received);
                        //intent.putExtra("sender", sender);
                        startActivity(intent);
                    }
                }
            });
            dial_refuse_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    vibrator.cancel();
                    timer.cancel();
                    isStop = true;

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference(sender).child("dial");
                    ChatData chatData = new ChatData();
                    String temp = reference.push().toString();
                    DatabaseReference ref = reference.child(temp.split("/")[temp.split("/").length - 1]);
                    chatData.setMsg("");
                    chatData.setFuntcion("dial_refuse");
                    chatData.setUser_name(chat_user_name);
                    chatData.setUser_phoneNumber(MainActivity.PhoneNumber);
                    chatData.setUser_room("");

                    chatData.setDatabaseReference(temp);
                    ref.setValue(chatData);
                    ref.removeValue();

                    finish();
                }
            });
        } else {
            setContentView(R.layout.dial_waiting);
            target_name = findViewById(R.id.target_name);

            FindAddressClick_send(KakaoTalkFriendListActivity.chat_target_name);
            //target_name.setText(info[0]);


            dial_cancel_btn = findViewById(R.id.exit_btn);
            dial_cancel_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                    timer.cancel();
                    reference.removeEventListener(childEventListener);
                    isStop = true;
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference(KakaoTalkFriendListActivity.chat_target_name).child("dial");

                    ChatData chatData = new ChatData();
                    String temp = reference.push().toString();
                    DatabaseReference ref = reference.child(temp.split("/")[temp.split("/").length - 1]);
                    chatData.setMsg("");
                    chatData.setFuntcion("dial_cancel");
                    chatData.setUser_name(chat_user_name);
                    chatData.setUser_phoneNumber(MainActivity.PhoneNumber);
                    chatData.setUser_room("");
                    chatData.setDatabaseReference(temp);
                    ref.setValue(chatData);
                    ref.removeValue();

                    FirebaseDatabase.getInstance().getReference("통화").child(chat_user_name + "&" + KakaoTalkFriendListActivity.chat_target_name).removeValue();
                    sendNotification(KakaoTalkFriendListActivity.chat_target_name, chat_myName);


                }
            });


        }
    }

    private void startVibrate() {
        if (isStop) {
            return;
        }
        vibrator.vibrate(new long[]{0, 500000, 0, 500000, 0, 500000, 0}, -1);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startVibrate();
            }
        }, 1000);
    }

    @Override
    protected void onStart() {
        Log.d("TAG_select", "onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d("TAG_RoomActivity", "onResume");
        super.onResume();
    }

    @Override
    protected void onRestart() {
        Log.d("TAG_select", "onRestart");
        super.onRestart();
    }

    @Override
    protected void onPause() {
        Log.d("TAG_select", "onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d("TAG_select", "onStop");
        timer.cancel();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d("TAG_select", "onDestroy");

        if (received) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            vibrator.cancel();
            timer.cancel();
            isStop = true;
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(sender).child("dial");
            ChatData chatData = new ChatData();
            String temp = reference.push().toString();
            DatabaseReference ref = reference.child(temp.split("/")[temp.split("/").length - 1]);
            chatData.setMsg("");
            chatData.setFuntcion("dial_refuse");
            chatData.setUser_name(chat_user_name);
            chatData.setUser_phoneNumber(MainActivity.PhoneNumber);
            chatData.setUser_room("");


            chatData.setDatabaseReference(temp);
            ref.setValue(chatData);
            ref.removeValue();

            finish();
        } else {
            finish();
            timer.cancel();
            reference.removeEventListener(childEventListener);
            isStop = true;

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(KakaoTalkFriendListActivity.chat_target_name).child("dial");
            ChatData chatData = new ChatData();
            String temp = reference.push().toString();
            DatabaseReference ref = reference.child(temp.split("/")[temp.split("/").length - 1]);
            chatData.setMsg("");
            chatData.setFuntcion("dial_cancel");
            chatData.setUser_name(chat_user_name);
            chatData.setUser_phoneNumber(MainActivity.PhoneNumber);
            chatData.setUser_room("");
            chatData.setDatabaseReference(temp);
            ref.setValue(chatData);
            ref.removeValue();

        }

        super.onDestroy();
        /*
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        reference.removeEventListener(childEventListener);
        */
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d("TAG_select", "onBackPressed");


        if (received) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            vibrator.cancel();
            timer.cancel();
            isStop = true;
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(sender).child("dial");
            ChatData chatData = new ChatData();
            String temp = reference.push().toString();
            DatabaseReference ref = reference.child(temp.split("/")[temp.split("/").length - 1]);
            chatData.setMsg("");
            chatData.setFuntcion("dial_refuse");
            chatData.setUser_name(chat_user_name);
            chatData.setUser_phoneNumber(MainActivity.PhoneNumber);
            chatData.setUser_room("");

            chatData.setDatabaseReference(temp);
            ref.setValue(chatData);
            ref.removeValue();

            finish();
        } else {
            finish();
            timer.cancel();
            reference.removeEventListener(childEventListener);
            isStop = true;
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(KakaoTalkFriendListActivity.chat_target_name).child("dial");
            ChatData chatData = new ChatData();
            String temp = reference.push().toString();
            DatabaseReference ref = reference.child(temp.split("/")[temp.split("/").length - 1]);
            chatData.setMsg("");
            chatData.setFuntcion("dial_cancel");
            chatData.setUser_name(chat_user_name);
            chatData.setUser_phoneNumber(MainActivity.PhoneNumber);
            chatData.setUser_room("");
            chatData.setDatabaseReference(temp);
            ref.setValue(chatData);
            ref.removeValue();
        }
        // finish();
        // reference.removeEventListener(childEventListener);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        return false;
    }

    private void chatConversation(DataSnapshot dataSnapshot) {
        Iterator i = dataSnapshot.getChildren().iterator();
        while (i.hasNext()) {
            chat_databaseReference = (String) ((DataSnapshot) i.next()).getValue();
            Log.d("PARK", chat_databaseReference);
            chat_function = (String) ((DataSnapshot) i.next()).getValue();
            Log.d("PARK", chat_function);
            chat_message = (String) ((DataSnapshot) i.next()).getValue();
            Log.d("PARK", chat_message);
            chat_user = (String) ((DataSnapshot) i.next()).getValue();
            Log.d("PARK", chat_user);
            chat_phoneNumber = (String) ((DataSnapshot) i.next()).getValue();
            Log.d("PARK", chat_phoneNumber);
            chat_room = (String) ((DataSnapshot) i.next()).getValue();
            Log.d("PARK", chat_room);
        }
        if (chat_function.equals("dial_accept")) {
            if (MyService.deaf) {
                finish();
                reference.removeEventListener(childEventListener);
                Intent intent = new Intent(getApplicationContext(), ChatActivity_mode3_deaf.class);
                // intent.putExtra("received", received);
                // intent.putExtra("sender", sender);
                startActivity(intent);
            } else {
                finish();
                reference.removeEventListener(childEventListener);
                Intent intent = new Intent(getApplicationContext(), ChatActivity_mode3_nondeaf.class);
                //intent.putExtra("received", received);
                //intent.putExtra("sender", sender);
                startActivity(intent);

            }

            //reference.child(chat_databaseReference.split("/")[chat_databaseReference.split("/").length - 1]).removeValue();

        } else if (chat_function.equals("dial_refuse")) {
            FirebaseDatabase.getInstance().getReference("통화").child(chat_user_name + "&" + KakaoTalkFriendListActivity.chat_target_name).removeValue();
            reference.child(chat_databaseReference.split("/")[chat_databaseReference.split("/").length - 1]).removeValue();
            finish();
            reference.removeEventListener(childEventListener);


        } else if (chat_function.equals("dial_cancel")) {
            Log.d("dialselect", chat_function);
            mediaPlayer.stop();
            mediaPlayer.reset();
            vibrator.cancel();
            reference.child(chat_databaseReference.split("/")[chat_databaseReference.split("/").length - 1]).removeValue();
            finish();
            reference.removeEventListener(childEventListener);
        }

    }

    private void sendNotification(String target, String senderName) {
        Log.d("TAG", "targetname:" + target);
        JSONObject json = new JSONObject();
        try {
            json.put("to", "/topics/" + target);
            JSONObject notificationObj = new JSONObject();
            notificationObj.put("title", "부재중 알림");
            notificationObj.put("body", senderName + "님께서 통화를 요청했습니다.");
            //notificationObj.put("clickAction", "ModeActivity");
            json.put("notification", notificationObj);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, json, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("TAG", "onResponse: ");
                    Log.d("TAG", response.toString());
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("TAG", "onError: " + error.networkResponse);
                }
            }
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> header = new HashMap<>();
                    header.put("content-type", "application/json");
                    header.put("authorization", "key=AIzaSyALE16PJpPxq3ZUCb-WpefCzE4bvv3jFxU");
                    return header;
                }
            };
            ModeActivity.mRequestQue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void FindAddressClick_received(String id) {

        FirebaseDatabase.getInstance().getReference("유저목록").orderByPriority().addListenerForSingleValueEvent(new ValueEventListener() {

            String temp;
            String deaf;
            String nickname;
            String userId;
            String phoneNumber;

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

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

                        if (userId.equals(id)) {
                            temp = nickname + "&" + phoneNumber + "&" + deaf;
                            info[0] = temp;
                            // textView.setText(info[0].split("&")[1]+"\n"+info[0].split("&")[0]);
                            textView.setText(info[0].split("&")[0] + (deaf = deaf.equals("deaf") ? "(농인)" : "(비농인)"));
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
    }

    private void FindAddressClick_send(String id) {


        FirebaseDatabase.getInstance().getReference("유저목록").orderByPriority().addListenerForSingleValueEvent(new ValueEventListener() {

            String temp;
            String deaf;
            String nickname;
            String userId;
            String phoneNumber;


            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

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

                        if (userId.equals(id)) {
                            temp = nickname + "&" + phoneNumber + "&" + deaf;
                            info[0] = temp;
                            //target_name.setText(info[0].split("&")[1]+"\n"+info[0].split("&")[0]);
                            target_name.setText(info[0].split("&")[0] + chat_isdeaf);
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
    }
}
