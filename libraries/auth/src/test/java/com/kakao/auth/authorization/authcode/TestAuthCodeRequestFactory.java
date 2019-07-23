package com.kakao.auth.authorization.authcode;

import com.kakao.auth.ApprovalType;
import com.kakao.auth.AuthCodeCallback;
import com.kakao.auth.ISessionConfig;
import com.kakao.auth.StringSet;
import com.kakao.auth.authorization.authcode.AuthCodeRequest;

import java.util.List;

/**
 * @author kevin.kang. Created on 2017. 6. 12..
 */

public class TestAuthCodeRequestFactory {
    public static AuthCodeRequest createAuthCodeRequest(final int requestCode, final String appKey, final ISessionConfig sessionConfig, final AuthCodeCallback callback) {
        AuthCodeRequest request = new AuthCodeRequest(appKey, StringSet.REDIRECT_URL_PREFIX + appKey + StringSet.REDIRECT_URL_POSTFIX, requestCode, callback);
        request.putExtraParam(StringSet.approval_type, sessionConfig.getApprovalType() == null ? ApprovalType.INDIVIDUAL.toString() : sessionConfig.getApprovalType().toString());
        return request;
    }

    public static AuthCodeRequest createAuthCodeRequest(final int requestCode, final String appKey, final ISessionConfig sessionConfig, final String refreshToken, final List<String> scopes, final AuthCodeCallback callback) {
        AuthCodeRequest request = new AuthCodeRequest(appKey, StringSet.REDIRECT_URL_PREFIX + appKey + StringSet.REDIRECT_URL_POSTFIX, requestCode, callback);
        request.putExtraHeader(StringSet.RT, refreshToken);
        request.putExtraParam(StringSet.scope, getScopesString(scopes));
        request.putExtraParam(StringSet.approval_type, sessionConfig.getApprovalType() == null ? ApprovalType.INDIVIDUAL.toString() : sessionConfig.getApprovalType().toString());
        return request;
    }

    static String getScopesString(final List<String> requiredScopes) {
        String scopeParam = null;
        if (requiredScopes == null) {
            return null;
        }
        StringBuilder builder = null;
        for (String scope : requiredScopes) {
            if (builder != null) {
                builder.append(",");
            } else {
                builder = new StringBuilder("");
            }

            builder.append(scope);
        }

        if (builder != null) {
            scopeParam = builder.toString();
        }

        return scopeParam;
    }

}
