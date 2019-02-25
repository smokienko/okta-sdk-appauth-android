package com.okta.android.requests;

import android.net.Uri;
import com.okta.android.OktaConfig;
import com.okta.android.requests.http.HttpRequest;
import com.okta.android.requests.http.HttpResponse;
import com.okta.openid.appauth.AuthorizationException;
import com.okta.openid.appauth.AuthorizationServiceConfiguration;
import com.okta.openid.appauth.AuthorizationServiceDiscovery;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ConfigurationRequest {

    private static final String TAG = ConfigurationRequest.class.getSimpleName();

    private final OktaConfig oktaConfig;

    private static final String OIDC_DISCOVERY = ".well-known/openid-configuration";

    public ConfigurationRequest(OktaConfig oktaConfig) {
        this.oktaConfig = oktaConfig;
    }

    private Uri getDiscoveryUri() {
        return oktaConfig.getIssuerUri().buildUpon().appendEncodedPath(OIDC_DISCOVERY).build();
    }

    public AuthorizationServiceConfiguration execute() throws AuthorizationException {
        AuthorizationException exception = null;
        HttpResponse response = null;
        try {
            response = new HttpRequest.Builder().setRequestMethod(HttpRequest.RequestMethod.GET)
                    .setUri(getDiscoveryUri())
                    .create()
                    .executeRequest();
            JSONObject json = response.asJson();
            AuthorizationServiceDiscovery discovery =
                    new AuthorizationServiceDiscovery(json);
            return new AuthorizationServiceConfiguration(discovery);
        } catch (IOException ex) {
            exception = AuthorizationException.fromTemplate(
                    AuthorizationException.GeneralErrors.NETWORK_ERROR,
                    ex);
        } catch (JSONException ex) {
            exception = AuthorizationException.fromTemplate(
                    AuthorizationException.GeneralErrors.JSON_DESERIALIZATION_ERROR,
                    ex);
        } catch (AuthorizationServiceDiscovery.MissingArgumentException ex) {
            exception = AuthorizationException.fromTemplate(
                    AuthorizationException.GeneralErrors.INVALID_DISCOVERY_DOCUMENT,
                    ex);
        } finally {
            if (response != null) {
                response.disconnect();
            }
            if (exception != null) {
                throw exception;
            }
        }
        throw new IllegalStateException("Unexpected exception happened");
    }

}
