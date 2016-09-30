package pl.dostrzegaj.soft.flicloader;

import java.util.Properties;

class UploadConfig {

    private final boolean isPublic;
    private final boolean isFriend;
    private final boolean isFamily;

    public UploadConfig(final Properties properties) {
        isPublic = Boolean.parseBoolean(properties.getProperty("photoFlags.isPublic", "false"));
        isFriend = Boolean.parseBoolean(properties.getProperty("photoFlags.isFriend", "false"));
        isFamily = Boolean.parseBoolean(properties.getProperty("photoFlags.isFamily", "false"));
    }

    public boolean getIsPublic() {
        return isPublic;
    }

    public boolean getIsFriend() {
        return isFriend;
    }

    public boolean getIsFamily() {
        return isFamily;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UploadConfig{");
        sb.append("isPublic=").append(isPublic);
        sb.append(", isFriend=").append(isFriend);
        sb.append(", isFamily=").append(isFamily);
        sb.append('}');
        return sb.toString();
    }
}
