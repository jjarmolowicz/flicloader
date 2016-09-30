package pl.dostrzegaj.soft.flicloader;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import org.junit.rules.TemporaryFolder;

import com.google.common.collect.Lists;

public class LocalCachePhotosTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void givenNonStoredFilesThenAllOfThemAreReturned() {
        LocalCache localCache = new LocalCache(folder.getRoot());

        List<PhotoFile> photos = Collections.singletonList(new PhotoFile(new File("sth")));
        PhotoFolder photoFolder = new PhotoFolder("1", "path");
        List<PhotoFile> nonExistingPhotos = localCache.getNonExistingPhotos(photos, photoFolder);

        Assert.assertEquals(photos, nonExistingPhotos);
    }

    @Test
    public void givenStoredFilesThenNoneOfThemReturned() throws IOException {
        LocalCache localCache = new LocalCache(folder.getRoot());

        PhotoFile photoFile = new PhotoFile(folder.newFile());
        List<PhotoFile> photos = Collections.singletonList(photoFile);
        PhotoFolder photoFolder = new PhotoFolder("1", "path");
        List<UploadedPhoto> uploadedPhotos = Lists.newArrayList(new UploadedPhoto("1", photoFile.getFile().getAbsolutePath()));

        localCache.storePhotoFolder(photoFolder);
        localCache.storeUploadedFiles(uploadedPhotos, photoFolder);
        List<PhotoFile> nonExistingPhotos = localCache.getNonExistingPhotos(photos, photoFolder);

        Assert.assertTrue(nonExistingPhotos.isEmpty());
    }

    @Test
    public void givenSomeStoredFilesThenNonStoredReturned() throws IOException {
        LocalCache localCache = new LocalCache(folder.getRoot());

        PhotoFile photoFile1 = new PhotoFile(folder.newFile());
        PhotoFile photoFile2 = new PhotoFile(folder.newFile());
        PhotoFile photoFile3 = new PhotoFile(folder.newFile());
        List<PhotoFile> photos = Lists.newArrayList(photoFile1,photoFile2,photoFile3);
        PhotoFolder photoFolder = new PhotoFolder("1", "path");
        List<UploadedPhoto> uploadedPhotos = Lists.newArrayList(new UploadedPhoto("1", photoFile1.getFile().getAbsolutePath()),new UploadedPhoto("3", photoFile3.getFile().getAbsolutePath()));

        localCache.storePhotoFolder(photoFolder);
        localCache.storeUploadedFiles(uploadedPhotos, photoFolder);
        List<PhotoFile> nonExistingPhotos = localCache.getNonExistingPhotos(photos, photoFolder);

        Assert.assertThat(nonExistingPhotos, CoreMatchers.hasItem(photoFile2));
        Assert.assertThat(nonExistingPhotos, CoreMatchers.not(CoreMatchers.hasItem(photoFile1)));
        Assert.assertThat(nonExistingPhotos, CoreMatchers.not(CoreMatchers.hasItem(photoFile3)));
    }

    @Test
    public void givenStoredFilesButInOtherFolderThenAllReturned() throws IOException {
        LocalCache localCache = new LocalCache(folder.getRoot());

        PhotoFile photoFile = new PhotoFile(folder.newFile());
        List<PhotoFile> photos = Collections.singletonList(photoFile);
        PhotoFolder photoFolder1 = new PhotoFolder("1", "path1");
        PhotoFolder photoFolder2 = new PhotoFolder("2", "path2");
        List<UploadedPhoto> uploadedPhotos = Lists.newArrayList(new UploadedPhoto("1", photoFile.getFile().getAbsolutePath()));

        localCache.storePhotoFolder(photoFolder1);
        localCache.storePhotoFolder(photoFolder2);
        localCache.storeUploadedFiles(uploadedPhotos, photoFolder1);
        List<PhotoFile> nonExistingPhotos = localCache.getNonExistingPhotos(photos, photoFolder2);

        Assert.assertEquals(photos, nonExistingPhotos);
    }
}
