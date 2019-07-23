package com.kakao.util.apicompatibility;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 * @author leoshin on 15. 11. 4.
 */
public abstract class APICompatibility {
    private static volatile APICompatibility instance;

    public static APICompatibility getInstance() {
        if (instance == null) {
            synchronized (APICompatibility.class) {
                if (instance == null) {
                    int sdkVersion = Build.VERSION.SDK_INT;

                    if (sdkVersion > Build.VERSION_CODES.KITKAT_WATCH) {
                        instance = new APILevel21Compatibility();
                    } else if (sdkVersion > Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        instance = new APILevel19Compatibility();
                    } else {
                        instance = new APILevel9Compatibility();
                    }
                }
            }
        }

        return instance;
    }

    public abstract String getSmsMessage(Intent intent);
}
