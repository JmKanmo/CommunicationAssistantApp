package com.kakao.auth.mocks;

import com.kakao.auth.network.AuthorizedRequest;
import com.kakao.network.multipart.Part;
import com.kakao.common.IConfiguration;
import com.kakao.common.PhaseInfo;

import java.util.List;
import java.util.Map;

/**
 * @author kevin.kang. Created on 2017. 12. 4..
 */

public class TestAuthorizedRequest implements AuthorizedRequest {
    private String accessToken;
    @Override
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public void setConfiguration(PhaseInfo phaseInfo, IConfiguration configuration) {
    }

    @Override
    public String getMethod() {
        return "GET";
    }

    @Override
    public String getUrl() {
        return null;
    }

    @Override
    public Map<String, String> getParams() {
        return null;
    }

    @Override
    public Map<String, String> getHeaders() {
        return null;
    }

    @Override
    public List<Part> getMultiPartList() {
        return null;
    }

    @Override
    public String getBodyEncoding() {
        return null;
    }
}
