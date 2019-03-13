package com.okta.android;

import android.content.Context;
import android.util.Log;
import com.okta.appauth.android.Tokens;
import com.okta.openid.appauth.AuthorizationRequest;
import com.okta.openid.appauth.AuthorizationServiceConfiguration;
import org.json.JSONException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.*;

import static android.content.ContentValues.TAG;

public class OktaRepository {
    private static final String AUTH_CONFIGURATION_KEY = "authConfigurationKey";
    private static final String AUTH_REQUEST_KEY = "authRequestKey";
    private static final String AUTH_ID_TOKEN_KEY = "authIdTokeKey";
    private static final String AUTH_REFRESH_TOKEN_KEY = "authRefreshTokenKey";
    private static final String AUTH_ACCESS_TOKEN_KEY = "authAccessTokenKey";

    private final OktaStorage storage;
    private final EncryptionManager encryptionManager;

    private AuthorizationServiceConfiguration authConfiguration;
    private AuthorizationRequest authRequest;
    private Tokens tokens;
    private final Object tokensLock = new Object();
    private final Object authRequestLock = new Object();
    private final Object authConfigurationLock = new Object();

    public OktaRepository(OktaStorage storage, Context context) {
        this.storage = storage;
        this.encryptionManager = buildEncriptionManager(context);
    }


    public void saveOktaConfiguration(AuthorizationServiceConfiguration configuration) {
        synchronized (authConfigurationLock) {
            storage.save(getHashed(AUTH_CONFIGURATION_KEY),
                    getEncrypted(configuration.toJsonString()));
            authConfiguration = configuration;
        }
    }

    public AuthorizationServiceConfiguration getOktaConfiguration() {
        synchronized (authConfigurationLock) {
            if (authConfiguration == null
                    && storage.get(getHashed(AUTH_CONFIGURATION_KEY)) != null) {
                try {
                    authConfiguration = AuthorizationServiceConfiguration
                            .fromJson(
                                    getDencrypted(
                                            storage.get(getHashed(AUTH_CONFIGURATION_KEY))));
                } catch (JSONException e) {
                    Log.e(TAG, "saveAuthorizationResponse: ", e);
                }
            }
            return authConfiguration;
        }
    }

    public void saveTokens(Tokens tokens) {
        synchronized (tokensLock) {
            storage.save(getHashed(AUTH_ID_TOKEN_KEY), getEncrypted(tokens.getIdToken()));
            storage.save(getHashed(AUTH_REFRESH_TOKEN_KEY), getEncrypted(tokens.getRefreshToken()));
            storage.save(getHashed(AUTH_ACCESS_TOKEN_KEY), getEncrypted(tokens.getAccessToken()));
            this.tokens = tokens;
        }
    }

    public Tokens getTokens() {
        synchronized (tokensLock) {
            if (tokens == null && storage.get(getHashed(AUTH_ID_TOKEN_KEY)) != null) {
                tokens = new Tokens(
                        getDencrypted(storage.get(getHashed(AUTH_ID_TOKEN_KEY))),
                        getDencrypted(storage.get(getHashed(AUTH_ACCESS_TOKEN_KEY))),
                        getDencrypted(storage.get(getHashed(AUTH_ID_TOKEN_KEY)))
                );
            }
            return tokens;
        }
    }

    public void saveAuthorizationRequest(AuthorizationRequest request) {
        synchronized (authRequestLock) {
            storage.save(getHashed(AUTH_REQUEST_KEY), getEncrypted(request.jsonSerializeString()));
            authRequest = request;
        }
    }

    public AuthorizationRequest getAuthorizationRequest() {
        synchronized (authRequestLock) {
            if (authRequest == null && storage.get(getHashed(AUTH_REQUEST_KEY)) != null) {
                try {
                    authRequest = AuthorizationRequest
                            .jsonDeserialize(
                                    getDencrypted(storage.get(getHashed(AUTH_REQUEST_KEY))));
                } catch (JSONException e) {
                    Log.e(TAG, "saveAuthorizationResponse: ", e);
                }
            }
            return authRequest;
        }
    }

    private String getEncrypted(String value) {
        if (encryptionManager == null) return value;
        try {
            return encryptionManager.encrypt(value);
        } catch (GeneralSecurityException ex) {
            Log.d(TAG, "getEncrypted: " + ex.getCause());
            return value;
        } catch (IOException ex) {
            Log.d(TAG, "getEncrypted: " + ex.getCause());
            return value;
        }
    }

    private String getDencrypted(String value) {
        if (encryptionManager == null) return value;
        try {
            return encryptionManager.decrypt(value);
        } catch (GeneralSecurityException ex) {
            Log.d(TAG, "getEncrypted: " + ex.getCause());
            return value;
        } catch (IOException ex) {
            Log.d(TAG, "getEncrypted: " + ex.getCause());
            return value;
        }
    }

    private String getHashed(String value) {
        try {
            return EncryptionManager.getHashed(value);
        } catch (NoSuchAlgorithmException ex) {
            Log.d(TAG, "getEncrypted: " + ex.getCause());
            return value;
        } catch (UnsupportedEncodingException ex) {
            Log.d(TAG, "getEncrypted: " + ex.getCause());
            return value;
        }
    }

    private EncryptionManager buildEncriptionManager(Context context){
        try {
            return new EncryptionManager(context,
                    context.getSharedPreferences("Encpription", Context.MODE_PRIVATE));
        } catch (IOException ex) {
            Log.d(TAG, "getEncrypted: " + ex.getCause());
            return null;
        } catch (GeneralSecurityException ex) {
            Log.d(TAG, "getEncrypted: " + ex.getCause());
            return null;
        }
    }

}
