package com.kakao.push.response;

import com.kakao.network.response.ResponseStringConverter;
import com.kakao.push.StringSet;
import com.kakao.push.response.model.PushTokenInfo;
import com.kakao.test.common.KakaoTestCase;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;
/**
 * @author kevin.kang. Created on 2017. 12. 6..
 */

public class GetPushTokenResponseTest extends KakaoTestCase {
    @Test
    public void convert() {
        ResponseStringConverter<PushTokenInfo> converter = GetPushTokenResponse.CONVERTER;
        String response = getResponseString();
        PushTokenInfo tokenInfo = converter.convert(response);

        assertEquals("1234", tokenInfo.getUserId());
        assertEquals("device_id", tokenInfo.getDeviceId());
        assertEquals(StringSet.gcm, tokenInfo.getPushType());
        assertEquals("push_token", tokenInfo.getPushToken());
        assertEquals("2014-07-29T06:24:12Z", tokenInfo.getCreatedAt());
        assertEquals("2014-07-29T06:24:12Z", tokenInfo.getUpdatedAt());
    }

    private String getResponseString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(StringSet.user_id, "1234");
            jsonObject.put(StringSet.device_id, "device_id");
            jsonObject.put(StringSet.push_type, StringSet.gcm);
            jsonObject.put(StringSet.push_token, "push_token");
            jsonObject.put(StringSet.created_at, "2014-07-29T06:24:12Z");
            jsonObject.put(StringSet.updated_at, "2014-07-29T06:24:12Z");
            return jsonObject.toString();
        } catch (JSONException e) {
            return null;
        }
    }
}
