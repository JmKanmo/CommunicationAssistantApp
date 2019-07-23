/**
 * Copyright 2014-2015 Kakao Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kakao.auth;

import android.content.Context;

/**
 * This interface provides application context to Kakao SDK via getApplicationConfig() method in
 * {@link com.kakao.auth.KakaoAdapter}
 *
 * @author leo.shin
 * Created by leoshin on 15. 8. 17..
 */
public interface IApplicationConfig {
    /**
     * Returns application context of the application.
     * @return Application context
     */
    Context getApplicationContext();
}
