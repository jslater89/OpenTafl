package com.manywords.softworks.tafl.test;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.ai.tables.TranspositionTable;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.copenhagen.Copenhagen;

class TranspositionTableConsistencyTest extends TaflTest {

    @Override
    public void run() {
        Rules rules = Copenhagen.newLargeEdgeFortTest();
        Game game = new Game(rules, null);
        GameState state = game.getCurrentState();

        TranspositionTable t = new TranspositionTable(5);
        long zobrist = state.mZobristHash;
        short value = 11541;
        char age = 5;
        byte depth = 4;

        t.putValue(zobrist, value, depth, age);
        long data = t.getData(zobrist);

        // Constants copied from TranspositionTable
        final long BYTE_MASK = 255;
        final long EVAL_MASK = BYTE_MASK + (BYTE_MASK << 8) ;
        final long AGE_MASK = (BYTE_MASK << 16) + (BYTE_MASK << 24);
        final long DEPTH_MASK = (BYTE_MASK << 32);
        final int EVAL_SHIFT = 0;
        final int AGE_SHIFT = 16;
        final int DEPTH_SHIFT = 32;

        byte entryDepth = (byte) ((data & DEPTH_MASK) >>> DEPTH_SHIFT);
        char entryAge = (char) ((data & AGE_MASK) >>> AGE_SHIFT);
        short entryValue = (short)((data & EVAL_MASK));

        //System.out.println("Packed data: " + data);
        //System.out.println("Unpacked depth: " + entryDepth);
        //System.out.println("Unpacked age: " + (int) entryAge);
        //System.out.println("Unpacked value: " + entryValue);

        assert entryDepth == depth;
        assert entryAge == age;
        assert entryValue == value;
    }

}
