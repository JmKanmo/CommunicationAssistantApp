package com.kakao.sdk.sample;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.Iterator;
import java.util.Vector;

import static com.kakao.sdk.sample.BluetoothChatFragment.chat_user_nickname;
import static com.kakao.sdk.sample.BluetoothChatFragment.mChatService;

public class ChatActivity_mode2 extends Activity implements TextView.OnEditorActionListener {

    public static Activity mode2Activity;

    String[] perMissionList = {Manifest.permission.RECORD_AUDIO, Manifest.permission.INTERNET};

    Vector<String> tts_text = new Vector<String>();
    Vector<String> stt_text = new Vector<String>();
    //layout
    final Context context = this;
    Intent i;
    ImageView exit_Btn;
    ImageView help_Btn;
    ImageView menu_Btn;
    ImageView stt_Reset;
    ImageView tts_Reset;
    ImageView tts_EditBtn;
    ScrollView tts_ScrollView;
    ScrollView stt_ScrollView;
    LinearLayout tts_LinearLayout;
    LinearLayout stt_LinearLayout;
    LinearLayout.LayoutParams params;
    TextView tts_TextView;
    TextView stt_TextView;
    EditText ttsEdit;
    int n1; // 말풍선
    int n2;
    boolean clickFlag;
    private String chat_room_name;
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
        setContentView(R.layout.activity_profile_mode2);

        mode2Activity = ChatActivity_mode2.this;

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
        help_Btn = findViewById(R.id.help_btn);
        tts_EditBtn = findViewById(R.id.tts_Edit);
        stt_Reset = findViewById(R.id.stt_reset);
        tts_Reset = findViewById(R.id.tts_reset);
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

        if (ChatActivity_device.ref != null) {
            chat_room_name = ChatActivity_device.chat_room_name;
            setTitle(ChatActivity_device.chat_room_name + " 채팅방");
            reference = ChatActivity_device.ref;
            reference.addChildEventListener(childEventListener);
            ChatActivity_device.ref = null;

        } else if (BluetoothChatFragment.ref != null) {
            chat_room_name = BluetoothChatFragment.chat_room_name;
            setTitle(BluetoothChatFragment.chat_room_name + " 채팅방");
            reference = BluetoothChatFragment.ref;
            reference.addChildEventListener(childEventListener);
            BluetoothChatFragment.ref = null;

        }
        Log.d("TAG", chat_room_name);

        // 나가기 버튼 클릭 이벤트
        exit_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        help_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder4 = new AlertDialog.Builder(context);
                 builder4.setMessage("1. 하드웨어가 인식한 음성이 텍스트로 출력됩니다.\n\n" + "2. 하단의 입력창을 누르고 입력 후 말하기버튼을 클릭하면 말풍선과 함께 하드웨어 스피커로 출력됩니다.").setCancelable(false).setNegativeButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //finish();
                    }
                });
                AlertDialog alertDialog4 = builder4.create();
                alertDialog4.show();
            }
        });

        // 메뉴 버튼 클릭 이벤트
        tts_EditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder3 = new AlertDialog.Builder(context);
                builder3.setTitle("하고싶은말을입력하세요.");
                ttsEdit = new EditText(context);
                ttsEdit.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                builder3.setView(ttsEdit);

                builder3.setCancelable(true).setNegativeButton("말하기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (MyService.network) {
                            ChatData chatData = new ChatData();
                            String temp = reference.push().toString();
                            DatabaseReference ref = reference.child(temp.split("/")[temp.split("/").length - 1]);
                            chatData.setMsg(ttsEdit.getText().toString());
                            chatData.setFuntcion("tts_speak");
                            chatData.setUser_name(chat_user_nickname);
                            chatData.setUser_phoneNumber("");
                            chatData.setUser_room(chat_room_name);
                            chatData.setDatabaseReference(temp);
                            ref.setValue(chatData);
                            ref.removeValue();

                            chatData = new ChatData();
                            temp = reference.push().toString();
                            ref = reference.child(temp.split("/")[temp.split("/").length - 1]);
                            chatData.setMsg(ttsEdit.getText().toString());
                            chatData.setFuntcion("tts_text");
                            chatData.setUser_name(chat_user_nickname);
                            chatData.setUser_phoneNumber("");
                            chatData.setUser_room(chat_room_name);
                            chatData.setDatabaseReference(temp);
                            ref.setValue(chatData);

                            tts_text.add(chat_databaseReference);
                            tts_TextView = new TextView(context);
                            tts_TextView.setText(ttsEdit.getText().toString());
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
                            tts_text.add(temp);
                        } else {
                            Toast toast = Toast.makeText(context, "네트워크 연결 상태를 확인해주세요.", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                }).setPositiveButton("취소" +
                        "", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog alertDialog3 = builder3.create();
                alertDialog3.show();
            }
        });

        stt_Reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                builder1.setTitle("텍스트 초기화");
                builder1.setMessage("텍스트를 초기화하시겠습니까?").setCancelable(false).setNegativeButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        stt_LinearLayout.removeAllViews();
                        if (BluetoothChatFragment.device_on) {
                            for (int a = 0; a < stt_text.size(); a++) {
                                reference.child(stt_text.get(a).split("/")[stt_text.get(a).split("/").length - 1]).removeValue();
                            }
                            stt_text.clear();
                        }
                    }
                }).setPositiveButton("아니요", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog alertDialog2 = builder1.create();
                alertDialog2.show();
            }
        });
        tts_Reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder2 = new AlertDialog.Builder(context);
                builder2.setTitle("텍스트 초기화");
                builder2.setMessage("텍스트를 초기화하시겠습니까?").setCancelable(false).setNegativeButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tts_LinearLayout.removeAllViews();
                        for (int a = 0; a < tts_text.size(); a++) {

                            reference.child(tts_text.get(a).split("/")[tts_text.get(a).split("/").length - 1]).removeValue();
                        }
                        tts_text.clear();
                    }
                }).setPositiveButton("아니요", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                AlertDialog alertDialog3 = builder2.create();
                alertDialog3.show();
            }
        });
//
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
        Log.d("TAG_ChatActivity_mode2", "onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d("TAG_ChatActivity_mode2", "onResume");
        //mic_Btn.setImageResource(R.drawable.offbtn);
        super.onResume();
    }

    @Override
    protected void onRestart() {
        Log.d("TAG_ChatActivity_mode2", "onRestart");
        super.onRestart();

    }

    @Override
    protected void onPause() {
        Log.d("TAG_ChatActivity_mode2", "onPause");
        super.onPause();

    }

    @Override
    protected void onStop() {
        Log.d("프로세스", "모드2액티비티종료onStop");
        super.onStop();

    }

    protected void onDestroy() {
        Log.d("프로세스", "모드2액티비티종료onDestroy");
        //Log.d("프로세스", "" + mChatService.getState());
        //Log.d("프로세스", "" + BluetoothChatService.STATE_CONNECTED);


        if (mChatService != null) {
            String message = "exit";
            //speak("네트워크 상태가 원활하지 않습니다.");
            byte[] send = message.getBytes();
            if (mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
                mChatService.write(send);
            }
        }

        if (ChatActivity_device.room_exit_ref != null) {
            ChatData chatData = new ChatData();
            String temp = reference.push().toString();
            DatabaseReference ref = reference.child(temp.split("/")[temp.split("/").length - 1]);
            chatData.setMsg("");
            chatData.setFuntcion("master_exit");
            chatData.setUser_name(chat_user_name);
            chatData.setUser_phoneNumber("");
            chatData.setUser_room(chat_room_name);
            chatData.setDatabaseReference(temp);
            ref.setValue(chatData);
            ref.removeValue();
            ChatActivity_device.room_exit_ref.removeValue();
            ChatActivity_device.room_exit_ref = null;

        } else {

            ChatData chatData = new ChatData();
            String temp = reference.push().toString();
            DatabaseReference ref = reference.child(temp.split("/")[temp.split("/").length - 1]);
            chatData.setMsg(BluetoothChatFragment.chat_user_nickname);
            chatData.setFuntcion("방 퇴장");
            chatData.setUser_name(chat_user_name);
            chatData.setUser_phoneNumber("");
            chatData.setUser_room(chat_room_name);
            chatData.setDatabaseReference(temp);
            ref.setValue(chatData);
            ref.removeValue();
        }
        reference.removeEventListener(childEventListener);
        super.onDestroy();

    }

    @Override
    public void onBackPressed() {
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
        if (chat_room_name.equals(chat_room)) {
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
        }
        if (chat_function.equals("master_exit")) {
            AlertDialog.Builder builder4 = new AlertDialog.Builder(context);
            builder4.setMessage("방장이 퇴장했습니다.").setCancelable(false).setNegativeButton("예", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            AlertDialog alertDialog4 = builder4.create();
            alertDialog4.show();
        }
        if (chat_function.equals("방 입장")) {

            Toast toast = Toast.makeText(context, chat_message + "님이 입장했습니다.", Toast.LENGTH_SHORT);
            toast.show();


        }
        if (chat_function.equals("방 퇴장")) {

            Toast toast = Toast.makeText(context, chat_message + "님이 퇴장했습니다.", Toast.LENGTH_SHORT);
            toast.show();

        }

    }
}

