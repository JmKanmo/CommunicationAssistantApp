package com.kakao.sdk.sample;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Iterator;

public class MyService extends Service {

    static boolean deaf = false;
    static ArrayList<String> room = new ArrayList<String>();
    static ArrayAdapter<String> arrayAdapter;
    public static boolean network = false;

    private IBinder mIBinder = new MyBinder();

    private String chat_room;
    private String chat_user;
    private String chat_message;
    private String chat_phoneNumber;
    private String chat_function;
    private String chat_databaseReference;

    private DatabaseReference reference1;
    private DatabaseReference reference2;
    private ChildEventListener childEventListener1 = new ChildEventListener() {
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

    private ChildEventListener childEventListener2 = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            user_list(dataSnapshot);
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

    TestReceiver mReceiver;

    class MyBinder extends Binder {
        MyService getService() {
            return MyService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("MyService", "onBind()");
        return mIBinder;
    }

    @Override
    public void onCreate() {


        startForeground(1, new Notification());
        super.onCreate();

        Log.d("프로세스","마이서비스실행");


        IntentFilter intentfilter = new IntentFilter();
        // "com.dwfox.myapplication.SEND_BROAD_CAST"
        intentfilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        //동적 리시버 구현
        mReceiver = new TestReceiver() {
            public void onRecevie(Context context, Intent intent) {
                String sendString = intent.getStringExtra("sendString");
                Log.d("박관호", sendString);
            }
        }; //Receiver 등록
        registerReceiver(mReceiver, intentfilter);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);

            Toast.makeText(this, "회원가입이 필요합니다.", Toast.LENGTH_LONG).show();
        } else {

            String chat_user_name = FirebaseAuth.getInstance().getCurrentUser().getEmail().split("@")[0];
            reference1 = FirebaseDatabase.getInstance().getReference(chat_user_name).child("dial");
            reference1.addChildEventListener(childEventListener1);

            room.clear();
            arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, room);

            reference2 = FirebaseDatabase.getInstance().getReference("유저목록");
            reference2.addChildEventListener(childEventListener2);
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("MyService", "onStartCommand()");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.e("MyService", "onDestroy()");
        super.onDestroy();
        stopForeground(true);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e("MyService", "onUnbind()");
        return super.onUnbind(intent);
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
            chat_phoneNumber  = (String) ((DataSnapshot) i.next()).getValue();
            Log.d("PARK", chat_room);
            chat_room = (String) ((DataSnapshot) i.next()).getValue();
            Log.d("PARK", chat_room);
        }
        if (chat_function.equals("dial_request")) {

            Log.d("DIAL", "액티비티...");
            Intent intent = new Intent(getApplicationContext(), ChatActivity_mode3_select.class);
            intent.putExtra("received", true);
            intent.putExtra("sender", chat_user);
            reference1.child(chat_databaseReference.split("/")[chat_databaseReference.split("/").length - 1]).removeValue();
            startActivity(intent);
        }
    }

    private void user_list(DataSnapshot dataSnapshot) {
        Iterator i = dataSnapshot.getChildren().iterator();
        while (i.hasNext()) {
            chat_databaseReference = (String) ((DataSnapshot) i.next()).getValue();
            chat_function = (String) ((DataSnapshot) i.next()).getValue();
            chat_message = (String) ((DataSnapshot) i.next()).getValue();
            chat_user = (String) ((DataSnapshot) i.next()).getValue();
            chat_phoneNumber = (String) ((DataSnapshot) i.next()).getValue();
            chat_room = (String) ((DataSnapshot) i.next()).getValue();
        }
        if (chat_function.equals("regist")) {


            Log.d("DIAL","목록뜨냐?");

            if (chat_user.equals(FirebaseAuth.getInstance().getCurrentUser().getEmail().split("@")[0])) {
                room.add("Me");
                Log.e("MyService", chat_user);
                if (chat_message.equals("deaf")) {
                    deaf = true;
                } else {
                    deaf = false;
                }

            } else {
                room.add(chat_user);
            }
            arrayAdapter.notifyDataSetChanged();
        }
    }
}

class TestReceiver extends BroadcastReceiver {
    private static final String TAG = TestReceiver.class.getSimpleName();

    public void onReceive(Context context, Intent intent) {
        Log.d("네트워크","네트워크상황출력");

        String name = intent.getAction(); // Intent SendBroadCast로 보낸 action TAG 이름으로 필요한 방송을 찾는다.
        if (name.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();

            if (activeNetInfo != null) {
                Log.d("TAG", "데이터연결됏음");
                MyService.network = true;
            }

            if (activeNetInfo == null) {
                Log.d("TAG", "데이터 연결안됏음");
                MyService.network = false;
            }
        }
    }
}