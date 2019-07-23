package com.kakao.auth;

import com.kakao.network.ErrorResult;

/**
 * Success/failure callback for getting authorization code.
 *
 * @author kevin.kang. Created on 2017. 4. 26..
 */

public abstract class AuthCodeCallback {
    /**
     * Called when authorization code was successfully retrieved.
     *
     * @param authCode Auuthorization code received
     */
    public abstract void onAuthCodeReceived(final String authCode);

    /**
     * Called when there was a failure getting authorization code.
     *
     * @param errorResult ErrorResult object
     */
    public abstract void onAuthCodeFailure(final ErrorResult errorResult);
}
