package pl.dostrzegaj.soft.flicloader;

import java.io.*;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import pl.dostrzegaj.soft.flicloader.api.AuthWrapper;
import pl.dostrzegaj.soft.flicloader.api.UserWrapper;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        Properties properties = verifyInputAndProduceProperties(args);
        AuthEnforcer authEnforcer = new AuthEnforcer(properties, new AuthEnforcer.AuthWrapperFactory() {

            @Override
            public AuthWrapper factory(String apiKey, String secret) {
                return new FlickrAuthImpl(apiKey, secret);
            }
        });
        UserWrapper userWrapper = authEnforcer.enforceAuthentication();

        userWrapper.getPhotosetsAsList().getPhotosets().stream().forEach(i-> System.out.println( i.getTitle()));
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
