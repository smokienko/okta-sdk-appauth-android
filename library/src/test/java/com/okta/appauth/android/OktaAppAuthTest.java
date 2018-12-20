package com.okta.appauth.android;

import android.app.PendingIntent;
import android.content.Context;

import android.net.Uri;
import androidx.browser.customtabs.CustomTabsIntent;
import com.okta.TestUtils;
import net.openid.appauth.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class OktaAppAuthTest {
    private static final String TAG = "OktaAppAuth";
    @Mock
    AuthorizationService mAuthService;
    @Mock
    AuthStateManager mAuthStateManager;
    @Mock
    OAuthClientConfiguration mConfiguration;
    @Mock
    AuthState mAuthState;
    @Mock
    ClientAuthentication mClientAuthentication;
    private OktaAppAuth sut;
    private Context mContext;

    @Before
    public void setUp() throws Exception {
        mContext = RuntimeEnvironment.application.getApplicationContext();
        MockitoAnnotations.initMocks(this);
        sut = OktaAppAuth.getInstance(RuntimeEnvironment.application.getApplicationContext());
        sut.mAuthService = mAuthService;
        sut.mAuthStateManager = mAuthStateManager;
        sut.mConfiguration = mConfiguration;
        sut.mExecutor = TestUtils.buildSyncynchronesExecutorService();
        when(mAuthStateManager.getCurrent()).thenReturn(mAuthState);
    }

    @Test
    public void testDisposeNullsAuthenticationService() {
        sut = OktaAppAuth.getInstance(mContext);
        sut.mAuthService = new AuthorizationService(mContext);
        assertThat(sut.mAuthService).isNotNull();
        sut.dispose();
        assertThat(sut.mAuthService).isNull();
    }

    @Test
    public void testAuthServiceCreatesWhenNeeded() {
        sut = OktaAppAuth.getInstance(mContext);
        sut.mAuthService = null;
        sut.createAuthorizationServiceIfNeeded();
        assertThat(sut.mAuthService).isNotNull();
    }

    @Test
    public void testAuthServiceUsesOriginalWhenSet() {
        AuthorizationService authorizationService = new AuthorizationService(mContext);
        sut = OktaAppAuth.getInstance(mContext);
        sut.mAuthService = authorizationService;
        sut.createAuthorizationServiceIfNeeded();
        assertThat(sut.mAuthService).isNotNull();
        assertThat(sut.mAuthService).isSameAs(authorizationService);
    }

    @Test
    public void testAuthServiceRecreatesWhenDisposed() {
        AuthorizationService authorizationService = new AuthorizationService(mContext);
        sut = OktaAppAuth.getInstance(mContext);
        sut.mAuthService = authorizationService;
        sut.dispose();
        sut.createAuthorizationServiceIfNeeded();
        assertThat(sut.mAuthService).isNotNull();
        assertThat(sut.mAuthService).isNotSameAs(authorizationService);
    }

    @Test
    public void testRefreshWithoutTokenCallsListener() {
        FakeOktaAuthListener listener = new FakeOktaAuthListener();
        sut.refreshAccessToken(listener);
        assertThat(listener.hasCalledOnTokenFailure()).isTrue();
        assertThat(listener.getTokenExceptions().get(0))
                .isEqualTo(AuthorizationException.TokenRequestErrors.INVALID_REQUEST);
    }

    @Test
    public void testRefreshFailsClientAuthenticationCallsListener()
            throws ClientAuthentication.UnsupportedAuthenticationMethod {

        when(mAuthState.getRefreshToken()).thenReturn("refreshTokenHere");
        when(mAuthState.getClientAuthentication())
                .thenThrow(new ClientAuthentication.
                        UnsupportedAuthenticationMethod("tokenEndpointAuthMethod"));
        FakeOktaAuthListener listener = new FakeOktaAuthListener();

        sut.refreshAccessToken(listener);

        verify(mAuthState, times(1)).getClientAuthentication();
        assertThat(listener.hasCalledOnTokenFailure()).isTrue();
        assertThat(listener.getTokenExceptions().get(0))
                .isEqualTo(AuthorizationException.TokenRequestErrors.INVALID_REQUEST);
    }

    @Test
    public void testSignOutFromOkta() {
        PendingIntent success = mock(PendingIntent.class);
        PendingIntent failure = mock(PendingIntent.class);
        AuthState authState = mock(AuthState.class);
        String idToken = TestUtils.getUnsignedIdToken();

        when(mAuthService.createCustomTabsIntentBuilder(any(Uri.class)))
                .thenReturn(new CustomTabsIntent.Builder());
        when(mAuthStateManager.getCurrent()).thenReturn(authState);
        when(authState.getAuthorizationServiceConfiguration())
                .thenReturn(TestUtils.getTestServiceConfig());
        when(authState.isAuthorized()).thenReturn(true);
        when(authState.getIdToken()).thenReturn(idToken);
        when(mConfiguration.getEndSessionRedirectUri())
                .thenReturn(TestUtils.TEST_APP_REDIRECT_URI);
        when(mConfiguration.hasConfigurationChanged()).thenReturn(false);

        ArgumentCaptor<EndSessionRequest> argument = ArgumentCaptor.forClass(EndSessionRequest.class);

        sut.signOutFromOkta(mContext, success, failure);

        verify(mAuthService, times(1))
                .performEndOfSessionRequest(
                        argument.capture()
                        ,any(PendingIntent.class)
                        ,any(PendingIntent.class)
                        ,any(CustomTabsIntent.class)
                );

        assertThat(argument.getValue().idToken)
                .isEqualTo(idToken);
        assertThat(argument.getValue().configuration.toJsonString())
                .isEqualTo(TestUtils.getTestServiceConfig().toJsonString());
        assertThat(argument.getValue().redirectUri)
                .isEqualTo(TestUtils.TEST_APP_REDIRECT_URI);
    }

    @Test(expected = IllegalStateException.class)
    public void testLogoutNoLogedinUserFoundException(){
        PendingIntent success = mock(PendingIntent.class);
        PendingIntent failure = mock(PendingIntent.class);
        AuthState authState = mock(AuthState.class);

        when(mAuthStateManager.getCurrent()).thenReturn(authState);
        when(authState.isAuthorized()).thenReturn(false);
        when(mConfiguration.hasConfigurationChanged()).thenReturn(false);
        when(authState.getAuthorizationServiceConfiguration())
                .thenReturn(TestUtils.getTestServiceConfig());

        sut.signOutFromOkta(mContext, success, failure);
    }

    @Test
    public void testRefreshCallsIntoAppAuth() throws ClientAuthentication.UnsupportedAuthenticationMethod {
        TokenRequest tokenRequest = mock(TokenRequest.class);
        FakeOktaAuthListener listener = new FakeOktaAuthListener();
        when(mAuthState.getRefreshToken()).thenReturn("refreshTokenHere");
        when(mAuthState.getClientAuthentication()).thenReturn(mClientAuthentication);
        when(mAuthState.createTokenRefreshRequest()).thenReturn(tokenRequest);
        sut.refreshAccessToken(listener);
        verify(mAuthService, times(1))
                .performTokenRequest(
                        any(TokenRequest.class),
                        any(ClientAuthentication.class),
                        any(AuthorizationService.TokenResponseCallback.class));
    }
}