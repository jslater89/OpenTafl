package com.manywords.softworks.tafl.network.packet;

/**
 * Created by jay on 5/23/16.
 */
public class LoginPacket {
    public final String username;
    private final String hashedPassword;

    public static LoginPacket parse(String data) {
        String[] firstSplit = data.split("\"");
        String username = firstSplit[1];

        String hashedPassword = firstSplit[2].trim();

        return new LoginPacket(username, hashedPassword);
    }

    public LoginPacket(String username, String hashedPassword) {
        this.username = username;
        this.hashedPassword = hashedPassword;
    }

    @Override
    public String toString() {
        return "login \"" + username +"\" " + hashedPassword;
    }
}
