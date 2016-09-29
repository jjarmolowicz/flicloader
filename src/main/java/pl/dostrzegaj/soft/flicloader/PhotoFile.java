package pl.dostrzegaj.soft.flicloader;

import java.io.File;

class PhotoFile {

    private final File file;

    public PhotoFile(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }
}
