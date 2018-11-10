package pl.dostrzegaj.soft.flicloader;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.collect.Lists;

public class LocalCachePhotosTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void givenNonStoredFilesThenAllOfThemAreReturned() {

        LocalCache localCache = new LocalCache(folder.getRoot());

        List<PhotoFile> photos = Collections.singletonList(new PhotoFile(folder.getRoot(), new File("sth")));
        PhotoFolderId photoFolderId = new PhotoFolderId("1");
        List<PhotoFile> nonExistingPhotos = localCache.getNonExistingPhotos(photos, photoFolderId);

        Assert.assertEquals(photos, nonExistingPhotos);
    }

    @Test
    public void givenStoredFilesThenNoneOfThemReturned() throws IOException {

        LocalCache localCache = new LocalCache(folder.getRoot());

        File innerFolder = this.folder.newFolder();
        PhotoFile photoFile = new PhotoFile(innerFolder, folder.newFile());
        List<PhotoFile> photos = Collections.singletonList(photoFile);
        String id = "1";
        PhotoFolderId photoFolderId = new PhotoFolderId(id);
        PhotoFolder photoFolder = new PhotoFolder(id, new RelativePath(folder.getRoot(), innerFolder));
        List<UploadedPhoto> uploadedPhotos = Lists.newArrayList(new UploadedPhoto("1", new RelativePath(innerFolder, photoFile.getFile())));

        localCache.storePhotoFolder(photoFolder);
        storeUploadedFiles(uploadedPhotos, photoFolderId, localCache);
        List<PhotoFile> nonExistingPhotos = localCache.getNonExistingPhotos(photos, photoFolderId);

        Assert.assertTrue(nonExistingPhotos.isEmpty());
    }

    void storeUploadedFiles(List<UploadedPhoto> uploadedPhotos, PhotoFolderId photoFolderId, LocalCache localCache) {
        for (UploadedPhoto uploadedPhoto : uploadedPhotos) {
            localCache.storeUploadedFile(uploadedPhoto, photoFolderId);
        }
    }

    @Test
    public void givenSomeStoredFilesThenNonStoredReturned() throws IOException {

        LocalCache localCache = new LocalCache(folder.getRoot());

        File innerFolder = this.folder.newFolder();
        PhotoFile photoFile1 = new PhotoFile(innerFolder, folder.newFile());
        PhotoFile photoFile2 = new PhotoFile(innerFolder, folder.newFile());
        PhotoFile photoFile3 = new PhotoFile(innerFolder, folder.newFile());
        List<PhotoFile> photos = Lists.newArrayList(photoFile1, photoFile2, photoFile3);
        String id = "1";
        PhotoFolderId photoFolderId = new PhotoFolderId(id);
        PhotoFolder photoFolder = new PhotoFolder(id, new RelativePath(this.folder.getRoot(), innerFolder));
        List<UploadedPhoto> uploadedPhotos = Lists.newArrayList(new UploadedPhoto("1", new RelativePath(innerFolder, photoFile1.getFile())),
            new UploadedPhoto("3", new RelativePath(innerFolder, photoFile3.getFile())));

        localCache.storePhotoFolder(photoFolder);
        storeUploadedFiles(uploadedPhotos, photoFolderId, localCache);
        List<PhotoFile> nonExistingPhotos = localCache.getNonExistingPhotos(photos, photoFolderId);

        Assert.assertThat(nonExistingPhotos, CoreMatchers.hasItem(photoFile2));
        Assert.assertThat(nonExistingPhotos, CoreMatchers.not(CoreMatchers.hasItem(photoFile1)));
        Assert.assertThat(nonExistingPhotos, CoreMatchers.not(CoreMatchers.hasItem(photoFile3)));
    }

    @Test
    public void givenStoredFilesButInOtherFolderThenAllReturned() throws IOException {

        LocalCache localCache = new LocalCache(folder.getRoot());

        File innerFolder = folder.newFolder();
        PhotoFile photoFile = new PhotoFile(innerFolder, folder.newFile());
        List<PhotoFile> photos = Collections.singletonList(photoFile);
        PhotoFolder photoFolder1 = new PhotoFolder("1", new RelativePath(folder.getRoot(), innerFolder));
        PhotoFolder photoFolder2 = new PhotoFolder("2", new RelativePath(folder.getRoot(), folder.newFolder()));
        List<UploadedPhoto> uploadedPhotos = Lists.newArrayList(new UploadedPhoto("1", new RelativePath(folder.getRoot(), photoFile.getFile())));

        localCache.storePhotoFolder(photoFolder1);
        localCache.storePhotoFolder(photoFolder2);
        storeUploadedFiles(uploadedPhotos, new PhotoFolderId(photoFolder1.getId()), localCache);
        List<PhotoFile> nonExistingPhotos = localCache.getNonExistingPhotos(photos, new PhotoFolderId(photoFolder2.getId()));

        Assert.assertEquals(photos, nonExistingPhotos);
    }

    @Test
    public void givenTwoFilesWithSameRelativePathThenBothCanBeStoredd() throws IOException {

        LocalCache localCache = new LocalCache(folder.getRoot());

        File innerFolder = folder.newFolder();
        PhotoFile photoFile = new PhotoFile(innerFolder, folder.newFile());
        List<PhotoFile> photos = Collections.singletonList(photoFile);
        PhotoFolder photoFolder1 = new PhotoFolder("1", new RelativePath(folder.getRoot(), innerFolder));
        PhotoFolder photoFolder2 = new PhotoFolder("2", new RelativePath(folder.getRoot(), folder.newFolder()));
        List<UploadedPhoto> uploadedPhotos = Lists.newArrayList(new UploadedPhoto("1", new RelativePath(folder.getRoot(), photoFile.getFile())));

        List<UploadedPhoto> uploadedPhotos2 = Lists.newArrayList(new UploadedPhoto("2", new RelativePath(folder.getRoot(), photoFile.getFile())));

        localCache.storePhotoFolder(photoFolder1);
        localCache.storePhotoFolder(photoFolder2);
        storeUploadedFiles(uploadedPhotos, new PhotoFolderId(photoFolder1.getId()), localCache);
        storeUploadedFiles(uploadedPhotos2, new PhotoFolderId(photoFolder2.getId()), localCache);
        List<PhotoFile> nonExistingPhotos = localCache.getNonExistingPhotos(photos, new PhotoFolderId(photoFolder2.getId()));

        Assert.assertEquals(photos, nonExistingPhotos);
    }
}
