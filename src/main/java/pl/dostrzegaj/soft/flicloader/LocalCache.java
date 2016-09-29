package pl.dostrzegaj.soft.flicloader;

import java.io.File;
import java.util.List;
import java.util.Optional;

class LocalCache {
    public LocalCache(File dir) {
    }

    public Optional<PhotoFolder> getPhotoFolder(File folder) {
        return null;
    }

    public void storePhotoFolder(PhotoFolder folder) {

    }

    public List<File> getNonExistingPhotos(List<File> photos, PhotoFolder folder) {
        return null;
    }

    public void storeUploadedFiles(List<UploadedPhoto> photos, PhotoFolder folder) {
    }
}
