package com.okta.android;

import android.content.SharedPreferences;
import android.util.Log;
import com.okta.appauth.android.Tokens;
import com.okta.openid.appauth.AuthorizationRequest;
import com.okta.openid.appauth.AuthorizationResponse;
import com.okta.openid.appauth.AuthorizationServiceConfiguration;
import org.json.JSONException;

import static android.content.ContentValues.TAG;

public class SimpleOktaSrorage implements OktaStorage {

    private static final String AUTH_CONFIGURATION_KEY = "authConfigurationKey";
    private static final String AUTH_REQUEST_KEY = "authRequestKey";
    private static final String AUTH_ID_TOKEN_KEY = "authIdTokeKey";
    private static final String AUTH_REFRESH_TOKEN_KEY = "authRefreshTokenKey";
    private static final String AUTH_ACCESS_TOKEN_KEY = "authAccessTokenKey";

    private SharedPreferences prefs;

    private AuthorizationServiceConfiguration authConfiguration;
    private AuthorizationRequest authRequest;
    private Tokens tokens;

    public SimpleOktaSrorage(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    @Override
    public void saveOktaConfiguration(AuthorizationServiceConfiguration configuration) {
        prefs.edit().putString(AUTH_CONFIGURATION_KEY, configuration.toJsonString()).apply();
        authConfiguration = configuration;
    }

    @Override
    public AuthorizationServiceConfiguration getOktaConfiguration() {
        if (authConfiguration == null && prefs.contains(AUTH_CONFIGURATION_KEY)) {
            try {
                authConfiguration = AuthorizationServiceConfiguration
                        .fromJson(prefs.getString(AUTH_CONFIGURATION_KEY, ""));
            } catch (JSONException e) {
                Log.e(TAG, "saveAuthorizationResponse: ", e);
            }
        }
        return authConfiguration;
    }

    @Override
    public void saveTokens(Tokens tokens) {
        prefs.edit().putString(AUTH_ID_TOKEN_KEY, tokens.getIdToken()).apply();
        prefs.edit().putString(AUTH_REFRESH_TOKEN_KEY, tokens.getRefreshToken()).apply();
        prefs.edit().putString(AUTH_ACCESS_TOKEN_KEY, tokens.getAccessToken()).apply();
        this.tokens = tokens;
    }

    @Override
    public Tokens getTokens() {
        if (tokens == null && prefs.contains(AUTH_ID_TOKEN_KEY)) {
            tokens = new Tokens(
                    prefs.getString(AUTH_ID_TOKEN_KEY, null),
                    prefs.getString(AUTH_ACCESS_TOKEN_KEY, null),
                    prefs.getString(AUTH_ID_TOKEN_KEY, null)
            );
        }
        return tokens;
    }

    @Override
    public void saveAuthorizationRequest(AuthorizationRequest request) {
        prefs.edit().putString(AUTH_REQUEST_KEY, request.jsonSerializeString()).apply();
        authRequest = request;
    }

    @Override
    public AuthorizationRequest getAuthorizationRequest() {
        if (authRequest == null && prefs.contains(AUTH_REQUEST_KEY)) {
            try {
                authRequest = AuthorizationRequest
                        .jsonDeserialize(prefs.getString(AUTH_REQUEST_KEY, ""));
            } catch (JSONException e) {
                Log.e(TAG, "saveAuthorizationResponse: ", e);
            }
        }
        return authRequest;
    }
}
