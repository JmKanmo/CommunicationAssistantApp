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
package com.kakao.auth.authorization.authcode;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.text.TextUtils;

import com.kakao.auth.ISessionConfig;
import com.kakao.auth.R;
import com.kakao.auth.StringSet;
import com.kakao.auth.authorization.AuthorizationResult;
import com.kakao.auth.helper.StartActivityWrapper;
import com.kakao.auth.network.request.AuthRequest;
import com.kakao.network.ServerProtocol;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.Utility;
import com.kakao.util.helper.log.Logger;

/**
 * @author kevin.kang. Created on 2017. 5. 30..
 */

class WebAuthCodeService implements AuthCodeService {
    private Context context;
    private Handler handler;
    private ISessionConfig sessionConfig;

    WebAuthCodeService(final Context context, final Handler handler, final ISessionConfig sessionConfig) {
        this.context = context;
        this.handler = handler;
        this.sessionConfig = sessionConfig;
    }

    void onWebViewCompleted(final int requestCode, final String redirectURL, KakaoException exception, final AuthCodeListener listener) {
        AuthorizationResult result;
        if (redirectURL != null) {
            Uri redirectedUri = Uri.parse(redirectURL);
            final String code = redirectedUri.getQueryParameter(StringSet.code);
            if (!TextUtils.isEmpty(code)) {
                result = AuthorizationResult.createSuccessAuthCodeResult(redirectURL);
                listener.onAuthCodeReceived(requestCode, result);
                return;
            } else {
                String error = redirectedUri.getQueryParameter(StringSet.error);
                String errorDescription = redirectedUri.getQueryParameter(StringSet.error_description);
                if (error != null && error.equalsIgnoreCase(StringSet.access_denied)) {
                    result = AuthorizationResult.createAuthCodeCancelResult(context.getString(R.string.auth_code_cancel));
                } else {
                    result = AuthorizationResult.createAuthCodeOAuthErrorResult(errorDescription);
                }
            }
        } else {
            if (exception == null) {
                result = AuthorizationResult.createAuthCodeOAuthErrorResult("Failed to get Authorization Code.");
            } else if (exception.isCancledOperation()) {
                result = AuthorizationResult.createAuthCodeCancelResult(exception.getMessage());
            } else {
                result = AuthorizationResult.createAuthCodeOAuthErrorResult(exception);
            }
        }
        listener.onAuthCodeReceived(requestCode, result);
    }

    /**
     *
     * This is
     * @param requestCode Identifier for this auth code request
     * @param resultCode Result code indicating whether the redirect url is received or not
     * @param resultData Result bundle delivered from KakaoWebViewActivity
     * @param listener Listner instance who will handle the result of the auth code request
     */
    void onReceivedResult(int requestCode, int resultCode, Bundle resultData, AuthCodeListener listener) {
        String redirectUrl = null;
        KakaoException kakaoException = null;
        switch (resultCode) {
            case KakaoWebViewActivity.RESULT_SUCCESS:
                redirectUrl =  resultData.getString(KakaoWebViewActivity.KEY_REDIRECT_URL);
                break;
            case KakaoWebViewActivity.RESULT_ERROR:
                kakaoException = (KakaoException) resultData.getSerializable(KakaoWebViewActivity.KEY_EXCEPTION);
                break;
        }
        onWebViewCompleted(requestCode, redirectUrl, kakaoException, listener);
    }

    ResultReceiver getResultReceiver(final AuthCodeRequest authCodeRequest, final AuthCodeListener listener) {
        return new ResultReceiver(handler) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                WebAuthCodeService.this.onReceivedResult(authCodeRequest.getRequestCode(), resultCode, resultData, listener);
            }
        };
    }

    @Override
    public boolean requestAuthCode(AuthCodeRequest request, StartActivityWrapper wrapper, AuthCodeListener listener) {
        try {
            Intent intent = createAuthorizeIntentWithUri(context, request, listener);
            startActivity(wrapper, intent);
        } catch (Throwable t) {
            Logger.e("WebViewAuthHandler failed", t);
            return false;
        }
        return true;
    }

    void startActivity(final StartActivityWrapper wrapper, final Intent intent) {
        wrapper.startActivity(intent);
    }

    /*
        Create intent for KakaoWebViewActivity where uri parameter represents url to bo loaded into
        webview for getting auth code. Intent contains uri to be loaded, authorization header (refresh token)
        result receiver instance.
     */
    Intent createAuthorizeIntentWithUri(final Context context, final AuthCodeRequest request, final AuthCodeListener listener) {
        Intent intent = KakaoWebViewActivity.newIntent(context);
        if (request.getAccountUri() != null) {
            intent.putExtra(KakaoWebViewActivity.KEY_URL, request.getAccountUri().toString());
        }
        intent.putExtra(KakaoWebViewActivity.KEY_EXTRA_HEADERS, request.getExtraHeaders());
        intent.putExtra(KakaoWebViewActivity.KEY_USE_WEBVIEW_TIMERS, sessionConfig.isUsingWebviewTimer());
        intent.putExtra(KakaoWebViewActivity.KEY_RESULT_RECEIVER, getResultReceiver(request, listener));
        return intent;
    }

    @Override
    public boolean handleActivityResult(int requestCode, int resultCode, Intent data, AuthCodeListener listener) {
        return false;
    }

    /**
     * Checks whether kakao login via webview is available. Always true for now.
     *
     * @return true if kakao account login via webview is available, false otherwise.
     */
    @Override
    public boolean isLoginAvailable() {
        return true;
    }

}

