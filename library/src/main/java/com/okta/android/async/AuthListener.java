package com.okta.android.async;

import com.okta.android.OktaException;
import com.okta.appauth.android.Tokens;

public interface AuthListener {
    void onSuccess(Tokens tokens);
    void onError(OktaException error);
}
