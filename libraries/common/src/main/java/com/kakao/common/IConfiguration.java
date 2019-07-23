package com.kakao.common;

import android.content.Context;

import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.CommonProtocol;
import com.kakao.util.helper.SystemInfo;
import com.kakao.util.helper.Utility;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 앱 패키지 관련 정보들을 담기 위한 클래스.
 *
 * Class for holding package-specific information.
 *
 * @author kevin.kang. Created on 2017. 11. 13..
 */

public interface IConfiguration {
    /**
     * 앱의 시그너쳐를 특정 알고리즘으로 해싱한 값.
     * @return  android key hash value
     * @see android.content.pm.PackageInfo#signatures
     */
    String getKeyHash();
    /**
     * SDK에서 사용하는 KA 헤더를 리턴한다.
     *
     * @return KA header
     *
     * @see SystemInfo#getKAHeader()
     */
    String getKAHeader();

    /**
     * 앱의 패키지네임
     *
     * @return app's package name
     */
    String getPackageName();

    /**
     * 앱 버전. 통계 및 디버깅 용으로 카카오 측에서 수집.
     * @return return current app's version string
     */
    String getAppVer();

    /**
     * 카카오링크 v2 에서 사용하는 extras 객체를 리턴.
     *
     * @return extras object used by Kakaolink SDK
     */
    JSONObject getExtrasJson();

    /**
     * 카카오링크 v2에서 사용하는 extras 스트링 리턴. Extras를 조작해야할 필요성이 생김에 따라 deprecate 시키고
     * {@link #getExtrasJson()}로 대체
     *
     * @deprecated use {@link #getExtrasJson()} instead
     */
    @Deprecated
    String getExtras();

    class Factory {
        static IConfiguration createConfiguration(final Context context) throws KakaoException {
            SystemInfo.initialize(context);
            String keyHash = Utility.getKeyHash(context);
            String kaHeader = SystemInfo.getKAHeader();
            String appVer = String.valueOf(Utility.getAppVersion(context));
            String packageName = context.getPackageName();
            JSONObject extras = new JSONObject();
            try {
                extras.put(CommonProtocol.APP_PACKAGE, context.getPackageName());
                extras.put(CommonProtocol.KA_HEADER_KEY, kaHeader);
                extras.put(CommonProtocol.APP_KEY_HASH, keyHash);
            } catch (JSONException e) {
                throw new IllegalArgumentException("JSON parsing error. Malformed parameters were provided. Detailed error message: " + e.toString());
            }
            return new RequestConfiguration(keyHash, kaHeader, appVer, packageName, extras);
        }
    }
}
