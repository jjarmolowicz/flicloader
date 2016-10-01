package pl.dostrzegaj.soft.flicloader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;
import org.hamcrest.Description;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class PhotoFoldersIterableTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private static class ItemMatcher extends TypeSafeMatcher<PhotoFolderInfo> {

        private File expected;

        public ItemMatcher(final File expected) {
            this.expected = expected;
        }

        @Override
        protected boolean matchesSafely(final PhotoFolderInfo item) {
            return item.getFolder().getDir().equals(expected);
        }

        @Override
        public void describeTo(final Description description) {
            description.appendValue(expected);
        }
    }

    @Test
    public void givenNestedDirectoryStructureWhenIteratadThenChildNodesComeBeforeParentSiblings() throws IOException,
        InterruptedException {
        File dira = folder.newFolder("a");
        TimeUnit.MILLISECONDS.sleep(10);
        File dirb = folder.newFolder("b");
        TimeUnit.MILLISECONDS.sleep(10);
        File dirc = folder.newFolder("c");
        TimeUnit.MILLISECONDS.sleep(10);
        File diraa = new File(dira, "a");
        diraa.mkdir();
        File dirab = new File(dira, "b");
        dirab.mkdir();
        File diraaa = new File(diraa, "a");
        diraaa.mkdir();
        File dirca = new File(dirc, "a");
        dirca.mkdir();

        for (File dir : new File[] {dira, diraa, diraaa, dirab, dirb, dirc, dirca}) {
            new File(dir, "photo.jpg").createNewFile();
        }

        PhotoFoldersIterable iterable = new PhotoFoldersIterable(folder.getRoot(), new UploadConfig(new Properties()));
        MatcherAssert.assertThat(iterable, Matchers.contains(new ItemMatcher(dira), new ItemMatcher(diraa), new ItemMatcher(
            diraaa), new ItemMatcher(dirab), new ItemMatcher(dirb), new ItemMatcher(dirc), new ItemMatcher(dirca)));

    }

}