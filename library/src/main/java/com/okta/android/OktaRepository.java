package com.okta.android;

import android.util.Log;
import com.okta.appauth.android.Tokens;
import com.okta.openid.appauth.AuthorizationRequest;
import com.okta.openid.appauth.AuthorizationServiceConfiguration;
import org.json.JSONException;

import static android.content.ContentValues.TAG;

public class OktaRepository {
    private static final String AUTH_CONFIGURATION_KEY = "authConfigurationKey";
    private static final String AUTH_REQUEST_KEY = "authRequestKey";
    private static final String AUTH_ID_TOKEN_KEY = "authIdTokeKey";
    private static final String AUTH_REFRESH_TOKEN_KEY = "authRefreshTokenKey";
    private static final String AUTH_ACCESS_TOKEN_KEY = "authAccessTokenKey";

    private final OktaStorage storage;

    private AuthorizationServiceConfiguration authConfiguration;
    private AuthorizationRequest authRequest;
    private Tokens tokens;
    private final Object tokensLock = new Object();
    private final Object authRequestLock = new Object();
    private final Object authConfigurationLock = new Object();

    public OktaRepository(OktaStorage storage) {
        this.storage = storage;
    }


    public void saveOktaConfiguration(AuthorizationServiceConfiguration configuration) {
        synchronized (authConfigurationLock) {
            storage.save(AUTH_CONFIGURATION_KEY, configuration.toJsonString());
            authConfiguration = configuration;
        }
    }

    public AuthorizationServiceConfiguration getOktaConfiguration() {
        synchronized (authConfigurationLock) {
            if (authConfiguration == null && storage.get(AUTH_CONFIGURATION_KEY) != null) {
                try {
                    authConfiguration = AuthorizationServiceConfiguration
                            .fromJson(storage.get(AUTH_CONFIGURATION_KEY));
                } catch (JSONException e) {
                    Log.e(TAG, "saveAuthorizationResponse: ", e);
                }
            }
            return authConfiguration;
        }
    }

    public void saveTokens(Tokens tokens) {
        synchronized (tokensLock) {
            storage.save(AUTH_ID_TOKEN_KEY, tokens.getIdToken());
            storage.save(AUTH_REFRESH_TOKEN_KEY, tokens.getRefreshToken());
            storage.save(AUTH_ACCESS_TOKEN_KEY, tokens.getAccessToken());
            this.tokens = tokens;
        }
    }

    public Tokens getTokens() {
        synchronized (tokensLock) {
            if (tokens == null && storage.get(AUTH_ID_TOKEN_KEY) != null) {
                tokens = new Tokens(
                        storage.get(AUTH_ID_TOKEN_KEY),
                        storage.get(AUTH_ACCESS_TOKEN_KEY),
                        storage.get(AUTH_ID_TOKEN_KEY)
                );
            }
            return tokens;
        }
    }

    public void saveAuthorizationRequest(AuthorizationRequest request) {
        synchronized (authRequestLock) {
            storage.save(AUTH_REQUEST_KEY, request.jsonSerializeString());
            authRequest = request;
        }
    }

    public AuthorizationRequest getAuthorizationRequest() {
        synchronized (authRequestLock) {
            if (authRequest == null && storage.get(AUTH_REQUEST_KEY) != null) {
                try {
                    authRequest = AuthorizationRequest
                            .jsonDeserialize(storage.get(AUTH_REQUEST_KEY));
                } catch (JSONException e) {
                    Log.e(TAG, "saveAuthorizationResponse: ", e);
                }
            }
            return authRequest;
        }
    }
}
