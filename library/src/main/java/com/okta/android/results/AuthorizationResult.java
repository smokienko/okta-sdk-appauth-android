package com.okta.android.results;

import com.okta.android.OktaException;
import com.okta.appauth.android.Tokens;

public class AuthorizationResult extends Result {

    private final Tokens tokens;

    public static AuthorizationResult success(Tokens tokens) {
        return new AuthorizationResult(null, tokens);
    }

    public static AuthorizationResult error(OktaException error) {
        return new AuthorizationResult(error, null);
    }

    AuthorizationResult(OktaException error, Tokens tokens) {
        super(error);
        this.tokens = tokens;
    }

    public Tokens getTokens() {
        return tokens;
    }
}
