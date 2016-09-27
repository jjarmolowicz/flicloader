package pl.dostrzegaj.soft.flicloader;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.scribe.model.Token;

public class AuthEnforcerTest {

    @Test(expected = IllegalArgumentException.class)
    public void givenNoApiKeyNorSecretWhenEnforcedThenIllegalArgumentExceptionRaised() {
        Properties properties = new Properties();
        new AuthEnforcer(properties, null).enforceAuthentication();
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenNoApiKeyWhenEnforcedThenIllegalArgumentExceptionRaised() {
        Properties properties = new Properties();
        properties.put(AuthEnforcer.APIKEY, "mykey");
        new AuthEnforcer(properties, null).enforceAuthentication();
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenNoSecretWhenEnforcedThenIllegalArgumentExceptionRaised() {
        Properties properties = new Properties();
        properties.put(AuthEnforcer.SECRET, "mysecret");
        new AuthEnforcer(properties, null).enforceAuthentication();
    }

    @Test()
    public void givenKeyAndSecretButNoTokenThenAuthRequested() {
        Properties properties = new Properties();
        properties.put(AuthEnforcer.APIKEY, "apikeyvalue");
        properties.put(AuthEnforcer.SECRET, "mysecret");
        AuthWrapper authMock = Mockito.mock(AuthWrapper.class);
        UserWrapper userMock = Mockito.mock(UserWrapper.class);
        Token t = new Token("a", "b");
        Mockito.when(authMock.authoriseNewToken()).thenReturn(t);
        Mockito.when(authMock.authorise(t.getToken(), t.getSecret())).thenReturn(userMock);
        UserWrapper user = new AuthEnforcer(properties, getFactory(authMock)).enforceAuthentication();
        assertEquals(user, userMock);
    }

    @Test
    public void givenKeySecretAndTokenWhenEnforcedThenAuthRequested() {
        Properties properties = new Properties();
        String apikeyvalue = "apikeyvalue";
        String mysecret = "mysecret";
        String mysupertoken = "mysupertoken";
        String mysupertokenSecret = "mysupertokensecret";
        properties.put(AuthEnforcer.APIKEY, apikeyvalue);
        properties.put(AuthEnforcer.SECRET, mysecret);
        properties.put(AuthEnforcer.TOKEN, mysupertoken);
        properties.put(AuthEnforcer.TOKEN_SECRET, mysupertokenSecret);
        AuthWrapper authMock = Mockito.mock(AuthWrapper.class);
        UserWrapper expectedUser = Mockito.mock(UserWrapper.class);
        Mockito.when(authMock.authorise(mysupertoken, mysupertokenSecret)).thenReturn(expectedUser);
        UserWrapper user = new AuthEnforcer(properties, getFactory(authMock)).enforceAuthentication();

        Mockito.verify(authMock).authorise(mysupertoken, mysupertokenSecret);
        Assert.assertEquals(expectedUser, user);
    }

    AuthEnforcer.AuthWrapperFactory getFactory(final AuthWrapper authMock) {
        return new AuthEnforcer.AuthWrapperFactory() {

            @Override
            public AuthWrapper factory(String apiKey, String secret) {
                return authMock;
            }
        };
    }
}