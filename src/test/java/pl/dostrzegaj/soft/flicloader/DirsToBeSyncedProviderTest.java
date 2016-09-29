package pl.dostrzegaj.soft.flicloader;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class DirsToBeSyncedProviderTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test(expected = IllegalArgumentException.class)
    public void givenNoPropertyThenException() {
        new DirsToBeSyncedProvider(new Properties()).extractAndVerifyCorrectness();
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenEmptyPropertyThenException() {
        Properties properties = new Properties();
        properties.put(DirsToBeSyncedProvider.DIRS_KEY, "");
        new DirsToBeSyncedProvider(properties).extractAndVerifyCorrectness();
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenNonExistingDirectoryThenException() {
        Properties properties = new Properties();
        properties.put(DirsToBeSyncedProvider.DIRS_KEY, "nonexisting");
        new DirsToBeSyncedProvider(properties).extractAndVerifyCorrectness();
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenAFileInsteadOfDirectoryThenException() throws IOException {
        Properties properties = new Properties();
        properties.put(DirsToBeSyncedProvider.DIRS_KEY, File.createTempFile("temp", Long.toString(System.nanoTime()))
            .toString());
        new DirsToBeSyncedProvider(properties).extractAndVerifyCorrectness();
    }

    @Test
    public void givenCorrectDirectoryWhenVerifiedThenReturnedAsIs() throws IOException {
        Properties properties = new Properties();

        File temp = folder.getRoot();

        properties.put(DirsToBeSyncedProvider.DIRS_KEY, temp.toString());
        List<File> files = new DirsToBeSyncedProvider(properties).extractAndVerifyCorrectness();
        Assert.assertEquals(1, files.size());
        Assert.assertEquals(files.get(0), temp);
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenSameDirectoryTwoTimesThenException() throws IOException {
        Properties properties = new Properties();

        File temp = folder.getRoot();

        properties.put(DirsToBeSyncedProvider.DIRS_KEY, temp.toString() + "," + temp.toString());
        new DirsToBeSyncedProvider(properties).extractAndVerifyCorrectness();
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenParentAndChildDirectoryThenException() throws IOException {
        Properties properties = new Properties();

        File temp = folder.getRoot();

        properties.put(DirsToBeSyncedProvider.DIRS_KEY, temp.toString() + "," + temp.getParent().toString());
        List<File> files = new DirsToBeSyncedProvider(properties).extractAndVerifyCorrectness();
    }


}