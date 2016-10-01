package pl.dostrzegaj.soft.flicloader;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class LocalCacheFolderTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void givenNonStoredFolderThenEmptyResponseReturned() throws IOException {
        LocalCache localCache = new LocalCache(folder.getRoot());

        Optional<PhotoFolderId> result = localCache.getPhotoFolder(new PhotoFolderDir(folder.getRoot(), folder.newFolder()));

        Assert.assertFalse(result.isPresent());
    }

    @Test
    public void givenAlreadyStoredFolderThanItsReturned() throws IOException {
        LocalCache localCache = new LocalCache(folder.getRoot());

        File dirForPhoto = folder.newFolder();
        String id = "ID";
        PhotoFolder photoFolder = new PhotoFolder(id, new RelativePath(folder.getRoot(), dirForPhoto));
        localCache.storePhotoFolder(photoFolder);
        Optional<PhotoFolderId> result = localCache.getPhotoFolder(new PhotoFolderDir(folder.getRoot(), dirForPhoto));

        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(new PhotoFolderId(id), result.get());
    }
}