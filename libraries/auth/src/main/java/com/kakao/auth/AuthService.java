/*
  Copyright 2014-2017 Kakao Corp.

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

import java.util.concurrent.Future;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.kakao.auth.ageauth.DefaultAgeAuthService;
import com.kakao.auth.api.AuthApi;
import com.kakao.auth.callback.AccountResponseCallback;
import com.kakao.auth.network.response.AccessTokenInfoResponse;
import com.kakao.network.tasks.ITaskQueue;
import com.kakao.network.tasks.KakaoResultTask;
import com.kakao.network.tasks.KakaoTaskQueue;

/**
 * Service class for age authentication or checking access token info.
 *
 * @author leoshin
 */
public class AuthService {
    /**
     * 연령인증 레벨을 설정한다.
     */
    public enum AgeAuthLevel {
        /**
         * 본인인증
         */
        LEVEL_1("10", "AUTH_LEVEL1"),

        /**
         * 연령인증
         */
        LEVEL_2("20", "AUTH_LEVEL2");

        final private String value;
        final private String name;
        AgeAuthLevel(String value, String name) {
            this.value = value;
            this.name = name;
        }

        public static AgeAuthLevel convertByName(String name) {
            for (AgeAuthLevel value : values()) {
                if (value.getName().equals(name)) {
                    return value;
                }
            }

            return null;
        }

        public String getValue() {
            return value;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * 연령제한, 일반적으로 12세, 15세, 19세
     */
    public enum AgeLimit {
        /**
         * 12세 인증
         */
        LIMIT_12("12"),

        /**
         * 15세 인증
         */
        LIMIT_15("15"),

        /**
         * 18세 인증
         */
        LIMIT_18("18"),

        /**
         * 19세 인증
         */
        LIMIT_19("19");

        final private String value;
        AgeLimit(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * 연령인증시 응답받을 수 있는 StatusCode.
     */
    public enum AgeAuthStatus {
        /**
         * 성공 code = 0
         */
        SUCCESS(0),
        /**
         * 클라이언트 쪽에서 (ex. 웹뷰 에러) 발생한 에러로 인해 연령 인증이 실패한 경우 code = -777
         */
        CLIENT_ERROR(ApiErrorCode.CLIENT_ERROR_CODE),
        /**
         * 유저의 인터액션을 (ex. 뒤로가기 버튼 클릭) 통하여 연령 인증이 취소되거나 실패한 경우 code = -778
         */
        CANCELED_OPERATION(-778),
        /**
         * 인증되지 않은 사용자 일 경우 code = -401
         */
        UNAUTHORIZED(-401),
        /**
         * 클라이언트 정보 호환 안됨, 업체에서 온 데이터가 비어있을경우, 앱에도 연령인증 정보가 없고 실제 입력도 없는경우. code = -440
         */
        BAD_PARAMETERS(-440),
        /**
         * 연령인증이 되지 않아서 연령인증이 필요한 상황(기본적으로는 정상인 상황) code = -450
         */
        NOT_AUTHORIZED_AGE(-450),
        /**
         * 현재 앱의 연령제한보다 사용자의 연령이 낮은 경우 code = -451
         */
        LOWER_AGE_LIMIT(-451),
        /**
         * 이미 연령인증을 마친상황. code = -452
         */
        ALREADY_AGE_AUTHORIZED(-452),
        /**
         * 연령인증 횟수 초과 code = -453
         */
        EXCEED_AGE_CHECK_LIMIT(-453),
        /**
         * 이전에 인정했던 정보와 불일치 (생일). code = -480
         */
        AGE_AUTH_RESULT_MISMATCH(-480),
        /**
         * CI 정보가 불일치 할 경우. code = -481
         */
        CI_RESULT_MISMATCH(-481),
        /**
         * 사용자 찾기 실패, 받아온 생일이 불일치할 경우, 예기치 못한 에러 발생시  code = -500
         */
        ERROR(-500),
        /**
         * 알수 없는 type의 status
         */
        UNKOWN(-999);

        final private int value;
        AgeAuthStatus(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static AgeAuthStatus valueOf(int i) {
            for (AgeAuthStatus value : values()) {
                if (value.getValue() == i) {
                    return value;
                }
            }
            return UNKOWN;
        }
    }

    private static AuthService instance = new AuthService(AuthApi.getInstance(), KakaoTaskQueue.getInstance());

    private AuthApi api;
    private ITaskQueue taskQueue;

    public static AuthService getInstance() {
        return instance;
    }

    AuthService(final AuthApi authApi, ITaskQueue taskQueue) {
        this.api = authApi;
        this.taskQueue = taskQueue;
    }

    /**
     * 연령인증이 필요한 경우에 동의창을 띄우기 위한 용도로 사용된다.
     * UI Thread 에서 동작해야하며, 기본적으로 SDK 내부에서 필요한 경우 자동으로 띄우기 때문에
     * 수동을 콘텐츠 연령인증이 필요한경우 띄우기 동의창을 띄우기 위한 용도로 사용한다.
     * (제휴를 통해 권한이 부여된 특정 앱에서만 호출이 가능합니다.)
     *
     * @param callback 요청 결과에 대한 callback
     * @param ageAuthParams {@link AgeAuthParamBuilder}를 통해 만든 연령인증에 필요한 파람들
     * @param context Permission 을 체크하는 용도로 사용하는 현재 앱 context
     * @return {@link AgeAuthStatus} 연령인증 요청 응답 status
     */
    public Future<Integer> requestShowAgeAuthDialog(final AccountResponseCallback callback, final Bundle ageAuthParams, final Context context) {
        return KakaoTaskQueue.getInstance().addTask(new KakaoResultTask<Integer>(callback) {
            @Override
            public Integer call() {
                return DefaultAgeAuthService.getInstance().requestAgeAuth(ageAuthParams, context);
            }
        });
    }

    /**
     * 로그인을 통해 얻은 사용자 토큰의 정보를 얻는다.
     * 정보 뿐 아니라 해당 토큰의 만료 기간 등에 대한 유효성을 검증 있다.
     * 사용자 토큰이 유효할 경우 토큰의 정보를 응답으로 받을 수 있으며, 토큰이 유효하지 않을 경우 상황에 맞는 오류를 받는다.
     * 다른 API와 달리 sigup이 호출되기 전에도 호출할 수 있다 즉, 미가입 상태 사용자 id를 확인할 때에도 사용할 수 있다
     * @param callback 요청 결과에 대한 callback
     */
    public void requestAccessTokenInfo(final ApiResponseCallback<AccessTokenInfoResponse> callback) {
        taskQueue.addTask(new KakaoResultTask<AccessTokenInfoResponse>(callback) {
            @Override
            public AccessTokenInfoResponse call() throws Exception {
                return api.requestAccessTokenInfo();
            }
        });    }
}
