package pl.dostrzegaj.soft.flicloader;

import java.io.File;
import java.util.List;

class PhotoFolderInfo {
    private PhotoFolderDir folder;
    private List<PhotoFile> photos;

    public PhotoFolderInfo(PhotoFolderDir folder,List<PhotoFile> photos) {
        this.folder = folder;
        this.photos = photos;
    }

    public PhotoFolderDir getFolder() {
        return folder;
    }

    public List<PhotoFile> getPhotos() {
        return photos;
    }
}
