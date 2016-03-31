package com.manywords.softworks.tafl.ui.command;

import com.manywords.softworks.tafl.rules.Coord;

import java.util.List;

/**
 * Created by jay on 2/15/16.
 */
public class CommandResult {
    public static final int SUCCESS = 1;
    public static final int FAIL = 0;

    public enum Type {
        NONE, // No or unknown command type
        SENT, // The command just issued
        MOVE,
        INFO,
        SHOW,
        HISTORY,
        HELP,
        RULES,
        QUIT,
        ANALYZE,
        REPLAY_ENTER,
        REPLAY_PLAY_HERE,
        REPLAY_RETURN,
        REPLAY_NEXT,
        REPLAY_PREVIOUS,
        REPLAY_JUMP,
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
