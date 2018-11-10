package pl.dostrzegaj.soft.flicloader;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

class LazySender {

    private static final Logger LOGGER = LoggerFactory.getLogger(LazySender.class);

    private File root;
    private LocalCache localCache;
    private UserAccount userAccount;

    public LazySender(
        File root,
        LocalCache localCache,
        UserAccount userAccount) {

        this.root = root;

        this.localCache = localCache;
        this.userAccount = userAccount;
    }

    public void sendIfNeeded(
        PhotoFolderInfo i) {

        LOGGER.debug("About to process dir {}", i.getFolder()
            .getDir());
        if (i.getPhotos()
            .isEmpty()) {
            LOGGER.debug("No photos skipping.");
            return;
        }
        String folderName = createFolderName(i.getFolder()
            .getDir());
        Optional<PhotoFolderId> optionalFolder = localCache.getPhotoFolder(i.getFolder());
        if (optionalFolder.isPresent()) {
            PhotoFolderId folderId = optionalFolder.get();
            List<PhotoFile> nonExistingPhotos = localCache.getNonExistingPhotos(i.getPhotos(), folderId);
            if (!nonExistingPhotos.isEmpty()) {
                LOGGER.info("Local folder is out of sync: {}. Syncing", folderName);
                for (PhotoFile nonExistingPhoto : nonExistingPhotos) {
                    Optional<UploadedPhoto> uploadedPhoto = userAccount.uploadPhoto(nonExistingPhoto, i.getUploadConfig());
                    if (uploadedPhoto.isPresent()) {
                        userAccount.movePhotoToFolder(uploadedPhoto.get(), folderId);
                        localCache.storeUploadedFile(uploadedPhoto.get(), folderId);
                    }

                }
            }
        } else {
            LOGGER.info("Creating new photo folder: {}", folderName);
            Optional<PhotoFolderId> newFolderId = Optional.empty();
            for (PhotoFile photo : i.getPhotos()) {
                Optional<UploadedPhoto> maybeUploaded = userAccount.uploadPhoto(photo, i.getUploadConfig());
                if (maybeUploaded.isPresent()) {
                    if (!newFolderId.isPresent()) {
                        PhotoFolderId newPhotoFolderId = new PhotoFolderId(userAccount.createPhotoFolder(folderName, maybeUploaded.get()
                            .getId()));
                        newFolderId = Optional.of(newPhotoFolderId);
                        PhotoFolder newFolder = new PhotoFolder(newPhotoFolderId.getId(), i.getFolder()
                            .getRelativePath());
                        localCache.storePhotoFolder(newFolder);
                        localCache.storeUploadedFile(maybeUploaded.get(), newFolderId.get());
                    } else {
                        localCache.storeUploadedFile(maybeUploaded.get(), newFolderId.get());
                        // first picture is already a part of album
                        userAccount.movePhotoToFolder(maybeUploaded.get(), newFolderId.get());
                    }
                }
            }
        }
    }

    private String createFolderName(
        File folder) {

        List<String> nameParts = Lists.newArrayList();
        File i = folder;
        while (!i.equals(root)) {
            nameParts.add(0, i.getName());
            i = i.getParentFile();
        }
        nameParts.add(0, root.getName());

        return nameParts.stream()
            .collect(Collectors.joining("/"));
    }
}
