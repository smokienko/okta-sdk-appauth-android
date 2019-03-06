# Okta

[![Build Status](https://travis-ci.org/okta/okta-sdk-appauth-android.svg?branch=master)](https://travis-ci.org/okta/okta-sdk-appauth-android)

## Example

To run the example project, clone the repo, and run `./gradlew assemble` from the root directory.
You can then install the example APK onto an Android device or emulator.

## Overview

This library currently supports:

- [OAuth 2.0 Authorization Code Flow](https://tools.ietf.org/html/rfc6749#section-4.1) using the [PKCE extension](https://tools.ietf.org/html/rfc7636)

## Getting Started

You can create an Okta developer account at [https://developer.okta.com/](https://developer.okta.com/).

1. After login, from the Admin dashboard, navigate to **Applications**&rarr;**Add Application**
2. Choose **Native** as the platform
3. Populate your new Native OpenID Connect application with values similar to:

| Setting              | Value                                               |
| -------------------- | --------------------------------------------------- |
| Application Name     | Native OpenId Connect App *(must be unique)*        |
| Login URI            | com.okta.example:/callback                          |
| End Session URI      | com.okta.example:/logoutCallback                    |
| Allowed grant types  | Authorization Code, Refresh Token *(recommended)*   |

4. Click **Finish** to redirect back to the *General Settings* of your application.
5. Copy the **Client ID**, as it will be needed for the client configuration.
6. Get your issuer, which is a combination of your Org URL (found in the upper right of the console home page) and /oauth2/default. For example, https://dev-1234.oktapreview.com/oauth2/default.

**Note:** *As with any Okta application, make sure you assign Users or Groups to the application.
          Otherwise, no one can use it.*

### Configuration

Create  `OktaConfig` using `OktaConfig.OktaConfigBuilder`
as follows:

```java
    OktaConfig config = new OktaConfig.Builder()
                .withClientId("clientIdValue")
                .withRedirectUri("redirectUriValue")
                .withEndSessionRedirectUri("endSessionUriValue")
                .withScopes("openid", "profile", "offline_access")
                .withIssuerUri("https://{yourOktaDomain}.com/oauth2/default")
                .build();
```

**Note**: *To receive a **refresh_token**, you must include the `offline_access` scope.*

### Building Okta

In order to construct Okta Api you need to use `OktaBuilder`

```java
        OktaBuilder()
                .withConfig(config)
                .withStorage(new SimpleOktaSrorage(getPreferences(MODE_PRIVATE)))
                .withOktaFactory(new PlainOktaFactory())
                .build();
``` 

Depending on what `OktaFactory` implementation you provide you will receive different implementation:
`PlainOktaFactory` will return `Okta` that is a synchronous API.
`AsyncOktaFactory` will return `OktaAsync` that is an asynchronous API.   

### Update the URI Scheme

In order to redirect back to your application from a web browser, you must specify a unique URI to
your app. To do this, you must define a gradle manifest placeholder in your app's `build.gradle`:

```java
android.defaultConfig.manifestPlaceholders = [
    "appAuthRedirectScheme": "com.okta.example"
]
```

Make sure this is consistent with the redirect URI used in `okta_app_auth_config.json`. For example,
if your **Redirect URI** is `com.okta.example:/callback`, the **AppAuth Redirect Scheme** should be
`com.okta.example`.

#### Chrome Custom Tabs `ERR_UNKNOWN_URL_SCHEME`

There is a [known issue](https://github.com/okta/okta-sdk-appauth-android/issues/8) when redirecting back to a URI scheme from the browser via Chrome Custom Tabs. This is due to Chrome **not supporting** JavaScript initiated redirects back to native applications.

To handle this, the AppAuth team proposes the following:
> Create a web page for your redirect URI that displays an interstitial page with a **Click here to return to app** button. [Ian McGinniss (AppAuth-Android Author)](https://github.com/openid/AppAuth-Android/issues/187#issuecomment-285546334)

There is a working sample application referenced [here](https://github.com/iainmcgin/AppAuth-Demo), where the interstitial page is used to capture the `code` and `state` values from the callback, then redirects the user back to the private-use URI scheme specified by the application.

The flow should look similar to:
<p align="center">
  <img src="https://user-images.githubusercontent.com/20212241/38281780-4c3105dc-3761-11e8-81a5-36337b2ab39a.png" width="500px">
</p>

More information on this topic is recorded in [this issue](https://github.com/okta/okta-sdk-appauth-android/issues/8).

### Browser Authorization 
 
#### Synchronous Browser Authorization

You need to initialize your `OktaConfig` and use `PlainOktaFactory` to instantiate `Okta` object 

Once the Okta instance is initialized, you can start the authorization flow by simply calling
`authenticateWithBrowser` whenever you're ready, but do not call it on Ui Thread:

```java
// LoginActivity.java

public class LoginActivity extends Activity {

    ExecutorService loginExecutor;

    private void performBrowserLogin() {
            loginExecutor.submit(() -> {
                AuthorizationResult result = okta.authenticateWithBrowser(this, null);
                if (result.isSuccess()) {
                    //work on success results
                } else {
                    //handle error
                }
            }); 
    }
}

```

Also you can provide additional parameters like `customState` or `loginHint` or add `additional parameters` by
providing `AuthenticationPayload`, that is an optional parameter.

#### Asynchronous Browser Authorization

You need to initialize your `OktaConfig` and use `AsyncOktaFactory` to instantiate `OktaAsync` object

Once the Okta instance is initialized, you can start the authorization flow by simply calling
`authenticateWithBrowser` whenever you're ready:

```java
public class LoginActivity extends Activity {
    
    private void performAsyncBrowserLogin() {
        
        oktaAsync.authenticateWithBrowser(this, null, new AuthListener() {
            @Override
            public void onSuccess(Tokens tokens) {
                //Success handling code goes here
            }

            @Override
            public void onError(OktaException error) {
               //Error handling code goes here
            }
        });
    }
}
```

Also you can provide additional parameters like `customState` or `loginHint` or add `additional parameters` by
providing `AuthenticationPayload`, that is an optional parameter.

### Native Authentication

NOTE: In order to use Native Authentication you need to get `sessionToken`. 
For this purpose you can use:
[Okta Java Authentication SDK](https://github.com/okta/okta-auth-java)

#### Synchronous Native Authentication

You need to initialize your `OktaConfig` and use `PlainOktaFactory` to instantiate `Okta` object 

Once the Okta instance is initialized, you can start the authorization flow by simply calling
`authenticate` with valid `sessionToken` whenever you're ready, but do not call it on Ui Thread:

```java
// LoginActivity.java

public class LoginActivity extends Activity {

    ExecutorService loginExecutor;

    private void performBrowserLogin(String sessionToken) {
            loginExecutor.submit(() -> {
                AuthorizationResult result = okta.authenticateWithBrowser(sessionToken, null);
                if (result.isSuccess()) {
                    //Success handling code goes here
                } else {
                    //Error handling code goes here
                }
            }); 
    }
}

```

Also you can provide additional parameters like `customState` or `loginHint` or add `additional parameters` by
providing `AuthenticationPayload`, that is an optional parameter.

#### Asynchronous Native Authentication

You need to initialize your `OktaConfig` and use `AsyncOktaFactory` to instantiate `OktaAsync` object 

Once the Okta instance is initialized, you can start the authorization flow by simply calling
`authenticate` with valid `sessionToken` whenever you're ready:

```java
public class LoginActivity extends Activity {
    
    private void performAsyncBrowserLogin() {
        
        oktaAsync.authenticate(this, new AuthListener() {
            @Override
            public void onSuccess(Tokens tokens) {
                //Success handling code goes here
            }

            @Override
            public void onError(OktaException error) {
               //Error handling code goes here
            }
        });
    }
}
```

Also you can provide additional parameters like `customState` or `loginHint` or add `additional parameters` by
providing `AuthenticationPayload`, that is an optional parameter.

### End browser session (Sing out from Okta)

#### Synchronous end browser session

You need to initialize your `OktaConfig` and use `PlainOktaFactory` to instantiate `Okta` object.
You need end browser session only if you have performed browser authorization

Once the Okta instance is initialized, you can end session in browser by calling `singOutFromOkta`
whenever you are ready,  but do not call it on Ui Thread:


```java
// LoginActivity.java

public class CustomActivity extends Activity {

    ExecutorService loginExecutor;

    private void singOutFromOkta() {
            loginExecutor.submit(() -> {
                Result result = okta.singOutFromOkta(this);
                if (result.isSuccess()) {
                    //Success handling code goes here
                } else {
                    //Error handling code goes here
                }
            }); 
    }
}

```

#### Asynchronous end browser session

You need to initialize your `OktaConfig` and use `PlainOktaFactory` to instantiate `Okta` object.
You need end browser session only if you have performed browser authorization

Once the Okta instance is initialized, you can end session in browser by calling `singOutFromOkta`
whenever you are ready:


```java

public class CustomActivity extends Activity {

    private void singOutFromOkta() {
            
            oktaAsync.authenticate(this, new EndSessionListener() {
                @Override
                public void onSuccess() {
                    //Success handling code goes here
                }
    
                @Override
                public void onError(OktaException error) {
                   //Error handling code goes here
                }
            });
        }
}

```

### Clear Okta State

In order to clear all user data saved by Okta you should:
- Get `OktaState` from initialized `Okta` or `OktaAsync`
- call `clear()`

```java

public class CustomActivity extends Activity {

    private void clearOktaState() {
            
            okta.getState().clear();
            
        }
}

```

## License

See the LICENSE file for more info.
