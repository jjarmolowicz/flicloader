package pl.dostrzegaj.soft.flicloader;

import java.util.Scanner;

import org.scribe.model.Token;
import org.scribe.model.Verifier;

import pl.dostrzegaj.soft.flicloader.api.AuthWrapper;
import pl.dostrzegaj.soft.flicloader.api.UserWrapper;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.auth.Auth;
import com.flickr4java.flickr.auth.AuthInterface;
import com.flickr4java.flickr.auth.Permission;
import com.google.common.base.Throwables;

public class FlickrAuthImpl implements AuthWrapper {

    private Flickr f;

    public FlickrAuthImpl(String apiKey, String secret) {
        f = new Flickr(apiKey, secret, new REST());
    }

    @Override
    public UserWrapper authorise(String token, String tokenSecret) {
        AuthInterface authInterface = f.getAuthInterface();
        Token requestToken = new Token(token, tokenSecret);
        try {
            Auth auth = authInterface.checkToken(requestToken);
            System.out.println("Authentication success");
            return new FlickrUserImpl(f,auth);
        } catch (FlickrException e) {
            throw Throwables.propagate(e);
        }

    }

    @Override
    public Token authoriseNewToken() {
        AuthInterface authInterface = f.getAuthInterface();
        Token token = authInterface.getRequestToken();
        String authorizationUrl = authInterface.getAuthorizationUrl(token, Permission.READ);
        System.out.println("Follow this URL to authorise yourself on Flickr");
        System.out.println(authorizationUrl);
        System.out.println("Paste in the token it gives you:");
        System.out.print(">>");
        try (Scanner scanner = new Scanner(System.in)) {
            String tokenKey = scanner.nextLine();
            Token accessToken = authInterface.getAccessToken(token, new Verifier(tokenKey));
            return accessToken;
        }
    }
}
