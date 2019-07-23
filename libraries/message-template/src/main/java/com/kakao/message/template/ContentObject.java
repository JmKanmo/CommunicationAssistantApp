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
 * Class that defines main content in both feed and list template. List tempalte contains a list
 * of content objects.
 *
 * @author kevin.kang. Created on 2017. 3. 10..
 */

public class ContentObject {
    private String title;
    private String imageUrl;
    private Integer imageWidth;
    private Integer imageHeight;
    private String description;
    private LinkObject link;

    public JSONObject toJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(MessageTemplateProtocol.TITLE, title);
        jsonObject.put(MessageTemplateProtocol.IMAGE_URL, imageUrl);
        jsonObject.put(MessageTemplateProtocol.IMAGE_WIDTH, imageWidth);
        jsonObject.put(MessageTemplateProtocol.IMAGE_HEIGHT, imageHeight);
        jsonObject.put(MessageTemplateProtocol.DESCRIPTION, description);
        jsonObject.put(MessageTemplateProtocol.LINK, link.toJSONObject());
        return jsonObject;
    }

    public static Builder newBuilder(final String title, final String imageUrl, final LinkObject link) {
        return new Builder(title, imageUrl, link);
    }

    private ContentObject(final Builder builder) {
        this.title = builder.title;
        this.imageUrl = builder.imageUrl;
        this.imageWidth = builder.imageWidth;
        this.imageHeight = builder.imageHeight;
        this.description = builder.description;
        this.link = builder.link;
    }

    public String getTitle() {
        return title;
    }

    @SuppressWarnings("WeakerAccess")
    public String getImageUrl() {
        return imageUrl;
    }

    @SuppressWarnings("WeakerAccess")
    public Integer getImageWidth() {
        return imageWidth;
    }

    @SuppressWarnings("WeakerAccess")
    public Integer getImageHeight() {
        return imageHeight;
    }

    @SuppressWarnings("WeakerAccess")
    public String getDescription() {
        return description;
    }

    @SuppressWarnings("WeakerAccess")
    public LinkObject getLink() {
        return link;
    }

    /**
     * Class for building content objects.
     */
    public static class Builder {
        private String title;
        private String imageUrl;
        private Integer imageWidth;
        private Integer imageHeight;
        private String description;
        private LinkObject link;

        public Builder(final String title, final String imageUrl, final LinkObject link) {
            this.title = title;
            this.imageUrl = imageUrl;
            this.link = link;
        }

        @SuppressWarnings("WeakerAccess")
        public Builder setImageWidth(final int width) {
            this.imageWidth = width;
            return this;
        }

        @SuppressWarnings("WeakerAccess")
        public Builder setImageHeight(final int height) {
            this.imageHeight = height;
            return this;
        }

        public Builder setDescrption(final String description) {
            this.description = description;
            return this;
        }

        public ContentObject build() {
            return new ContentObject(this);
        }
    }
}
