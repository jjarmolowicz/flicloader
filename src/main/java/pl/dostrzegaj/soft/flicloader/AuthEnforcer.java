package pl.dostrzegaj.soft.flicloader;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.scribe.model.Token;

import com.google.common.collect.Lists;

class AuthEnforcer {

    static final java.lang.String APIKEY = "apiKey";
    static final java.lang.String SECRET = "secret";
    static final java.lang.String TOKEN = "token";
    static final java.lang.String TOKEN_SECRET = "tokenSecret";
    private Properties properties;
    private AuthWrapperFactory authFactory;

    public abstract static class AuthWrapperFactory {

        public abstract AuthWrapper factory(String apiKey, String secret);
    }

    public AuthEnforcer(Properties properties, AuthWrapperFactory factory) {
        this.properties = properties;
        this.authFactory = factory;
    }

    public UserWrapper enforceAuthentication() {
        String apiKey = properties.getProperty(APIKEY);
        List<String> errors = Lists.newArrayList();
        if (apiKey == null || apiKey.isEmpty()) {
            errors.add("apiKey entry in properties is required");
        }
        String secret = properties.getProperty(SECRET);
        if (secret == null || secret.isEmpty()) {
            errors.add("secret entry in properties is required");
        }
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(errors.stream().collect(Collectors.joining(System.lineSeparator())));
        }
        AuthWrapper auth = authFactory.factory(apiKey, secret);
        String token = properties.getProperty(TOKEN);
        String tokenSecret = properties.getProperty(TOKEN_SECRET);
        if (token == null || token.isEmpty() || tokenSecret == null || tokenSecret.isEmpty()) {
            System.out.println("Token and token secret are missing. Yuo need to authorise new.");
            Token requestToken = auth.authoriseNewToken();
            System.out.println("Paste that into properties file to persist it for next runs:");
            System.out.println(TOKEN + "=" + requestToken.getToken());
            System.out.println(TOKEN_SECRET + "=" + requestToken.getSecret());
            return auth.authorise(requestToken.getToken(), requestToken.getSecret());
        }
        return auth.authorise(token, tokenSecret);

    }
}
