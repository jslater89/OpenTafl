package com.manywords.softworks.tafl.engine;

import com.manywords.softworks.tafl.Log;

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
            Log.println(Log.Level.NORMAL, "Unable to get text data from the clipboard");
        }
        catch (IOException e) {
            Log.println(Log.Level.NORMAL, "Unable to get text data from the clipboard");
        }
        return "";
    }

    public static void printArray(short[] array) {
        Log.print(Log.Level.VERBOSE, "[");

        if(array.length > 0) {
            Log.print(Log.Level.VERBOSE, array[0]);
        }

        for(int i = 1; i < array.length; i++) {
            Log.print(Log.Level.VERBOSE, ",");
            Log.print(Log.Level.VERBOSE, array[i]);
        }

        Log.println(Log.Level.VERBOSE, "]");
    }

    public static void printArray(int[] array) {
        Log.print(Log.Level.VERBOSE, "[");

        if(array.length > 0) {
            Log.print(Log.Level.VERBOSE, array[0]);
        }

        for(int i = 1; i < array.length; i++) {
            Log.print(Log.Level.VERBOSE, ",");
            Log.print(Log.Level.VERBOSE, array[i]);
        }

        Log.println(Log.Level.VERBOSE, "]");
    }

    public static void printArray(Object[] array) {
        Log.print(Log.Level.VERBOSE, "[");

        if(array.length > 0) {
            Log.print(Log.Level.VERBOSE, array[0]);
        }

        for(int i = 1; i < array.length; i++) {
            Log.print(Log.Level.VERBOSE, ",");
            Log.print(Log.Level.VERBOSE, array[i]);
        }

        Log.println(Log.Level.VERBOSE, "]");
    }
}
