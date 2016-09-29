package pl.dostrzegaj.soft.flicloader;

import java.io.File;
import java.util.List;

interface UserAccount {
    PhotoFolder createPhotoFolder(String name);

    List<UploadedPhoto> uploadPhotos(List<File> photos, PhotoFolder folder);
}
