package pl.dostrzegaj.soft.flicloader;

import java.io.File;
import java.util.Objects;

class PhotoFile {

    private final File file;
    private RelativePath relativePath;

    public PhotoFile(File root, File file) {
        this.file = file;
        this.relativePath = new RelativePath(root, file);
    }

    public File getFile() {
        return file;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhotoFile photoFile = (PhotoFile) o;
        return Objects.equals(file, photoFile.file) &&
                Objects.equals(relativePath, photoFile.relativePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, relativePath);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PhotoFile{");
        sb.append("file=").append(file);
        sb.append('}');
        return sb.toString();
    }

    public RelativePath getRelativePath() {
        return relativePath;
    }
}
