package com.manywords.softworks.tafl.network.packet.pregame;

import com.manywords.softworks.tafl.OpenTafl;

/**
 * Created by jay on 5/23/16.
 */
public class LoginPacket {
    public static final String PREFIX = "login";
    public final String username;
    public final String password;
    public final int networkProtocolVersion;

    public static LoginPacket parse(String data) {
        String[] firstSplit = data.split("\"");
        String username = firstSplit[1];

        String[] secondSplit = firstSplit[2].trim().split(" ");
        String hashedPassword = secondSplit[0].trim();
        int networkProtocolVersion = Integer.parseInt(secondSplit[1].trim());

        return new LoginPacket(username, hashedPassword, networkProtocolVersion);
    }

    public LoginPacket(String username, String password, int networkProtocolVersion) {
        this.username = username;
        this.password = password;
        this.networkProtocolVersion = networkProtocolVersion;
    }

    @Override
    public String toString() {
        return PREFIX + " \"" + username +"\" " + password + " " + networkProtocolVersion;
    }
}
