package pl.dostrzegaj.soft.flicloader;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.scribe.exceptions.OAuthConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.FlickrRuntimeException;
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
    private int SEND_RETRIES = 5;
    private int RETRY_BASE = 2;
    private long SEND_RETRY_DELAY = TimeUnit.SECONDS.toMillis(1);

    public FlickrAccountImpl(
        Flickr f,
        Auth auth) {

        this.f = f;
        this.auth = auth;
        RequestContext.getRequestContext()
            .setAuth(auth);
    }

    @Override
    public String createPhotoFolder(
        String title,
        String primaryPhotoId) {

        PhotosetsInterface i = f.getPhotosetsInterface();
        try {
            return i.create(title, "", primaryPhotoId)
                .getId();
        } catch (FlickrException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public Optional<UploadedPhoto> uploadPhoto(
        PhotoFile photo,
        UploadConfig config) {

        Uploader uploader = f.getUploader();
        UploadMetaData metaData = new UploadMetaData();
        metaData.setPublicFlag(config.getIsPublic());
        metaData.setFriendFlag(config.getIsFriend());
        metaData.setFamilyFlag(config.getIsFamily());
        String basefilename = photo.getFile()
            .getName(); // "image.jpg";
        String title = basefilename;
        if (basefilename.lastIndexOf('.') > 0) {
            title = basefilename.substring(0, basefilename.lastIndexOf('.'));
        }
        metaData.setTitle(title);
        metaData.setFilename(basefilename);
        Optional<UploadedPhoto> uploadedPhoto = Optional.empty();
        try {
            for (int i = 1 ; i <= SEND_RETRIES ; ++i) {
                try {
                    String photoId = uploader.upload(photo.getFile(), metaData);
                    uploadedPhoto = Optional.of(new UploadedPhoto(photoId, photo.getRelativePath()));
                    break;
                } catch (OAuthConnectionException | FlickrRuntimeException oce) {
                    handleRetriableException(i, oce);
                    LOGGER.debug("During uploadPhotos", oce);
                }
            }
        } catch (FlickrException e) {
            LOGGER.debug("Error during flickr uplaoding of :" + photo.getFile()
                .toString(), e);
            LOGGER.error("Error during flickr uplaoding of :" + photo.getFile()
                .toString());
        }
        LOGGER.debug("File {} ({}) uploaded.", title, basefilename);
        return uploadedPhoto;
    }

    void sleepSome(
        int i) {

        try {
            Thread.sleep(Math.round(pow(RETRY_BASE, i) * SEND_RETRY_DELAY));
        } catch (InterruptedException ie) {
            Thread.currentThread()
                .interrupt();
            Throwables.propagate(ie);
        }
    }

    long pow(
        int a,
        int b) {

        long result = 1;
        for (int i = 1 ; i <= b ; i++) {
            result *= a;
        }
        return result;
    }

    @Override
    public void movePhotoToFolder(
        final UploadedPhoto uploadedPhoto,
        final PhotoFolderId folder) {

        moveWithRetries(folder, uploadedPhoto);
    }

    private void moveWithRetries(
        PhotoFolderId folder,
        UploadedPhoto uploadedPhoto) {

        for (int i = 1 ; i <= SEND_RETRIES ; ++i) {
            try {
                f.getPhotosetsInterface()
                    .addPhoto(folder.getId(), uploadedPhoto.getId());
                return;
            } catch (FlickrRuntimeException | IllegalArgumentException fre) {
                handleRetriableException(i, fre);
            } catch (FlickrException e) {
                if ("0".equals(e.getErrorCode())) {
                    handleRetriableException(i, e);
                } else {
                    Throwables.propagate(e);
                }
            }
        }

    }

    private void handleRetriableException(
        int i,
        Exception fre) {

        LOGGER.debug("During movePhotosToFolder got exception. Will sleep and retry", fre);
        LOGGER.warn("Got exception while processing folder calling flickr. Will wait and retry. Details can be found in detailed upload log");
        if (i == SEND_RETRIES) {
            Throwables.propagate(fre);
        }
        sleepSome(i);
    }

}
