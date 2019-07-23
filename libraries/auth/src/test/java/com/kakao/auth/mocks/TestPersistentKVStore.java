package com.kakao.auth.mocks;

import android.os.Bundle;

import com.kakao.util.helper.PersistentKVStore;
import com.kakao.util.helper.log.Logger;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author kevin.kang. Created on 2017. 8. 8..
 */

public class TestPersistentKVStore implements PersistentKVStore {
    private Map<String, String> stringMap = new HashMap<>();
    private Map<String, Long> dateMap = new HashMap<>();

    @Override
    public String getString(String key) {
        return stringMap.get(key);
    }

    @Override
    public Date getDate(String key) {
        if (dateMap.get(key) == null) return null;
        return new Date(dateMap.get(key));
    }

    @Override
    public void save(Bundle bundle) {
        for (String key : bundle.keySet()) {
            if (bundle.get(key) instanceof Long) {
                dateMap.put(key, (Long) bundle.get(key));
            } else if (bundle.get(key) instanceof String) {
                stringMap.put(key, bundle.getString(key));
            }
        }
    }

    @Override
    public void clear(List<String> keys) {
        stringMap.clear();
        dateMap.clear();
    }

    @Override
    public void clearAll() {
        stringMap.clear();
        dateMap.clear();
    }
}
