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

/**
 * Enum class for the position of currency unit in currency representation. This feature is not yet
 * supported by Commerce message.
 *
 * @author kevin.kang. Created on 2017. 6. 14..
 */

public enum CurrencyUnitPosition {
    /**
     * 20,000 won, etc.
     */
    REAR(0),

    /**
     * $600, etc.
     */
    FRONT(1);

    final Integer value;

    CurrencyUnitPosition(final Integer value) {
        this.value = value;
    }

    Integer getValue() {
        return value;
    }
}
