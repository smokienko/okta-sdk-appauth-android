package com.okta.android.async;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.okta.android.Okta;
import com.okta.android.OktaSate;
import com.okta.android.results.AuthorizationResult;
import com.okta.appauth.android.AuthenticationPayload;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class OktaAsync {

    private final Okta okta;
    private final ExecutorService service = Executors.newSingleThreadExecutor();
    private final AtomicReference<AuthListener> authListenerWrapper = new AtomicReference<>();


    OktaAsync(Okta okta) {
        this.okta = okta;
    }

    public void authenticateWithBrowser(@NonNull Context context,
                                        @Nullable AuthenticationPayload payload,
                                        @NonNull AuthListener listener) {
        authListenerWrapper.set(listener);

        service.submit(() -> {

            AuthorizationResult result = okta.authenticateWithBrowser(context, payload);

            AuthListener resultListener = authListenerWrapper.get();
            if (resultListener != null) {
                if (result.isSuccess()) {
                    resultListener.onSuccess(result.getTokens());
                } else {
                    resultListener.onError(result.getError());
                }
            }

        });

    }

    public OktaSate provideAuthState() {
        return okta.provideOktaState();
    }

    public void dispose(){
        authListenerWrapper.set(null);
    }


}
