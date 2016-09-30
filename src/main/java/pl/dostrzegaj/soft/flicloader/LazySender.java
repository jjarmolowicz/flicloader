package pl.dostrzegaj.soft.flicloader;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

class LazySender {

    private File root;
    private LocalCache localCache;
    private UserAccount userAccount;

    public LazySender(File root, LocalCache localCache, UserAccount userAccount) {
        this.root = root;

        this.localCache = localCache;
        this.userAccount = userAccount;
    }

    public void sendIfNeeded(PhotoFolderInfo i) {
        if (i.getPhotos().isEmpty()) {
            return;
        }
        Optional<PhotoFolder> optionalFolder = localCache.getPhotoFolder(i.getFolder());
        PhotoFolder folder;
        List<UploadedPhoto> uploadedPhotos;
        if (optionalFolder.isPresent()) {
            folder = optionalFolder.get();
            uploadedPhotos = userAccount.uploadPhotos(localCache.getNonExistingPhotos(i.getPhotos(), folder),i.getUploadConfig());
            userAccount.movePhotosToFolder(uploadedPhotos, folder);
        } else {
            uploadedPhotos = userAccount.uploadPhotos(i.getPhotos(),i.getUploadConfig());
            UploadedPhoto firstPhoto = uploadedPhotos.get(0);
            String folderId = userAccount.createPhotoFolder(createFolderName(i.getFolder().getDir()), firstPhoto.getId());
            folder = new PhotoFolder(folderId, i.getFolder().getDir().getAbsolutePath());
            if (uploadedPhotos.size() >1) { //first picture is already a part of photoFolder
                userAccount.movePhotosToFolder(uploadedPhotos.subList(1, uploadedPhotos.size()), folder);
            }
            localCache.storePhotoFolder(folder);
        }
        localCache.storeUploadedFiles(uploadedPhotos, folder);
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
