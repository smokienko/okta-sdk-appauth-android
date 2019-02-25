package com.okta.android;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import com.okta.android.requests.ConfigurationRequest;
import com.okta.android.requests.TokeExchangeRequest;
import com.okta.android.results.AuthorizationResult;
import com.okta.appauth.android.AuthenticationPayload;
import com.okta.appauth.android.Tokens;
import com.okta.openid.appauth.*;
import com.okta.openid.appauth.internal.Logger;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class Okta {

    private static final String TAG = Okta.class.getSimpleName();
    private final OktaConfig config;
    private final OktaStorage storage;
    private final OktaSate state;
    @ColorRes
    private final int color;


    Okta(OktaConfig config, OktaStorage storage, int color) {
        this.config = config;
        this.storage = storage;
        this.color = color;
        this.state = new OktaSate(storage);
    }

    public AuthorizationResult authenticateWithBrowser(Context context, AuthenticationPayload payload) {

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Intent> resultIntent = new AtomicReference<>();
        LocalIntentBus.IntentObserver observer = intent -> {
            resultIntent.set(intent);
            latch.countDown();
        };
        LocalIntentBus.getInstance().register(LocalIntentBus.BROWSER_AUTH_CHANNEL, observer);

        if (resultIntent.get() == null) {
            if (!isRedirectRegistered(context, config.getRedirecUri())) {
                return AuthorizationResult.error(
                        new OktaException("No redirec activity registered registered for URI "
                                + config.getRedirecUri()));
            }
            try {
                obtainConfiguration();
            } catch (AuthorizationException e) {
                return AuthorizationResult.error(new OktaException(e.errorDescription, e));
            }
            AuthorizationRequest request = createAuthRequest(payload);
            storage.saveAuthorizationRequest(request);
            Intent intent = OktaAuthenticateActivity.createAuthIntent(context, request, color);
            context.startActivity(intent);
            try {
                latch.await();
            } catch (InterruptedException e) {
                return AuthorizationResult.error(new OktaException("Thread have been interrupted"));
            }
        }

        Intent intent = resultIntent.get();

        if (intent.getBooleanExtra(OktaAuthenticateActivity.EXTRA_RESULT, false)) {

            Uri responseUri = intent.getData();
            Intent responseData = extractResponseData(responseUri);

            AuthorizationManagementResponse response =
                    AuthorizationManagementResponse.fromIntent(responseData);

            if (response instanceof AuthorizationResponse) {
                TokeExchangeRequest tokeExchangeRequest = new TokeExchangeRequest(
                        (AuthorizationResponse) response,
                        config.getClientId(),
                        storage.getOktaConfiguration());

                try {
                    Tokens tokens = tokeExchangeRequest.execute();
                    storage.saveTokens(tokens);
                    return AuthorizationResult.success(tokens);
                } catch (AuthorizationException e) {
                    return AuthorizationResult.error(new OktaException(e.error, e));
                }
            }

            AuthorizationException ex = AuthorizationException.fromIntent(intent);

            return AuthorizationResult.error(new OktaException(ex.error, ex));
        } else {
            return AuthorizationResult.error(new OktaException("Authentication have been canceled"));
        }
    }

    public OktaSate provideOktaState() {
        return state;
    }


    private void obtainConfiguration() throws AuthorizationException {
        if (!state.isConfigured()) {
            AuthorizationServiceConfiguration configuration = new ConfigurationRequest(config).execute();
            storage.saveOktaConfiguration(configuration);
        }
    }

    private boolean isRedirectRegistered(Context context, @NonNull Uri uri) {
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfos = null;
        if (pm != null) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(uri);
            resolveInfos = pm.queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER);
        }
        boolean found = false;
        if (resolveInfos != null) {
            for (ResolveInfo info : resolveInfos) {
                ActivityInfo activityInfo = info.activityInfo;
                if (activityInfo.name.equals(OktaRedirectActivity.class.getCanonicalName()) &&
                        activityInfo.packageName.equals(context.getPackageName())) {
                    found = true;
                } else {
                    Log.w(TAG, "Warning! Multiple applications found registered with same scheme");
                    //Another installed app have same url scheme.
                    //return false as if no activity found to prevent hijacking of redirect.
                    return false;
                }
            }
        }
        return found;
    }

    private AuthorizationRequest createAuthRequest(AuthenticationPayload payload) {
        AuthorizationRequest.Builder authRequestBuilder = new AuthorizationRequest.Builder(
                storage.getOktaConfiguration(),
                config.getClientId(),
                ResponseTypeValues.CODE,
                config.getRedirecUri())
                .setScopes(config.getScopes());

        if (payload != null) {
            authRequestBuilder.setAdditionalParameters(payload.getAdditionalParameters());
            if (!TextUtils.isEmpty(payload.toString())) {
                authRequestBuilder.setState(payload.getState());
            }
            if (!TextUtils.isEmpty(payload.getLoginHint())) {
                authRequestBuilder.setLoginHint(payload.getLoginHint());
            }
        }
        return authRequestBuilder.build();
    }

    private Intent extractResponseData(Uri responseUri) {
        if (responseUri.getQueryParameterNames().contains(AuthorizationException.PARAM_ERROR)) {
            return AuthorizationException.fromOAuthRedirect(responseUri).toIntent();
        } else {
            //TODO mAuthRequest is null if Activity is destroyed.
            AuthorizationRequest authRequest = storage.getAuthorizationRequest();
            if (authRequest == null) {

            }
            AuthorizationManagementResponse response = AuthorizationManagementResponse
                    .buildFromRequest(authRequest, responseUri);

            if (authRequest.getState() == null && response.getState() != null
                    || (authRequest.getState() != null && !authRequest.getState()
                    .equals(response.getState()))) {

                Logger.warn("State returned in authorization response (%s) does not match state "
                                + "from request (%s) - discarding response",
                        response.getState(),
                        authRequest.getState());

                return AuthorizationException.AuthorizationRequestErrors.STATE_MISMATCH.toIntent();
            }
            return response.toIntent();
        }
    }
}
