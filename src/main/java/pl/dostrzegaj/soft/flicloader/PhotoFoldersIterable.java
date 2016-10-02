package pl.dostrzegaj.soft.flicloader;

import java.io.File;
import java.io.FileFilter;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

class PhotoFoldersIterable implements Iterable<PhotoFolderInfo> {

    private static FileFilter filter = pathname -> {
        String s = pathname.getName().toLowerCase();
        return (!s.startsWith(".")) &&  (pathname.isDirectory() || s.endsWith(".jpg") || s.endsWith("mov"));
    };
    private File root;
    private UploadConfig uploadConfig;

    public PhotoFoldersIterable(File root, UploadConfig uploadConfig) {
        this.root = root;
        this.uploadConfig = uploadConfig;
    }

    @Override
    public Iterator<PhotoFolderInfo> iterator() {
        return new Iterator<PhotoFolderInfo>() {

            private final List<File> dirs;
            private PhotoFolderInfo nextElement;
            {
                dirs = Lists.newArrayList();
                dirs.add(root);
            }

            @Override
            public boolean hasNext() {
                while (!dirs.isEmpty()) {
                    List<File> files = Lists.newArrayList();
                    File dir = dirs.remove(0);
                    List<File> subdirs = Lists.newArrayList();
                    for (File file : dir.listFiles(filter)) {
                        if (file.isDirectory()) {
                            subdirs.add( file);
                        } else {
                            files.add(file);
                        }
                    }
                    dirs.addAll(0,subdirs);
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
