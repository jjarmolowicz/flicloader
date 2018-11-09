package pl.dostrzegaj.soft.flicloader;

import java.util.List;
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
    public List<UploadedPhoto> uploadPhotos(
        final List<PhotoFile> photos,
        UploadConfig config) {

        Uploader uploader = f.getUploader();
        List<UploadedPhoto> result = Lists.newArrayListWithCapacity(photos.size());
        LOGGER.debug("Files to upload: {}", photos);
        for (PhotoFile photo : photos) {
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

            try {
                for (int i = 1 ; i <= SEND_RETRIES ; ++i) {
                    try {
                        String photoId = uploader.upload(photo.getFile(), metaData);
                        result.add(new UploadedPhoto(photoId, photo.getRelativePath()));
                        break;
                    } catch (OAuthConnectionException | FlickrRuntimeException oce) {
                        handleRetriableException(i, oce);
                        LOGGER.debug("During uploadPhotos", oce);
                    }
                }
            } catch (FlickrException e) {
                LOGGER.error("Error during flickr uplaoding of :" + photo.getFile()
                    .toString(), e);
            }
            LOGGER.debug("File {} ({}) uploaded.", title, basefilename);

        }
        LOGGER.debug("New files uploaded: {}", photos);
        if (!photos.isEmpty()) {
            LOGGER.info("Uploaded {} new photos", photos.size());
        }
        return result;
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
    public void movePhotosToFolder(
        final List<UploadedPhoto> uploadedPhotos,
        final PhotoFolderId folder) {

        for (UploadedPhoto uploadedPhoto : uploadedPhotos) {
            moveWithRetries(folder, uploadedPhoto);
        }
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
                }else {
                    Throwables.propagate(e);
                }
            }
        }

    }

    private void handleRetriableException(
        int i,
        Exception fre) {

        LOGGER.warn("During movePhotosToFolder got exception. Will sleep and retry", fre);
        if (i == SEND_RETRIES) {
            Throwables.propagate(fre);
        }
        sleepSome(i);
    }

}
