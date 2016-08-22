package com.manywords.softworks.tafl;

import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.swing.*;
import com.manywords.softworks.tafl.network.client.HeadlessAIClient;
import com.manywords.softworks.tafl.network.server.NetworkServer;
import com.manywords.softworks.tafl.rules.BuiltInVariants;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.test.Benchmark;
import com.manywords.softworks.tafl.test.Test;
import com.manywords.softworks.tafl.ui.AdvancedTerminal;
import com.manywords.softworks.tafl.ui.SwingWindow;
import com.manywords.softworks.tafl.command.player.external.engine.ExternalEngineClient;
import com.manywords.softworks.tafl.ui.lanterna.settings.TerminalSettings;

import java.awt.*;
import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.manywords.softworks.tafl.OpenTafl.LogLevel.SILENT;

public class OpenTafl {
    private static enum Mode {
        WINDOW,
        GRAPHICAL_TERMINAL,
        DEBUG,
        TEST,
        EXTERNAL_ENGINE,
        FALLBACK,
        SERVER,
        HEADLESS_AI,
        BENCHMARK,
        HELP
    }

    public static enum LogLevel {
        CHATTY,
        NORMAL,
        SILENT
    }

    public static final String CURRENT_VERSION = "v0.4.3.3pre";
    public static final int NETWORK_PROTOCOL_VERSION = 7;

    public static boolean devMode = false;
    public static LogLevel logLevel = LogLevel.NORMAL;
    private static final int LOG_BUFFER_SIZE = 1024 * 32;
    private static final StringBuilder logBuffer = new StringBuilder(LOG_BUFFER_SIZE);
    private static File logFile;
    private static Mode runMode = Mode.GRAPHICAL_TERMINAL;

    public static void main(String[] args) {
        Map<String, String> mapArgs = getArgs(args);

        //System.out.println(mapArgs);

        for (String arg : args) {
            if (arg.contains("--server") && runMode == Mode.GRAPHICAL_TERMINAL) {
                runMode = Mode.SERVER;
            }
            else if (arg.contains("--engine") && runMode == Mode.GRAPHICAL_TERMINAL) {
                runMode = Mode.EXTERNAL_ENGINE;
            }
            else if (arg.contains("--test") && runMode == Mode.GRAPHICAL_TERMINAL) {
                runMode = Mode.TEST;
                logLevel = SILENT;
            }
            else if (arg.contains("--window") && runMode == Mode.GRAPHICAL_TERMINAL) {
                runMode = Mode.WINDOW;
            }
            else if(arg.contains("--fallback") && runMode == Mode.GRAPHICAL_TERMINAL) {
                runMode = Mode.FALLBACK;
            }
            else if(arg.contains("--headless") && runMode == Mode.GRAPHICAL_TERMINAL) {
                runMode = Mode.HEADLESS_AI;
            }
            else if(arg.contains("--benchmark") && runMode == Mode.GRAPHICAL_TERMINAL) {
                logLevel = SILENT;
                runMode = Mode.BENCHMARK;
            }
            else if(arg.contains("--help")) {
                runMode = Mode.HELP;
            }
            else if(arg.contains("--dev") || arg.contains("--debug")) {
                logLevel = LogLevel.CHATTY;
                devMode = true;
            }
            else if (arg.contains("--chatty")) {
                logLevel = LogLevel.CHATTY;
            }
            else if (arg.contains("--normal")) {
                logLevel = LogLevel.NORMAL;
            }
            else if (arg.contains("--silent")) {
                logLevel = SILENT;
            }
        }

        directoryCheck();
        setupLogging();
        Coord.initialize();
        BuiltInVariants.loadExternalRules(new File("external-rules.conf"));
        TerminalSettings.loadFromFile();

        switch(runMode) {
            case SERVER:
                // Blocks here
                int threads = 4;
                if(mapArgs.containsKey("threads")) {
                    threads = Integer.parseInt(mapArgs.get("threads"));
                }
                NetworkServer ns = new NetworkServer(threads);
                ns.start();
                break;
            case HEADLESS_AI:
                try {
                    HeadlessAIClient client = HeadlessAIClient.startFromArgs(mapArgs);
                }
                catch(Exception e) {
                    OpenTafl.logPrintln(SILENT, "Failed to start headless AI client with error: " + e);
                    OpenTafl.logStackTrace(SILENT, e);
                }
                break;
            case WINDOW:
                SwingWindow w = new SwingWindow();
                break;
            case GRAPHICAL_TERMINAL:
                DefaultTerminalFactory factory = new DefaultTerminalFactory();

                Font[] preferredFonts = new Font[5];
                preferredFonts[0] = new Font("Monospace", Font.PLAIN, TerminalSettings.fontSize);
                preferredFonts[1] = new Font("Monaco", Font.PLAIN, TerminalSettings.fontSize);
                preferredFonts[2] = new Font("Courier New", Font.PLAIN, TerminalSettings.fontSize);
                preferredFonts[3] = new Font("Courier", Font.PLAIN, TerminalSettings.fontSize);
                preferredFonts[4] = new Font("Courier New", Font.PLAIN, TerminalSettings.fontSize);

                preferredFonts = AWTTerminalFontConfiguration.filterMonospaced(preferredFonts);
                if(preferredFonts.length == 0) throw new IllegalStateException("No monospaced fonts available");

                SwingTerminalFontConfiguration font = SwingTerminalFontConfiguration.newInstance(preferredFonts);

                factory.setTerminalEmulatorFontConfiguration(font);

                Terminal t = factory.createSwingTerminal();
                AdvancedTerminal<? extends Terminal> th = new AdvancedTerminal<>(t);
                break;
            case DEBUG:
                Debug.run(mapArgs);
                break;
            case TEST:
                Test.run();
                break;
            case EXTERNAL_ENGINE:
                ExternalEngineClient.run();
                break;
            case FALLBACK:
                factory = new DefaultTerminalFactory();
                factory.setForceTextTerminal(true);
                try {
                    t = factory.createTerminal();
                    th = new AdvancedTerminal<>(t);
                } catch (IOException e) {
                    OpenTafl.logPrintln(SILENT, "Failed to start text terminal.");
                }

                //RawTerminal display = new RawTerminal();
                //display.runUi();
                break;
            case BENCHMARK:
                new Benchmark().run();
                break;
            case HELP:
                printHelpMessage();
                break;
        }
    }

    private static Map<String, String> getArgs(String[] args) {
        Map<String, String> mapArgs = new HashMap<String, String>();

        String argName = "";
        String argBody = "";
        for (String arg : args) {
            if (arg.startsWith("--")) {
                if (!argName.isEmpty()) {
                    mapArgs.put(argName, argBody);
                }

                argName = arg;
                argBody = "";
            }
            else {
                if (argBody.isEmpty()) {
                    argBody = arg;
                }
                else {
                    argBody += " " + arg;
                }
            }
        }

        if(!argName.isEmpty()) {
            mapArgs.put(argName, argBody);
        }

        return mapArgs;
    }

    public static void logPrintln(LogLevel messageLevel, Object o) {
        synchronized (logBuffer) {
            internalLogPrint(messageLevel, o.toString());
            internalLogPrint(messageLevel, "\n");
        }
    }

    public static void logPrint(LogLevel messageLevel, Object o) {
        synchronized (logBuffer) {
            internalLogPrint(messageLevel, o.toString());
        }
    }

    private static void internalLogPrint(LogLevel messageLevel, String s) {
        synchronized (logBuffer) {
            if (logLevel == LogLevel.CHATTY) {
                // Incidental messages
                System.out.print(s);
                logBuffer.append(s);
            } else if (logLevel == LogLevel.NORMAL && messageLevel != LogLevel.CHATTY) {
                // Normal messages
                System.out.print(s);
                logBuffer.append(s);
            } else if (messageLevel == SILENT) {
                // Critical errors which should always be displayed
                System.out.print(s);
                logBuffer.append(s);
            }


            if (logBuffer.length() > (LOG_BUFFER_SIZE - 1024)) {
                flushLog();
            }
        }
    }

    public static void logStackTrace(LogLevel messageLevel, Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        logPrintln(messageLevel, sw.toString());
    }

    private static void flushLog() {
            new Thread("LogSaveThread") {
                @Override
                public void run() {
                    synchronized (logBuffer) {
                        unsafeFlushLog();
                    }
                }
            }.start();
    }

    private static void unsafeFlushLog() {
        if(logFile.exists() && logFile.canWrite()) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(logFile))) {
                bw.append(logBuffer.toString());
                bw.flush();
                logBuffer.delete(0, logBuffer.length());
            } catch (IOException e) {
                logPrintln(LogLevel.NORMAL, "Failed to write to log file: " + e);
            }
        }
    }

    private static void directoryCheck() {
        if(!new File("saved-games/replays").exists()) {
            new File("saved-games/replays").mkdirs();
            new File("saved-games/headless-ai").mkdirs();
        }

        if(!new File("saved-games/headless-ai").exists()) {
            new File("saved-games/headless-ai").mkdirs();
        }

        if(!new File("engines").exists()) {
            new File("engines").mkdirs();
        }

        if(!new File("log").exists()) {
            new File("log").mkdirs();
        }
    }

    private static void setupLogging() {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            logPrintln(SILENT, "Uncaught exception! This OpenTafl is running as " + runMode);
            logPrintln(SILENT, "Exception in thread " + t.getName());
            logPrintln(SILENT, "Exception: " + e);
            logStackTrace(SILENT, e);
            flushLog();

            System.exit(1);
        });


        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                unsafeFlushLog();
            }
        });

        logFile = new File("log", "lastrun.log");
        if(logFile.exists()) { logFile.delete(); }

        try {
            logFile.createNewFile();
            OpenTafl.logPrintln(SILENT, "OpenTafl log from " + new Date() + " on " + getComputerName());
            OpenTafl.logPrintln(SILENT, "Java version: " + System.getProperty("java.version", "unknown version"));
        } catch (IOException e) {
            logPrintln(LogLevel.NORMAL, "Failed to create log file:" + e);
        }
    }

    private static String getComputerName() {
        Map<String, String> env = System.getenv();
        if (env.containsKey("COMPUTERNAME"))
            return env.get("COMPUTERNAME");
        else if (env.containsKey("HOSTNAME"))
            return env.get("HOSTNAME");
        else if (new File ("/etc/hostname").exists()) {
            String hostname = null;
            try (BufferedReader br = new BufferedReader(new FileReader(new File("/etc/hostname")))) {
                hostname = br.readLine();

                if(hostname != null && !hostname.isEmpty()) {
                    return hostname;
                }
                else {
                    return "Unknown Computer";
                }
            } catch (Exception e) {
                return "Unknown Computer";
            }
        }
        else
            return "Unknown Computer";
    }

    private static void printHelpMessage() {
        System.out.println("OPENTAFL COMMAND USAGE");
        System.out.println("OpenTafl runs in several modes, each with a selection of options:");
        System.out.println();
        System.out.println("<no flags>: standard UI");
        System.out.println("--server: network server mode (runs on port 11541)");
        System.out.println("\t--threads [#]: number of worker threads to spawn");
        System.out.println("--engine: run as external engine");
        System.out.println("--test: run the built-in tests");
        System.out.println("--headless: run as a headless AI client");
        System.out.println("\t--server [address]: server address");
        System.out.println("\t--username [name]: server login username");
        System.out.println("\t--password [pw]: server login password");
        System.out.println("\t--engine [path]: path to the external engine config file to use");
        System.out.println("\t--create: run in game host mode");
        System.out.println("\t\t--rules [#]: rules variant to host (use 1-[number-of-variants] from in-game variant selector)");
        System.out.println("\t\t--clock [maintime-millis]+[overtime-millis]/[overtime-count]+[increment-millis]: set game clock");
        System.out.println("\t\t--side [attackers|defenders]: set the side to play");
        System.out.println("\t\t--game-password [pw]: password for created games");
        System.out.println("\t--join: run in join game mode");
        System.out.println("\t\t--opponent [username]: join game against opponent username");
        System.out.println("\t\t--game-password [pw]: password for game to join");
        System.out.println();
        System.out.println("The following flags apply to all modes: ");
        System.out.println();
        System.out.println("--chatty: print extra debug messages");
        System.out.println("--silent: print no messages");
    }
}
