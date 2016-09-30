package pl.dostrzegaj.soft.flicloader;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

public class LazySenderTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void givenNewPhotoFolderThenPhotosetCreated() throws IOException {
        // given
        LocalCache localCache = Mockito.mock(LocalCache.class);
        UserAccount userAccount = Mockito.mock(UserAccount.class);
        LazySender sender = new LazySender(folder.getRoot(), localCache, userAccount);
        PhotoFolderDir folder = new PhotoFolderDir(this.folder.getRoot());
        PhotoFolderInfo photoInfo =
            new PhotoFolderInfo(folder, Collections.singletonList(new PhotoFile(this.folder
                .newFile())));
        Mockito.when(localCache.getPhotoFolder(folder)).thenReturn(Optional.empty());

        // when
        sender.sendIfNeeded(photoInfo);

        // then
        Mockito.verify(userAccount).createPhotoFolder(this.folder.getRoot().getName());
    }

    @Test
    public void givenNewPhotoFolderThenPhotosetStoredInLocalCache() throws IOException {
        // given
        LocalCache localCache = Mockito.mock(LocalCache.class);
        UserAccount userAccount = Mockito.mock(UserAccount.class);
        LazySender sender = new LazySender(folder.getRoot(), localCache, userAccount);
        PhotoFolderDir folder = new PhotoFolderDir(this.folder.getRoot());
        PhotoFolderInfo photoInfo =
            new PhotoFolderInfo(folder, Collections.singletonList(new PhotoFile(this.folder
                .newFile())));
        Mockito.when(localCache.getPhotoFolder(folder)).thenReturn(Optional.empty());

        // when
        sender.sendIfNeeded(photoInfo);

        // then
        Mockito.verify(localCache).storePhotoFolder(Mockito.any(PhotoFolder.class));
    }

    @Test
    public void givenNewFileInExistingPhotoFolderThenPhotoUploaded() throws IOException {
        // given
        PhotoFolder photoFolder = new PhotoFolder("","");
        List<PhotoFile> photos = Collections.singletonList(new PhotoFile(folder.newFile()));

        LocalCache localCache = Mockito.mock(LocalCache.class);
        UserAccount userAccount = Mockito.mock(UserAccount.class);
        LazySender sender = new LazySender(folder.getRoot(), localCache, userAccount);
        PhotoFolderDir folder = new PhotoFolderDir(this.folder.getRoot());
        PhotoFolderInfo photoInfo = new PhotoFolderInfo(folder, photos);

        Mockito.when(localCache.getPhotoFolder(folder)).thenReturn(Optional.of(photoFolder));
        Mockito.when(localCache.getNonExistingPhotos(photos, photoFolder)).thenReturn(photos);

        // when
        sender.sendIfNeeded(photoInfo);

        // then
        Mockito.verify(userAccount).uploadPhotos(photos);
        Mockito.verify(userAccount).movePhotosToFolder(Mockito.any(), Mockito.eq(photoFolder));
    }

    @Test
    public void givenNewFileInExistingPhotoFolderThenPhotoStoredInLocalCache() throws IOException {
        // given
        PhotoFolder photoFolder = new PhotoFolder("","");
        List<PhotoFile> photos = Collections.singletonList(new PhotoFile(folder.newFile()));

        LocalCache localCache = Mockito.mock(LocalCache.class);
        UserAccount userAccount = Mockito.mock(UserAccount.class);
        LazySender sender = new LazySender(folder.getRoot(), localCache, userAccount);
        PhotoFolderDir folder = new PhotoFolderDir(this.folder.getRoot());
        PhotoFolderInfo photoInfo = new PhotoFolderInfo(folder, photos);

        Mockito.when(localCache.getPhotoFolder(folder)).thenReturn(Optional.of(photoFolder));

        // when
        sender.sendIfNeeded(photoInfo);

        // then
        Mockito.verify(localCache).storeUploadedFiles(Mockito.anyList(), Mockito.eq(photoFolder));
    }

    @Test
    public void givenAlreadySentPhotosThenNoUploadPerformed() throws IOException {
        PhotoFolder photoFolder = new PhotoFolder("","");
        List<PhotoFile> photos = Collections.singletonList(new PhotoFile(folder.newFile()));
        List<PhotoFile> emptyList = Collections.emptyList();

        LocalCache localCache = Mockito.mock(LocalCache.class);
        UserAccount userAccount = Mockito.mock(UserAccount.class);
        LazySender sender = new LazySender(folder.getRoot(), localCache, userAccount);
        PhotoFolderDir folder = new PhotoFolderDir(this.folder.getRoot());
        PhotoFolderInfo photoInfo = new PhotoFolderInfo(folder, photos);

        Mockito.when(localCache.getPhotoFolder(folder)).thenReturn(Optional.of(photoFolder));
        Mockito.when(localCache.getNonExistingPhotos(photos, photoFolder)).thenReturn(emptyList);

        // when
        sender.sendIfNeeded(photoInfo);

        // then
        Mockito.verify(userAccount).uploadPhotos(Mockito.eq(emptyList));
        Mockito.verify(userAccount).movePhotosToFolder(Mockito.any(), Mockito.eq(photoFolder));
    }
}