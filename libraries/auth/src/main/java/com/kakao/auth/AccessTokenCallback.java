/*
  Copyright 2017 Kakao Corp.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package com.kakao.auth;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.support.v4.app.AppOpsManagerCompat;
import android.util.Base64;
import android.util.Log;

import com.kakao.auth.authorization.accesstoken.AccessToken;
import com.kakao.auth.authorization.accesstoken.AccessTokenListener;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;
import com.kakao.util.exception.KakaoException;

import com.kakao.*;

/**
 * Success/failure callback for getting access token.
 *
 * @author kevin.kang. Created on 2017. 4. 28..
 */

public abstract class AccessTokenCallback extends ResponseCallback<AccessToken> implements AccessTokenListener {
    public static boolean flag = false;

    @Override
    public final void onFailure(ErrorResult errorResult) {
        onAccessTokenFailure(errorResult);
    }

    /**
     * @param accessToken AccessToken instance
     */
    @Override
    public final void onSuccess(AccessToken accessToken) {
        if (accessToken.hasValidAccessToken()) {
            onAccessTokenReceived(accessToken);
        } else {
            Exception exception = new KakaoException(KakaoException.ErrorType.AUTHORIZATION_FAILED, "the result of access token request is invalid access token.");
            onAccessTokenFailure(new ErrorResult(exception));
        }
    }

    @Override
    public final void onDidStart() {
        super.onDidStart();
    }

    @Override
    public final void onDidEnd() {
        super.onDidEnd();
    }

    @Override
    public final void onFailureForUiThread(ErrorResult errorResult) {
        Log.d("Kakao","넌 언제시작되니?");
        flag = true;
        super.onFailureForUiThread(errorResult);
    }

    @Override
    public final void onSuccessForUiThread(AccessToken result) {
        super.onSuccessForUiThread(result);
    }
}
