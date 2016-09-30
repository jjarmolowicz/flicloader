package pl.dostrzegaj.soft.flicloader;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class PhotoFoldersProviderTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void givenEmptyDirThenEmptyOutput() throws IOException {
        File tempDir = folder.getRoot();
        PhotoFoldersIterator photoFolderInfos = new PhotoFoldersIterator(tempDir,new UploadConfig(new Properties()));
        Assert.assertFalse(photoFolderInfos.iterator().hasNext());
    }

    @Test
    public void givenDirWithFilesThenInfoProduced() throws IOException {
        File tempDir = folder.getRoot();
        new File(tempDir, "photo1.jpg").createNewFile();
        PhotoFoldersIterator photoFolderInfos = new PhotoFoldersIterator(tempDir,new UploadConfig(new Properties()));
        Iterator<PhotoFolderInfo> iterator = photoFolderInfos.iterator();
        Assert.assertTrue(iterator.hasNext());
        PhotoFolderInfo next = iterator.next();
        Assert.assertEquals(1, next.getPhotos().size());
        Assert.assertFalse(iterator.hasNext());
    }

    @Test
    public void givenDirWithSubdirThenInfoProducedOnlyForSubdir() throws IOException {
        File tempDir = folder.getRoot();
        File subdir = new File(tempDir, "subdir");
        subdir.mkdir();
        new File(subdir, "photo1.jpg").createNewFile();
        PhotoFoldersIterator photoFolderInfos = new PhotoFoldersIterator(tempDir,new UploadConfig(new Properties()));
        Iterator<PhotoFolderInfo> iterator = photoFolderInfos.iterator();
        Assert.assertTrue(iterator.hasNext());
        PhotoFolderInfo next = iterator.next();
        Assert.assertEquals(1, next.getPhotos().size());
        Assert.assertEquals(subdir, next.getFolder().getDir());
        Assert.assertFalse(iterator.hasNext());
    }

    @Test
    public void givenDirWithSubdirAndPhotoThenInfoProducedForBoth() throws IOException {
        File tempDir = folder.getRoot();
        new File(tempDir, "photo1.jpg").createNewFile();
        File subdir = new File(tempDir, "subdir");
        subdir.mkdir();
        new File(subdir, "photo1.jpg").createNewFile();
        new File(subdir, "photo2.jpg").createNewFile();
        PhotoFoldersIterator photoFolderInfos = new PhotoFoldersIterator(tempDir,new UploadConfig(new Properties()));
        Iterator<PhotoFolderInfo> iterator = photoFolderInfos.iterator();
        Assert.assertTrue(iterator.hasNext());
        PhotoFolderInfo next = iterator.next();
        Assert.assertEquals(1, next.getPhotos().size());
        Assert.assertTrue(iterator.hasNext());
        next = iterator.next();
        Assert.assertEquals(2, next.getPhotos().size());
        Assert.assertEquals(subdir, next.getFolder().getDir());
        Assert.assertFalse(iterator.hasNext());
    }

}