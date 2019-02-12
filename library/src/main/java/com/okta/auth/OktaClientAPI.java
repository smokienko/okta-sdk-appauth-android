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
package com.okta.auth;

import android.support.annotation.WorkerThread;

import com.okta.auth.http.HttpRequest;
import com.okta.auth.http.HttpResponse;
import com.okta.openid.appauth.AuthorizationServiceConfiguration;
import com.okta.openid.appauth.TokenResponse;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

//Client API for a client that is already logged in (authenticated).
public class OktaClientAPI {
    private AuthorizationServiceConfiguration mOktaConfiguration;
    private TokenResponse mTokenResponse;

    OktaClientAPI(AuthorizationServiceConfiguration oktaConfiguration, TokenResponse response) {
        mOktaConfiguration = oktaConfiguration;
        mTokenResponse = response;
    }

    @WorkerThread
    public JSONObject getUserProfile() throws IOException, JSONException {
        HttpResponse response = new HttpRequest.Builder().setRequestMethod(HttpRequest.RequestMethod.POST)
                .setUri(mOktaConfiguration.discoveryDoc.getUserinfoEndpoint())
                .setRequestProperty("Authorization", "Bearer " + mTokenResponse.accessToken)
                .create()
                .executeRequest();
        return response.asJson();
    }
}
