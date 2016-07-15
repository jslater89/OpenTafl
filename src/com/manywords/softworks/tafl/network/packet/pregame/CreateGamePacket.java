package com.manywords.softworks.tafl.network.packet.pregame;

import com.manywords.softworks.tafl.engine.clock.TimeSpec;
import com.manywords.softworks.tafl.network.packet.GameInformation;
import com.manywords.softworks.tafl.network.packet.NetworkPacket;
import com.manywords.softworks.tafl.notation.RulesSerializer;

import java.util.UUID;

/**
 * Created by jay on 5/26/16.
 */
public class CreateGamePacket extends NetworkPacket {
    public static final String PREFIX = "create-game";
    public final UUID uuid;
    public final boolean attackingSide;
    public final boolean passworded;
    public final String passwordHash;
    public final String otnRulesString;
    public final TimeSpec timeSpec;
    public final boolean combineChat;
    public final boolean allowReplay;

    public static CreateGamePacket parse(String data) {
        data = data.replaceFirst("create-game", "").trim();
        String[] split = data.split(" ");

        UUID uuid = UUID.fromString(split[0].trim());
        boolean combineChat = split[1].contains("combinechat:true");
        boolean allowReplay = split[2].contains("allowreplay:true");
        boolean attackingSide = split[3].contains("attackers");
        String passwordHash = split[4];
        TimeSpec ts = TimeSpec.parseMachineReadableString(split[5]);

        data = data.replaceFirst(split[0], "").replaceFirst(split[1], "").replaceFirst(split[2], "").replace(split[3], "").replace(split[4], "").replace(split[5], "").trim();
        String otnRulesString = data;

        return new CreateGamePacket(uuid, attackingSide, passwordHash, otnRulesString, ts, combineChat, allowReplay);
    }

    public CreateGamePacket(UUID uuid, boolean attackingSide, String passwordHash, String otnRulesString, TimeSpec timeSpec, boolean combineChat, boolean allowReplay) {
        this.uuid = uuid;
        this.attackingSide = attackingSide;
        this.passwordHash = passwordHash;
        this.passworded = !passwordHash.equals("none");
        this.otnRulesString = otnRulesString;
        this.timeSpec = timeSpec;
        this.combineChat = combineChat;
        this.allowReplay = allowReplay;
    }

    /**
     * Incomplete information: used only for making sure the ClientServerConnection has access to the timespec, the combined chat, and
     * the allowed replay settings.
     * @return
     */
    public GameInformation toGameInformation() {
        return new GameInformation(uuid.toString(), "", "", "", false, false, false, combineChat, allowReplay, 0, timeSpec.toMachineReadableString());
    }

    public String toString() {
        return PREFIX + " " + uuid.toString() + " combinechat:" + combineChat + " allowreplay:" + allowReplay + " " + (attackingSide ? "attackers" : "defenders") + " " + passwordHash + " " + timeSpec.toMachineReadableString() + " " + otnRulesString;
    }
}
