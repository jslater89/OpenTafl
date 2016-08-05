package com.manywords.softworks.tafl.network.packet.pregame;

import com.manywords.softworks.tafl.engine.DetailedMoveRecord;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.network.packet.GameInformation;
import com.manywords.softworks.tafl.network.packet.NetworkPacket;
import com.manywords.softworks.tafl.network.packet.ingame.HistoryPacket;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.Rules;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jay on 5/26/16.
 */
public class StartGamePacket extends NetworkPacket {
    public static final String PREFIX = "start-game";

    public final Rules rules;
    public final List<DetailedMoveRecord> history;

    public static StartGamePacket parse(String data) {
        String rulesOnly = data.replaceFirst("history.*", "");
        Rules r = RulesSerializer.loadRulesRecord(rulesOnly.replaceFirst("start-game", "").trim());

        if(r == null) {
            throw new IllegalArgumentException("Start game packet rules tag failed to parse");
        }

        List<DetailedMoveRecord> history = null;
        if(data.contains("history")) {
            String historyOnly = data.replaceFirst(".*history", "history");
            history = new ArrayList<>();
            HistoryPacket p = HistoryPacket.parse(historyOnly);
            if(p != null) {
                history.addAll(p.moves);
            }
            else {
                throw new IllegalArgumentException("Start game packet has history tag, but invalid history");
            }
        }

        if(history != null && history.isEmpty()) history = null;

        return new StartGamePacket(r, history);
    }

    public StartGamePacket(Rules rules, List<DetailedMoveRecord> history) {
        this.rules = rules;
        this.history = history;
    }

    @Override
    public String toString() {
        return PREFIX + " " + rules.getOTRString() + (history != null ? " " + new HistoryPacket(history, rules.boardSize).toString() : "");
    }
}
