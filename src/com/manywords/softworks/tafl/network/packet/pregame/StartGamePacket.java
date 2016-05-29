package com.manywords.softworks.tafl.network.packet.pregame;

import com.manywords.softworks.tafl.network.packet.GameInformation;
import com.manywords.softworks.tafl.network.packet.NetworkPacket;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.Rules;

/**
 * Created by jay on 5/26/16.
 */
public class StartGamePacket extends NetworkPacket {
    public static final String PREFIX = "start-game";

    public final Rules rules;

    public static StartGamePacket parse(String data) {
        Rules r = RulesSerializer.loadRulesRecord(data.replaceFirst("start-game", "").trim());

        return new StartGamePacket(r);
    }

    public StartGamePacket(Rules rules) {
        this.rules = rules;
    }

    @Override
    public String toString() {
        return PREFIX + " " + rules.getOTRString();
    }
}
