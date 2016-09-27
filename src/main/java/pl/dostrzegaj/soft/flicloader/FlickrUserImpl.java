package pl.dostrzegaj.soft.flicloader;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.RequestContext;
import com.flickr4java.flickr.photosets.Photosets;
import com.google.common.base.Throwables;

import com.flickr4java.flickr.auth.Auth;

class FlickrUserImpl implements UserWrapper {

    private Flickr f;
    private Auth auth;

    public FlickrUserImpl(Flickr f, Auth auth) {
        this.f = f;
        this.auth = auth;
        RequestContext.getRequestContext().setAuth(auth);
    }


    public Photosets getPhotosetsAsList() {
        try {
            return f.getPhotosetsInterface().getList(auth.getUser().getId());
        } catch (FlickrException e) {
            throw Throwables.propagate(e);
        }
    }
}
