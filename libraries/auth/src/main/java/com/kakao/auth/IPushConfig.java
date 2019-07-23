package com.kakao.auth;

/**
 * 푸시를 사용하기 위해 사용되는 정보값을 받아오기 위한 클래스.
 * @author kevin.kang
 * Created by kevin.kang on 2017. 1. 31..
 */

public interface IPushConfig {
    String getDeviceUUID();
    ApiResponseCallback<Integer> getTokenRegisterCallback();
}
