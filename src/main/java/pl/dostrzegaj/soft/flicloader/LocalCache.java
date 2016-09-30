package pl.dostrzegaj.soft.flicloader;

import java.io.File;
import java.sql.*;
import java.util.List;
import java.util.Optional;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.logging.LogLevel;
import liquibase.resource.ClassLoaderResourceAccessor;

import com.google.common.base.Throwables;

class LocalCache {

    private Connection c;
    private PreparedStatement selectFolderStatement;
    private PreparedStatement insertFolderStatement;

    public LocalCache(final File dir) {
        initConnection(dir);
        applyLiquibase();
        prepareStatements();
    }

    private void prepareStatements() {
        try {
            selectFolderStatement =
                c.prepareStatement("SELECT photofolder_id,photofolder_absolute_path FROM photofolder WHERE photofolder_absolute_path=?");
            insertFolderStatement = c.prepareStatement("INSERT INTO photofolder VALUES (?,?)");
        } catch (final SQLException e) {
            Throwables.propagate(e);
        }
    }

    private void applyLiquibase() {
        try {
            LogFactory.getInstance().setDefaultLoggingLevel(LogLevel.WARNING);
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c));
            Liquibase liquibase = new liquibase.Liquibase("dbchangelog.xml", new ClassLoaderResourceAccessor(), database);
            liquibase.update(new Contexts(), new LabelExpression());
        } catch (final LiquibaseException e) {
            Throwables.propagate(e);
        }
    }

    void initConnection(File dir) {
        try {
            Class.forName("org.sqlite.JDBC");
            c =
                DriverManager.getConnection("jdbc:sqlite:" + dir.getAbsoluteFile() + File.separator + Main.PROJECT_NAME
                    + ".db");
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }

    public Optional<PhotoFolder> getPhotoFolder(PhotoFolderDir folder) {
        try {
            selectFolderStatement.setString(1, folder.getDir().getAbsolutePath());
            ResultSet resultSet = selectFolderStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(new PhotoFolder(resultSet.getString("photofolder_id"), resultSet
                    .getString("photofolder_absolute_path")));
            }
        } catch (final SQLException e) {
            Throwables.propagate(e);
        }
        return Optional.empty();
    }

    public void storePhotoFolder(PhotoFolder folder) {
        try {
            insertFolderStatement.setString(1, folder.getId());
            insertFolderStatement.setString(2, folder.getAbsolutePath());
            insertFolderStatement.executeUpdate();
        } catch (final SQLException e) {
            Throwables.propagate(e);
        }
    }

    public List<PhotoFile> getNonExistingPhotos(List<PhotoFile> photos, PhotoFolder folder) {
        return null;
    }

    public void storeUploadedFiles(List<UploadedPhoto> photos, PhotoFolder folder) {
    }
}
