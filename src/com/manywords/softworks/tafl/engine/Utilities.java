package com.manywords.softworks.tafl.engine;

import com.manywords.softworks.tafl.OpenTafl;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * Created by jay on 8/16/16.
 */
public class Utilities {
    public static void printArray(Object[] array) {
        OpenTafl.logPrint(OpenTafl.LogLevel.CHATTY, "[");
        for(Object o : array) {
            OpenTafl.logPrint(OpenTafl.LogLevel.CHATTY, o + ",");
        }
        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "]");
    }

    public static void printArray(int[] array) {
        OpenTafl.logPrint(OpenTafl.LogLevel.CHATTY, "[");
        for(int o : array) {
            OpenTafl.logPrint(OpenTafl.LogLevel.CHATTY, o + ",");
        }
        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "]");
    }

    public static void printArray(short[] array) {
        OpenTafl.logPrint(OpenTafl.LogLevel.CHATTY, "[");
        for(short o : array) {
            OpenTafl.logPrint(OpenTafl.LogLevel.CHATTY, o + ",");
        }
        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "]");
    }

    public static void fillArray(byte[] array, byte value) {
        int len = array.length;

        if (len > 0) {
            array[0] = value;
        }

        for (int i = 1; i < len; i += i) {
            System.arraycopy(array, 0, array, i, ((len - i) < i) ? (len - i) : i);
        }
    }

    public static void fillArray(char[] array, char value) {
        int len = array.length;

        if (len > 0) {
            array[0] = value;
        }

        for (int i = 1; i < len; i += i) {
            System.arraycopy(array, 0, array, i, ((len - i) < i) ? (len - i) : i);
        }
    }

    public static void fillArray(int[] array, int value) {
        int len = array.length;

        if (len > 0) {
            array[0] = value;
        }

        for (int i = 1; i < len; i += i) {
            System.arraycopy(array, 0, array, i, ((len - i) < i) ? (len - i) : i);
        }
    }

    public static void fillArray(long[] array, long value) {
        int len = array.length;

        if (len > 0) {
            array[0] = value;
        }

        for (int i = 1; i < len; i += i) {
            System.arraycopy(array, 0, array, i, ((len - i) < i) ? (len - i) : i);
        }
    }

    public static void fillArray(boolean[] array, boolean value) {
        int len = array.length;

        if (len > 0) {
            array[0] = value;
        }

        for (int i = 1; i < len; i += i) {
            System.arraycopy(array, 0, array, i, ((len - i) < i) ? (len - i) : i);
        }
    }

    public static void pushToClipboard(String string) {
        StringSelection s = new StringSelection(string);
        Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
        c.setContents(s, s);
    }

    public static String getFromClipboard() {
        Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            String s = (String) c.getData(DataFlavor.stringFlavor);
            return s;
        }
        catch (UnsupportedFlavorException e) {
            OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "Unable to get text data from the clipboard");
        }
        catch (IOException e) {
            OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "Unable to get text data from the clipboard");
        }
        return "";
    }
}
