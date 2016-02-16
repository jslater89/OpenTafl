package com.manywords.softworks.tafl.ui.lanterna.settings;

import com.manywords.softworks.tafl.ui.player.Player;

/**
 * Created by jay on 2/15/16.
 */
public class TerminalSettings {
    public static final int HUMAN = 0;
    public static final int AI = 1;
    public static final int NETWORK = 2;
    public static final int ENGINE = 3;

    public static int attackers = AI;
    public static int defenders = HUMAN;

    public static int variant = 1;

    public static String labelForPlayerType(int i) {
        switch(i) {
            case HUMAN:
                return "Human";
            case AI:
                return "OpenTafl AI";
            case NETWORK:
                return "Network";
            default:
                return "External Engine";
        }
    }

    public static Player getNewPlayer(int type) {
        switch(type) {
            case HUMAN:
                return Player.getNewPlayer(Player.Type.HUMAN);
            case AI:
                return Player.getNewPlayer(Player.Type.AI);
            case NETWORK:
                return Player.getNewPlayer(Player.Type.HUMAN);
            case ENGINE:
                return Player.getNewPlayer(Player.Type.AI);
        }

        return null;
    }
}
