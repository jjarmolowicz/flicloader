package pl.dostrzegaj.soft.flicloader;

import java.io.File;

class PhotoFolderDir {

    private final File dir;
    private RelativePath relativePath;

    public PhotoFolderDir(File root,File dir) {
        this.dir = dir;
        this.relativePath = new RelativePath(root,dir);
    }

    public File getDir() {
        return dir;
    }

    public RelativePath getRelativePath() {
        return relativePath;
    }
}
