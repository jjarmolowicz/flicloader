package pl.dostrzegaj.soft.flicloader;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.hamcrest.CustomTypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import org.mockito.internal.verification.Times;

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
        Mockito.when(userAccount.uploadPhoto(Mockito.any(), Mockito.any())).thenReturn(
            Optional.of(new UploadedPhoto(photoId, new RelativePath(this.folder.getRoot(), photoFile))));

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
        Mockito.when(userAccount.uploadPhoto(Mockito.any(), Mockito.any())).thenReturn(
           Optional.of(new UploadedPhoto(photoId, new RelativePath(this.folder.getRoot(), photoFile))));

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
        Iterator<UploadedPhoto> iterator = uploaded.iterator();
        Mockito.when(userAccount.uploadPhoto(Mockito.any(), Mockito.any())).thenReturn(Optional.of(iterator.next()));

        // when
        sender.sendIfNeeded(photoInfo);

        // then
        Mockito.verify(userAccount, new Times(photos.size())).uploadPhoto(Mockito.any(), Mockito.any());
        Mockito.verify(userAccount, new Times(photos.size())).movePhotoToFolder(Mockito.any(), Mockito.eq(photoFolder));
        Mockito.verify(localCache, new Times(photos.size())).storeUploadedFile(Mockito.any(), Mockito.eq(photoFolder));
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
        Iterator<UploadedPhoto> photoIterator = uploaded.iterator();
        Mockito.when(userAccount.uploadPhoto(Mockito.any(), Mockito.any())).thenReturn(Optional.of(photoIterator.next()));

        // when
        sender.sendIfNeeded(photoInfo);

        // then
        CustomTypeSafeMatcher<UploadedPhoto> customTypeSafeMatcher = getCustomTypeSafeMatcher(uploaded);
        Mockito.verify(userAccount).createPhotoFolder(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(userAccount, new Times(photos.size())).uploadPhoto(Mockito.any(), Mockito.any());
        Mockito.verify(localCache).storeUploadedFile(Mockito.argThat(customTypeSafeMatcher), Mockito.any());
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
        Iterator<UploadedPhoto> photoIterator = uploaded.iterator();
        Mockito.when(userAccount.uploadPhoto(Mockito.any(), Mockito.any())).thenAnswer(a -> Optional.of(photoIterator.next()));

        // when
        sender.sendIfNeeded(photoInfo);

        // then
        Mockito.verify(userAccount).createPhotoFolder(Mockito.anyString(), Mockito.anyString());
        Times asMuchAsPhotos = new Times(photos.size());
        Mockito.verify(userAccount, asMuchAsPhotos).uploadPhoto(Mockito.any(), Mockito.any());
        CustomTypeSafeMatcher<UploadedPhoto> customTypeSafeMatcher = getCustomTypeSafeMatcher(uploaded);
        Mockito.verify(userAccount,new Times(photos.size()-1)).movePhotoToFolder(Mockito.argThat(customTypeSafeMatcher), Mockito.any());
        Mockito.verify(localCache, asMuchAsPhotos).storeUploadedFile(Mockito.argThat(customTypeSafeMatcher), Mockito.any());
    }

    CustomTypeSafeMatcher<UploadedPhoto> getCustomTypeSafeMatcher(List<UploadedPhoto> uploaded) {
        return new CustomTypeSafeMatcher<UploadedPhoto>("Second uploaded file") {

            @Override
            protected boolean matchesSafely(final UploadedPhoto item) {
                return uploaded.contains(item);
            }
        };
    }

    @Test
    public void givenNewFileInExistingPhotoFolderThenPhotoStoredInLocalCache() throws IOException {
        // given
        PhotoFolderId photoFolder = new PhotoFolderId("");
        PhotoFile o = new PhotoFile(folder.getRoot(), folder.newFile());
        List<PhotoFile> photos = Collections.singletonList(o);

        LocalCache localCache = Mockito.mock(LocalCache.class);
        UserAccount userAccount = Mockito.mock(UserAccount.class);
        LazySender sender = new LazySender(folder.getRoot(), localCache, userAccount);
        PhotoFolderDir folder = new PhotoFolderDir(this.folder.getRoot(),this.folder.getRoot());
        PhotoFolderInfo photoInfo = new PhotoFolderInfo(folder, photos, new UploadConfig(new Properties()));

        Mockito.when(localCache.getPhotoFolder(folder)).thenReturn(Optional.of(photoFolder));
        Mockito.when(localCache.getNonExistingPhotos(Mockito.eq(photos), Mockito.any())).thenReturn(photos);
        Mockito.when(userAccount.uploadPhoto(Mockito.any(), Mockito.any())).thenReturn(Optional.of(new UploadedPhoto("", o.getRelativePath())));

        // when
        sender.sendIfNeeded(photoInfo);

        // then
        Mockito.verify(localCache, new Times(photos.size())).storeUploadedFile(Mockito.any(), Mockito.eq(photoFolder));
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
        Mockito.verifyNoMoreInteractions(userAccount);
    }
}