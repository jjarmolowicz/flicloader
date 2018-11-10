package pl.dostrzegaj.soft.flicloader;

import java.io.*;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    public static final String PROJECT_NAME = "flicloader";

    public static void main(String[] args) throws IOException, InterruptedException {
        Properties properties = verifyInputAndProduceProperties(args);
        AuthEnforcer authEnforcer = new AuthEnforcer(properties, new AuthEnforcer.AuthWrapperFactory() {

            @Override
            public AuthWrapper factory(String apiKey, String secret) {
                return new FlickrAuthImpl(apiKey, secret);
            }
        });
        UserAccount userAccount = authEnforcer.enforceAuthentication();

        UploadConfig uploadConfig = new UploadConfig(properties);
        LOGGER.debug("upload config: {}", uploadConfig);

        LOGGER.info("Checking sync state of Your folders");
        List<File> dirsToBeSynced = new DirsToBeSyncedProvider(properties).extractAndVerifyCorrectness();
        for (File dir : dirsToBeSynced) {
            try (LocalCache localCache = new LocalCache(dir)) {
                LazySender sender = new LazySender(dir, localCache, userAccount);
                for (PhotoFolderInfo i : new PhotoFoldersIterable(dir, uploadConfig)) {
                    int tries = 1;
                    try {
                        sender.sendIfNeeded(i);
                    }catch (RuntimeException e) {
                        if (tries++ > 3) {
                            throw e;
                        }
                        LOGGER.debug("sender.sendIfNeeded threw",e);
                        LOGGER.warn("Got exception while processing folder {}. Will wait and retry. Details can be found in detailed upload log", i.getFolder().getDir());
                        TimeUnit.MINUTES.sleep(1);
                    }
                }
            }
        }
        LOGGER.info("Processing complete. Process will now finish");

    }

    static Properties verifyInputAndProduceProperties(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException(
                    "One argument is required. You must point to properties file containing basic key/token information");
        }
        File file = new File(args[0]);
        if (!file.exists()) {
            throw new IllegalArgumentException(MessageFormatter.format("Provided file {} does not exist!", file.getPath())
                    .getMessage());
        }
        Properties properties = new Properties();
        try (InputStream stream = new BufferedInputStream(new FileInputStream(file))) {
            properties.load(stream);
        }
        return properties;
    }
}
