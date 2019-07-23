package com.kakao.sdk.sample.kakaotalk;

import com.kakao.kakaotalk.response.model.ChatInfo;

/**
 * @author leoshin on 15. 9. 4.
 */
public interface IChatListCallback {
    void onItemSelected(int position, ChatInfo chatInfo);
    void onPreloadNext();
}
