package com.kakao.auth.authorization.accesstoken;

import com.kakao.auth.ApprovalType;
import com.kakao.auth.StringSet;
import com.kakao.test.common.TestAppConfiguration;
import com.kakao.test.common.TestPhaseInfo;
import com.kakao.common.IConfiguration;
import com.kakao.common.PhaseInfo;
import com.kakao.util.helper.CommonProtocol;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;


import static org.junit.jupiter.api.Assertions.*;
/**
 * @author kevin.kang. Created on 2018. 4. 16..
 */
@DisplayName("AccessTokenRequest's")
class AccessTokenRequestTest {
    @Test
    @DisplayName("Constructor should populate every header and parameter")
    void create() {
        IConfiguration configuration = new TestAppConfiguration();
        PhaseInfo phaseInfo = new TestPhaseInfo();
        AccessTokenRequest request = new AccessTokenRequest(
                phaseInfo,
                configuration,
                "sample_auth_code",
                null,
                ApprovalType.INDIVIDUAL.toString()
        );

        Map<String, String> headers = request.getHeaders();
        Map<String, String> params = request.getParams();


        assertEquals("POST", request.getMethod());

        assertTrue(headers.containsKey(CommonProtocol.KA_HEADER_KEY));

        assertEquals(configuration.getKeyHash(), params.get(StringSet.android_key_hash));
        assertEquals(ApprovalType.INDIVIDUAL.toString(), params.get(StringSet.approval_type));
        assertEquals(phaseInfo.appKey(), params.get(StringSet.client_id));
        assertEquals(phaseInfo.clientSecret(), params.get(StringSet.client_secret));
        assertEquals("sample_auth_code", params.get(StringSet.code));
        assertEquals(StringSet.authorization_code, params.get(StringSet.grant_type));
        assertEquals(String.format("kakao%s://oauth", phaseInfo.appKey()), params.get(StringSet.redirect_uri));
    }
}
