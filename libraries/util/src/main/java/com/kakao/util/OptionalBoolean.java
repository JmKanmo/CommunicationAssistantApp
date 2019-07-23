package com.kakao.util;

/**
 * Enum class for representing nullable boolean values, usually in API responses. For example,
 * the following API responses will translate to a variable (in a wrapper class) with enum type
 * {@link OptionalBoolean} like below:
 *
 * {"has_email": true} = {@link OptionalBoolean#TRUE}
 * {"has_email": false} = {@link OptionalBoolean#FALSE}
 * {} = {@link OptionalBoolean#NONE}
 *
 * @author kevin.kang. Created on 2018. 4. 26..
 */
public enum OptionalBoolean {
    /**
     * Represents true
     */
    TRUE(true),
    /**
     * Represents false
     */
    FALSE(false),
    /**
     * Represents no value
     */
    NONE(null);

    final Boolean bool;

    OptionalBoolean(Boolean bool) {
        this.bool = bool;
    }

    public static OptionalBoolean getOptionalBoolean(Boolean bool) {
        if (bool == null) {
            return NONE;
        } else if (bool) {
            return TRUE;
        } else {
            return FALSE;
        }
    }

    public Boolean getBoolean() {
        return bool;
    }
}
