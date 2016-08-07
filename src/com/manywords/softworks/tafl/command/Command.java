package com.manywords.softworks.tafl.command;

/**
 * Created by jay on 2/15/16.
 */
public abstract class Command {
    public Command(Type type) {
        mType = type;
    }

    protected Type mType;
    protected String mError = "";

    public Type getType() { return mType; }
    public String getError() {
        return mError;
    }

    public enum Type {
        NONE, // No or unknown command type
        SENT, // The command just issued
        MOVE,
        INFO,
        SHOW,
        HISTORY,
        HELP,
        RULES,
        SAVE,
        QUIT,
        ANALYZE,
        REPLAY_ENTER,
        REPLAY_PLAY_HERE,
        REPLAY_RETURN,
        REPLAY_NEXT,
        REPLAY_PREVIOUS,
        REPLAY_JUMP,
        VARIATION,
        DELETE,
        ANNOTATE,
        CHAT,
    }
}
