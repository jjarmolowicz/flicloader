package pl.dostrzegaj.soft.flicloader;

class UploadedPhoto {
    private String id;
    private String relativePath;

    public UploadedPhoto(final String id, RelativePath path) {

        this.id = id;
        this.relativePath = path.getPath();
    }

    public String getId() {
        return id;
    }

    public String getRelativePath() {
        return relativePath;
    }
}
