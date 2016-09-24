package pl.dostrzegaj.soft.flicloader;

import pl.dostrzegaj.soft.flicloader.api.UserWrapper;

import com.flickr4java.flickr.auth.Auth;

public class FlickrUserImpl implements UserWrapper {

    private Auth auth;

    public FlickrUserImpl(Auth auth) {

        this.auth = auth;
    }

    public void showIt() {
        System.out.println("nsid: " + auth.getUser().getId());
        System.out.println("Realname: " + auth.getUser().getRealName());
        System.out.println("Username: " + auth.getUser().getUsername());
        System.out.println("Permission: " + auth.getPermission().getType());
    }

}
