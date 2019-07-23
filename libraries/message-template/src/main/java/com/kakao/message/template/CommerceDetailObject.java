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
 * Class that defines commerce detail for commerce template.
 *
 * @author kevin.kang. Created on 2017. 6. 14..
 */

public class CommerceDetailObject {
    private final Integer regularPrice;
//    private final String currencyUnit;
//    private final CurrencyUnitPosition unitPosition;
    private final Integer discountPrice;
    private final Integer discountRate;
    private final Integer fixedDiscountPrice;

    public CommerceDetailObject(Builder builder) {
        regularPrice = builder.regularPrice;
//        currencyUnit = builder.currencyUnit;
//        unitPosition = builder.unitPosition;
        discountPrice = builder.discountPrice;
        discountRate = builder.discountRate;
        fixedDiscountPrice = builder.fixedDiscountPrice;
    }

    public static Builder newBuilder(final Integer regularPrice) {
        return new Builder(regularPrice);
    }

    public JSONObject toJSONObject() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(MessageTemplateProtocol.REGULAR_PRICE, regularPrice);
//            jsonObject.put(MessageTemplateProtocol.CURRENCY_UNIT, currencyUnit);
//            if (unitPosition != null)
//                jsonObject.put(MessageTemplateProtocol.CURRENCY_UNIT_POSITION, unitPosition.getValue());
            jsonObject.put(MessageTemplateProtocol.DISCOUNT_PRICE, discountPrice);
            jsonObject.put(MessageTemplateProtocol.DISCOUNT_RATE, discountRate);
            jsonObject.put(MessageTemplateProtocol.FIXED_DISCOUNT_PRICE, fixedDiscountPrice);
            return jsonObject;
        } catch (JSONException e) {
            return null;
        }
    }

    /**
     * Class for building commerce detail objects.
     */
    public static class Builder {
        private Integer regularPrice;
//        private String currencyUnit;
//        private CurrencyUnitPosition unitPosition;
        private Integer discountPrice;
        private Integer discountRate;
        private Integer fixedDiscountPrice;

        Builder(final Integer regularPrice) {
            this.regularPrice = regularPrice;
        }
        
//        public Builder setCurrencyUnit(String currencyUnit) {
//            this.currencyUnit = currencyUnit;
//            return this;
//        }
//
//        public Builder setCurrencyUnitPosition(CurrencyUnitPosition unitPosition) {
//            this.unitPosition = unitPosition;
//            return this;
//        }

        public Builder setDiscountPrice(Integer discountPrice) {
            this.discountPrice = discountPrice;
            return this;
        }

        public Builder setDiscountRate(Integer discountRate) {
            this.discountRate = discountRate;
            return this;
        }

        public Builder setFixedDiscountPrice(Integer fixedDiscountPrice) {
            this.fixedDiscountPrice = fixedDiscountPrice;
            return this;
        }

        public CommerceDetailObject build() {
            return new CommerceDetailObject(this);
        }
    }
}
