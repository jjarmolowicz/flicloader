package pl.dostrzegaj.soft.flicloader;

import java.util.List;

class PhotoFolderInfo {
    private PhotoFolderDir folder;
    private List<PhotoFile> photos;
    private UploadConfig uploadConfig;

    public PhotoFolderInfo(PhotoFolderDir folder, List<PhotoFile> photos, final UploadConfig uploadConfig) {
        this.folder = folder;
        this.photos = photos;
        this.uploadConfig = uploadConfig;
    }

    public PhotoFolderDir getFolder() {
        return folder;
    }

    public List<PhotoFile> getPhotos() {
        return photos;
    }

    public UploadConfig getUploadConfig() {
        return uploadConfig;
    }
}
