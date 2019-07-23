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
 * Class representing various social components of messages.
 *
 * @author kevin.kang. Created on 2017. 3. 10..
 */

public class SocialObject {
    private final Integer likeCount;
    private final Integer commentCount;
    private final Integer sharedCount;
    private final Integer viewCount;
    private final Integer subscriberCount;

    SocialObject(final Builder builder) {
        this.likeCount = builder.likeCount;
        this.commentCount = builder.commentCount;
        this.sharedCount = builder.sharedCount;
        this.viewCount = builder.viewCount;
        this.subscriberCount = builder.subscriberCount;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(MessageTemplateProtocol.LIKE_COUNT, likeCount);
        jsonObject.put(MessageTemplateProtocol.COMMENT_COUNT, commentCount);
        jsonObject.put(MessageTemplateProtocol.SHARED_COUNT, sharedCount);
        jsonObject.put(MessageTemplateProtocol.VIEW_COUNT, viewCount);
        jsonObject.put(MessageTemplateProtocol.SUBSCRIBER_COUNT, subscriberCount);
        return jsonObject;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public Integer getSharedCount() {
        return sharedCount;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public Integer getSubscriberCount() {
        return subscriberCount;
    }

    /**
     * Class for building social objects.
     */
    public static class Builder {
        private Integer likeCount;
        private Integer commentCount;
        private Integer sharedCount;
        private Integer viewCount;
        private Integer subscriberCount;

        public Builder setLikeCount(final int count) {
            this.likeCount = count;
            return this;
        }
        public Builder setCommentCount(final int count) {
            this.commentCount = count;
            return this;
        }
        public Builder setSharedCount(final int count) {
            this.sharedCount = count;
            return this;
        }
        public Builder setViewCount(final int count) {
            this.viewCount = count;
            return this;
        }
        public Builder setSubscriberCount(final int count) {
            this.subscriberCount = count;
            return this;
        }

        public SocialObject build() {
            return new SocialObject(this);
        }
    }
}
