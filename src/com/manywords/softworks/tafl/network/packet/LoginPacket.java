package com.manywords.softworks.tafl.network.packet;

/**
 * Created by jay on 5/23/16.
 */
public class LoginPacket {
    public final String username;
    private final String salt;
    private final String hashedPassword;

    public static LoginPacket parse(String data) {
        String[] firstSplit = data.split("\"");
        String username = firstSplit[1];

        String[] secondSplit = firstSplit[2].split(" ");
        String salt = secondSplit[0];
        String hashedPassword = secondSplit[1];

        return new LoginPacket(username, salt, hashedPassword);
    }

    public LoginPacket(String username, String salt, String hashedPassword) {
        this.username = username;
        this.salt = salt;
        this.hashedPassword = hashedPassword;
    }
}
