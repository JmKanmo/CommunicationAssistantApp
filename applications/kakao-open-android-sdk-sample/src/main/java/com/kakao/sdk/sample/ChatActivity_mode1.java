package com.kakao.sdk.sample;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kakao.sdk.sample.common.ChatData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

public class ChatActivity_mode1 extends Activity implements TextView.OnEditorActionListener {

    boolean mic = false;
    Vector<String> tts_text = new Vector<String>();

    Vector<String> stt_text = new Vector<String>();

    String[] perMissionList = {Manifest.permission.INTERNET};
    final Context context = this;
    ImageView exit_Btn;
    ImageView help_Btn;
    ImageView mic_Btn;
    ImageView tts_Btn;
    ImageView stt_Reset;
    ImageView tts_Reset;
    ImageView tts_EditBtn;
    ScrollView tts_ScrollView;
    ScrollView stt_ScrollView;
    LinearLayout tts_LinearLayout;
    LinearLayout stt_LinearLayout;
    LinearLayout.LayoutParams params;
    EditText ttsEdit;
    TextView tts_TextView;
    TextView stt_TextView;
    int n1; // 말풍선
    int n2;
    private String chat_user_name = FirebaseAuth.getInstance().getCurrentUser().getEmail().split("@")[0];
    private DatabaseReference reference = FirebaseDatabase.getInstance().getReference("일상생활").child(chat_user_name);
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
    private String chat_user;
    private String chat_message;
    private String chat_phonenumber;
    private String chat_room;

    private String chat_function;
    private String chat_databaseReference;
    AudioManager am;
    VolumeObserver volumeObserver;
    static int volume_val = 0, prevolume_val = 0;
    private boolean clickFlag = false, micFlag = false, speakerFlag = false, cntFlag = false;
    private TextToSpeech mTTS;
    int pitch_val = 50, speed_val = 50;
    SeekBar pitch_bar, speed_bar;
    Intent i;
    boolean x = true;
    private SpeechRecognizer speechRecognizer;

    private void speak(String text) {
        am.setStreamVolume(AudioManager.STREAM_MUSIC, 15, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        float pitch = (pitch_bar == null) ? pitch_val / 50 : (float) pitch_bar.getProgress() / 50;
        if (pitch < 0.1) pitch = 0.1f;
        float speed = (speed_bar == null) ? speed_val / 50 : (float) speed_bar.getProgress() / 50;
        if (speed < 0.1) speed = 0.1f;
        mTTS.setPitch(pitch);
        mTTS.setSpeechRate(speed);
        mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);

        if (mic)
            stopListen();
        if (mic) {
            startListen();
        }
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
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    stopListen();
                    startListen();
                    break;
                case SpeechRecognizer.ERROR_AUDIO:
                    stopListen();
                    startListen();
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
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
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    stopListen();
                    startListen();
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    stopListen();
                    startListen();
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    stopListen();
                    startListen();
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    stopListen();
                    startListen();
                    break;
            }
        }

        @Override
        public void onResults(Bundle results) {
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
        am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        speechRecognizer.setRecognitionListener(listener);
        speechRecognizer.startListening(i);
    }

    private void stopListen() {
        if (speechRecognizer != null) {
            speechRecognizer.cancel();
            speechRecognizer.destroy();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        for (String permission : perMissionList) {
            int check = checkCallingOrSelfPermission(permission);
            if (check == PackageManager.PERMISSION_DENIED) {
                requestPermissions(perMissionList, 0);
            }
        }

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
        exit_Btn = findViewById(R.id.exit_btn);
        help_Btn = findViewById(R.id.help_btn);
        tts_EditBtn = findViewById(R.id.tts_Edit);
        tts_Btn = findViewById(R.id.tts_Btn);
        mic_Btn = findViewById(R.id.tts_Btn);
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
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        reference.addChildEventListener(childEventListener);

        exit_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        help_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speak("안녕하십니까 저는 농인입니다. 저를 보고 편하게 말씀해주시면 당신의 음성이 텍스트로 보여집니다. ");
                AlertDialog.Builder builder4 = new AlertDialog.Builder(context);
                builder4.setMessage("1. 하단의 마이크버튼을 클릭하고 말하면 음성이 텍스트로 출력됩니다.\n\n " +"2. 하단의 입력창을 누르고 단어를 입력 후 말하기버튼을 클릭하면 말풍선과 함께 스피커로 단어가 출력됩니다.").setCancelable(false).setNegativeButton("예", new DialogInterface.OnClickListener() {
                });
                AlertDialog alertDialog4 = builder4.create();
                alertDialog4.show();
            }
        });

        // 메뉴 버튼 클릭 이벤트
        tts_EditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.d("TAG", "tts_EditBtn");
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
                            //Log.d("TAGsex","중복실행");
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
        stt_Reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                builder1.setTitle("텍스트 초기화");
                builder1.setMessage("텍스트를 초기화하시겠습니까?").setCancelable(false).setNegativeButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        stt_LinearLayout.removeAllViews();
                        for (int a = 0; a < stt_text.size(); a++) {
                            reference.child(stt_text.get(a).split("/")[stt_text.get(a).split("/").length - 1]).removeValue();
                        }
                        stt_text.clear();
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
        mic_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatData chatData = new ChatData();
                String temp = reference.push().toString();
                DatabaseReference ref = reference.child(temp.split("/")[temp.split("/").length - 1]);
                chatData.setMsg("");
                chatData.setFuntcion("mic_on");
                chatData.setUser_name(chat_user_name);
                chatData.setUser_phoneNumber(MainActivity.PhoneNumber);
                chatData.setUser_room("");
                chatData.setDatabaseReference(temp);
                ref.setValue(chatData);
                ref.removeValue();
            }
        });
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
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        Log.d("TAG_ChatActivity_mode1", "onEditorAction");
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("TAG_ChatActivity_mode1", "onRequestPermissionsResult");
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
    protected void onResume() {
        Log.d("TAG", "mode1onResume");
        mic_Btn.setImageResource(R.drawable.offbtn);
        volume_val = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        volumeObserver = new VolumeObserver(this, new Handler());
        getApplicationContext().getContentResolver().registerContentObserver(Settings.System.CONTENT_URI, true, volumeObserver);
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(listener);
        }
        super.onResume();
    }
    
    @Override
    protected void onStop() {
        am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        if (mic) {
            stopListen();
            mic_Btn.performClick();
        }
        getApplicationContext().getContentResolver().unregisterContentObserver(volumeObserver);
        mTTS.stop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d("TAG_ChatActivity_mode1", "onStop");
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }
        speechRecognizer.destroy();
        reference.removeEventListener(childEventListener);
        super.onDestroy();
    }

    private void chatConversation(DataSnapshot dataSnapshot) {
        Iterator i = dataSnapshot.getChildren().iterator();
        while (i.hasNext()) {
            chat_databaseReference = (String) ((DataSnapshot) i.next()).getValue();
            chat_function = (String) ((DataSnapshot) i.next()).getValue();
            chat_message = (String) ((DataSnapshot) i.next()).getValue();
            chat_user = (String) ((DataSnapshot) i.next()).getValue();
            chat_phonenumber = (String) ((DataSnapshot) i.next()).getValue();
            chat_room = (String) ((DataSnapshot) i.next()).getValue();
        }
        
        if (chat_user_name.equals(chat_user)) {
            if (chat_function.equals("tts_speak")) {
                speak(chat_message);
                reference.child(chat_databaseReference.split("/")[chat_databaseReference.split("/").length - 1]).removeValue();
                ChatData chatData = new ChatData();
                String temp = reference.push().toString();
                DatabaseReference ref = reference.child(temp.split("/")[temp.split("/").length - 1]);
                chatData.setMsg(chat_message);
                chatData.setFuntcion("tts_text");
                chatData.setUser_name(chat_user_name);
                chatData.setUser_phoneNumber(MainActivity.PhoneNumber);
                chatData.setUser_room("");
                chatData.setDatabaseReference(temp);
                ref.setValue(chatData);
            }
         
            if (chat_function.equals("mic_on")) {
                reference.child(chat_databaseReference.split("/")[chat_databaseReference.split("/").length - 1]).removeValue();
                if (clickFlag)
                    clickFlag = false;
                else
                    clickFlag = true;
                
                if (clickFlag) {
                    startListen();
                    mic_Btn.setImageResource(R.drawable.onbtn);
                    prevolume_val = volume_val;
                    final Toast toast = Toast.makeText(context, "Mic on", Toast.LENGTH_SHORT);
                    mic = true;
                    toast.show();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            toast.cancel();
                        }
                    }, 700);
                } else {
                    stopListen();
                    mic_Btn.setImageResource(R.drawable.offbtn);
                    volume_val = prevolume_val;
                    final Toast toast = Toast.makeText(context, "Mic off", Toast.LENGTH_SHORT);
                    mic = false;
                    toast.show();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            toast.cancel();
                        }
                    }, 700);
                }
            }
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
        }
    }
}



