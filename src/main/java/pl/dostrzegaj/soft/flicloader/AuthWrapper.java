package pl.dostrzegaj.soft.flicloader;

import org.scribe.model.Token;

interface AuthWrapper {
    UserAccount authorise(String token, String tokenSecret);
    Token authoriseNewToken();
}
