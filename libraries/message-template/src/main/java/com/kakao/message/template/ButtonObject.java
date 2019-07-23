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
 * Class representing clickable buttons in message template v2.
 *
 * @author kevin.kang. Created on 2017. 3. 10..
 */

public class ButtonObject {
    private final String title;
    private final LinkObject link;

    public ButtonObject(final String title, final LinkObject link) {
        this.title = title;
        this.link = link;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(MessageTemplateProtocol.TITLE, title);
        jsonObject.put(MessageTemplateProtocol.LINK, link.toJSONObject());
        return jsonObject;
    }

    public String getTitle() {
        return title;
    }

    public LinkObject getLink() {
        return link;
    }
}
