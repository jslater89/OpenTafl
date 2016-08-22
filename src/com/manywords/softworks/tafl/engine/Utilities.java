package com.manywords.softworks.tafl.engine;

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
}
