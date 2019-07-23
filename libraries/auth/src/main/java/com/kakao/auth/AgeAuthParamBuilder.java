package com.kakao.auth;

import android.os.Bundle;
import android.text.TextUtils;

import com.kakao.auth.AuthService.AgeAuthLevel;
import com.kakao.auth.AuthService.AgeLimit;

/**
 * Builder for age authentication parameters
 *
 * @author leoshin on 15. 11. 25.
 */
public class AgeAuthParamBuilder {

    private AgeAuthLevel authLevel;
    private AgeLimit ageLimit;
    private boolean isWesternAge;
    private boolean isSkipTerms;
    private String authFrom;
    private Boolean adultsOnly;

    /**
     * 연령인증 레벨. {@link AgeAuthLevel}
     * @param authLevel 연령인증 레벨.
     * @return AgeAuthParamBuilder
     */
    public AgeAuthParamBuilder setAuthLevel(AgeAuthLevel authLevel) {
        this.authLevel = authLevel;
        return this;
    }

    /**
     * 연령제한, 일반적으로 12세, 15세, 19세 {@link AgeLimit}
     * @param ageLimit 연령제한, 일반적으로 12세, 15세, 19세
     * @return AgeAuthParamBuilder
     */
    public AgeAuthParamBuilder setAgeLimit(AgeLimit ageLimit) {
        this.ageLimit = ageLimit;
        return this;
    }

    /**
     * 만 나이여부
     * 동의하고 시작하기 뷰에 연령제한이 만 나이 기준으로 표시되는지의 여부.
     * default false
     * @param isWesternAge 동의하고 시작하기 뷰에 연령제한이 만 나이 기준으로 표시되는지의 여부.
     * @return AgeAuthParamBuilder
     */
    public AgeAuthParamBuilder setIsWesternAge(boolean isWesternAge) {
        this.isWesternAge = isWesternAge;
        return this;
    }

    /**
     * 동의하기 안내화면 skip 여부
     * @param isSkipTerms 동의하기 안내화면 skip 여부
     * @return AgeAuthParamBuilder
     */
    public AgeAuthParamBuilder setSkipTerm(boolean isSkipTerms) {
        this.isSkipTerms = isSkipTerms;
        return this;
    }

    /**
     * 서비스 이름 (client_id or app_id or service_name)
     * @param authFrom 서비스 이름
     * @return AgeAuthParamBuilder
     */
    public AgeAuthParamBuilder setAuthFrom(String authFrom) {
        this.authFrom = authFrom;
        return this;
    }

    /**
     * 청소년 유해물 여부 (음악, TV 등). 주류 구매 등 단순 19세 이상 확인을 위해서는 false.
     *
     * @param adultsOnly 청소년 유해물 여부
     * @return AgeAuthParamBuilder
     */
    public AgeAuthParamBuilder setAdultsOnly(Boolean adultsOnly) {
        this.adultsOnly = adultsOnly;
        return this;
    }

    /**
     * 연령인증 요청시 필요한 param들을 Query String으로 return한다.
     * @return 연령인증 요청시 필요한 데이터를 포함한 Query String
     */
    public Bundle build() {
        final Bundle params = new Bundle();
        params.putString(StringSet.token_type, StringSet.api);
        params.putString(StringSet.access_token, Session.getCurrentSession().getTokenInfo().getAccessToken());
        params.putString(StringSet.return_url, getAgeAuthRedirectUrl());

        if (authLevel != null) {
            params.putString(StringSet.ageauth_level, authLevel.getValue());
        }
        if (ageLimit != null) {
            params.putString(StringSet.age_limit, ageLimit.getValue());
        }
        if (isWesternAge) {
            params.putString(StringSet.is_western_age, String.valueOf(isWesternAge));
        }
        if (adultsOnly != null) {
            params.putString(StringSet.adults_only, String.valueOf(adultsOnly));
        }
        if (isSkipTerms) {
            params.putString(StringSet.skip_term, String.valueOf(isSkipTerms));
        }
        if (!TextUtils.isEmpty(authFrom)) {
            params.putString(StringSet.auth_from, authFrom);
        }
        return params;
    }

    String getAgeAuthRedirectUrl() {
        return StringSet.REDIRECT_URL_PREFIX + Session.getCurrentSession().getAppKey() + StringSet.AGEAUTH_REDIRECT_URL_POSTFIX;
    }
}

