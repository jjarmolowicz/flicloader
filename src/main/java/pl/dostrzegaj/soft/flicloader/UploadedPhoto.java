package pl.dostrzegaj.soft.flicloader;

class UploadedPhoto {
    private String id;
    private String absoultePath;

    public UploadedPhoto(final String id, final String absoultePath) {

        this.id = id;
        this.absoultePath = absoultePath;
    }

    public String getId() {
        return id;
    }

    public String getAbsoultePath() {
        return absoultePath;
    }
}
