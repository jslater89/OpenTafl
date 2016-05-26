package com.manywords.softworks.tafl.network.packet;

/**
 * Created by jay on 5/23/16.
 */
public class ErrorPacket extends NetworkPacket {
    public static final String LOGIN_FAILED = "invalid-login";
    public static final String ALREADY_HOSTING = "already-hosting";
    public static final String GAME_CANCELED = "game-canceled";

    public final String error;

    public static ErrorPacket parse(String data) {
        return new ErrorPacket(data.replace("error ", ""));
    }

    public ErrorPacket(String error) {
        this.error = error;
    }

    public String toString() {
        return "error " + error;
    }
}
