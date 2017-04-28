package com.manywords.softworks.tafl;

import java.io.*;
import java.util.Date;
import java.util.Map;

import static com.manywords.softworks.tafl.Log.Level.SILENT;

/**
 * Created by jay on 4/28/17.
 */
public class Log {
    private static final int LOG_BUFFER_SIZE = 1024 * 32;
    private static final StringBuilder logBuffer = new StringBuilder(LOG_BUFFER_SIZE);
    public static Level level = Level.NORMAL;
    private static File logFile;

    static void setupFile() {
        Log.logFile = new File("log", "lastrun.log");
        if(Log.logFile.exists()) { Log.logFile.delete(); }

        try {
            Log.logFile.createNewFile();
            Log.println(SILENT, "OpenTafl " + OpenTafl.CURRENT_VERSION + " log from " + new Date() + " on " + getComputerName());
            Log.println(SILENT, "Java version: " + System.getProperty("java.version", "unknown version"));
        } catch (IOException e) {
            Log.println(Log.Level.NORMAL, "Failed to create log file:" + e);
        }
    }

    public static void println(Level messageLevel, Object o) {
        synchronized (logBuffer) {
            internalLogPrint(messageLevel, o.toString());
            internalLogPrint(messageLevel, "\n");
        }
    }

    public static void print(Level messageLevel, Object o) {
        synchronized (logBuffer) {
            internalLogPrint(messageLevel, o.toString());
        }
    }

    private static void internalLogPrint(Level messageLevel, String s) {
        synchronized (logBuffer) {
            if (level == Level.CHATTY) {
                // Incidental messages
                System.out.print(s);
                logBuffer.append(s);
            } else if (level == Level.NORMAL && messageLevel != Level.CHATTY) {
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

    public static void stackTrace(Level messageLevel, Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        println(messageLevel, sw.toString());
    }

    static void flushLog() {
            new Thread("LogSaveThread") {
                @Override
                public void run() {
                    synchronized (logBuffer) {
                        unsafeFlushLog();
                    }
                }
            }.start();
    }

    static void unsafeFlushLog() {
        if(logFile.exists() && logFile.canWrite()) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(logFile))) {
                bw.append(logBuffer.toString());
                bw.flush();
                logBuffer.delete(0, logBuffer.length());
            } catch (IOException e) {
                println(Level.NORMAL, "Failed to write to log file: " + e);
            }
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

    public static enum Level {
        CHATTY,
        NORMAL,
        SILENT
    }
}
