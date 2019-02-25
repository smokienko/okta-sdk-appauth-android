package com.okta.android;

import com.okta.appauth.android.Tokens;
import com.okta.openid.appauth.AuthorizationRequest;
import com.okta.openid.appauth.AuthorizationServiceConfiguration;

public interface OktaStorage {

    void saveOktaConfiguration(AuthorizationServiceConfiguration configuration);

    AuthorizationServiceConfiguration getOktaConfiguration();

    void saveTokens(Tokens tokens);

    Tokens getTokens();

    void saveAuthorizationRequest(AuthorizationRequest request);

    AuthorizationRequest getAuthorizationRequest();

}
