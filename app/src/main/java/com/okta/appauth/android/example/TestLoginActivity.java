/*
 * Copyright (c) 2019, Okta, Inc. and/or its affiliates. All rights reserved.
 * The Okta software accompanied by this notice is provided pursuant to the Apache License,
 * Version 2.0 (the "License.")
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under the
 * License.
 */

package com.okta.appauth.android.example;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.MainThread;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.okta.android.*;
import com.okta.android.async.AsyncOktaFactory;
import com.okta.android.async.AuthListener;
import com.okta.android.async.OktaAsync;
import com.okta.android.results.AuthorizationResult;
import com.okta.appauth.android.Tokens;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Example Login Activity where authentication takes place.
 */
public class TestLoginActivity extends AppCompatActivity {

    private static final String TAG = "TestLoginActivity";


    private View mLoginLayout;
    private ProgressBar mAuthProgress;
    private Button mButton;
    private View mAuthorizedInfo;
    private TextView mIdToken;
    private TextView mAccessToken;
    private Okta okta;
    private OktaAsync oktaAsync;
    private ExecutorService loginExecutor = Executors.newSingleThreadExecutor();
    private Future loginTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity_login);
        mLoginLayout = findViewById(R.id.loading_container);
        mButton = findViewById(R.id.start_button);
        mIdToken = findViewById(R.id.id_token_info);
        mAccessToken = findViewById(R.id.access_token_info);
        mAuthorizedInfo = findViewById(R.id.auth_container);
        mAuthProgress = findViewById(R.id.progress_bar);

        mButton.setOnClickListener(v -> performAsyncBrowserLogin());
        initAsyncOkta();

        //In case our activity died during authentication
        //and have been recreated
//        if (okta.provideOktaState().hasPendingAuthentication()) {
//            performAsyncBrowserLogin();
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showAuthInfo();
    }

    private void performBrowserLogin() {
        loginTask = loginExecutor.submit(() -> {
            startLogin();
            AuthorizationResult result = okta.authenticateWithBrowser(this, null);
            stopLogin();
            if (result.isSuccess()) {
                showAuthInfo();
            } else {
                showSnackbar("Login error " + result.getError().getMessage());
            }
        });
    }

    private void performAsyncBrowserLogin() {
        startLogin();
        oktaAsync.authenticateWithBrowser(this, null, new AuthListener() {
            @Override
            public void onSuccess(Tokens tokens) {
                stopLogin();
                showAuthInfo();
            }

            @Override
            public void onError(OktaException error) {
                stopLogin();
                showSnackbar("Login error " + error.getMessage());
            }
        });
    }

    private void showAuthInfo() {
        mButton.post(() -> {
            OktaSate state = okta != null ? okta.provideOktaState() : oktaAsync.provideAuthState();
            if (state.isAuthenticatedd()) {
                mAccessToken.setText(state.getTokens().getAccessToken());
                mIdToken.setText(state.getTokens().getIdToken());
                mAuthorizedInfo.setVisibility(View.VISIBLE);
                mLoginLayout.setVisibility(View.GONE);
            }
        });
    }

    private void initOkta() {
        OktaConfig config = new OktaConfig.Builder()
                .withClientId("0oahnzhsegzYjqETc0h7")
                .withRedirectUri("com.lohika.android.test:/callback")
                .withEndSessionRedirectUri("com.lohika.android.test:/logout")
                .withScopes("openid", "profile", "offline_access")
                .withIssuerUri("https://lohika-um.oktapreview.com/oauth2/default")
                .build();

        okta = new OktaBuilder()
                .withOktaFactory(new PlainOktaFactory())
                .withConfig(config)
                .withStorage(new SimpleOktaSrorage(getPreferences(MODE_PRIVATE)))
                .build();
    }

    private void initAsyncOkta() {
        OktaConfig config = new OktaConfig.Builder()
                .withClientId("0oahnzhsegzYjqETc0h7")
                .withRedirectUri("com.lohika.android.test:/callback")
                .withEndSessionRedirectUri("com.lohika.android.test:/logout")
                .withScopes("openid", "profile", "offline_access")
                .withIssuerUri("https://lohika-um.oktapreview.com/oauth2/default")
                .build();

        oktaAsync = new OktaBuilder()
                .withOktaFactory(new AsyncOktaFactory())
                .withConfig(config)
                .withStorage(new SimpleOktaSrorage(getPreferences(MODE_PRIVATE)))
                .build();
    }

    @MainThread
    private void showSnackbar(String message) {
        mButton.post(() -> Snackbar.make(findViewById(R.id.coordinator),
                message,
                Snackbar.LENGTH_SHORT)
                .show());
    }

    private void startLogin() {
        mButton.post(() -> {
            mAuthProgress.setVisibility(View.VISIBLE);
            mButton.setEnabled(false);
        });
    }

    private void stopLogin() {
        mButton.post(() -> {
            mAuthProgress.setVisibility(View.INVISIBLE);
            mButton.setEnabled(true);
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        if (loginTask != null && loginTask.isDone()) {
            loginTask.cancel(true);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @SuppressWarnings("deprecation")
    private int getColorCompat(@ColorRes int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getColor(color);
        } else {
            return getResources().getColor(color);
        }
    }
}
