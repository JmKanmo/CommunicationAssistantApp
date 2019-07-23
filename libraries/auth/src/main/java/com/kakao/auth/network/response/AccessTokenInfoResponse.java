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
package com.kakao.auth.network.response;

import com.kakao.auth.StringSet;
import com.kakao.network.response.ApiResponseStatusError;
import com.kakao.network.response.JSONObjectResponse;
import com.kakao.network.response.ResponseBody;
import com.kakao.network.response.ResponseData;
import com.kakao.network.response.ResponseStringConverter;


/**
 * @author leoshin, created at 15. 8. 10..
 */
public class AccessTokenInfoResponse extends JSONObjectResponse {
    private final long userId;
    private final long expiresInMillis;

    public AccessTokenInfoResponse(String stringData) throws ResponseBody.ResponseBodyException {
        super(stringData);
        userId = getBody().getLong(StringSet.id);
        expiresInMillis = getBody().getLong(StringSet.expiresInMillis);
    }

    @Deprecated
    public AccessTokenInfoResponse(ResponseData responseData) throws ResponseBody.ResponseBodyException, ApiResponseStatusError {
        super(responseData);
        userId = getBody().getLong(StringSet.id);
        expiresInMillis = getBody().getLong(StringSet.expiresInMillis);
    }

    public long getUserId() {
        return userId;
    }

    public long getExpiresInMillis() {
        return expiresInMillis;
    }

    @Override
    public String toString() {
        return "AccessTokenInfoResponse{" + "userId=" + userId +
                ", expiresInMillis=" + expiresInMillis +
                '}';
    }

    public static final ResponseStringConverter<AccessTokenInfoResponse> CONVERTER = new ResponseStringConverter<AccessTokenInfoResponse>() {
        @Override
        public AccessTokenInfoResponse convert(String o) throws ResponseBody.ResponseBodyException {
            return new AccessTokenInfoResponse(o);
        }
    };

}
