package com.okta.android;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.Log;
import com.okta.appauth.android.Tokens;
import com.okta.openid.appauth.AuthorizationRequest;
import com.okta.openid.appauth.AuthorizationServiceConfiguration;
import org.json.JSONException;

import static android.content.ContentValues.TAG;

public class SimpleOktaSrorage implements OktaStorage {

    private SharedPreferences prefs;

    public SimpleOktaSrorage(SharedPreferences prefs) {
        this.prefs = prefs;
    }


    @Override
    public void save(String key, String value) {
        prefs.edit().putString(key, value).apply();
    }

    @Override
    @Nullable
    public String get(String key) {
        return prefs.getString(key, null);
    }

    @Override
    public void delete(String key) {
        prefs.edit().putString(key, null).apply();
    }
}
