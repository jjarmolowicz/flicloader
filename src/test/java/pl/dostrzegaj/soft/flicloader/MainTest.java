package pl.dostrzegaj.soft.flicloader;

import java.io.*;
import java.util.Map;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Maps;

public class MainTest {

    @Test(expected = IllegalArgumentException.class)
    public void givenNoArgumentProvidedWhenCalledThenIllegalArgumentExeptionRaised() throws IOException {
        Main.verifyInputAndProduceProperties(new String[] {});
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenNonExistingFileAsArgumentWhenCalledThenIllegalArgumentExeptionRaised() throws IOException {
        Main.verifyInputAndProduceProperties(new String[] {"nonExistingfile"});
    }

    @Test()
    public void givenExistingFileAsArgumentWhenCalledThenPropertiesReturned() throws IOException {
        File tempFile = File.createTempFile("secrets", ".properties");
        Map<String, String> expected = Maps.newHashMap();
        expected.put("k", "v");
        expected.put("k2", "value");
        expected.put("k3", "value with space");
        try (PrintWriter writer = new PrintWriter(new BufferedOutputStream(new FileOutputStream(tempFile)))) {

            for (Map.Entry<String, String> kv : expected.entrySet()) {
                writer.println(kv.getKey() + "=" + kv.getValue());

            }
        }
        Properties properties = Main.verifyInputAndProduceProperties(new String[]{tempFile.getPath()});

        for (Map.Entry<String, String> kv : expected.entrySet()) {
            Assert.assertEquals(kv.getValue(), properties.getProperty(kv.getKey()));
        }
    }
}