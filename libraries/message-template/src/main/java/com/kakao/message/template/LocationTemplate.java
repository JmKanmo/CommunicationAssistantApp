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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that defines parameters for building location messages.
 *
 * @author kevin.kang. Created on 2017. 3. 13..
 */

public class LocationTemplate implements TemplateParams {
    private final ContentObject contentObject;
    private final SocialObject socialObject;
    private final List<ButtonObject> buttons;
    private final String address;
    private final String addressTitle;

    LocationTemplate(Builder builder) {
        this.address = builder.address;
        this.addressTitle = builder.addressTitle;
        this.contentObject = builder.contentObject;
        this.socialObject = builder.socialObject;
        this.buttons = builder.buttons;
    }

    @Override
    public String getObjectType() {
        return MessageTemplateProtocol.TYPE_LOCATION;
    }

    public JSONObject toJSONObject() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(MessageTemplateProtocol.OBJ_TYPE, getObjectType());
            jsonObject.put(MessageTemplateProtocol.ADDRESS, address);
            jsonObject.put(MessageTemplateProtocol.ADDRESS_TITLE, addressTitle);

            if (contentObject != null)
                jsonObject.put(MessageTemplateProtocol.CONTENT, contentObject.toJSONObject());
            if (socialObject != null)
                jsonObject.put(MessageTemplateProtocol.SOCIAL, socialObject.toJSONObject());
            if (buttons != null) {
                JSONArray buttonArray = new JSONArray();
                for (ButtonObject button : buttons) {
                    buttonArray.put(button.toJSONObject());
                }
                jsonObject.put(MessageTemplateProtocol.BUTTONS, buttonArray);
            }
            return jsonObject;
        } catch (JSONException e) {
            return null;
        }
    }

    public static FeedTemplate.Builder newBuilder(final ContentObject contentObject) {
        throw new UnsupportedOperationException("LocationTemplate does not support this method.");
    }

    public static Builder newBuilder(final String address, final ContentObject contentObject) {
        return new Builder(address, contentObject);
    }

    /**
     * Class for building location templates.
     */
    public static class Builder {
        private final ContentObject contentObject;
        private SocialObject socialObject;
        private List<ButtonObject> buttons;
        private final String address;
        private String addressTitle;

        Builder(final String address, final ContentObject contentObject) {
            this.address = address;
            this.contentObject = contentObject;
            this.buttons = new ArrayList<>();
        }

        public Builder setSocial(SocialObject socialObject) {
            this.socialObject = socialObject;
            return this;
        }

        public Builder addButton(ButtonObject buttonObject) {
            buttons.add(buttonObject);
            return this;
        }

        public Builder setAddressTitle(final String addressTitle) {
            this.addressTitle = addressTitle;
            return this;
        }

        public LocationTemplate build() {
            return new LocationTemplate(this);
        }
    }
}
