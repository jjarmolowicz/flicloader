package pl.dostrzegaj.soft.flicloader;

import java.util.Objects;

class PhotoFolder {
    private final String id;
    private final String absolutePath;

    public PhotoFolder(String id, String absolutePath) {

        this.id = id;
        this.absolutePath = absolutePath;
    }

    public String getId() {
        return id;
    }
    public String getAbsolutePath() {
        return absolutePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhotoFolder that = (PhotoFolder) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(absolutePath, that.absolutePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, absolutePath);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PhotoFolder{");
        sb.append("id='").append(id).append('\'');
        sb.append(", absolutePath='").append(absolutePath).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
