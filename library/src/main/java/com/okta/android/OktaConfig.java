package com.okta.android;


import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

public class OktaConfig {
    private final String clientId;
    private final Uri redirecUri;
    private final Uri endSessionRedictUri;
    private final List<String> scopes;
    private final Uri issuerUri;

    private OktaConfig(String clientId, Uri redirecUri, Uri endSessionRedictUri, List<String> scopes, Uri issuerUri) {
        this.clientId = clientId;
        this.redirecUri = redirecUri;
        this.endSessionRedictUri = endSessionRedictUri;
        this.scopes = scopes;
        this.issuerUri = issuerUri;
    }

    public static class Builder {
        private String clientId;
        private Uri redirecUri;
        private Uri endSessionRedictUri;
        private List<String> scopes;
        private Uri issuerUri;

        public Builder withClientId(@NonNull String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder withRedirectUri(@NonNull String redirectUri) {
            this.redirecUri = Uri.parse(redirectUri);
            return this;
        }

        public Builder withEndSessionRedirectUri(@NonNull String endSessionRedictUri) {
            this.endSessionRedictUri = Uri.parse(endSessionRedictUri);
            return this;
        }

        public Builder withScopes(@NonNull String... scopes) {
            this.scopes = Arrays.asList(scopes);
            return this;
        }

        public Builder withIssuerUri(@NonNull String issuerUri) {
            this.issuerUri = Uri.parse(issuerUri);
            return this;
        }

        public OktaConfig build(){
            if (clientId == null) {
                throw new IllegalStateException("ClientId can not be null!");
            }
            if (redirecUri == null) {
                throw new IllegalStateException("RedirecUri can not be null!");
            }
            if (scopes == null || scopes.isEmpty()) {
                throw new IllegalStateException("Scopes can not be null or empty!");
            }
            if (issuerUri == null) {
                throw new IllegalStateException("IssuerUri can not be null!");
            }
            return new OktaConfig(clientId, redirecUri, endSessionRedictUri, scopes, issuerUri);
        }

    }

    public String getClientId() {
        return clientId;
    }

    public Uri getRedirecUri() {
        return redirecUri;
    }

    public Uri getEndSessionRedictUri() {
        return endSessionRedictUri;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public Uri getIssuerUri() {
        return issuerUri;
    }

}
