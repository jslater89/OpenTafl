package com.manywords.softworks.tafl.rules.brandub;

import com.manywords.softworks.tafl.rules.Board;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.rules.brandub.seven.Brandub7Attackers;
import com.manywords.softworks.tafl.rules.brandub.seven.Brandub7Board;
import com.manywords.softworks.tafl.rules.brandub.seven.Brandub7Defenders;

/**
 * Created by jay on 8/10/16.
 */
public class Magpie extends Brandub {
    public Magpie(Board board, Side attackers, Side defenders) {
        super(board, attackers, defenders);
    }

    public static Magpie newMagpie7() {
        Brandub7Board board = new Brandub7Board();
        Brandub7Attackers attackers = new Brandub7Attackers(board);
        Brandub7Defenders defenders = new Brandub7Defenders(board);

        Magpie rules = new Magpie(board, attackers, defenders);
        return rules;
    }

    @Override
    public int getTaflmanSpeedLimit(char taflman) {
        return 1;
    }

    @Override
    public String getName() {
        return "Magpie";
    }
}
