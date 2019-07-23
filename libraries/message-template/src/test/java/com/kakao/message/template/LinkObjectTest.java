package com.kakao.message.template;

import com.kakao.test.common.KakaoTestCase;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;
/**
 * @author kevin.kang. Created on 2017. 3. 14..
 */

public class LinkObjectTest extends KakaoTestCase {
    private final String androidExecutionParams = "androidParams";
    private final String iosExecutionParams = "iosParams";
    private final String mobileWebUrl = "mobileWebUrl";
    private final String webUrl = "webUrl";
    @Test
    public void testMembersAreNull() {
        LinkObject linkObject = LinkObject.newBuilder().build();
        assertNull(linkObject.getAndroidExecutionParams());
        assertNull(linkObject.getIosExecutionParams());
        assertNull(linkObject.getMobileWebUrl());
        assertNull(linkObject.getWebUrl());
    }

    @Test
    public void testMembersAreCorrectlySet() {
        LinkObject linkObject = LinkObject.newBuilder()
                .setAndroidExecutionParams(androidExecutionParams)
                .setIosExecutionParams(iosExecutionParams)
                .setMobileWebUrl(mobileWebUrl)
                .setWebUrl(webUrl).build();

        assertEquals(androidExecutionParams, linkObject.getAndroidExecutionParams());
        assertEquals(iosExecutionParams, linkObject.getIosExecutionParams());
        assertEquals(mobileWebUrl, linkObject.getMobileWebUrl());
        assertEquals(webUrl, linkObject.getWebUrl());
    }

    @Test
    public void testToJSONObject() throws JSONException {
        LinkObject linkObject = LinkObject.newBuilder()
                .setAndroidExecutionParams(androidExecutionParams)
                .setIosExecutionParams(iosExecutionParams)
                .setMobileWebUrl(mobileWebUrl)
                .setWebUrl(webUrl).build();

        JSONObject linkJson = linkObject.toJSONObject();
        assertNotNull(linkJson);
        assertEquals(4, linkJson.length());
        assertEquals(androidExecutionParams, linkJson.getString(MessageTemplateProtocol.ANDROID_PARAMS));
        assertEquals(iosExecutionParams, linkJson.getString(MessageTemplateProtocol.IOS_PARAMS));
        assertEquals(mobileWebUrl, linkJson.getString(MessageTemplateProtocol.MOBILE_WEB_URL));
        assertEquals(webUrl, linkJson.getString(MessageTemplateProtocol.WEB_URL));
    }
}
