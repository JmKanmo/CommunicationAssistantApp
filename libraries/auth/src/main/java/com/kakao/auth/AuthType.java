/**
 * Copyright 2014-2016 Kakao Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kakao.auth;

/**
 * Kakao SDK 로그인을 하는 방식에 대한 Enum class
 * @author MJ
 */
public enum AuthType {
    /**
     * Kakaotalk으로 login을 하고 싶을 경우 지정. Webviews are used if not installed.
     */
    KAKAO_TALK(0),

    /**
     * Kakaostory으로 login을 하고 싶을 경우 지정. Webviews are used if not installed.
     */
    HEAR_SPEAK(1),

    /**
     * 웹뷰를 통해 카카오 계정연결을 제공하고 싶을 경우 지정.
     */
    KAKAO_ACCOUNT(2),

    /**
     * 카카오톡으로만 로그인을 유도하고 싶으면서 계정이 없을때 계정생성을 위한 버튼도 같이 제공을 하고 싶을 경우 지정.
     * KAKAO_TALK과 중복 지정불가.
     */
    @Deprecated
    KAKAO_TALK_EXCLUDE_NATIVE_LOGIN(3),

    /**
     * 모든 로그인방식을 사용하고 싶을때 지정.
     */
    KAKAO_LOGIN_ALL(4),

    /**
     * Kakaotalkㅇㅡ로 login을 하고 싶을 경우 지정. Webviews are not used even if talk is not installed.
     */
    KAKAO_TALK_ONLY(5);

    private final int number;

    AuthType(int i) {
        this.number = i;
    }

    public int getNumber() {
        return number;
    }

    public static AuthType valueOf(int number){
        if(number == KAKAO_TALK.getNumber()) {
            return KAKAO_TALK;
        } else if (number == HEAR_SPEAK.getNumber()) {
            return HEAR_SPEAK;
        } else if (number == KAKAO_ACCOUNT.getNumber()) {
            return KAKAO_ACCOUNT;
        } else if (number == KAKAO_TALK_EXCLUDE_NATIVE_LOGIN.getNumber()) {
            return KAKAO_TALK_EXCLUDE_NATIVE_LOGIN;
        } else if (number == KAKAO_LOGIN_ALL.getNumber()) {
            return KAKAO_LOGIN_ALL;
        } else {
            return null;
        }
    }
}
