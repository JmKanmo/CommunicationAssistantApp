package com.kakao.sdk.sample.kakaotalk;

import java.util.HashMap;
import java.util.Map;

/**
 * @author leo.shin
 */
public class KakaoTalkMessageBuilder {
    public final Map<String, String> messageParams = new HashMap<String, String>();

    public KakaoTalkMessageBuilder addParam(String key, String value) {
        messageParams.put("${" + key + "}", value);
        return this;
    }

    public Map<String, String> build() {
        return messageParams;
    }
}
