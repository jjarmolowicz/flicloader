package pl.dostrzegaj.soft.flicloader;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
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
import com.google.common.collect.Lists;

class LocalCache  implements Closeable{

    private Connection c;
    private PreparedStatement selectFolderStatement;
    private PreparedStatement insertFolderStatement;
    private PreparedStatement selectPhotosStatement;
    private PreparedStatement insertPhotosStatement;

    public LocalCache(final File dir) {
        initConnection(dir);
        applyLiquibase();
        prepareStatements();
    }



    private void prepareStatements() {
        try {
            selectFolderStatement =
                c.prepareStatement("SELECT photofolder_id FROM photofolder WHERE photofolder_relative_path=?");
            insertFolderStatement = c.prepareStatement("INSERT INTO photofolder VALUES (?,?)");
            selectPhotosStatement =
                c.prepareStatement("SELECT photofile_relative_path FROM photofile WHERE photofolder_id = ?");
            insertPhotosStatement = c.prepareStatement("INSERT INTO photofile VALUES (?,?,?)");
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
            c.setAutoCommit(true);
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }

    public Optional<PhotoFolderId> getPhotoFolder(PhotoFolderDir folder) {
        try {
            selectFolderStatement.setString(1, folder.getRelativePath().getPath());
            ResultSet resultSet = selectFolderStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(new PhotoFolderId(resultSet.getString("photofolder_id")));
            }
        } catch (final SQLException e) {
            Throwables.propagate(e);
        }
        return Optional.empty();
    }

    public void storePhotoFolder(PhotoFolder folder) {
        try {
            insertFolderStatement.setString(1, folder.getId());
            insertFolderStatement.setString(2, folder.getRelativePath());
            insertFolderStatement.executeUpdate();
            c.commit();
        } catch (final SQLException e) {
            Throwables.propagate(e);
        }
    }

    public List<PhotoFile> getNonExistingPhotos(List<PhotoFile> photos, PhotoFolderId folder) {
        List<PhotoFile> result = Lists.newArrayList(photos);
        try {
            selectPhotosStatement.setString(1, folder.getId());
            ResultSet resultSet = selectPhotosStatement.executeQuery();
            while (resultSet.next()) {
                String existingPath = resultSet.getString(1);
                for (int i=0;i<result.size();i++) {
                    if (result.get(i).getRelativePath().getPath().equals(existingPath)) {
                        result.remove(i);
                    }
                }
            }
        } catch (final SQLException e) {
            Throwables.propagate(e);
        }
        return result;
    }

    public void storeUploadedFile(
        UploadedPhoto photo,
        PhotoFolderId folder) {
        try {
            insertPhotosStatement.clearParameters();
            insertPhotosStatement.clearBatch();
            insertPhotosStatement.setString(1, photo.getId());
            insertPhotosStatement.setString(2, photo.getRelativePath());
            insertPhotosStatement.setString(3, folder.getId());
            insertPhotosStatement.executeUpdate();
            c.commit();
        } catch (final SQLException e) {
            Throwables.propagate(e);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            c.close();
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }
}
