package com.manywords.softworks.tafl.command;

/**
 * Created by jay on 2/15/16.
 */
public class CommandResult {
    public static final int SUCCESS = 1;
    public static final int FAIL = 0;

    public final Command.Type type;
    public final int result;
    public final String message;
    public final Object extra;

    public CommandResult(Command.Type t, int res, String mes, Object ex) {
        result = res;
        message = mes;
        type = t;
        extra = ex;
    }
}
