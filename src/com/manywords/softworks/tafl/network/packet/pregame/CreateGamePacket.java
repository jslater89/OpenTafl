package com.manywords.softworks.tafl.network.packet.pregame;

import com.manywords.softworks.tafl.engine.clock.TimeSpec;
import com.manywords.softworks.tafl.network.packet.NetworkPacket;

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
    public final TimeSpec timeSpec;

    public static CreateGamePacket parse(String data) {
        data = data.replaceFirst("create-game", "").trim();
        String[] split = data.split(" ");

        UUID uuid = UUID.fromString(split[0].trim());
        boolean attackingSide = split[1].contains("attackers");
        String passwordHash = split[2];
        TimeSpec ts = TimeSpec.parseMachineReadableString(split[3]);

        data = data.replaceFirst(split[0], "").replaceFirst(split[1], "").replaceFirst(split[2], "").replace(split[3], "").trim();
        String otnRulesString = data;

        return new CreateGamePacket(uuid, attackingSide, passwordHash, otnRulesString, ts);
    }

    public CreateGamePacket(UUID uuid, boolean attackingSide, String passwordHash, String otnRulesString, TimeSpec timeSpec) {
        this.uuid = uuid;
        this.attackingSide = attackingSide;
        this.passwordHash = passwordHash;
        this.passworded = !passwordHash.equals("none");
        this.otnRulesString = otnRulesString;
        this.timeSpec = timeSpec;
    }

    public String toString() {
        return "create-game " + uuid.toString() + " " + (attackingSide ? "attackers" : "defenders") + " " + passwordHash + " " + timeSpec.toMachineReadableString() + " " + otnRulesString;
    }
}
