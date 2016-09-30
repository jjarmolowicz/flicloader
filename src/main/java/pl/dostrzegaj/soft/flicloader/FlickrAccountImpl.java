package pl.dostrzegaj.soft.flicloader;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.RequestContext;
import com.flickr4java.flickr.auth.Auth;
import com.flickr4java.flickr.photosets.PhotosetsInterface;
import com.flickr4java.flickr.uploader.UploadMetaData;
import com.flickr4java.flickr.uploader.Uploader;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

class FlickrAccountImpl implements UserAccount {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlickrAccountImpl.class);
    private Flickr f;
    private Auth auth;

    public FlickrAccountImpl(Flickr f, Auth auth) {
        this.f = f;
        this.auth = auth;
        RequestContext.getRequestContext().setAuth(auth);
    }

    @Override
    public String createPhotoFolder(String title, String primaryPhotoId) {
        PhotosetsInterface i = f.getPhotosetsInterface();
        try {
            return i.create(title, "", primaryPhotoId).getId();
        } catch (FlickrException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public List<UploadedPhoto> uploadPhotos(final List<PhotoFile> photos, UploadConfig config) {
        Uploader uploader = f.getUploader();
        List<UploadedPhoto> result = Lists.newArrayListWithCapacity(photos.size());
        for (PhotoFile photo : photos) {
            UploadMetaData metaData = new UploadMetaData();
            metaData.setPublicFlag(config.getIsPublic());
            metaData.setFriendFlag(config.getIsFriend());
            metaData.setFamilyFlag(config.getIsFamily());
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
        LOGGER.debug("New files uploaded: {}", photos);
        if (!photos.isEmpty()) {
            LOGGER.info("Uploaded {} new photos", photos.size());
        }
        return result;
    }

    @Override
    public void movePhotosToFolder(final List<UploadedPhoto> uploadedPhotos, final PhotoFolder folder) {
        for (UploadedPhoto uploadedPhoto : uploadedPhotos) {
            try {
                f.getPhotosetsInterface().addPhoto(folder.getId(), uploadedPhoto.getId());
            } catch (FlickrException e) {
                Throwables.propagate(e);
            }
        }
    }
}
