package com.manywords.softworks.tafl.notation;

/**
 * Created by jay on 10/25/16.
 */
public class NotationParseException extends Exception {
    public final String context;
    public final int index;
    public NotationParseException(int index, String context, String message) {
        super(message);
        this.index = index;
        this.context = context;
    }

    @Override
    public String toString() {
        return "NotationParseException(" + index + ")(" + context + "): " + getMessage();
    }
}
