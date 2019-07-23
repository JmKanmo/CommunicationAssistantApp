package com.kakao.push.request;

import android.net.Uri;

import com.kakao.auth.network.AuthorizedRequest;
import com.kakao.network.ServerProtocol;
import com.kakao.push.StringSet;
import com.kakao.test.common.KakaoTestCase;
import com.kakao.test.common.TestAppConfiguration;
import com.kakao.test.common.TestPhaseInfo;
import com.kakao.common.IConfiguration;
import com.kakao.common.PhaseInfo;
import com.kakao.util.helper.CommonProtocol;

import org.json.JSONObject;
import org.junit.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
/**
 * @author kevin.kang. Created on 2017. 12. 7..
 */

public class SendPushRequestTest extends KakaoTestCase {
    @Test
    public void create() {
        JSONObject json = new JSONObject();
        String pushMessage = json.toString();
        AuthorizedRequest request = new SendPushRequest(pushMessage, "device_id");
        IConfiguration configuration = new TestAppConfiguration();
        PhaseInfo phaseInfo = new TestPhaseInfo();
        request.setAccessToken("access_token");
        request.setConfiguration(phaseInfo, configuration);

        Uri uri = Uri.parse(request.getUrl());
        assertEquals("POST", request.getMethod());
        assertEquals(ServerProtocol.apiAuthority(), uri.getHost());
        assertEquals("/" + ServerProtocol.PUSH_SEND_PATH, uri.getPath());

        Map<String, String> headers = request.getHeaders();
        assertEquals(String.format("%s %s", ServerProtocol.AUTHORIZATION_BEARER, "access_token"), headers.get(ServerProtocol.AUTHORIZATION_HEADER_KEY));
        assertEquals(configuration.getKAHeader(), headers.get(CommonProtocol.KA_HEADER_KEY));

        Map<String, String> params = request.getParams();
        assertEquals(pushMessage, params.get(StringSet.push_message));
        assertEquals("device_id", params.get(com.kakao.push.StringSet.device_id));
    }
}
