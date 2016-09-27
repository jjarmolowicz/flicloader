package pl.dostrzegaj.soft.flicloader;

import com.google.common.collect.Lists;

import java.io.File;
import java.util.Iterator;
import java.util.List;

class PhotoFoldersIterator implements  Iterable<PhotoFolderInfo> {
    private File root;
    private List<PhotoFolderInfo> result;

    public PhotoFoldersIterator(File root) {
        this.root = root;
        result = Lists.newArrayList();
        walkOverDir(root);
    }

    private void walkOverDir(File dir) {
        List<File> files = Lists.newArrayList();
        boolean hasSubDirs = false;
        for (File file: dir.listFiles()){
            if (file.isDirectory()) {
                walkOverDir(file);
                hasSubDirs = true;
            }else {
                files.add(file);
            }
        }
        if (!files.isEmpty()) {
            result.add(new PhotoFolderInfo(dir,root,files,hasSubDirs));
        }
    }

    @Override
    public Iterator<PhotoFolderInfo> iterator() {
        return result.iterator();
    }
}
