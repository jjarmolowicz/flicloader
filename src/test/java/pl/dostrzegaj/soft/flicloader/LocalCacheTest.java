package pl.dostrzegaj.soft.flicloader;

import java.io.File;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class LocalCacheTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void givenNonStoredFolderThenEmptyResponseReturned() {
        LocalCache localCache = new LocalCache(folder.getRoot());

        Optional<PhotoFolder> result = localCache.getPhotoFolder(new PhotoFolderDir(new File("anyDirWillDo")));

        Assert.assertFalse(result.isPresent());
    }

    @Test
    public void givenAlreadyStoredFolderThanItsReturned() {
        LocalCache localCache = new LocalCache(folder.getRoot());

        File dirForPhoto = new File("anyDirWillDo");
        PhotoFolder photoFolder = new PhotoFolder("ID",dirForPhoto.getAbsolutePath());
        localCache.storePhotoFolder(photoFolder);
        Optional<PhotoFolder> result = localCache.getPhotoFolder( new PhotoFolderDir(dirForPhoto));

        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(photoFolder, result.get());
    }
}