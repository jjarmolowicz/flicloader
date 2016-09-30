package pl.dostrzegaj.soft.flicloader;

import java.util.List;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.RequestContext;
import com.flickr4java.flickr.auth.Auth;
import com.flickr4java.flickr.photosets.Photosets;
import com.flickr4java.flickr.uploader.UploadMetaData;
import com.flickr4java.flickr.uploader.Uploader;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

class FlickrUserImpl implements UserAccount {

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

    @Override
    public PhotoFolder createPhotoFolder(String name) {
        return null;
    }

    @Override
    public List<UploadedPhoto> uploadPhotos(final List<PhotoFile> photos) {
        Uploader uploader = f.getUploader();
        List<UploadedPhoto> result = Lists.newArrayListWithCapacity(photos.size());
        for (PhotoFile photo : photos) {
            UploadMetaData metaData = new UploadMetaData();
            metaData.setPublicFlag(false);
            metaData.setFriendFlag(false);
            metaData.setFamilyFlag(false);
            String basefilename = photo.getFile().getName(); // "image.jpg";
            String title = basefilename;
            if (basefilename.lastIndexOf('.') > 0) {
                title = basefilename.substring(0, basefilename.lastIndexOf('.'));
            }
            metaData.setTitle(title);
            metaData.setFilename(basefilename);

            try {
                String photoId = uploader.upload(photo.getFile(), metaData);
                result.add(new UploadedPhoto(photoId, photo.getFile().getAbsolutePath()));
            } catch (FlickrException e) {
                Throwables.propagate(e);
            }

        }
        return result;
    }

    @Override
    public void movePhotosToFolder(final List<UploadedPhoto> uploadedPhotos, final PhotoFolder folder) {

    }
}
