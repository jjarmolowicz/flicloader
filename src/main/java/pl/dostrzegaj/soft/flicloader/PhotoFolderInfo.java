package pl.dostrzegaj.soft.flicloader;

import java.io.File;
import java.util.List;

class PhotoFolderInfo {
    private File folder;
    private List<File> photos;

    public PhotoFolderInfo(File folder,List<File> photos) {
        this.folder = folder;
        this.photos = photos;
    }

    public File getFolder() {
        return folder;
    }

    public List<File> getPhotos() {
        return photos;
    }
}
