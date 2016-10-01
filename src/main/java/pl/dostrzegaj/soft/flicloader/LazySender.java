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

    public LazySender(File root, LocalCache localCache, UserAccount userAccount) {
        this.root = root;

        this.localCache = localCache;
        this.userAccount = userAccount;
    }

    public void sendIfNeeded(PhotoFolderInfo i) {
        LOGGER.debug("About to process dir {}", i.getFolder().getDir());
        if (i.getPhotos().isEmpty()) {
            LOGGER.debug("No photos skipping.");
            return;
        }
        Optional<PhotoFolderId> optionalFolder = localCache.getPhotoFolder(i.getFolder());
        PhotoFolderId folderId;
        List<UploadedPhoto> uploadedPhotos;
        if (optionalFolder.isPresent()) {
            folderId = optionalFolder.get();
            uploadedPhotos =
                userAccount.uploadPhotos(localCache.getNonExistingPhotos(i.getPhotos(), folderId), i.getUploadConfig());
            userAccount.movePhotosToFolder(uploadedPhotos, folderId);
        } else {
            String folderName = createFolderName(i.getFolder().getDir());
            LOGGER.info("Creating new photo folder: {}", folderName);
            uploadedPhotos = userAccount.uploadPhotos(i.getPhotos(), i.getUploadConfig());
            UploadedPhoto firstPhoto = uploadedPhotos.get(0);
            folderId = new PhotoFolderId(userAccount.createPhotoFolder(folderName, firstPhoto.getId()));
            PhotoFolder folder = new PhotoFolder(folderId.getId(), i.getFolder().getRelativePath());
            if (uploadedPhotos.size() > 1) { // first picture is already a part of photoFolder
                userAccount.movePhotosToFolder(uploadedPhotos.subList(1, uploadedPhotos.size()), folderId);
            }
            localCache.storePhotoFolder(folder);
        }
        localCache.storeUploadedFiles(uploadedPhotos, folderId);
    }

    private String createFolderName(File folder) {
        List<String> nameParts = Lists.newArrayList();
        File i = folder;
        while (!i.equals(root)) {
            nameParts.add(0, i.getName());
            i = i.getParentFile();
        }
        nameParts.add(0, root.getName());

        return nameParts.stream().collect(Collectors.joining("/"));
    }
}
