package com.okta.android.requests;

import com.okta.android.requests.http.HttpRequest;
import com.okta.android.requests.http.HttpResponse;
import com.okta.appauth.android.Tokens;
import com.okta.openid.appauth.*;
import com.okta.openid.appauth.internal.Logger;
import com.okta.openid.appauth.internal.UriUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

public class TokeExchangeRequest {

    private final AuthorizationResponse authorizationResponse;
    private final String clientId;
    private final AuthorizationServiceConfiguration configuration;

    public TokeExchangeRequest(AuthorizationResponse authorizationResponse,
                               String clientId,
                               AuthorizationServiceConfiguration configuration) {
        this.authorizationResponse = authorizationResponse;
        this.clientId = clientId;
        this.configuration = configuration;
    }

    public Tokens execute() throws AuthorizationException{
            HttpResponse response = null;
            try {
                TokenRequest tokenRequest = authorizationResponse.createTokenExchangeRequest();
                Map<String, String> parameters = tokenRequest.getRequestParameters();
                parameters.put(TokenRequest.PARAM_CLIENT_ID, clientId);

                response = new HttpRequest.Builder().setRequestMethod(HttpRequest.RequestMethod.POST)
                        .setUri(configuration.tokenEndpoint)
                        .setRequestProperty("Accept", "application/json")
                        .addPostParameters(parameters)
                        .create()
                        .executeRequest();

                JSONObject json = response.asJson();
                if (json.has(AuthorizationException.PARAM_ERROR)) {
                    try {
                        String error = json.getString(AuthorizationException.PARAM_ERROR);
                        throw AuthorizationException.fromOAuthTemplate(
                                AuthorizationException.TokenRequestErrors.byString(error),
                                error,
                                json.optString(AuthorizationException.PARAM_ERROR_DESCRIPTION, null),
                                UriUtil.parseUriIfAvailable(
                                        json.optString(AuthorizationException.PARAM_ERROR_URI)));
                    } catch (JSONException jsonEx) {
                        throw AuthorizationException.fromTemplate(
                                AuthorizationException.GeneralErrors.JSON_DESERIALIZATION_ERROR,
                                jsonEx);
                    }
                }

                TokenResponse tokenResponse;
                try {
                    tokenResponse = new TokenResponse.Builder(tokenRequest).fromResponseJson(json).build();
                } catch (JSONException jsonEx) {
                    throw AuthorizationException.fromTemplate(
                            AuthorizationException.GeneralErrors.JSON_DESERIALIZATION_ERROR,
                            jsonEx);
                }

                if (tokenResponse.idToken != null) {
                    IdToken idToken;
                    try {
                        idToken = IdToken.from(tokenResponse.idToken);
                    } catch (IdToken.IdTokenException | JSONException ex) {
                       throw AuthorizationException.fromTemplate(
                                        AuthorizationException.GeneralErrors.ID_TOKEN_PARSING_ERROR,
                                        ex);
                    }

                    idToken.validate(tokenRequest, SystemClock.INSTANCE);
                }

                return new Tokens(tokenResponse.idToken, tokenResponse.accessToken, tokenResponse.refreshToken);
            } catch (IOException ex) {
                Logger.debugWithStack(ex, "Failed to complete exchange request");
                throw  AuthorizationException.fromTemplate(
                        AuthorizationException.GeneralErrors.NETWORK_ERROR, ex);
            } catch (JSONException ex) {
                Logger.debugWithStack(ex, "Failed to complete exchange request");
                throw  AuthorizationException.fromTemplate(
                        AuthorizationException.GeneralErrors.JSON_DESERIALIZATION_ERROR, ex);
            } finally {
                if (response != null) {
                    response.disconnect();
                }
            }
    }
}
