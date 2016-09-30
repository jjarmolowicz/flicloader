package pl.dostrzegaj.soft.flicloader;

import java.util.List;

interface UserAccount {
    String createPhotoFolder(String title, String primaryPhotoId);

    List<UploadedPhoto> uploadPhotos(List<PhotoFile> photos);

    void movePhotosToFolder(List<UploadedPhoto> uploadedPhotos, PhotoFolder folder);
}
