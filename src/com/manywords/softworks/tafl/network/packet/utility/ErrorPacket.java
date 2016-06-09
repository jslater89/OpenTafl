package com.manywords.softworks.tafl.network.packet.utility;

import com.manywords.softworks.tafl.network.packet.NetworkPacket;

/**
 * Created by jay on 5/23/16.
 */
public class ErrorPacket extends NetworkPacket {
    public static final String LOGIN_FAILED = "invalid-login";
    public static final String ALREADY_HOSTING = "already-hosting";
    public static final String LEAVE_GAME = "game-canceled";
    public static final String GAME_FULL = "game-full";
    public static final String INVALID_GAME_PASSWORD = "invalid-game-password";
    public static final String OPPONENT_LEFT = "opponent-left";
    public static final String DESYNC = "desync";
    public static final String VERSION_MISMATCH = "version-mismatch";
    public static final String UNKNOWN_ERROR = "unknown-error";

    public static final String PREFIX = "error";

    public final String error;

    public static ErrorPacket parse(String data) {
        return new ErrorPacket(data.replace("error ", ""));
    }

    public ErrorPacket(String error) {
        this.error = error;
    }

    public String toString() {
        return PREFIX + " " + error;
    }
}
