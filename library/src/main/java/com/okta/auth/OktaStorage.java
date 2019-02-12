package com.okta.auth;

import com.okta.openid.appauth.AuthorizationRequest;
import com.okta.openid.appauth.AuthorizationResponse;
import com.okta.openid.appauth.AuthorizationServiceConfiguration;

public interface OktaStorage {

    void saveAuthorizationRequest(AuthorizationRequest request);

    AuthorizationRequest getAuthorizationRequest();

    void saveAuthorizationResponse(AuthorizationResponse response);

    AuthorizationResponse getAuthorizationResponse();

    void saveOktaConfiguration(AuthorizationServiceConfiguration configuration);

    AuthorizationServiceConfiguration getOktaConfiguration();

}
