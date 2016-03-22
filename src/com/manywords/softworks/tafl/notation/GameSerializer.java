package com.manywords.softworks.tafl.notation;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jay on 3/22/16.
 */
public class GameSerializer {
    public static String getGameRecord(Game g) {
        String tagString = "";

        tagString += "[date:";
        String date = new SimpleDateFormat("yyyy.MM.dd").format(new Date());
        tagString += date + "]\n";

        tagString += "[result:";
        if(g.getCurrentState().checkVictory() == GameState.ATTACKER_WIN) tagString += "1";
        else if(g.getCurrentState().checkVictory() == GameState.DEFENDER_WIN) tagString += "-1";
        else if(g.getCurrentState().checkVictory() == GameState.DRAW) tagString += "0";
        else tagString += "?";
        tagString += "]\n";

        tagString += "[compiler:OpenTafl]\n";

        if(g.getClock() != null) {
            String timeString = g.getClock().getMainTime() / 1000 + " " + g.getClock().getOvertimeCount() + "/" + g.getClock().getOvertimeTime() / 1000;
            tagString += "[time-control:" + timeString + "]\n";
        }

        String rulesString = "[rules:" + g.getGameRules().getOTRString() + "]\n\n";

        // TODO: split on moves, add comments
        String movesString = g.getHistoryString();

        return tagString + rulesString + movesString;
    }
}
