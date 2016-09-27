package pl.dostrzegaj.soft.flicloader;

import java.io.File;
import java.util.List;

class PhotoFolderInfo {
    private File folder;
    private File root;
    private List<File> photos;
    private boolean hasSubFolders;

    public PhotoFolderInfo(File folder, File root, List<File> photos, boolean hasSubFolders) {
        this.folder = folder;
        this.root = root;
        this.photos = photos;
        this.hasSubFolders = hasSubFolders;
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

    public boolean isHasSubFolders() {
        return hasSubFolders;
    }
}
