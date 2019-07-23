package com.kakao.auth.authorization.authcode;

import com.kakao.auth.authorization.AuthorizationResult;

/**
 * Interface for auth code callback. Success/error results are all received by the only method
 * onAuthCodeReceived. This interface is subject to improvement in the future, when success/error
 * can be converted to different classes, not same AuthorizationResult class.
 *
 * This interface is used internally only and subject to change.
 *
 * @author kevin.kang. Created on 2017. 4. 28..
 */

public interface AuthCodeListener {
    void onAuthCodeReceived(final int requestCode, final AuthorizationResult result);
}
