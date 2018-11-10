package pl.dostrzegaj.soft.flicloader;

import java.util.Optional;

interface UserAccount {
    String createPhotoFolder(String title, String primaryPhotoId);

    Optional<UploadedPhoto> uploadPhoto(PhotoFile photo, UploadConfig config);

    void movePhotoToFolder(
            UploadedPhoto uploadedPhoto,
            PhotoFolderId folder);
}
