package com.manywords.softworks.tafl.network.packet;

import java.util.UUID;

/**
 * Created by jay on 5/26/16.
 */
public class CreateGamePacket extends NetworkPacket {
    public final UUID uuid;
    public final boolean attackingSide;
    public final boolean passworded;
    public final String passwordHash;
    public final String otnRulesString;

    public static CreateGamePacket parse(String data) {
        data = data.replaceFirst("create-game", "").trim();
        String[] split = data.split(" ");

        UUID uuid = UUID.fromString(split[0].trim());
        boolean attackingSide = split[1].contains("attackers");
        String passwordHash = split[2];

        data = data.replaceFirst(split[0], "").replaceFirst(split[1], "").replaceFirst(split[2], "").trim();
        String otnRulesString = data;

        return new CreateGamePacket(uuid, attackingSide, passwordHash, otnRulesString);
    }

    public CreateGamePacket(UUID uuid, boolean attackingSide, String passwordHash, String otnRulesString) {
        this.uuid = uuid;
        this.attackingSide = attackingSide;
        this.passwordHash = passwordHash;
        this.passworded = !passwordHash.equals("none");
        this.otnRulesString = otnRulesString;
    }

    public String toString() {
        return "create-game " + uuid.toString() + " " + (attackingSide ? "attackers" : "defenders") + " " + passwordHash + " " + otnRulesString;
    }
}
