package com.kakao.auth.mocks;

import com.kakao.auth.AccessTokenCallback;
import com.kakao.auth.authorization.accesstoken.AccessToken;
import com.kakao.network.ErrorResult;
import com.kakao.util.helper.log.Logger;

/**
 * @author kevin.kang. Created on 2017. 12. 4..
 */

public class TestAccessTokenCallback extends AccessTokenCallback {
    @Override
    public void onAccessTokenReceived(AccessToken accessToken) {
    }

    @Override
    public void onAccessTokenFailure(ErrorResult errorResult) {
    }
}
