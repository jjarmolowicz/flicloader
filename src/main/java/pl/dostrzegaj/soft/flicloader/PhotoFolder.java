package pl.dostrzegaj.soft.flicloader;

import java.util.Objects;

class PhotoFolder {
    private final String id;
    private final String relativePath;

    public PhotoFolder(String id, RelativePath path) {

        this.id = id;
        this.relativePath = path.getPath();
    }

    public String getId() {
        return id;
    }
    public String getRelativePath() {
        return relativePath;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhotoFolder that = (PhotoFolder) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(relativePath, that.relativePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, relativePath);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PhotoFolder{");
        sb.append("id='").append(id).append('\'');
        sb.append(", relativePath='").append(relativePath).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
