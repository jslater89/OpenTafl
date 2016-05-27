package com.manywords.softworks.tafl.network.packet.pregame;

/**
 * Created by jay on 5/23/16.
 */
public class LoginPacket {
    public final String username;
    public final String password;

    public static LoginPacket parse(String data) {
        String[] firstSplit = data.split("\"");
        String username = firstSplit[1];

        String hashedPassword = firstSplit[2].trim();

        return new LoginPacket(username, hashedPassword);
    }

    public LoginPacket(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public String toString() {
        return "login \"" + username +"\" " + password;
    }
}
