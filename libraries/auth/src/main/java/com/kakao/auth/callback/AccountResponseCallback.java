/**
 * Copyright 2014-2017 Kakao Corp.
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
package com.kakao.auth.callback;

import com.kakao.auth.AuthService.AgeAuthStatus;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;

/**
 * @author leoshin, created at 15. 8. 4..
 */
public abstract class AccountResponseCallback extends ResponseCallback<Integer> {
    public abstract void onAgeAuthFailure(AccountErrorResult status);

    @Override
    public void onFailure(ErrorResult errorResult) {
        onAgeAuthFailure(new AccountErrorResult(errorResult.getErrorCode(), errorResult.getException()));
    }

    @Override
    public void onSuccessForUiThread(Integer statusCode) {
        AgeAuthStatus status = AgeAuthStatus.valueOf(statusCode);
        if (status == AgeAuthStatus.SUCCESS || status == AgeAuthStatus.ALREADY_AGE_AUTHORIZED) {
            onSuccess(statusCode);
        } else {
            onAgeAuthFailure(new AccountErrorResult(statusCode));
        }
    }
}
