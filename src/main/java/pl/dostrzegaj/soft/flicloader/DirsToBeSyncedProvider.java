package pl.dostrzegaj.soft.flicloader;

import java.io.File;
import java.util.List;
import java.util.Properties;

import com.google.common.collect.Lists;

class DirsToBeSyncedProvider {

    static final String DIRS_KEY = "commaSeparatedDirsToScan";

    private Properties properties;

    public DirsToBeSyncedProvider(Properties properties) {
        this.properties = properties;
    }

    public List<File> extractAndVerifyCorrectness() {
        String inputString = properties.getProperty(DIRS_KEY);
        if (inputString == null || inputString.isEmpty()) {
            throw new IllegalArgumentException("Please add folders to be synchronised into properties file (" + DIRS_KEY
                + "=...)");
        }
        String[] dirs = inputString.split(",");

        List<String> errors = Lists.newArrayListWithCapacity(dirs.length);
        List<File> result = Lists.newArrayListWithCapacity(dirs.length);

        allDirs: for (String dir : dirs) {
            File newDir = new File(dir);
            if (!newDir.exists()) {
                errors.add(dir + " does not exist!");
                continue;
            }
            if (!newDir.isDirectory()) {
                errors.add(dir + " is not a directory!");
                continue;
            }
            for (File other : result) {
                File parent = other;
                while (parent != null) {
                    if (parent.equals(newDir)) {
                        errors.add(other + " and a " + newDir + " are in parent/ancestor relation. This is forbidden");
                        continue allDirs;
                    }
                    parent = parent.getParentFile();
                }
            }

            result.add(newDir);
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("During directory validation following errors occurred: "
                + System.lineSeparator() + errors.toString());
        }

        return result;
    }
}
