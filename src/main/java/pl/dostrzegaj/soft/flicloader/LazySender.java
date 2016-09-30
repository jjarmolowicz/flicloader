package pl.dostrzegaj.soft.flicloader;

import com.google.common.collect.Lists;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        Optional<PhotoFolder> optionalFolder = localCache.getPhotoFolder(i.getFolder());
        PhotoFolder folder;
        List<UploadedPhoto> uploadedPhotos;
        if (optionalFolder.isPresent()) {
            folder = optionalFolder.get();
            uploadedPhotos = userAccount.uploadPhotos(localCache.getNonExistingPhotos(i.getPhotos(), folder));
            userAccount.movePhotosToFolder(uploadedPhotos,folder);
        }else {
            uploadedPhotos = userAccount.uploadPhotos(i.getPhotos());
            folder = userAccount.createPhotoFolder(createFolderName(i.getFolder().getDir()));
            userAccount.movePhotosToFolder(uploadedPhotos,folder);
            localCache.storePhotoFolder(folder);
        }
        localCache.storeUploadedFiles(uploadedPhotos, folder);
    }

    private String createFolderName(File folder) {
        List<String> nameParts = Lists.newArrayList();
        File i = folder;
        while(!i.equals(root)) {
            nameParts.add(0,i.getName());
            i = i.getParentFile();
        }
        nameParts.add(0,root.getName());

        return nameParts.stream().collect(Collectors.joining("/"));
    }
}
