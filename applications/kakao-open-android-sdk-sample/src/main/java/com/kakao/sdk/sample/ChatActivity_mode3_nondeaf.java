package com.kakao.sdk.sample;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kakao.sdk.sample.common.ChatData;
import com.kakao.sdk.sample.common.widget.KakaoToast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

public class ChatActivity_mode3_nondeaf extends AppCompatActivity implements TextView.OnEditorActionListener {
    final String[] info = new String[1];

    Vector<String> tts_text = new Vector<String>();
    Vector<String> stt_text = new Vector<String>();

    private String sender;
    private boolean received;
    ///////////////////////////
    ////////////////////////////
    private String chat_user;
    private String chat_room;
    private String chat_message;
    private String chat_function;
    private String chat_phoneNumber;
    private String chat_databaseReference;
    ////////////////////////////////////
    /////////////////////////////////////

    ImageView exit_btn;
    TextView target_name;
    private String chat_user_name = FirebaseAuth.getInstance().getCurrentUser().getEmail().split("@")[0];

    private DatabaseReference reference;

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

    AudioManager am;
    int volume = 10;
    //tts
    private TextToSpeech mTTS;
    int pitch_val = 50, speed_val = 50;
    SeekBar pitch_bar, speed_bar;
    Intent i;
    boolean x = true;
    private SpeechRecognizer speechRecognizer;

    private void speak(String text) {
        //if (micFlag == true && text != null) mic_Btn.performClick();
        am.setStreamVolume(AudioManager.STREAM_MUSIC, 15, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        float pitch = (pitch_bar == null) ? pitch_val / 50 : (float) pitch_bar.getProgress() / 50;
        if (pitch < 0.1) pitch = 0.1f;
        float speed = (speed_bar == null) ? speed_val / 50 : (float) speed_bar.getProgress() / 50;
        if (speed < 0.1) speed = 0.1f;
        mTTS.setPitch(pitch);
        mTTS.setSpeechRate(speed);
        //if (arrayList.size() > 0)  mTTS.speak(arrayList.get(arrayList.size() - 1), TextToSpeech.QUEUE_FLUSH, null);
        mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        stopListen();
        while (mTTS.isSpeaking()) {
            Log.d("TAG", "말하는중!");
        }
        startListen();
    }

    //stt
    RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
        }

        @Override
        public void onBeginningOfSpeech() {
        }

        @Override
        public void onRmsChanged(float rmsDB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {
        }

        @Override
        public void onEndOfSpeech() {
        }

        @Override
        public void onError(int error) {
            switch (error) {
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    stopListen();
                    startListen();

                    // Log.d("TAG", "ERROR_NETWORK_TIMEOUT");
                    break;

                case SpeechRecognizer.ERROR_NETWORK:
                    stopListen();
                    startListen();

                    // Log.d("TAG", "ERROR_NETWORK");
                    break;

                case SpeechRecognizer.ERROR_AUDIO:
                    stopListen();
                    startListen();

                    //  Log.d("TAG", "ERROR_AUDIO");
                    break;

                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:

                    //   Log.d("TAG", "ERROR_SPEECH_TIMEOUT");
                    try {
                        Thread.sleep(100);
                        stopListen();
                        startListen();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;

                case SpeechRecognizer.ERROR_CLIENT:
                    stopListen();
                    startListen();

                    //      Log.d("TAG", "ERROR_CLIENT");
                    break;

                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    stopListen();
                    startListen();

                    //   Log.d("TAG", "ERROR_INSUFFICIENT_PERMISSIONS");
                    break;

                case SpeechRecognizer.ERROR_NO_MATCH:
                    stopListen();
                    startListen();

                    //  Log.d("TAG", "ERROR_NO_MATCH");

                    break;

                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    stopListen();
                    startListen();
                    // Log.d("TAG", "ERROR_RECOGNIZER_BUSY");

                    break;

                case SpeechRecognizer.ERROR_SERVER:
                    stopListen();
                    startListen();

                    //   Log.d("TAG", "ERROR_SERVER");

                    break;
            }
        }

        @Override
        public void onResults(Bundle results) {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            String key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResults = results.getStringArrayList(key);
            String[] rs = new String[mResults.size()];
            mResults.toArray(rs);
            ChatData chatData = new ChatData();
            String temp = reference.push().toString();
            DatabaseReference ref = reference.child(temp.split("/")[temp.split("/").length - 1]);
            chatData.setMsg(rs[0]);
            chatData.setFuntcion("stt");
            chatData.setUser_name(chat_user_name);
            chatData.setUser_phoneNumber(MainActivity.PhoneNumber);
            chatData.setUser_room("");
            chatData.setDatabaseReference(temp);
            ref.setValue(chatData);
            Log.d("KJM", temp.split("/")[temp.split("/").length - 1]);
            KakaoToast.makeToast(getApplicationContext(), rs[0], Toast.LENGTH_SHORT).show();
            stt_text.add(temp);

            stopListen();
            startListen();
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
        }
    };

    private void startListen() {
        Log.d("TAG", "스타트리슨");
        am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        speechRecognizer.setRecognitionListener(listener);
        speechRecognizer.startListening(i);
    }

    private void stopListen() {

        if (speechRecognizer != null) {
            speechRecognizer.cancel();
            Log.d("TAG", "스탑리슨");
            speechRecognizer.destroy();
        }
    }

    String[] perMissionList = {Manifest.permission.RECORD_AUDIO, Manifest.permission.INTERNET};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("프로세스", "전화모드비농인실행");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_dial_nondeaf);

        if (ChatActivity_mode3_select.received) {
            Log.d("TTT", "받은사람입니다");
            reference = FirebaseDatabase.getInstance().getReference("통화").child(ChatActivity_mode3_select.sender + "&" + chat_user_name);
            Log.d("TTT", FireBaseMessagingService.chat_from_name + "&" + chat_user_name);
            reference.addChildEventListener(childEventListener);
            target_name = findViewById(R.id.target_name);
            FindAddressClick(ChatActivity_mode3_select.sender);
            //target_name.setText(info[0]);
            Log.d("TAG", "target_name: " + target_name.getText());


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
            Log.d("TTT", "보낸사람입니다");
            reference = FirebaseDatabase.getInstance().getReference("통화").child(chat_user_name + "&" + KakaoTalkFriendListActivity.chat_target_name);
            Log.d("TTT", chat_user_name + "&" + KakaoTalkFriendListActivity.chat_target_name);
            reference.addChildEventListener(childEventListener);
            target_name = findViewById(R.id.target_name);
            FindAddressClick(KakaoTalkFriendListActivity.chat_target_name);
            //target_name.setText(info[0]);
            Log.d("TAG", "target_name: " + target_name.getText());


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

        exit_btn = findViewById(R.id.exit_btn);
        exit_btn.setOnClickListener(new View.OnClickListener() {
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

        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTTS.setLanguage(Locale.KOREAN);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    }
                }
            }
        });
        i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(listener);
        startListen();
    }

    @Override

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (volume < 15)
                    volume++;
                return true;

            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (volume > 0)
                    volume--;
                return true;
        }
        return super.onKeyDown(keyCode, event);
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

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        Log.d("TAG", "onEditorAction");
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("TAG", "onRequestPermissionsResult");
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

    @Override
    protected void onStart() {
        Log.d("TAG", "onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d("TAG", "onResume");
        super.onResume();
    }

    @Override
    protected void onRestart() {
        Log.d("TAG", "onRestart");
        super.onRestart();
    }

    @Override
    protected void onPause() {
        Log.d("TAG", "onPause");
        super.onPause();

    }

    @Override
    protected void onStop() {
        Log.d("TAG", "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d("TAG", "onDestroy");

        super.onDestroy();
        mTTS.shutdown();
        speechRecognizer.destroy();
        reference.removeEventListener(childEventListener);

        Log.d("KJM", Integer.toString(stt_text.size()));

        for (int a = 0; a < stt_text.size(); a++) {
            reference.child(stt_text.get(a).split("/")[stt_text.get(a).split("/").length - 1]).removeValue();
        }
        stt_text.clear();

        Log.d("KJM", Integer.toString(tts_text.size()));

        for (int a = 0; a < tts_text.size(); a++) {
            reference.child(tts_text.get(a).split("/")[tts_text.get(a).split("/").length - 1]).removeValue();
        }
        tts_text.clear();
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


        reference.removeValue();


        finish();
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
            if (chat_function.equals("tts_speak")) {
                speak(chat_message);
                reference.child(chat_databaseReference.split("/")[chat_databaseReference.split("/").length - 1]).removeValue();
                ChatData chatData = new ChatData();
                String temp = reference.push().toString();
                DatabaseReference ref = reference.child(temp.split("/")[temp.split("/").length - 1]);
                chatData.setMsg(chat_message);
                chatData.setFuntcion("tts_text");
                chatData.setUser_name(chat_user);
                chatData.setUser_phoneNumber(MainActivity.PhoneNumber);
                chatData.setUser_room("");
                chatData.setDatabaseReference(temp);
                ref.setValue(chatData);
                tts_text.add(temp);
            }

        /*
        if (chat_function.equals("tts_input")) {
            reference.child(chat_databaseReference.split("/")[chat_databaseReference.split("/").length - 1]).removeValue();
            ChatData chatData = new ChatData();
            String temp = reference.push().toString();
            DatabaseReference ref = reference.child(temp.split("/")[temp.split("/").length - 1]);
            chatData.setMsg(chat_message);
            chatData.setFuntcion("tts_text");
            chatData.setUser_name(chat_user);
            chatData.setUser_phoneNumber(chat_user_phoneNumber);
            chatData.setUser_room("");
            chatData.setDatabaseReference(temp);
            ref.setValue(chatData);
        }
        */
            if (!chat_user_name.equals(chat_user)) {
                if (chat_function.equals("exit")) {
                    reference.child(chat_databaseReference.split("/")[chat_databaseReference.split("/").length - 1]).removeValue();
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
                            Log.d("TAG", "변수에 값이 채워질때 " + info[0]);
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
        Log.d("TAG", "함수의 끝" + info[0]);
    }
}



