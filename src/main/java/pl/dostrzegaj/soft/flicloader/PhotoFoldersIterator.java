package pl.dostrzegaj.soft.flicloader;

import java.io.File;
import java.io.FileFilter;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

class PhotoFoldersIterator implements Iterable<PhotoFolderInfo> {

    private static FileFilter filter = pathname -> {
        String s = pathname.getName().toLowerCase();
        return pathname.isDirectory() || s.endsWith(".jpg") || s.endsWith(".bmp") || s.endsWith("mov");
    };
    private File root;
    private UploadConfig uploadConfig;

    public PhotoFoldersIterator(File root, UploadConfig uploadConfig) {
        this.root = root;
        this.uploadConfig = uploadConfig;
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
                    for (File file : dir.listFiles(filter)) {
                        if (file.isDirectory()) {
                            dirs.add(file);
                        } else {
                            files.add(file);
                        }
                    }
                    if (!files.isEmpty()) {
                        nextElement =
                            new PhotoFolderInfo(new PhotoFolderDir(root, dir), files.stream()
                                .map(i -> new PhotoFile(dir, i)).collect(Collectors.toList()), uploadConfig);
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
