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
 * Class that defines parameters for building feed messages.
 * @author kevin.kang. Created on 2017. 3. 10..
 */

public class FeedTemplate implements TemplateParams {
    private final ContentObject contentObject;

    public SocialObject getSocial() {
        return socialObject;
    }

    public List<ButtonObject> getButtons() {
        return buttons;
    }

    private final SocialObject socialObject;
    private final List<ButtonObject> buttons;

    FeedTemplate(Builder builder) {
        this.contentObject = builder.contentObject;
        this.socialObject = builder.socialObject;
        this.buttons = builder.buttons;
    }

    @Override
    public String getObjectType() {
        return MessageTemplateProtocol.TYPE_FEED;
    }

    public JSONObject toJSONObject() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(MessageTemplateProtocol.OBJ_TYPE, getObjectType());
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

    public static Builder newBuilder(final ContentObject contentObject) {
        return new Builder(contentObject);
    }

    public ContentObject getContentObject() {
        return contentObject;
    }

    /**
     * Class for building feed templates.
     */
    public static class Builder {
        ContentObject contentObject;
        SocialObject socialObject;
        List<ButtonObject> buttons;

        public Builder(final ContentObject contentObject) {
            this.contentObject = contentObject;
            this.buttons = new ArrayList<>();
        }

        public Builder setSocial(SocialObject socialObject) {
            this.socialObject = socialObject;
            return this;
        }

        public Builder addButton(ButtonObject buttonObject) {
            if (buttonObject != null) {
                buttons.add(buttonObject);
            }
            return this;
        }

        public FeedTemplate build() {
            return new FeedTemplate(this);
        }
    }
}
