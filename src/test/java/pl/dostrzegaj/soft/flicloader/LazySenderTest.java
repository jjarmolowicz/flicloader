package pl.dostrzegaj.soft.flicloader;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
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
        PhotoFolderDir folder = new PhotoFolderDir(this.folder.getRoot(),this.folder.getRoot());
        String photoId = "1";
        File photoFile = this.folder.newFile();
        PhotoFolderInfo photoInfo =
            new PhotoFolderInfo(folder, Collections.singletonList(new PhotoFile(this.folder.getRoot(), photoFile)),
                new UploadConfig(new Properties()));
        Mockito.when(localCache.getPhotoFolder(folder)).thenReturn(Optional.empty());
        Mockito.when(userAccount.uploadPhotos(Mockito.anyList(), Mockito.any())).thenReturn(
            Lists.newArrayList(new UploadedPhoto(photoId, new RelativePath(this.folder.getRoot(), photoFile))));

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
        PhotoFolderDir folder = new PhotoFolderDir(this.folder.getRoot(),this.folder.getRoot());
        String photoId = "1";
        File photoFile = this.folder.newFile();
        PhotoFolderInfo photoInfo =
            new PhotoFolderInfo(folder, Collections.singletonList(new PhotoFile(this.folder.getRoot(), photoFile)),
                new UploadConfig(new Properties()));
        Mockito.when(localCache.getPhotoFolder(folder)).thenReturn(Optional.empty());
        Mockito.when(userAccount.uploadPhotos(Mockito.anyList(), Mockito.any())).thenReturn(
            Lists.newArrayList(new UploadedPhoto(photoId, new RelativePath(this.folder.getRoot(), photoFile))));

        // when
        sender.sendIfNeeded(photoInfo);

        // then
        Mockito.verify(localCache).storePhotoFolder(Mockito.any(PhotoFolder.class));
    }

    @Test
    public void givenNewFileInExistingPhotoFolderThenPhotoUploadedAndStoredInCache() throws IOException {
        // given
        PhotoFolderId photoFolder = new PhotoFolderId("1");
        List<PhotoFile> photos = Collections.singletonList(new PhotoFile(folder.getRoot(), folder.newFile()));

        LocalCache localCache = Mockito.mock(LocalCache.class);
        UserAccount userAccount = Mockito.mock(UserAccount.class);
        LazySender sender = new LazySender(folder.getRoot(), localCache, userAccount);
        PhotoFolderDir folder = new PhotoFolderDir(this.folder.getRoot(),this.folder.getRoot());
        PhotoFolderInfo photoInfo = new PhotoFolderInfo(folder, photos, new UploadConfig(new Properties()));

        Mockito.when(localCache.getPhotoFolder(folder)).thenReturn(Optional.of(photoFolder));
        Mockito.when(localCache.getNonExistingPhotos(photos, photoFolder)).thenReturn(photos);
        List<UploadedPhoto> uploaded =
            photos.stream()
                .map(i -> new UploadedPhoto(i.getFile().getName(), new RelativePath(this.folder.getRoot(), i.getFile())))
                .collect(Collectors.toList());
        Mockito.when(userAccount.uploadPhotos(Mockito.eq(photos), Mockito.any())).thenReturn(Lists.newArrayList(uploaded));

        // when
        sender.sendIfNeeded(photoInfo);

        // then
        Mockito.verify(userAccount).uploadPhotos(Mockito.eq(photos), Mockito.any());
        Mockito.verify(userAccount).movePhotosToFolder(Mockito.any(), Mockito.eq(photoFolder));
        Mockito.verify(localCache).storeUploadedFiles(Mockito.eq(uploaded), Mockito.eq(photoFolder));
    }

    @Test
    public void givenNewFileInNewPhotoFolderThenAllPhotoUploadedAndStoredInCache() throws IOException {
        // given
        PhotoFile photoFile = new PhotoFile(folder.getRoot(), folder.newFile());
        List<PhotoFile> photos = Collections.singletonList(photoFile);

        LocalCache localCache = Mockito.mock(LocalCache.class);
        UserAccount userAccount = Mockito.mock(UserAccount.class);
        LazySender sender = new LazySender(folder.getRoot(), localCache, userAccount);
        PhotoFolderDir folder = new PhotoFolderDir(this.folder.getRoot(),this.folder.getRoot());
        PhotoFolderInfo photoInfo = new PhotoFolderInfo(folder, photos, new UploadConfig(new Properties()));

        Mockito.when(localCache.getPhotoFolder(folder)).thenReturn(Optional.empty());
        List<UploadedPhoto> uploaded =
            Lists.newArrayList(new UploadedPhoto("id", new RelativePath(this.folder.getRoot(), photoFile.getFile())));
        Mockito.when(userAccount.uploadPhotos(Mockito.eq(photos), Mockito.any())).thenReturn(Lists.newArrayList(uploaded));

        // when
        sender.sendIfNeeded(photoInfo);

        // then
        Mockito.verify(userAccount).createPhotoFolder(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(userAccount).uploadPhotos(Mockito.eq(photos), Mockito.any());
        Mockito.verify(localCache).storeUploadedFiles(Mockito.eq(uploaded), Mockito.any());
    }

    @Test
    public void givenNewFilesInNewPhotoFolderThenAllPhotoUploadedAndStoredInCache() throws IOException {
        // given
        PhotoFile photoFile1 = new PhotoFile(folder.getRoot(), folder.newFile());
        PhotoFile photoFile2 = new PhotoFile(folder.getRoot(), folder.newFile());
        List<PhotoFile> photos = Lists.newArrayList(photoFile1, photoFile2);

        LocalCache localCache = Mockito.mock(LocalCache.class);
        UserAccount userAccount = Mockito.mock(UserAccount.class);
        LazySender sender = new LazySender(folder.getRoot(), localCache, userAccount);
        PhotoFolderDir folder = new PhotoFolderDir(this.folder.getRoot(),this.folder.getRoot());
        PhotoFolderInfo photoInfo = new PhotoFolderInfo(folder, photos, new UploadConfig(new Properties()));

        Mockito.when(localCache.getPhotoFolder(folder)).thenReturn(Optional.empty());
        List<UploadedPhoto> uploaded =
            photos.stream()
                .map(i -> new UploadedPhoto(i.getFile().getName(), new RelativePath(this.folder.getRoot(), i.getFile())))
                .collect(Collectors.toList());
        Mockito.when(userAccount.uploadPhotos(Mockito.eq(photos), Mockito.any())).thenReturn(Lists.newArrayList(uploaded));

        // when
        sender.sendIfNeeded(photoInfo);

        // then
        Mockito.verify(userAccount).createPhotoFolder(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(userAccount).uploadPhotos(Mockito.eq(photos), Mockito.any());
        CustomTypeSafeMatcher<List<UploadedPhoto>> customTypeSafeMatcher =
            new CustomTypeSafeMatcher<List<UploadedPhoto>>("Second uploaded file") {

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
        PhotoFolderId photoFolder = new PhotoFolderId("");
        List<PhotoFile> photos = Collections.singletonList(new PhotoFile(folder.getRoot(), folder.newFile()));

        LocalCache localCache = Mockito.mock(LocalCache.class);
        UserAccount userAccount = Mockito.mock(UserAccount.class);
        LazySender sender = new LazySender(folder.getRoot(), localCache, userAccount);
        PhotoFolderDir folder = new PhotoFolderDir(this.folder.getRoot(),this.folder.getRoot());
        PhotoFolderInfo photoInfo = new PhotoFolderInfo(folder, photos, new UploadConfig(new Properties()));

        Mockito.when(localCache.getPhotoFolder(folder)).thenReturn(Optional.of(photoFolder));

        // when
        sender.sendIfNeeded(photoInfo);

        // then
        Mockito.verify(localCache).storeUploadedFiles(Mockito.anyList(), Mockito.eq(photoFolder));
    }

    @Test
    public void givenAlreadySentPhotosThenNoUploadPerformed() throws IOException {
        PhotoFolderId photoFolder = new PhotoFolderId("");
        List<PhotoFile> photos = Collections.singletonList(new PhotoFile(folder.getRoot(),folder.newFile()));
        List<PhotoFile> emptyList = Collections.emptyList();

        LocalCache localCache = Mockito.mock(LocalCache.class);
        UserAccount userAccount = Mockito.mock(UserAccount.class);
        LazySender sender = new LazySender(folder.getRoot(), localCache, userAccount);
        PhotoFolderDir folder = new PhotoFolderDir(this.folder.getRoot(),this.folder.getRoot());
        PhotoFolderInfo photoInfo = new PhotoFolderInfo(folder, photos, new UploadConfig(new Properties()));

        Mockito.when(localCache.getPhotoFolder(folder)).thenReturn(Optional.of(photoFolder));
        Mockito.when(localCache.getNonExistingPhotos(photos, photoFolder)).thenReturn(emptyList);

        // when
        sender.sendIfNeeded(photoInfo);

        // then
        Mockito.verify(userAccount).uploadPhotos(Mockito.eq(emptyList), Mockito.any());
        Mockito.verify(userAccount).movePhotosToFolder(Mockito.any(), Mockito.eq(photoFolder));
    }
}