package com.manywords.softworks.tafl.command.player.external.engine;

import com.manywords.softworks.tafl.Log;
import org.ini4j.Wini;

import java.io.File;
import java.io.IOException;

/**
 * Created by jay on 4/30/16.
 */
public class EngineSpec {
    public final File specFile;
    public final File directory;
    public final File file;
    public final String command;
    public final String[] arguments;
    public final String name;

    public EngineSpec(File iniFile) {
        try {
            if(!iniFile.exists() || !EngineSpec.validateEngineFile(iniFile)) {
                throw new IllegalArgumentException("Missing engine file for player:" + iniFile);
            }
            Wini ini = new Wini(iniFile);

            String dir = ini.get("engine", "directory", String.class);
            String filename = ini.get("engine", "filename", String.class);
            String command = ini.get("engine", "command", String.class);
            String args = ini.get("engine", "arguments", String.class);
            String name = ini.get("engine", "name", String.class);

            this.specFile = iniFile;
            this.directory = new File("engines", dir);
            this.file = new File(filename);
            this.command = command;
            this.arguments = args.split(" ");
            this.name = name;
        } catch (IOException e) {
            throw new IllegalStateException("Attempted to load unvalidated engine ini file");
        }
    }

    public String toString() {
        if(name == null || name.equals("")) {
            return specFile.getName();
        }
        else {
            return name;
        }
    }

    public static boolean validateEngineFile(File iniFile) {
        try {
            Wini ini = new Wini(iniFile);
            String dir = ini.get("engine", "directory", String.class);
            String filename = ini.get("engine", "filename", String.class);
            String command = ini.get("engine", "command", String.class);
            String args = ini.get("engine", "arguments", String.class);

            if(dir == null || dir.equals("") || command == null || command.equals("") || filename == null || filename.equals("")) {
                Log.println(Log.Level.NORMAL, "Missing elements");
                return false;
            }

            File engineFileDir = new File("engines");
            File engineDir = new File(engineFileDir, dir);
            File engineFile = new File(engineDir, filename);
            if(!engineFile.exists()) {
                Log.println(Log.Level.NORMAL, "File does not exist: " + engineFile);
                Log.println(Log.Level.NORMAL, engineFile.getAbsolutePath());
                return false;
            }

            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
