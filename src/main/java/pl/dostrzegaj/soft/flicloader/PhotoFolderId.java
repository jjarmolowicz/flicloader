package pl.dostrzegaj.soft.flicloader;

import java.util.Objects;

public class PhotoFolderId {
    private String id;

    public PhotoFolderId(final String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhotoFolderId that = (PhotoFolderId) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
