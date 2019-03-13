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
package com.okta.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.customtabs.CustomTabsIntent;
import com.okta.openid.appauth.AuthorizationRequest;
import com.okta.openid.appauth.browser.*;

public class OktaAuthenticateActivity extends Activity {

    static final String EXTRA_AUTH_STARTED = "com.okta.auth.AUTH_STARTED";
    static final String EXTRA_AUTH_URI = "com.okta.auth.AUTH_URI";
    static final String EXTRA_TAB_OPTIONS = "com.okta.auth.TAB_OPTIONS";
    static final String EXTRA_RESULT = "com.okta.auth.AUTH_RESULT";

    private CustomTabManager mTabManager;
    private boolean authStarted;
    private Uri authUri;
    private int customTabColor;
    private Intent resultIntent;

    public static Intent createAuthIntent(Context context, AuthorizationRequest request, int color) {
        Intent intent = new Intent(context, OktaAuthenticateActivity.class);
        intent.putExtra(EXTRA_AUTH_URI, request.toUri());
        intent.putExtra(EXTRA_TAB_OPTIONS, color);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (isRedirect(intent)) {
            setResultIntent(intent);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        Bundle bundle = savedInstanceBundle;
        if (bundle == null) {
            bundle = getIntent().getExtras();
        }
        initState(bundle);
        if (isRedirect(getIntent())) {
            setResultIntent(getIntent());
        } else {
            if (bundle != null) {
                startSigInBrowser();
            } else {
                setResultCanceled();
            }
        }
    }

    private void setResultCanceled() {
        resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_RESULT, false);
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRA_AUTH_STARTED, authStarted);
        outState.putParcelable(EXTRA_AUTH_URI, authUri);
        outState.putInt(EXTRA_TAB_OPTIONS, customTabColor);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!authStarted) {
            // The custom tab was closed without getting a result.
            setResultCanceled();
        }
        authStarted = false;
    }

    private boolean isRedirect(Intent intent) {
        return intent.getAction() != null
                && intent.getAction().equals(OktaRedirectActivity.REDIRECT_ACTION);
    }

    private void setResultIntent(Intent intent) {
        resultIntent = intent;
        resultIntent.putExtra(EXTRA_RESULT, true);
        finish();
    }

    private void initState(Bundle bundle) {
        if (bundle != null) {
            authUri = bundle.getParcelable(EXTRA_AUTH_URI);
            customTabColor = bundle.getInt(EXTRA_TAB_OPTIONS, -1);
            authStarted = bundle.getBoolean(EXTRA_AUTH_STARTED, false);
        }
    }

    private void startSigInBrowser() {
        if (!authStarted) {
            Intent browserIntent = createBrowserIntent(authUri, customTabColor);
            if (browserIntent != null) {
                authStarted = true;
                startActivity(browserIntent);
            } else {
                setResultCanceled();
            }
        }
    }

    private Intent createBrowserIntent(Uri authUri, @ColorInt int color) {
        BrowserDescriptor descriptor = BrowserSelector.select(this,
                new BrowserWhitelist(VersionedBrowserMatcher.CHROME_CUSTOM_TAB,
                        VersionedBrowserMatcher.CHROME_BROWSER,
                        VersionedBrowserMatcher.FIREFOX_CUSTOM_TAB,
                        VersionedBrowserMatcher.FIREFOX_BROWSER,
                        VersionedBrowserMatcher.SAMSUNG_CUSTOM_TAB,
                        VersionedBrowserMatcher.SAMSUNG_BROWSER));
        if (descriptor == null) {
            return null;
        }
        mTabManager = new CustomTabManager(this);
        mTabManager.bind(descriptor.packageName);
        CustomTabsIntent.Builder intentBuilder = mTabManager.createTabBuilder(authUri);
        if (color > 0) {
            intentBuilder.setToolbarColor(color);
        }
        CustomTabsIntent tabsIntent = intentBuilder.build();
        tabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        tabsIntent.intent.setPackage(descriptor.packageName);
        tabsIntent.intent.setData(authUri);
        return tabsIntent.intent;
    }

    @Override
    protected void onDestroy() {
        if (mTabManager != null) {
            mTabManager.dispose();
        }
        if (resultIntent != null) {
            LocalIntentBus.getInstance().post(LocalIntentBus.BROWSER_AUTH_CHANNEL, resultIntent);
        }
        super.onDestroy();
    }
}
