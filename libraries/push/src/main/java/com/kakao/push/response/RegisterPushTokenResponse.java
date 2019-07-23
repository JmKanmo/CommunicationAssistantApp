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
package com.kakao.push.response;

import com.kakao.network.response.PrimitiveTypeResponse;
import com.kakao.network.response.ResponseBody;
import com.kakao.network.response.ResponseStringConverter;

/**
 * @author leoshin, created at 15. 8. 10..
 */
public class RegisterPushTokenResponse extends PrimitiveTypeResponse {
    private final int expiredAt;

    public RegisterPushTokenResponse(String stringData) {
        super(stringData);
        this.expiredAt = Integer.valueOf(stringData);
    }

    public int getExpiredAt() {
        return expiredAt;
    }

    public static final ResponseStringConverter<Integer> CONVERTER = new ResponseStringConverter<Integer>() {
        @Override
        public Integer convert(String o) throws ResponseBody.ResponseBodyException {
            return new RegisterPushTokenResponse(o).getExpiredAt();
        }
    };
}
