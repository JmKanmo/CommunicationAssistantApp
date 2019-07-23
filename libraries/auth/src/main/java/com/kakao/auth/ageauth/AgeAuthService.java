package com.kakao.auth.ageauth;

import android.content.Context;
import android.os.Bundle;

/**
 * @author kevin.kang. Created on 2017. 11. 28..
 */

public interface AgeAuthService {
    int requestAgeAuth(final Bundle ageAuthParams, final Context context);
}
