/*
  Copyright 2017 Kakao Corp.

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
package com.kakao.message.template;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class representing links for various platforms in message template v2.
 *
 * @author kevin.kang. Created on 2017. 3. 10..
 */

public class LinkObject {
    private final String webUrl;
    private final String mobileWebUrl;
    private final String androidExecutionParams;
    private final String iosExecutionParams;


    LinkObject(final Builder builder) {
        this.webUrl = builder.webUrl;
        this.mobileWebUrl = builder.mobileWebUrl;
        this.androidExecutionParams = builder.androidExecutionParams;
        this.iosExecutionParams = builder.iosExecutionParams;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(MessageTemplateProtocol.WEB_URL, webUrl);
        jsonObject.put(MessageTemplateProtocol.MOBILE_WEB_URL, mobileWebUrl);
        jsonObject.put(MessageTemplateProtocol.ANDROID_PARAMS, androidExecutionParams);
        jsonObject.put(MessageTemplateProtocol.IOS_PARAMS, iosExecutionParams);
        return jsonObject;

    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getWebUrl() {
        return webUrl;
    }

    public String getMobileWebUrl() {
        return mobileWebUrl;
    }

    public String getAndroidExecutionParams() {
        return androidExecutionParams;
    }

    public String getIosExecutionParams() {
        return iosExecutionParams;
    }

    /**
     * Class for building link objects.
     */
    public static class Builder {
        private String webUrl;
        private String mobileWebUrl;
        private String androidExecutionParams;
        private String iosExecutionParams;

        public Builder setWebUrl(final String url) {
            this.webUrl = url;
            return this;
        }

        public Builder setMobileWebUrl(final String url) {
            this.mobileWebUrl = url;
            return this;
        }

        public Builder setAndroidExecutionParams(final String params) {
            this.androidExecutionParams = params;
            return this;
        }

        public Builder setIosExecutionParams(final String params) {
            this.iosExecutionParams = params;
            return this;
        }

        public LinkObject build() {
            return new LinkObject(this);
        }
    }
}
