package pl.dostrzegaj.soft.flicloader;

import java.io.File;
import java.util.Objects;

class PhotoFile {

    private final File file;

    public PhotoFile(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhotoFile photoFile = (PhotoFile) o;
        return Objects.equals(file, photoFile.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PhotoFile{");
        sb.append("file=").append(file);
        sb.append('}');
        return sb.toString();
    }
}
