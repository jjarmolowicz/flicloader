package pl.dostrzegaj.soft.flicloader.api;

import com.flickr4java.flickr.FlickrException;
import org.scribe.model.Token;

public interface AuthWrapper {
    UserWrapper authorise( String token, String tokenSecret);
    Token authoriseNewToken();
}
