package pl.dostrzegaj.soft.flicloader;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hamcrest.CustomTypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import com.google.common.collect.Lists;

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
        String photoId = "1";
        File photoFile = this.folder.newFile();
        PhotoFolderInfo photoInfo = new PhotoFolderInfo(folder, Collections.singletonList(new PhotoFile(photoFile)));
        Mockito.when(localCache.getPhotoFolder(folder)).thenReturn(Optional.empty());
        Mockito.when(userAccount.uploadPhotos(Mockito.anyList())).thenReturn(
                Lists.newArrayList(new UploadedPhoto(photoId, photoFile.getAbsolutePath())));

        // when
        sender.sendIfNeeded(photoInfo);

        // then
        Mockito.verify(userAccount).createPhotoFolder(this.folder.getRoot().getName(), photoId);
    }

    @Test
    public void givenNewPhotoFolderThenPhotosetStoredInLocalCache() throws IOException {
        // given
        LocalCache localCache = Mockito.mock(LocalCache.class);
        UserAccount userAccount = Mockito.mock(UserAccount.class);
        LazySender sender = new LazySender(folder.getRoot(), localCache, userAccount);
        PhotoFolderDir folder = new PhotoFolderDir(this.folder.getRoot());
        String photoId = "1";
        File photoFile = this.folder.newFile();
        PhotoFolderInfo photoInfo = new PhotoFolderInfo(folder, Collections.singletonList(new PhotoFile(photoFile)));
        Mockito.when(localCache.getPhotoFolder(folder)).thenReturn(Optional.empty());
        Mockito.when(userAccount.uploadPhotos(Mockito.anyList())).thenReturn(
                Lists.newArrayList(new UploadedPhoto(photoId, photoFile.getAbsolutePath())));

        // when
        sender.sendIfNeeded(photoInfo);

        // then
        Mockito.verify(localCache).storePhotoFolder(Mockito.any(PhotoFolder.class));
    }

    @Test
    public void givenNewFileInExistingPhotoFolderThenPhotoUploadedAndStoredInCache() throws IOException {
        // given
        PhotoFolder photoFolder = new PhotoFolder("", "");
        List<PhotoFile> photos = Collections.singletonList(new PhotoFile(folder.newFile()));

        LocalCache localCache = Mockito.mock(LocalCache.class);
        UserAccount userAccount = Mockito.mock(UserAccount.class);
        LazySender sender = new LazySender(folder.getRoot(), localCache, userAccount);
        PhotoFolderDir folder = new PhotoFolderDir(this.folder.getRoot());
        PhotoFolderInfo photoInfo = new PhotoFolderInfo(folder, photos);

        Mockito.when(localCache.getPhotoFolder(folder)).thenReturn(Optional.of(photoFolder));
        Mockito.when(localCache.getNonExistingPhotos(photos, photoFolder)).thenReturn(photos);
        List<UploadedPhoto> uploaded =
                photos.stream().map(i -> new UploadedPhoto(i.getFile().getName(), i.getFile().getAbsolutePath()))
                        .collect(Collectors.toList());
        Mockito.when(userAccount.uploadPhotos(photos)).thenReturn(Lists.newArrayList(uploaded));

        // when
        sender.sendIfNeeded(photoInfo);

        // then
        Mockito.verify(userAccount).uploadPhotos(photos);
        Mockito.verify(userAccount).movePhotosToFolder(Mockito.any(), Mockito.eq(photoFolder));
        Mockito.verify(localCache).storeUploadedFiles(Mockito.eq(uploaded), Mockito.eq(photoFolder));
    }

    @Test
    public void givenNewFileInNewPhotoFolderThenAllPhotoUploadedAndStoredInCache() throws IOException {
        // given
        PhotoFile photoFile = new PhotoFile(folder.newFile());
        List<PhotoFile> photos = Collections.singletonList(photoFile);

        LocalCache localCache = Mockito.mock(LocalCache.class);
        UserAccount userAccount = Mockito.mock(UserAccount.class);
        LazySender sender = new LazySender(folder.getRoot(), localCache, userAccount);
        PhotoFolderDir folder = new PhotoFolderDir(this.folder.getRoot());
        PhotoFolderInfo photoInfo = new PhotoFolderInfo(folder, photos);

        Mockito.when(localCache.getPhotoFolder(folder)).thenReturn(Optional.empty());
        List<UploadedPhoto> uploaded = Lists.newArrayList(new UploadedPhoto("id", photoFile.getFile().getAbsolutePath()));
        Mockito.when(userAccount.uploadPhotos(photos)).thenReturn(Lists.newArrayList(uploaded));

        // when
        sender.sendIfNeeded(photoInfo);

        // then
        Mockito.verify(userAccount).createPhotoFolder(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(userAccount).uploadPhotos(photos);
        Mockito.verify(localCache).storeUploadedFiles(Mockito.eq(uploaded), Mockito.any());
    }

    @Test
    public void givenNewFilesInNewPhotoFolderThenAllPhotoUploadedAndStoredInCache() throws IOException {
        // given
        PhotoFile photoFile1 = new PhotoFile(folder.newFile());
        PhotoFile photoFile2 = new PhotoFile(folder.newFile());
        List<PhotoFile> photos = Lists.newArrayList(photoFile1, photoFile2);

        LocalCache localCache = Mockito.mock(LocalCache.class);
        UserAccount userAccount = Mockito.mock(UserAccount.class);
        LazySender sender = new LazySender(folder.getRoot(), localCache, userAccount);
        PhotoFolderDir folder = new PhotoFolderDir(this.folder.getRoot());
        PhotoFolderInfo photoInfo = new PhotoFolderInfo(folder, photos);

        Mockito.when(localCache.getPhotoFolder(folder)).thenReturn(Optional.empty());
        List<UploadedPhoto> uploaded =
                photos.stream().map(i -> new UploadedPhoto(i.getFile().getName(), i.getFile().getAbsolutePath()))
                        .collect(Collectors.toList());
        Mockito.when(userAccount.uploadPhotos(photos)).thenReturn(Lists.newArrayList(uploaded));

        // when
        sender.sendIfNeeded(photoInfo);

        // then
        Mockito.verify(userAccount).createPhotoFolder(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(userAccount).uploadPhotos(photos);
        CustomTypeSafeMatcher<List<UploadedPhoto>> customTypeSafeMatcher = new CustomTypeSafeMatcher<List<UploadedPhoto>>("Second uploaded file") {

            @Override
            protected boolean matchesSafely(final List<UploadedPhoto> item) {
                return item.size() == 1 && item.get(0).equals(uploaded.get(1));
            }
        };
        Mockito.verify(userAccount).movePhotosToFolder(Mockito.argThat(customTypeSafeMatcher), Mockito.any());
        Mockito.verify(localCache).storeUploadedFiles(Mockito.eq(uploaded), Mockito.any());
    }

    @Test
    public void givenNewFileInExistingPhotoFolderThenPhotoStoredInLocalCache() throws IOException {
        // given
        PhotoFolder photoFolder = new PhotoFolder("", "");
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
        PhotoFolder photoFolder = new PhotoFolder("", "");
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