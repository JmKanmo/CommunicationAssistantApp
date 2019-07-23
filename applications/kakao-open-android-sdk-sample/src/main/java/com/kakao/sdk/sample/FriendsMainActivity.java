package com.kakao.sdk.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kakao.auth.common.MessageSendable;
import com.kakao.friends.FriendContext;
import com.kakao.friends.FriendsService;
import com.kakao.friends.request.FriendsRequest.FriendFilter;
import com.kakao.friends.request.FriendsRequest.FriendOrder;
import com.kakao.friends.request.FriendsRequest.FriendType;
import com.kakao.friends.response.FriendsResponse;
import com.kakao.friends.response.model.FriendInfo;
import com.kakao.kakaotalk.callback.TalkResponseCallback;
import com.kakao.kakaotalk.v2.KakaoTalkService;
import com.kakao.message.template.ButtonObject;
import com.kakao.message.template.ContentObject;
import com.kakao.message.template.LinkObject;
import com.kakao.message.template.ListTemplate;
import com.kakao.network.ErrorResult;
import com.kakao.sdk.sample.FriendsListAdapter.IFriendListCallback;
import com.kakao.sdk.sample.common.BaseActivity;
import com.kakao.sdk.sample.common.log.Logger;
import com.kakao.sdk.sample.common.widget.KakaoDialogSpinner;
import com.kakao.sdk.sample.common.widget.KakaoToast;

import java.util.ArrayList;
import java.util.List;

/**
 * @author leo.shin
 */

public class FriendsMainActivity extends BaseActivity implements OnClickListener, IFriendListCallback {
    public enum MSG_TYPE {
        FEED(0),
        LIST(1),
        DEFAULT(2);

        private final int value;

        MSG_TYPE(int value) {
            this.value = value;
        }

        public static MSG_TYPE valueOf(int i) {
            for (MSG_TYPE type : values()) {
                if (type.getValue() == i) {
                    return type;
                }
            }
            return FEED;
        }

        public int getValue() {
            return value;
        }
    }

    private class FriendsInfo {
        private final List<FriendInfo> friendInfoList = new ArrayList<>();
        private int totalCount;
        private String id;

        public FriendsInfo() {
        }

        public List<FriendInfo> getFriendInfoList() {
            return friendInfoList;
        }

        public void merge(FriendsResponse response) {
            this.id = response.getId();
            this.totalCount = response.getTotalCount();
            this.friendInfoList.addAll(response.getFriendInfoList());
        }

        public String getId() {
            return id;
        }

        public int getTotalCount() {
            return totalCount;
        }
    }

    public static final String EXTRA_KEY_SERVICE_TYPE = "KEY_FRIEND_TYPE";

    protected ListView list = null;
    private EditText editSearch;
    private FriendsListAdapter adapter = null;
    private final ArrayList<String> userlist = new ArrayList<>();
    private List<FriendInfo> temp_friendInfoList = new ArrayList<>();
    private final List<FriendType> friendTypeList = new ArrayList<>();
    private FriendContext friendContext = null;
    private FriendsInfo friendsInfo = null;
    protected KakaoDialogSpinner msgType = null;

    private boolean hnsUserFlag = false;
    private int btnClickToken = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d("프로세스", "FriendsMainActivity");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_friends_main);

        /*
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_KEY_SERVICE_TYPE)) {
            String[] serviceTypes = intent.getStringArrayExtra(EXTRA_KEY_SERVICE_TYPE);
            for (String serviceType : serviceTypes) {
                friendTypeList.add(FriendType.valueOf(serviceType));
            }
        }
        */

        friendTypeList.add(FriendType.HEAR_SPEAK);
        friendTypeList.add(FriendType.KAKAO_TALK);
        // friendTypeList.add(FriendType.KAKAO_TALK_AND_STORY);

        list = findViewById(R.id.friend_list);
        editSearch = findViewById(R.id.editSearch);
        Button talkButton = findViewById(R.id.all_talk_friends);
        Button hnsButton = findViewById(R.id.all_hns_friends);
        // Button talkStoryButton = findViewById(R.id.all_talk_and_story_friends);
        msgType = findViewById(R.id.message_type);

        talkButton.setVisibility(View.GONE);
        hnsButton.setVisibility(View.GONE);
        // talkStoryButton.setVisibility(View.GONE);

        // input창에 검색어를 입력시 "addTextChangedListener" 이벤트 리스너를 정의한다.
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                // input창에 문자를 입력할때마다 호출된다.
                // search 메소드를 호출한다.
                String text = editSearch.getText().toString();
                search(text.replaceAll(" ", ""));
            }
        });

        for (FriendType friendType : friendTypeList) {
            switch (friendType) {
                case KAKAO_TALK:
                    talkButton.setVisibility(View.VISIBLE);
                    talkButton.setOnClickListener(this);
                    break;
                case HEAR_SPEAK:
                    hnsButton.setVisibility(View.VISIBLE);
                    hnsButton.setOnClickListener(this);
                    break;
                    /*
                case KAKAO_TALK_AND_STORY:
                    talkStoryButton.setVisibility(View.VISIBLE);
                    talkStoryButton.setOnClickListener(this);
                    break;
                    */
            }
        }
        hnsUserFlag = true;
        setFriendsListFunc();
        requestFriends(FriendType.HEAR_SPEAK);
        findViewById(R.id.title_back).setOnClickListener(v -> finish());
    }

    // 검색을 수행하는 메소드
    public void search(String charText) {
        //이름검색 코드를 여기에 작성한다.
        List<FriendInfo> temp = new ArrayList<>();

        // 리스트의 모든 데이터를 검색한다.
        for (int i = 0; i < friendsInfo.getFriendInfoList().size(); i++) {
            // arraylist의 모든 데이터에 입력받은 단어(charText)가 포함되어 있으면 true를 반환한다.
            if (friendsInfo.getFriendInfoList().get(i).getProfileNickname().contains(charText)) {
                temp.add(friendsInfo.getFriendInfoList().get(i));
            }
        }

        if (friendsInfo.getFriendInfoList().size() == 0) {
            for (int i = 0; i < temp_friendInfoList.size(); i++) {
                if (temp_friendInfoList.get(i).getProfileNickname().contains(charText)) {
                    temp.add(temp_friendInfoList.get(i));
                }
            }
        }

        // 리스트 데이터가 변경되었으므로 아답터를 갱신하여 검색된 데이터를 화면에 보여준다.
        adapter.setItem(temp);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        FriendType type = FriendType.KAKAO_TALK;
        switch (v.getId()) {
            case R.id.all_talk_friends:
                type = FriendType.KAKAO_TALK;
                editSearch.setText("");
                requestFriends(type);
                btnClickToken = 2;
                break;
            case R.id.all_hns_friends:
                hnsUserFlag = true;
                editSearch.setText("");
                type = FriendType.HEAR_SPEAK;
                requestFriends(type);
                btnClickToken = 1;
                break;
                /*
            case R.id.all_talk_and_story_friends:
                type = FriendType.KAKAO_TALK_AND_STORY;
                break;
                */
        }
    }

    private void requestFriends(FriendType type) {
        adapter = null;
        friendsInfo = new FriendsInfo();
        friendContext = FriendContext.createContext(type, FriendFilter.NONE, FriendOrder.NICKNAME, false, 0, 1000, "asc");
        requestFriendsInner();
    }

    private void setFriendsList() {
        friendContext = FriendContext.createContext(FriendType.KAKAO_TALK, FriendFilter.NONE, FriendOrder.NICKNAME, false, 0, 1000, "asc");
        final IFriendListCallback callback = this;
        searchDBUserList();

        FriendsService.getInstance().requestFriends(new TalkResponseCallback<FriendsResponse>() {
            @Override
            public void onNotKakaoTalkUser() {
               // KakaoToast.makeToast(getApplicationContext(), "not a KakaoTalk user", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                redirectLoginActivity();
            }

            @Override
            public void onNotSignedUp() {
                redirectSignupActivity();
            }

            @Override
            public void onFailure(ErrorResult errorResult) {
               // KakaoToast.makeToast(getApplicationContext(), errorResult.toString(), Toast.LENGTH_SHORT).show();
                Logger.e("onFailure: " + errorResult.toString());
            }

            @Override
            public void onSuccess(FriendsResponse result) {
                temp_friendInfoList = new ArrayList<>();
                for (String userInfo : userlist) {
                    String[] arr = userInfo.split("&");

                    for (FriendInfo elem : result.getFriendInfoList()) {
                        if (elem.getId() == Long.parseLong(arr[1])) {
                            if (temp_friendInfoList.contains(elem) != true) {
                                elem.addProfileNickname(arr[3]);
                                temp_friendInfoList.add(elem);
                            }
                        }
                    }
                }
                if (adapter == null) {
                    adapter = new FriendsListAdapter(temp_friendInfoList, callback);
                    list.setAdapter(adapter);
                } else {
                    adapter.setItem(temp_friendInfoList);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onDidStart() {
                showWaitingDialog();
            }

            @Override
            public void onDidEnd() {
                cancelWaitingDialog();
            }
        }, friendContext);

    }

    private void requestFriendsInner() {
        final IFriendListCallback callback = this;
        FriendsService.getInstance().requestFriends(new TalkResponseCallback<FriendsResponse>() {
            @Override
            public void onNotKakaoTalkUser() {
               // KakaoToast.makeToast(getApplicationContext(), "not a KakaoTalk user", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                redirectLoginActivity();
            }

            @Override
            public void onNotSignedUp() {
                redirectSignupActivity();
            }

            @Override
            public void onFailure(ErrorResult errorResult) {
               // KakaoToast.makeToast(getApplicationContext(), errorResult.toString(), Toast.LENGTH_SHORT).show();
                Logger.e("onFailure: " + errorResult.toString());
            }

            @Override
            public void onSuccess(FriendsResponse result) {
                if (hnsUserFlag) {
                    setFriendsListFunc();
                    if (adapter == null) {
                        adapter = new FriendsListAdapter(temp_friendInfoList, callback);
                        list.setAdapter(adapter);
                    } else {
                        adapter.setItem(temp_friendInfoList);
                        adapter.notifyDataSetChanged();
                    }
                    hnsUserFlag = false;
                } else {
                    if (result != null) {
                        friendsInfo.merge(result);
                        if (adapter == null) {
                            adapter = new FriendsListAdapter(friendsInfo.getFriendInfoList(), callback);
                            list.setAdapter(adapter);
                        } else {
                            adapter.setItem(friendsInfo.getFriendInfoList());
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            @Override
            public void onDidStart() {
                showWaitingDialog();
            }

            @Override
            public void onDidEnd() {
                cancelWaitingDialog();
            }
        }, friendContext);
    }

    private void setFriendsListFunc() {
        //원활한 친구목록을 불러오기위해 갯수조정
        setFriendsList();
        setFriendsList();
        setFriendsList();
        setFriendsList();
        setFriendsList();
    }

    private ListTemplate getListTemplate(String senderName) {
        return ListTemplate.newBuilder("[들림e] " + senderName + "님의 초대메시지:)",
                LinkObject.newBuilder()
                        .setWebUrl("https://github.com/JmKanmo/Android_Project")
                        .setMobileWebUrl("https://play.google.com/store/apps")
                        .build())
                .addContent(ContentObject.newBuilder("청각장애인을위한 의사소통 어플리케이션, 들림e",
                        "https://postfiles.pstatic.net/MjAxOTA2MDhfMjAx/MDAxNTU5OTg5NDUyNjAw.BFUu5jcjg6_af1c5QzEx_aLKBYzoobw8s3awh7ny7kQg.GyBb8Bv2GGr5LWXl2OlAgmlW-mkSukRsjpH6zEPFl4Yg.JPEG.nebi25/icon.jpg?type=w773",
                        LinkObject.newBuilder()
                                .setWebUrl("https://github.com/JmKanmo/Android_Project")
                                .setMobileWebUrl("https://play.google.com/store/apps")
                                .build())
                        .setDescrption("#졸업작품 #어플리케이션")
                        .build())
                .addContent(ContentObject.newBuilder("들림e와 함께 소통하기!",
                        "https://item.kakaocdn.net/do/4be9625c0426fb7d21c0bff1e8af2e1df43ad912ad8dd55b04db6a64cddaf76d",
                        LinkObject.newBuilder()
                                .setWebUrl("https://item.kakaocdn.net/do/4be9625c0426fb7d21c0bff1e8af2e1df43ad912ad8dd55b04db6a64cddaf76d")
                                .setMobileWebUrl("https://play.google.com/store/apps")
                                .build())
                        .setDescrption("#들림e #엄지척").build())
                .addButton(new ButtonObject("웹으로 보기", LinkObject.newBuilder()
                        .setMobileWebUrl("https://github.com/JmKanmo/Android_Project")
                        .setMobileWebUrl("https://play.google.com/store/apps")
                        .build()))
                .addButton(new ButtonObject("앱으로 보기", LinkObject.newBuilder()
                        .setWebUrl("https://github.com/JmKanmo/Android_Project")
                        .setMobileWebUrl("https://play.google.com/store/apps")
                        .setAndroidExecutionParams("key1=value1")
                        .setIosExecutionParams("key1=value1")
                        .build()))
                .build();
    }

    private TalkResponseCallback<Boolean> getTalkResponseCallback() {
        return new TalkResponseCallback<Boolean>() {
            @Override
            public void onNotKakaoTalkUser() {
               // KakaoToast.makeToast(getApplicationContext(), "not a KakaoTalk user", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(ErrorResult errorResult) {
               // KakaoToast.makeToast(getApplicationContext(), "failure : " + errorResult, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                redirectLoginActivity();
            }

            @Override
            public void onNotSignedUp() {
               // KakaoToast.makeToast(getApplicationContext(), "onNotSignedUp : " + "User Not Registed App", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(Boolean result) {
                //KakaoToast.makeToast(getApplicationContext(), "Send message success", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onDidStart() {
                showWaitingDialog();
            }

            @Override
            public void onDidEnd() {
                cancelWaitingDialog();
            }
        };
    }

    protected void requestDefaultMemo() {
        KakaoTalkService.getInstance().requestSendMemo(getTalkResponseCallback(), getListTemplate(""));
    }

    protected void requestInviteMessage(final MessageSendable friendInfo, String senderName) {
        KakaoTalkService.getInstance().requestSendMessage(getTalkResponseCallback(), friendInfo, getListTemplate(senderName));
    }

    @Override
    public void onItemSelected(int position, FriendInfo friendInfo) {
        Log.d("TAG", friendsInfo.getFriendInfoList().get(position).toString());
    }

    @Override
    public void onPreloadNext() {
        if (friendContext.hasNext()) {
            requestFriendsInner();
        }
    }

    private void searchDBUserList() {

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
                        temp = nickname + "&" + userId + "&" + phoneNumber + "&" + deaf;
                        userlist.add(temp);
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
