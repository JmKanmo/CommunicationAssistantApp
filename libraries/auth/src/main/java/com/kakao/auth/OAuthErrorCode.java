package com.kakao.auth;

/**
 * Error code from Kakao OAuth server.
 *
 * @author kevin.kang. Created on 2017. 12. 1..
 */

public class OAuthErrorCode {
    public static final int INVALID_GRANT = -777;
    public static final int INVALID_REQUEST = -778;
    public static final int MISCONFIGURED = -779;
    public static final int UNAUTHORIZED = -780;
    public static final int ACCESS_DENIED = -781;
    public static final int SERVER_ERROR = - 782;

    public static final int RESTRICTED_ACCOUNT_CODE = -783;
    public static final int INVALID_AGREEMENT_CODE = -784;
}
