package com.kakao.util.helper;

import android.os.Bundle;

import java.util.Date;
import java.util.List;

/**
 * Used for storing user data for performance or persisting user login. First created to abstract
 * SharedPreferencesCache class.
 *
 * @author kevin.kang. Created on 2017. 7. 25..
 */

public interface PersistentKVStore {

    /**
     * Retrieves value for the given key
     *
     * @param key key
     * @return value
     */
    String getString(String key);

    /**
     * Retrieves the value for the given key in date format. Usually used to retrieve expire time
     * for access, refresh token, etc.
     *
     * @param key key
     * @return time in Date format
     */
    Date getDate(String key);

    /**
     *
     * @param bundle Bundle object containing key values to store
     */
    void save(Bundle bundle);

    /**
     * Deletes multiple keys from KV store.
     *
     * @param keys List of keys to be deleted from KV store
     */
    void clear(List<String> keys);

    /**
     * Deletes all keys from KV store.
     */
    void clearAll();
}
