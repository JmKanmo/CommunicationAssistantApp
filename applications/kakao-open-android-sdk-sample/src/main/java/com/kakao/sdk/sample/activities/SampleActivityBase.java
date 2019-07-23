/*
* Copyright 2013 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.kakao.sdk.sample.activities;

import android.os.Bundle;

import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.kakao.sdk.sample.R;


/**
 * Base launcher activity, to handle most of the common plumbing for samples.
 */
public class SampleActivityBase extends FragmentActivity {

    public static final String TAG = "SampleActivityBase";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("PARK","샘플액티비티초기화");
        super.onCreate(savedInstanceState);
    }

    @Override
    protected  void onStart() {
        super.onStart();

    }

    /** Set up targets to receive log data */
}

