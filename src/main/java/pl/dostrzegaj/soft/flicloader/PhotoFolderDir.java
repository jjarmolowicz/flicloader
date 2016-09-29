package pl.dostrzegaj.soft.flicloader;

import java.io.File;

class PhotoFolderDir {

    private final File dir;

    public PhotoFolderDir(File dir) {
        this.dir = dir;
    }

    public File getDir() {
        return dir;
    }
}
