package com.okta.android;

import com.okta.appauth.android.Tokens;

public class OktaSate {

    private final OktaRepository repository;

    OktaSate(OktaRepository repository) {
        this.repository = repository;
    }

    public boolean isConfigured(){
        return repository.getOktaConfiguration() != null;
    }

    public boolean isAuthenticatedd() {
        Tokens tokens = repository.getTokens();
        return tokens != null;
    }

    public boolean hasPendingAuthentication(){
        return LocalIntentBus.getInstance().containsPending(LocalIntentBus.BROWSER_AUTH_CHANNEL);
    }

    public boolean hasRefreshToken() {
        Tokens tokens = repository.getTokens();
        return tokens != null && tokens.getRefreshToken() != null;
    }

    public boolean hasAccessToken() {
        Tokens tokens = repository.getTokens();
        return tokens != null && tokens.getAccessToken() != null;
    }

    public Tokens getTokens() {
        return repository.getTokens();
    }

    public void clear() {
        repository.saveAuthorizationRequest(null);
        repository.saveTokens(null);
        repository.saveOktaConfiguration(null);
    }

}
