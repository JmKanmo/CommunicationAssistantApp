package com.kakao.sdk.sample;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.telephony.gsm.SmsManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kakao.auth.common.MessageSendable;
import com.kakao.friends.response.model.FriendInfo;
import com.kakao.kakaotalk.callback.TalkResponseCallback;
import com.kakao.kakaotalk.response.KakaoTalkProfile;
import com.kakao.kakaotalk.v2.KakaoTalkService;
import com.kakao.network.ErrorResult;
import com.kakao.sdk.sample.common.ChatData;
import com.kakao.sdk.sample.common.GlobalApplication;
import com.kakao.sdk.sample.common.log.Logger;
import com.kakao.sdk.sample.common.widget.KakaoToast;
import com.kakao.sdk.sample.kakaotalk.KakaoTalkMessageBuilder;
import com.kakao.sdk.sample.kakaotalk.TalkMessageHelper;
import com.kakao.usermgmt.response.model.UserProfile;

import java.util.Vector;

/**
 * @author leo.shin
 */
public class KakaoTalkFriendListActivity extends FriendsMainActivity {

    private String chat_user_name = FirebaseAuth.getInstance().getCurrentUser().getEmail().split("@")[0];
    static String chat_target_name;
    private String myName = "";
    final Context context = this;
    InputMethodManager imm;
    boolean is_calling = false;

    private DatabaseReference user_reference = FirebaseDatabase.getInstance().getReference("통화");
    private ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.getValue() != null) {
                get_kakao_info(dataSnapshot);
            } else {
                Log.d("TATA", "널이다.");
                is_calling = false;
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        Log.d("프로세스", "KakaoTalkFriendListActivity실행");

        final UserProfile userProfile = UserProfile.loadFromCache();
        if (userProfile != null) {
            View headerView = getLayoutInflater().inflate(R.layout.view_friend_item, list, false);
            NetworkImageView profileView = headerView.findViewById(R.id.profile_image);
            profileView.setDefaultImageResId(R.drawable.thumb_story);
            profileView.setErrorImageResId(R.drawable.thumb_story);
            TextView nickNameView = headerView.findViewById(R.id.nickname);

            String profileUrl = userProfile.getThumbnailImagePath();
            Application app = GlobalApplication.getGlobalApplicationContext();
            if (profileUrl != null && profileUrl.length() > 0) {
                profileView.setImageUrl(profileUrl, ((GlobalApplication) app).getImageLoader());
            } else {
                profileView.setImageResource(R.drawable.thumb_story);
            }
            setUserProfile(profileView, nickNameView);
            list.addHeaderView(headerView);

            headerView.setOnClickListener(v -> {
                KakaoTalkMessageBuilder builder = new KakaoTalkMessageBuilder();
                builder.addParam("username", userProfile.getNickname());
                builder.addParam("labelMsg", "Hi " + userProfile.getNickname() + ". this is test message");
                // 나 자신을 클릭할때..
            });
        }
    }

    @Override
    public void onItemSelected(final int position, final FriendInfo friendInfo) {
        if (!friendInfo.isAllowedMsg()) {
            return;
        }
        // 친구목록에 포함된 친구들을 클릭할때..

        if (friendInfo.getIsdeaf().isEmpty() && friendInfo.getId() == 0) {
            TalkMessageHelper.showSendMessageDialog(this, (dialog, which) -> {
                MSG_TYPE type = MSG_TYPE.valueOf(2);
                // 카카오링크 메시지보내기
                requestInviteMessage(friendInfo, myName);
            });

        } else {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
            builder1.setMessage("통화를 원하십니까?");

            builder1.setNegativeButton("예", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    user_reference.addValueEventListener(valueEventListener);
                    chat_target_name = Long.toString(friendInfo.getId());
                    if (!is_calling) {
                        dialFunc(chat_user_name, chat_target_name, friendInfo);
                    } else {
                        KakaoToast.makeToast(getApplicationContext(), "상대방이 통화중입니다.", Toast.LENGTH_SHORT).show();

                    }


                }
            });
            builder1.setPositiveButton("아니요", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder1.show();
        }
    }


    private void setUserProfile(NetworkImageView profileView, TextView nickNameView) {

        KakaoTalkService.getInstance().requestProfile(new TalkResponseCallback<KakaoTalkProfile>() {
            @Override
            public void onNotKakaoTalkUser() {
                KakaoToast.makeToast(getApplicationContext(), "not a KakaoTalk user", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                redirectLoginActivity();
            }

            @Override
            public void onNotSignedUp() {
                KakaoToast.makeToast(getApplicationContext(), "onNotSignedUp : " + "User Not Registed App", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(KakaoTalkProfile result) {
                profileView.setImageUrl(result.getThumbnailUrl(), ((GlobalApplication) GlobalApplication.getGlobalApplicationContext()).getImageLoader());
                nickNameView.setText((myName = result.getNickName()) + "(나)");
            }
        });
    }


    private KakaoTalkMessageBuilder makeMessageBuilder(MSG_TYPE type, String nickName) {

        Log.d("프로세스", "KakaoTalkFriendListActivity_메소드makeMessageBuilder");

        KakaoTalkMessageBuilder builder = new KakaoTalkMessageBuilder();

        if (type == MSG_TYPE.FEED) {
            builder.addParam("username", nickName);
            builder.addParam("labelMsg", "Hi " + nickName + ". this is test message");
        }
        return builder;
    }

    private void requestSendMessage(MSG_TYPE type, MessageSendable friendInfo, KakaoTalkMessageBuilder builder) {

        KakaoTalkService.getInstance().requestSendMessage(new TalkResponseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Logger.d("++ send message result : " + result);
                KakaoToast.makeToast(getApplicationContext(), "메시지를 보냈습니다.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNotKakaoTalkUser() {
                KakaoToast.makeToast(getApplicationContext(), "not a KakaoTalk user", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(ErrorResult errorResult) {
                KakaoToast.makeToast(getApplicationContext(), "failure : " + errorResult, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                redirectLoginActivity();
            }

            @Override
            public void onNotSignedUp() {
                KakaoToast.makeToast(getApplicationContext(), "onNotSignedUp : " + "User Not Registed App", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onDidStart() {
                showWaitingDialog();
            }

            @Override
            public void onDidEnd() {
                cancelWaitingDialog();
            }
        }, friendInfo, TalkMessageHelper.getSampleTemplateId(type), builder.build());
    }


    private void requestSendMemo(KakaoTalkMessageBuilder builder) {
        MSG_TYPE type = MSG_TYPE.valueOf(msgType.getSelectedItemPosition());
        if (type == MSG_TYPE.DEFAULT) {
            requestDefaultMemo();
            return;
        }

        KakaoTalkService.getInstance().requestSendMemo(new TalkResponseCallback<Boolean>() {
            @Override
            public void onNotKakaoTalkUser() {
                KakaoToast.makeToast(getApplicationContext(), "not a KakaoTalk user", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(ErrorResult errorResult) {
                KakaoToast.makeToast(getApplicationContext(), "failure : " + errorResult, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                redirectLoginActivity();
            }

            @Override
            public void onNotSignedUp() {
                KakaoToast.makeToast(getApplicationContext(), "onNotSignedUp : " + "User Not Registed App", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(Boolean result) {
                KakaoToast.makeToast(getApplicationContext(), "Send message success", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onDidStart() {
                showWaitingDialog();
            }

            @Override
            public void onDidEnd() {
                cancelWaitingDialog();
            }
        }, TalkMessageHelper.getSampleTemplateId(type), builder.build());
    }

    private void dialFunc(String myid, String targetid, FriendInfo friendInfo) {

        FirebaseDatabase.getInstance().getReference("유저목록").orderByPriority().addListenerForSingleValueEvent(new ValueEventListener() {
            String deaf;
            String userId;
            String phoneNumber;

            String myinfo;
            String targetinfo;

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (String str : dataSnapshot.getValue().toString().split(",")) {

                    if (str.split("=")[0].equals(" msg")) {
                        deaf = str.split("=")[1];
                    }

                    if (str.split("=")[0].equals(" user_name")) {
                        userId = str.split("=")[1];
                    }

                    if (str.split("=")[0].equals(" user_phoneNumber")) {
                        phoneNumber = str.split("=")[1];
                    }

                    if (str.split("=")[0].equals(" function")) {

                        if (userId.equals(myid)) {
                            myinfo = deaf;
                        }

                        if (userId.equals(targetid)) {
                            targetinfo = deaf + "&" + phoneNumber;
                        }
                        deaf = "";
                        userId = "";
                        phoneNumber = "";
                    }
                }
                if (myinfo.equals("nondeaf") && targetinfo.split("&")[0].equals("nondeaf")) {
                    final Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + targetinfo.split("&")[1]));
                    if (dialIntent.resolveActivity(context.getPackageManager()) != null) {
                        // put your logic to launch call app here
                        startActivity(dialIntent);
                    }
                } else if (myinfo.equals("deaf") && targetinfo.split("&")[0].equals("deaf")) {
                    //농인 - 농인 메시지전송

                    AlertDialog.Builder ad = new AlertDialog.Builder(context);
                    ad.setTitle("메시지를 입력하세요:)"); // 제목 설정

                    final EditText ttsEdit = new EditText(context);
                    ttsEdit.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                    // EditText 삽입하기
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
                            imm.hideSoftInputFromWindow(ttsEdit.getWindowToken(), 0);
                            dialog.dismiss();     //닫기
                        }
                    });

                    // 말하기 버튼 설정
                    ad.setNeutralButton("문자전송", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            //입력한 값을 가져와 변수에 담는다
                            String phoneNo = targetinfo.split("&")[1];
                            String sms = ttsEdit.getText().toString();

                            try {
                                //전송
                                SmsManager smsManager = SmsManager.getDefault();
                                smsManager.sendTextMessage(phoneNo, null, sms, null, null);
                                Toast.makeText(getApplicationContext(), "전송 완료!", Toast.LENGTH_LONG).show();
                                imm.hideSoftInputFromWindow(ttsEdit.getWindowToken(), 0);
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), "SMS faild, please try again later!", Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        }
                        //ttsEdit.setText(null);
                    });
                    // 창 띄우기
                    ad.show();
                } else {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference(chat_target_name).child("dial");
                    ChatData chatData = new ChatData();
                    String temp = reference.push().toString();
                    DatabaseReference ref = reference.child(temp.split("/")[temp.split("/").length - 1]);
                    chatData.setMsg("");
                    chatData.setFuntcion("dial_request");
                    chatData.setUser_name(chat_user_name);
                    chatData.setUser_phoneNumber(MainActivity.PhoneNumber);
                    chatData.setUser_room("");
                    chatData.setDatabaseReference(temp);
                    ref.setValue(chatData);
                    ref.removeValue();


                    FirebaseDatabase.getInstance().getReference("통화").child(chat_user_name + "&" + chat_target_name);
                    chatData.setMsg("");
                    chatData.setFuntcion("dial_request");
                    chatData.setUser_name(chat_user_name);
                    chatData.setUser_phoneNumber(MainActivity.PhoneNumber);
                    chatData.setUser_room("");
                    chatData.setDatabaseReference(temp);
                    FirebaseDatabase.getInstance().getReference("통화").child(chat_user_name + "&" + chat_target_name).setValue(chatData);

                    Intent intent = new Intent(getApplicationContext(), ChatActivity_mode3_select.class);
                    intent.putExtra("received", false);
                    intent.putExtra("isdeaf", friendInfo.getIsdeaf());
                    intent.putExtra("myName", myName);
                    startActivity(intent);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    void get_kakao_info(DataSnapshot dataSnapshot) {
        Vector<String> colling_channel = new Vector();
        for (String str : dataSnapshot.getValue().toString().split(",")) {
            //Log.d("TATA", str);

            if (str.split("=")[0].contains("{")) {
                colling_channel.add(str.split("=")[0].trim().replace("-", "").replace("{", "").split("&")[0].trim());
                colling_channel.add(str.split("=")[0].trim().replace("-", "").replace("{", "").split("&")[1].trim());
                Log.d("TATA", str.split("=")[0].trim().replace("-", "").replace("{", "").split("&")[0].trim());
                Log.d("TATA", str.split("=")[0].trim().replace("-", "").replace("{", "").split("&")[1].trim());
            }
        }
        if (colling_channel.contains(chat_target_name)) {
            is_calling = true;
            Log.d("TATA","통화중");
        } else {
            is_calling = false;
            Log.d("TATA","통화중아님");

        }

        colling_channel.removeAllElements();

    }
}
