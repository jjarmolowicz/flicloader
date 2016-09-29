package pl.dostrzegaj.soft.flicloader;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

class PhotoFoldersIterator implements Iterable<PhotoFolderInfo> {

    private File root;

    public PhotoFoldersIterator(File root) {
        this.root = root;
    }

    @Override
    public Iterator<PhotoFolderInfo> iterator() {
        return new Iterator<PhotoFolderInfo>() {

            private final Queue<File> dirs;
            private PhotoFolderInfo nextElement;
            {
                dirs = Lists.newLinkedList();
                dirs.add(root);
            }

            @Override
            public boolean hasNext() {
                while (!dirs.isEmpty()) {
                    List<File> files = Lists.newArrayList();
                    File dir = dirs.poll();
                    for (File file : dir.listFiles()) {
                        if (file.isDirectory()) {
                            dirs.add(file);
                        } else {
                            files.add(file);
                        }
                    }
                    if (!files.isEmpty()) {
                        nextElement =
                            new PhotoFolderInfo(new PhotoFolderDir(dir), files.stream().map(i -> new PhotoFile(i))
                                .collect(Collectors.toList()));
                        return true;
                    }
                }
                return false;
            }

            @Override
            public PhotoFolderInfo next() {
                return nextElement;
            }
        };
    }
}
