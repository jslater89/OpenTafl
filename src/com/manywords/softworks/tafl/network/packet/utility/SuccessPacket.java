package com.manywords.softworks.tafl.network.packet.utility;

import com.manywords.softworks.tafl.network.packet.NetworkPacket;

/**
 * Created by jay on 5/23/16.
 */
public class SuccessPacket extends NetworkPacket {
    public static final String JOINED_ATTACKERS = "attackers";
    public static final String JOINED_DEFENDERS = "defenders";
    public static final String JOINED_SPECTATOR = "spectator";
    public static final String LOGGED_IN = "logged-in";
    public static final String PREFIX = "success";

    public final String message;

    public static SuccessPacket parse(String data) {
        return new SuccessPacket(data.replaceFirst("success", "").trim());
    }

    public SuccessPacket(String message) {
        this.message = message;
    }
    @Override
    public String toString() {
        return PREFIX + " " + message;
    }
}
