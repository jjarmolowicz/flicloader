package pl.dostrzegaj.soft.flicloader;

import java.io.File;
import java.util.List;

class PhotoFolderInfo {
    private File folder;
    private File root;
    private List<File> photos;

    public PhotoFolderInfo(File folder, File root, List<File> photos) {
        this.folder = folder;
        this.root = root;
        this.photos = photos;
    }

    public File getFolder() {
        return folder;
    }

    public File getRoot() {
        return root;
    }

    public List<File> getPhotos() {
        return photos;
    }
}
