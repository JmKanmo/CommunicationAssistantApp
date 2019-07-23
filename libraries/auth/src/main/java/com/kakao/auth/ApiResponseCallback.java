/**
 * Copyright 2014-2015 Kakao Corp.
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

import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;

/**
 * API요청에 대한 공통 Callback class.
 * 각 API서비스는 해당 클래스를 상속하여 callback을 구현하게 된다.
 * @author leoshin, created at 15. 8. 4..
 */
public abstract class ApiResponseCallback<T> extends ResponseCallback<T> {

    /**
     * 세션이 닫혔을때 불리는 callback
     * @param errorResult errorResult
     */
    public abstract void onSessionClosed(ErrorResult errorResult);

    /**
     * 세션 오픈은 성공했으나 사용자 정보 요청 결과 사용자 가입이 안된 상태로
     * 일반적으로 가입창으로 이동한다.
     * 자동 가입 앱이 아닌 경우에만 호출된다.
     */
    public abstract void onNotSignedUp();

    @Override
    public void onFailure(ErrorResult errorResult) {
        /*
            이 메소드는 abstract여야 하는데 override되어 있어서 third-party들이 구현하지 않을 가능성이 있다.
            개선 필요.
        */
    }

    public void onFailureForUiThread(ErrorResult errorResult) {
        int result = errorResult.getErrorCode();
        if (result == ApiErrorCode.NOT_REGISTERED_USER_CODE) {
            onNotSignedUp();
        } else if(result == ApiErrorCode.INVALID_TOKEN_CODE) {
            onSessionClosed(errorResult);
        } else {
            super.onFailureForUiThread(errorResult);
        }
    }
}
