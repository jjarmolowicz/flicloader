package pl.dostrzegaj.soft.flicloader;

import java.io.File;

class RelativePath {
    public final String path;

    public RelativePath(final File root, final File file) {
        path = root.toURI().relativize(file.toURI()).getPath();
    }

    public String getPath() {
        return path;
    }
}
