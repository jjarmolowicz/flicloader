package pl.dostrzegaj.soft.flicloader;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;

public class PhotoFoldersProviderTest {

    @Test
    public void givenEmptyDirThenEmptyOutput() throws IOException {
        File tempDir = createTempDir();
        PhotoFoldersIterator photoFolderInfos = new PhotoFoldersIterator(tempDir);
        Assert.assertFalse(photoFolderInfos.iterator().hasNext());
    }

    @Test
    public void givenDirWithFilesThenInfoProduced() throws IOException {
        File tempDir = createTempDir();
        new File(tempDir, "photo1").createNewFile();
        PhotoFoldersIterator photoFolderInfos = new PhotoFoldersIterator(tempDir);
        Iterator<PhotoFolderInfo> iterator = photoFolderInfos.iterator();
        Assert.assertTrue(iterator.hasNext());
        PhotoFolderInfo next = iterator.next();
        Assert.assertEquals(1, next.getPhotos().size());
        Assert.assertFalse(iterator.hasNext());
    }

    @Test
    public void givenDirWithSubdirThenInfoProducedOnlyForSubdir() throws IOException {
        File tempDir = createTempDir();
        File subdir = new File(tempDir, "subdir");
        subdir.mkdir();
        new File(subdir, "photo1").createNewFile();
        PhotoFoldersIterator photoFolderInfos = new PhotoFoldersIterator(tempDir);
        Iterator<PhotoFolderInfo> iterator = photoFolderInfos.iterator();
        Assert.assertTrue(iterator.hasNext());
        PhotoFolderInfo next = iterator.next();
        Assert.assertEquals(1, next.getPhotos().size());
        Assert.assertEquals(subdir, next.getFolder());
        Assert.assertFalse(iterator.hasNext());
    }

    @Test
    public void givenDirWithSubdirAndPhotoThenInfoProducedForBoth() throws IOException {
        File tempDir = createTempDir();
        new File(tempDir, "photo1").createNewFile();
        File subdir = new File(tempDir, "subdir");
        subdir.mkdir();
        new File(subdir, "photo1").createNewFile();
        new File(subdir, "photo2").createNewFile();
        PhotoFoldersIterator photoFolderInfos = new PhotoFoldersIterator(tempDir);
        Iterator<PhotoFolderInfo> iterator = photoFolderInfos.iterator();
        Assert.assertTrue(iterator.hasNext());
        PhotoFolderInfo next = iterator.next();
        Assert.assertEquals(1, next.getPhotos().size());
        Assert.assertTrue(iterator.hasNext());
        next = iterator.next();
        Assert.assertEquals(2, next.getPhotos().size());
        Assert.assertEquals(subdir, next.getFolder());
        Assert.assertFalse(iterator.hasNext());
    }

    File createTempDir() throws IOException {
        File temp = File.createTempFile("temp", Long.toString(System.nanoTime()));
        temp.delete();
        temp.mkdir();
        return temp;
    }
}