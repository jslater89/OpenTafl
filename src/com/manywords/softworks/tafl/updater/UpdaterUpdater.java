package com.manywords.softworks.tafl.updater;

import java.io.File;

public class UpdaterUpdater {
    public static final String VERSION = "v0.0.0.1a";
    public static void main(String... args) {
        // Write out current version to version file
        File f = new File(".updater");
        f.mkdirs();

        if(!f.exists()) {
            // handle in a bad way
        }

        File versionFile = new File(f, ".usquared.txt");


        // Check updater version from manywords

        String latestVersion = "";

        // Check local updater version

        // Download new updater version

        // Unpack new updater version

        // Start updater
    }
}
