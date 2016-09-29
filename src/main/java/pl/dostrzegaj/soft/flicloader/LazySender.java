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
        if (optionalFolder.isPresent()) {
            folder = optionalFolder.get();
        }else {
            folder = userAccount.createPhotoFolder(createFolderName(i.getFolder()));
            localCache.storePhotoFolder(folder);
        }
        List<File> filesToUpload = localCache.getNonExistingPhotos(i.getPhotos(),folder);
        localCache.storeUploadedFiles(userAccount.uploadPhotos(filesToUpload, folder), folder);
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
