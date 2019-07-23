package com.kakao.test.common;

import com.kakao.common.IConfiguration;

import org.json.JSONObject;

public class TestAppConfiguration implements IConfiguration {
    @Override
    public String getKeyHash() {
        return "sample_key_hash";
    }

    @Override
    public String getKAHeader() {
        return "sample_ka_header";
    }

    @Override
    public String getPackageName() {
        return "sample_package_name";
    }

    @Override
    public String getAppVer() {
        return "sample_app_ver";
    }

    @Override
    public String getExtras() {
        return "";
    }

    @Override
    public JSONObject getExtrasJson() {
        return new JSONObject();
    }
}
