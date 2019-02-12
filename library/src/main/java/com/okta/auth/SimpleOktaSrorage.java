package com.okta.auth;

import android.content.SharedPreferences;
import android.util.Log;
import com.okta.openid.appauth.AuthorizationRequest;
import com.okta.openid.appauth.AuthorizationResponse;
import com.okta.openid.appauth.AuthorizationServiceConfiguration;
import org.json.JSONException;

import static android.content.ContentValues.TAG;

public class SimpleOktaSrorage implements OktaStorage {

    private static final String AUTH_REQUEST_KEY = "authRequestKey";
    private static final String AUTH_RESPONSE_KEY = "authResponseKey";
    private static final String AUTH_CONFIGURATION_KEY = "authConfigurationKey";

    private SharedPreferences prefs;

    private AuthorizationRequest authRequest;
    private AuthorizationResponse authResponse;
    private AuthorizationServiceConfiguration authConfiguration;

    SimpleOktaSrorage(SharedPreferences prefs) {
        this.prefs = prefs;
    }


    @Override
    public synchronized void saveAuthorizationRequest(AuthorizationRequest request) {
        prefs.edit().putString(AUTH_REQUEST_KEY, request.jsonSerializeString()).apply();
        authRequest = request;
    }

    @Override
    public synchronized AuthorizationRequest getAuthorizationRequest() {
        if (authRequest == null && prefs.contains(AUTH_REQUEST_KEY)) {
            try {
                authRequest = AuthorizationRequest
                        .jsonDeserialize(prefs.getString(AUTH_REQUEST_KEY, ""));
            } catch (JSONException e) {
                Log.e(TAG, "getAuthorizationRequest: ", e);
            }
        }
        return authRequest;
    }

    @Override
    public synchronized void saveAuthorizationResponse(AuthorizationResponse response) {
        prefs.edit().putString(AUTH_RESPONSE_KEY, response.jsonSerializeString()).apply();
        authResponse = response;
    }

    @Override
    public synchronized AuthorizationResponse getAuthorizationResponse() {
        if (authResponse == null && prefs.contains(AUTH_RESPONSE_KEY)) {
            try {
                authResponse = AuthorizationResponse
                        .jsonDeserialize(prefs.getString(AUTH_RESPONSE_KEY, ""));
            } catch (JSONException e) {
                Log.e(TAG, "saveAuthorizationResponse: ", e);
            }
        }
        return authResponse;
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
}
