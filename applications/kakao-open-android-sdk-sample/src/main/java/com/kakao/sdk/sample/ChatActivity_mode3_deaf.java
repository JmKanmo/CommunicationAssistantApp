package com.kakao.sdk.sample;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kakao.sdk.sample.common.ChatData;

import java.util.Iterator;
import java.util.Vector;

public class ChatActivity_mode3_deaf extends AppCompatActivity implements TextView.OnEditorActionListener {

    String[] info = new String[1];

    String[] perMissionList = {Manifest.permission.RECORD_AUDIO, Manifest.permission.INTERNET};

    Vector<String> tts_text = new Vector<String>();
    Vector<String> stt_text = new Vector<String>();
    //layout
    final Context context = this;
    Intent i;
    ImageView exit_Btn;
    ImageView tts_EditBtn;
    ScrollView tts_ScrollView;
    ScrollView stt_ScrollView;
    LinearLayout tts_LinearLayout;
    LinearLayout stt_LinearLayout;
    LinearLayout.LayoutParams params;
    TextView tts_TextView;
    TextView stt_TextView;
    TextView target_name;


    EditText ttsEdit;
    int n1; // 말풍선
    int n2;
    private String chat_user_name = FirebaseAuth.getInstance().getCurrentUser().getEmail().split("@")[0];
    private DatabaseReference reference;
    private String chat_room;
    private String chat_user;
    private String chat_message;
    private String chat_phoneNumber;
    private String chat_function;
    private String chat_databaseReference;

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
        setContentView(R.layout.activity_profile_dial_deaf);

        if (ChatActivity_mode3_select.received) {
            //전화를 받음
            reference = FirebaseDatabase.getInstance().getReference("통화").child(ChatActivity_mode3_select.sender + "&" + chat_user_name);
            reference.addChildEventListener(childEventListener);
            target_name = findViewById(R.id.target_name);
            FindAddressClick(ChatActivity_mode3_select.sender);
            ChatData chatData = new ChatData();
            String temp = reference.push().toString();
            DatabaseReference ref = reference.child(temp.split("/")[temp.split("/").length - 1]);
            chatData.setMsg("");
            chatData.setFuntcion("dial_start");
            chatData.setUser_name("");
            chatData.setUser_phoneNumber("");
            chatData.setUser_room("");
            chatData.setDatabaseReference(temp);
            ref.setValue(chatData);
        } else {
            //전화를 걸음
            reference = FirebaseDatabase.getInstance().getReference("통화").child(chat_user_name + "&" + KakaoTalkFriendListActivity.chat_target_name);
            reference.addChildEventListener(childEventListener);
            target_name = findViewById(R.id.target_name);
            FindAddressClick(KakaoTalkFriendListActivity.chat_target_name);
            ChatData chatData = new ChatData();
            String temp = reference.push().toString();
            DatabaseReference ref = reference.child(temp.split("/")[temp.split("/").length - 1]);
            chatData.setMsg("");
            chatData.setFuntcion("dial_start");
            chatData.setUser_name("");
            chatData.setUser_phoneNumber("");
            chatData.setUser_room("");
            chatData.setDatabaseReference(temp);
            ref.setValue(chatData);
        }


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        for (String permission : perMissionList) {
            int check = checkCallingOrSelfPermission(permission);
            if (check == PackageManager.PERMISSION_DENIED) {
                requestPermissions(perMissionList, 0);
            }
        }
        exit_Btn = findViewById(R.id.exit_btn);
        tts_EditBtn = findViewById(R.id.tts_Edit);
        tts_ScrollView = findViewById(R.id.tts_ScrollView);
        stt_ScrollView = findViewById(R.id.stt_ScrollView);
        tts_LinearLayout = findViewById(R.id.tts_linearlayout);
        stt_LinearLayout = findViewById(R.id.stt_linearlayout);
        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        params.topMargin = Math.round(17 * dm.density);
        i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

        //나가기버튼 클릭이벤트
        exit_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatData chatData = new ChatData();
                String temp = reference.push().toString();
                DatabaseReference ref = reference.child(temp.split("/")[temp.split("/").length - 1]);
                chatData.setMsg("");
                chatData.setFuntcion("exit");
                chatData.setUser_name(chat_user_name);
                chatData.setUser_phoneNumber(MainActivity.PhoneNumber);
                chatData.setUser_room("");
                chatData.setDatabaseReference(temp);
                ref.setValue(chatData);
                ref.removeValue();
                finish();
            }
        });
        // 메뉴 버튼 클릭 이벤트
        tts_EditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder ad = new AlertDialog.Builder(context);
                ad.setTitle("하고싶은 말을 입력하세요"); // 제목 설정
                ttsEdit = new EditText(context);
                ttsEdit.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                ad.setView(ttsEdit);
                ttsEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            InputMethodManager inputMgr = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                        }
                    }
                });
                // 취소 버튼 설정
                ad.setPositiveButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();     //닫기
                    }
                });

                // 말하기 버튼 설정
                ad.setNegativeButton("말하기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String msg = ttsEdit.getText().toString();
                        if (msg == null || msg.isEmpty() == true || spaceCheck(msg)) {
                        } else {
                            ChatData chatData = new ChatData();
                            String temp = reference.push().toString();
                            DatabaseReference ref = reference.child(temp.split("/")[temp.split("/").length - 1]);
                            chatData.setMsg(msg);
                            chatData.setFuntcion("tts_speak");
                            chatData.setUser_name(chat_user_name);
                            chatData.setUser_phoneNumber(MainActivity.PhoneNumber);
                            chatData.setUser_room("");
                            chatData.setDatabaseReference(temp);
                            ref.setValue(chatData);
                            ref.removeValue();
                        }
                    }
                });
                ad.show();
            }
        });
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    finish();
                }
            }
        }
    }

    private boolean spaceCheck(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) != ' ') {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        ChatData chatData = new ChatData();
        String temp = reference.push().toString();
        DatabaseReference ref = reference.child(temp.split("/")[temp.split("/").length - 1]);
        chatData.setMsg("");
        chatData.setFuntcion("exit");
        chatData.setUser_name(chat_user_name);
        chatData.setUser_phoneNumber(MainActivity.PhoneNumber);
        chatData.setUser_room("");
        chatData.setDatabaseReference(temp);
        ref.setValue(chatData);
        ref.removeValue();
        finish();
        super.onDestroy();
        reference.removeEventListener(childEventListener);
        reference.removeValue();
    }

    @Override
    public void onBackPressed() {
        ChatData chatData = new ChatData();
        String temp = reference.push().toString();
        DatabaseReference ref = reference.child(temp.split("/")[temp.split("/").length - 1]);
        chatData.setMsg("");
        chatData.setFuntcion("exit");
        chatData.setUser_name(chat_user_name);
        chatData.setUser_phoneNumber(MainActivity.PhoneNumber);
        chatData.setUser_room("");
        chatData.setDatabaseReference(temp);
        ref.setValue(chatData);
        ref.removeValue();
        finish();
        super.onBackPressed();
    }

    private void chatConversation(DataSnapshot dataSnapshot) {
        Iterator i = dataSnapshot.getChildren().iterator();

        while (i.hasNext()) {
            chat_databaseReference = (String) ((DataSnapshot) i.next()).getValue();
            chat_function = (String) ((DataSnapshot) i.next()).getValue();
            chat_message = (String) ((DataSnapshot) i.next()).getValue();
            chat_user = (String) ((DataSnapshot) i.next()).getValue();
            chat_phoneNumber = (String) ((DataSnapshot) i.next()).getValue();
            chat_room = (String) ((DataSnapshot) i.next()).getValue();
        }

        if (chat_function != null) {
            if (chat_function.equals("tts_text")) {
                tts_TextView = new TextView(context);
                tts_TextView.setText(chat_message);
                tts_TextView.setBackgroundResource(R.drawable.chatbubble);
                tts_TextView.setId(++n1);
                tts_TextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20f);
                tts_TextView.setLayoutParams(params);
                tts_LinearLayout.addView(tts_TextView);
                tts_TextView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        tts_ScrollView.scrollTo(0, tts_ScrollView.getChildAt(0).getBottom());
                    }
                });
                tts_text.add(chat_databaseReference);
            }


            if (chat_function.equals("stt")) {
                stt_TextView = new TextView(context);
                stt_TextView.setTextSize(25);
                stt_TextView.setText(chat_message);
                stt_TextView.setId(++n2);
                stt_TextView.setLayoutParams(params);
                stt_LinearLayout.addView(stt_TextView);
                stt_TextView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        stt_ScrollView.scrollTo(0, stt_ScrollView.getChildAt(0).getBottom());
                    }
                });
                stt_text.add(chat_databaseReference);
            }

            if (!chat_user_name.equals(chat_user)) {
                if (chat_function.equals("exit")) {
                    finish();
                }
            }
        }
    }

    private void FindAddressClick(String id) {

        FirebaseDatabase.getInstance().getReference("유저목록").orderByPriority().addListenerForSingleValueEvent(new ValueEventListener() {
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
                            info[0] = nickname + "&" + phoneNumber + "&" + deaf;
                            target_name.setText(info[0].split("&")[0] + (deaf.equals("deaf") ? "(농인)" : "(비농인)"));
                            return;
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
