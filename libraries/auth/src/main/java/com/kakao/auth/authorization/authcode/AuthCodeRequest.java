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
package com.kakao.auth.authorization.authcode;

import android.net.Uri;

import com.kakao.auth.AuthCodeCallback;
import com.kakao.auth.network.request.AuthRequest;

/**
 * @author leoshin, created at 15. 7. 13..
 */
class AuthCodeRequest extends AuthRequest {
    final private AuthCodeCallback callback;

    private Uri accountUri;
    private Integer requestCode;

    public AuthCodeRequest(String appKey, String redirectURI, Integer requestCode, final AuthCodeCallback callback) {
        super(appKey, redirectURI);
        this.callback = callback;
        this.requestCode = requestCode;
    }

    public void setAccountUri(final Uri accountUri) {
        this.accountUri = accountUri;
    }

    public Uri getAccountUri() {
        return accountUri;
    }

    public AuthCodeCallback getCallback() {
        return callback;
    }

    public Integer getRequestCode() {
        return requestCode;
    }
}
