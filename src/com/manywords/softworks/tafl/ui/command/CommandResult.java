package com.manywords.softworks.tafl.ui.command;

/**
 * Created by jay on 2/15/16.
 */
public class CommandResult {
    public static final int SUCCESS = 1;
    public static final int FAIL = 0;

    public enum Type {
        NONE,
        MOVE,
    }

    public final Type type;
    public final int result;
    public final String message;
    public final Object extra;

    public CommandResult(Type t, int res, String mes, Object ex) {
        result = res;
        message = mes;
        type = t;
        extra = ex;
    }
}
