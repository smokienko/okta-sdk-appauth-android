package com.okta.android;

import com.okta.appauth.android.Tokens;

public class OktaSate {

    private final OktaStorage storage;

    OktaSate(OktaStorage storage) {
        this.storage = storage;
    }

    public boolean isConfigured(){
        return storage.getOktaConfiguration() != null;
    }

    public boolean isAuthenticatedd() {
        Tokens tokens = storage.getTokens();
        return tokens != null;
    }

    public boolean hasPendingAthentication(){
        return storage.getAuthorizationRequest() != null;
    }

    public boolean hasRefreshToken() {
        Tokens tokens = storage.getTokens();
        return tokens != null && tokens.getRefreshToken() != null;
    }

    public boolean hasAccessToken() {
        Tokens tokens = storage.getTokens();
        return tokens != null && tokens.getAccessToken() != null;
    }

    public Tokens getTokens() {
        return storage.getTokens();
    }

}