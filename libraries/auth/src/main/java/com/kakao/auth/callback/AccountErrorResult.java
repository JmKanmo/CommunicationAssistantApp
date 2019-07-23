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
import com.kakao.util.exception.KakaoException;

public class AccountErrorResult extends ErrorResult {
    final private AgeAuthStatus status;

    public AccountErrorResult(int statusCode) {
        super(new KakaoException(KakaoException.ErrorType.UNSPECIFIED_ERROR, "Age Authentication failure"));
        this.status = AgeAuthStatus.valueOf(statusCode);
    }

    public AccountErrorResult(int statusCode, Exception exception) {
        super(exception);
        if (exception != null && exception instanceof KakaoException && ((KakaoException) exception).getErrorType() == KakaoException.ErrorType.CANCELED_OPERATION) {
            this.status = AgeAuthStatus.CANCELED_OPERATION;
        } else {
            this.status = AgeAuthStatus.valueOf(statusCode);
        }
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public AgeAuthStatus getStatus() {
        return status;
    }

    public Exception getException() {
        return exception;
    }

    @Override
    public String toString() {
        return "AccountErrorResult{" +
                "status=" + status +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
