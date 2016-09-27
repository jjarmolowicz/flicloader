package pl.dostrzegaj.soft.flicloader;

import org.scribe.model.Token;

interface AuthWrapper {
    UserWrapper authorise( String token, String tokenSecret);
    Token authoriseNewToken();
}
