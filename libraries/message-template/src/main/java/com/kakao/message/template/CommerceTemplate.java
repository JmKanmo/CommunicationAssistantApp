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
 * Class that defines parameters for building commerce messages.
 *
 * @author kevin.kang. Created on 2017. 6. 14..
 */

public class CommerceTemplate implements TemplateParams {
    private final ContentObject contentObject;
    private final CommerceDetailObject commerceDetail;
    private final List<ButtonObject> buttons;

    CommerceTemplate(Builder builder) {
        this.contentObject = builder.contentObject;
        this.commerceDetail = builder.commerceDetail;
        this.buttons = builder.buttons;
    }

    @Override
    public String getObjectType() {
        return MessageTemplateProtocol.TYPE_COMMERCE;
    }

    @Override
    public JSONObject toJSONObject() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(MessageTemplateProtocol.OBJ_TYPE, getObjectType());
            if (contentObject != null)
                jsonObject.put(MessageTemplateProtocol.CONTENT, contentObject.toJSONObject());
            if (commerceDetail != null)
                jsonObject.put(MessageTemplateProtocol.COMMERCE, commerceDetail.toJSONObject());
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
    public static Builder newBuilder(final ContentObject contentObject, final CommerceDetailObject commerceDetail) {
        return new Builder(contentObject, commerceDetail);
    }

    /**
     * Class for building commerce templates.
     */
    public static class Builder {
        ContentObject contentObject;
        CommerceDetailObject commerceDetail;
        List<ButtonObject> buttons;


        public Builder(final ContentObject contentObject, final CommerceDetailObject commerceDetail) {
            this.contentObject = contentObject;
            this.commerceDetail = commerceDetail;
            this.buttons = new ArrayList<>();
        }

        public Builder addButton(ButtonObject buttonObject) {
            buttons.add(buttonObject);
            return this;
        }

        public CommerceTemplate build() {
            return new CommerceTemplate(this);
        }
    }
}
