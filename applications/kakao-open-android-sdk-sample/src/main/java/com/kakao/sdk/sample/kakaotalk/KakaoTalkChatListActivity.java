package com.kakao.sdk.sample.kakaotalk;

import android.app.Application;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;
import com.kakao.kakaotalk.ChatFilterBuilder;
import com.kakao.kakaotalk.ChatFilterBuilder.ChatFilter;
import com.kakao.kakaotalk.ChatListContext;
import com.kakao.kakaotalk.v2.KakaoTalkService;
import com.kakao.kakaotalk.callback.TalkResponseCallback;
import com.kakao.kakaotalk.response.ChatListResponse;
import com.kakao.kakaotalk.response.model.ChatInfo;
import com.kakao.network.ErrorResult;
import com.kakao.sdk.sample.R;

import com.kakao.sdk.sample.common.BaseActivity;
import com.kakao.sdk.sample.common.GlobalApplication;
import com.kakao.sdk.sample.common.log.Logger;
import com.kakao.sdk.sample.common.widget.KakaoDialogSpinner;
import com.kakao.sdk.sample.common.widget.KakaoToast;
import com.kakao.sdk.sample.FriendsMainActivity.MSG_TYPE;

import java.util.ArrayList;
import java.util.List;

/**
 * @author by leoshin on 15. 8. 25..
 */
public class KakaoTalkChatListActivity extends BaseActivity implements IChatListCallback {

    private static class ChatInfos {
        private final List<ChatInfo> chatInfoList = new ArrayList<>();
        private int totalCount;

        public ChatInfos() {
        }

        public List<ChatInfo> getChatInfoList() {
            return chatInfoList;
        }

        public void merge(ChatListResponse response) {
            this.totalCount = response.getTotalCount();
            this.chatInfoList.addAll(response.getChatInfoList());
        }

        public int getTotalCount() {
            return totalCount;
        }
    }

    private ListView chatListView;
    private ChatListAdapter adapter = null;
    private ChatListContext chatListContext;
    private ChatInfos chatInfos;
    protected KakaoDialogSpinner msgType = null;
    protected KakaoDialogSpinner chatType = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_chat_list);

        msgType = findViewById(R.id.message_type);
        chatType = findViewById(R.id.chat_type);
        chatListView = findViewById(R.id.chat_list);
        requestChatLists();

        chatType.setOnListener((adapter, position) -> requestChatLists());
    }

    public class ChatListAdapter extends BaseAdapter {
        private class ViewHolder {
            NetworkImageView profileView;
            TextView title;
        }
        private List<ChatInfo> items = null;
        private IChatListCallback listener = null;

        public ChatListAdapter(List<ChatInfo> items, IChatListCallback listener) {
            this.items = items;
            this.listener = listener;
        }

        public void setItem(List<ChatInfo> items) {
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public ChatInfo getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (position == getCount() - 5) {
                if (listener != null) {
                    listener.onPreloadNext();
                }
            }

            View layout = convertView;
            ViewHolder holder;
            if (layout == null) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                layout = inflater.inflate(R.layout.view_chatroom_item, parent, false);
                holder = new ViewHolder();
                holder.profileView = layout.findViewById(R.id.chatroom_thumbnail);
                holder.profileView.setDefaultImageResId(R.drawable.thumb_story);
                holder.profileView.setErrorImageResId(R.drawable.thumb_story);
                holder.title = layout.findViewById(R.id.title);

                layout.setTag(holder);
            } else {
                holder = (ViewHolder)layout.getTag();
            }

            final ChatInfo info = getItem(position);
            Application app  = GlobalApplication.getGlobalApplicationContext();
            String profileUrl = getProfileUrl(info);
            holder.profileView.setImageUrl(profileUrl, ((GlobalApplication) app).getImageLoader());
            if (TextUtils.isEmpty(profileUrl)) {
                holder.profileView.setImageResource(R.drawable.thumb_story);
            }

            String nickName = info.getTitle();
            if (nickName != null && nickName.length() > 0) {
                holder.title.setVisibility(View.VISIBLE);
                holder.title.setText(nickName);
            } else {
                holder.title.setVisibility(View.INVISIBLE);
            }

            Log.d("TAG",info.getChatType());
            Log.d("TAG",info.getTargetId());
            Log.d("TAG",info.getTitle());
            Log.d("TAG",info.getType());
            Log.d("TAG",String.valueOf(info.getChatId()));
            Log.d("TAG",info.toString());


            layout.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemSelected(position, info);
                }
            });
            return layout;
        }
    }

    private String getProfileUrl(ChatInfo info) {
        String imageUrl = info.getImageUrl();
        if (TextUtils.isEmpty(imageUrl)) {
            if (info.getDisplayMemberImageList() != null && info.getDisplayMemberImageList().size() == 1) {
                imageUrl = info.getDisplayMemberImageList().get(0);
            }
        }

        Logger.d(imageUrl);
        return imageUrl;
    }

    private void requestChatLists() {
        adapter = null;
        chatListContext = ChatListContext.createContext(makeChatFilterBuilder(), 0, 30, "asc");
        chatInfos = new ChatInfos();
        requestChatListInner();
    }

    private ChatFilterBuilder makeChatFilterBuilder() {
        ChatFilterBuilder builder = new ChatFilterBuilder();
        int position = chatType.getSelectedItemPosition();
        switch (position) {
            case 0:
                // 권한이 있는 모든 채팅방
                break;
            case 1:
                // 일반 1:1 채팅방
                builder.addFilter(ChatFilter.DIRECT).addFilter(ChatFilter.REGULAR);
                break;
            case 2:
                // 일반 그룹채팅방
                builder.addFilter(ChatFilter.MULTI).addFilter(ChatFilter.REGULAR);
                break;
            case 3:
                // 일반 채팅방
                builder.addFilter(ChatFilter.REGULAR);
                break;
            case 4:
                // 오픈 1:1 채팅방
                builder.addFilter(ChatFilter.DIRECT).addFilter(ChatFilter.OPEN);
                break;
            case 5:
                // 오픈 그룹채팅방
                builder.addFilter(ChatFilter.MULTI).addFilter(ChatFilter.OPEN);
                break;
            case 6:
                // 오픈 채팅방
                builder.addFilter(ChatFilter.OPEN);
                break;
        }
        return builder;
    }

    private void requestChatListInner() {
        final IChatListCallback callback = this;
        KakaoTalkService.getInstance().requestChatRoomList(new KakaoTalkChatListResponseCallback<ChatListResponse>() {
            @Override
            public void onSuccess(ChatListResponse result) {
                if (result != null) {
                    if (result.getChatInfoList().size() == 0) {
                        KakaoToast.makeToast(getApplicationContext(), "Not exist ChatRoom", Toast.LENGTH_LONG).show();
                        return;
                    }

                    chatInfos.merge(result);
                    if (adapter == null) {
                        adapter = new ChatListAdapter(chatInfos.getChatInfoList(), callback);
                        chatListView.setAdapter(adapter);
                    } else {
                        adapter.setItem(chatInfos.getChatInfoList());
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }, chatListContext);
    }

    @Override
    public void onItemSelected(final int position, final ChatInfo chatInfo) {
        TalkMessageHelper.showSendMessageDialog(this, (dialog, which) -> requestSendMessage(MSG_TYPE.valueOf(msgType.getSelectedItemPosition()), chatInfo));
    }

    @Override
    public void onPreloadNext() {
        if (chatListContext.hasNext()) {
            requestChatListInner();
        }
    }

    private void requestSendMessage(MSG_TYPE msgType, ChatInfo chatInfo) {
        KakaoTalkMessageBuilder builder = new KakaoTalkMessageBuilder();
        if (chatInfo.getTitle() != null) {
            builder.addParam("username", chatInfo.getTitle());
        }
        KakaoTalkService.getInstance().requestSendMessage(new KakaoTalkChatListResponseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Logger.d("++ send message result : " + result);
                KakaoToast.makeToast(getApplicationContext(), "Send message success", Toast.LENGTH_LONG).show();
            }
        }, chatInfo, TalkMessageHelper.getSampleTemplateId(msgType), builder.build());
    }

    public abstract class KakaoTalkChatListResponseCallback<T> extends TalkResponseCallback<T> {

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
            redirectSignupActivity();
        }

        @Override
        public void onDidStart() {
            showWaitingDialog();
        }

        @Override
        public void onDidEnd() {
            cancelWaitingDialog();
        }
    }
}
