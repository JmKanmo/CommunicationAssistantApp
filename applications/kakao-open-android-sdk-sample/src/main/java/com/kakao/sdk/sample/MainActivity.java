package com.kakao.sdk.sample;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kakao.auth.AccessTokenCallback;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.sdk.sample.common.BaseActivity;
import com.kakao.sdk.sample.common.ChatData;
import com.kakao.sdk.sample.common.widget.KakaoToast;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.log.Logger;

import java.security.MessageDigest;


public class MainActivity extends BaseActivity implements TextView.OnEditorActionListener {

    public static String PhoneNumber = "";
    private Long kakao_id;
    private String phoneNumber;
    public static String nickname;
    private boolean kakao = false;
    boolean next = false;
    final Context context = this;


    String[] perMissionList = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS
    };

    //define view objects

    ProgressDialog progressDialog;
    //define firebase object
    FirebaseAuth firebaseAuth;
    private String password = "";

    ChatData chatData = new ChatData();
    private DatabaseReference reference = FirebaseDatabase.getInstance().getReference("유저목록");
    private Session_Callback callback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d("프로세스", "메인엑티비티실행");
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        //initializing views
        callback = new Session_Callback();
        Session.getCurrentSession().addCallback(callback);
        
/*
        if (i!=null) {
            if (!i.getExtras().isEmpty()) {
                Log.d("TAG", i.getStringExtra("email") + " " + i.getStringExtra("nickname") + " " +
                        i.getStringExtra("phoneNumber"));
            }
        }
*/
        if (getIntent().getExtras() != null) {
            Log.d("프로세스", "인텐트를 넘겨받음");
            kakao_id = (Long) getIntent().getExtras().get("id");
            Log.d("TAG", Long.toString(kakao_id));
            phoneNumber = (String) getIntent().getExtras().get("phoneNumber");
            PhoneNumber = phoneNumber;
            nickname = (String) getIntent().getExtras().get("nickname");
            Log.d("TAG", nickname);
            if (PhoneNumber.startsWith("+82")) {
                PhoneNumber = PhoneNumber.replace("+82", "0");
            }
            phoneNumber.replaceAll("-", "");
            Log.d("TAG", PhoneNumber);
            kakao = true;
            setPhoneNumber();
            registerUser();
        } else {
            Log.d("프로세스", "인텐트를 넘겨받지 않음");

        }

        for (String permission : perMissionList) {
            int check = checkCallingOrSelfPermission(permission);

            if (check == PackageManager.PERMISSION_DENIED) {
                requestPermissions(perMissionList, 0);
            }
        }

        //initializig firebase auth object

        if (firebaseAuth.getCurrentUser() != null) {
            Log.d("프로세스", "이미파이어베이스에 존재함");
            //이미 로그인 되었다면 이 액티비티를 종료함
            finish();
            //그리고 profile 액티비티를 연다.
            startActivity(new Intent(getApplicationContext(), ModeActivity.class)); //추가해 줄 ProfileActivity
        } else {

            Log.d("프로세스", "파이어베이스에 존재하지않음");
        }
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

    //Firebse creating a new user
    private void registerUser() {

        //사용자가 입력하는 email, password를 가져온다.

        password = "koreatech";

        if (isNetworkConnected() != true) {
            Toast.makeText(this, "모바일네트워크 연결바람", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("등록 중입니다...");
        progressDialog.show();

        //creating a new user
        firebaseAuth.createUserWithEmailAndPassword(Long.toString(kakao_id) + "@naver.com", password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                            builder1.setTitle("Q&A");

                            builder1.setMessage("당신은 농인이신가요?").setCancelable(false).setNegativeButton("예", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    next = true;
                                    dialog.dismiss();
                                    String temp = reference.push().toString();
                                    DatabaseReference ref = reference.child(temp.split("/")[temp.split("/").length - 1]);
                                    chatData.setFuntcion("regist");
                                    chatData.setUser_name(Long.toString(kakao_id));
                                    chatData.setMsg("deaf");
                                    chatData.setUser_phoneNumber(PhoneNumber.replaceAll(" ", ""));
                                    chatData.setUser_room(nickname);
                                    chatData.setDatabaseReference(temp);
                                    ref.setValue(chatData);
                                    finish();
                                    startActivity(new Intent(getApplicationContext(), ModeActivity.class));
                                }
                            }).setPositiveButton("아니요", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    next = true;
                                    dialog.dismiss();
                                    String temp = reference.push().toString();
                                    DatabaseReference ref = reference.child(temp.split("/")[temp.split("/").length - 1]);
                                    chatData.setFuntcion("regist");
                                    chatData.setUser_name(Long.toString(kakao_id));
                                    chatData.setMsg("nondeaf");
                                    chatData.setUser_phoneNumber(PhoneNumber.replaceAll(" ", ""));
                                    chatData.setUser_room(nickname);
                                    chatData.setDatabaseReference(temp);
                                    ref.setValue(chatData);
                                    finish();
                                    startActivity(new Intent(getApplicationContext(), ModeActivity.class));
                                    dialog.cancel();
                                }
                            });
                            AlertDialog alertDialog2 = builder1.create();
                            alertDialog2.show();
                        } else {
                            LoginUser();
                        }
                        progressDialog.dismiss();
                    }
                });
    }

    private void LoginUser() {
        String id = Long.toString(kakao_id) + "@naver.com";
        String password = "koreatech";
        firebaseAuth.signInWithEmailAndPassword(id, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // 로그인 성공

                            startActivity(new Intent(getApplicationContext(), ModeActivity.class));
                            finish();
                        } else {
                            // 로그인 실패
                            KakaoToast.makeToast(context, "로그인실패\n등록되지않은 계정입니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        return false;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo == null ? false : true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Session.getCurrentSession().removeCallback(callback);
    }

    public class Session_Callback implements ISessionCallback {

        @Override
        public void onSessionOpened() {
            redirectSignupActivity();
            Log.d("Kakao", "이게 열림?");
        }

        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            if (exception != null) {
                Logger.e(exception);
                Log.d("Kakao", "MainActivity에서");
                if (AccessTokenCallback.flag) {
                    AccessTokenCallback.flag = false;
                    Uri uri = Uri.parse("https://developers.kakao.com/user/sample-app");
                    getHashKey(getApplicationContext());
                    Intent it = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(it);
                }
            }
        }
    }

    public String getHashKey(Context context) {
        final String TAG = "Kakao";
        String keyHash = null;

        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);

            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                keyHash = new String(Base64.encode(md.digest(), 0));
                final ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                clipboardManager.setText(keyHash);
                Log.d("Kakao", keyHash);
            }
        } catch (Exception e) {
            Log.e("name not found", e.toString());
        }

        if (keyHash != null) {
            return keyHash;
        } else {
            return null;
        }
    }

    public void setPhoneNumber() {
        TelephonyManager telManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
        }
        if (!kakao) {
            PhoneNumber = telManager.getLine1Number();
        } else {
            phoneNumber = PhoneNumber;
        }
        if (PhoneNumber.startsWith("+82")) {
            PhoneNumber = PhoneNumber.replace("+82", "0");
        }
    }
}
