package com.kakao.auth.network;

import android.net.Uri;

import com.kakao.network.ServerProtocol;
import com.kakao.test.common.KakaoTestCase;
import com.kakao.test.common.TestAppConfiguration;
import com.kakao.test.common.TestPhaseInfo;
import com.kakao.common.IConfiguration;
import com.kakao.common.PhaseInfo;
import com.kakao.util.helper.CommonProtocol;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Test;

import java.util.Map;

/**
 * @author kevin.kang. Created on 2017. 12. 7..
 */

public class AuthorizedApiRequestTest extends KakaoTestCase {
    private IConfiguration configuration = new TestAppConfiguration();
    private PhaseInfo phaseInfo = new TestPhaseInfo();

    @Test
    public void create() {
        AuthorizedRequest request = new AuthorizedApiRequest() {
            @Override
            public String getMethod() {
                return "GET";
            }
        };
        request.setAccessToken("access_token");
        request.setConfiguration(phaseInfo, configuration);

        Map<String, String> headers = request.getHeaders();
        assertEquals(String.format("%s %s", ServerProtocol.AUTHORIZATION_BEARER, "access_token"), headers.get(ServerProtocol.AUTHORIZATION_HEADER_KEY));
        assertEquals(configuration.getKAHeader(), headers.get(CommonProtocol.KA_HEADER_KEY));

        Uri uri = Uri.parse(request.getUrl());
        assertEquals(ServerProtocol.apiAuthority(), uri.getHost());
    }
}
