package com.manywords.softworks.tafl.test.mechanics;

import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.replay.ReplayGame;
import com.manywords.softworks.tafl.engine.replay.ReplayGameState;
import com.manywords.softworks.tafl.notation.GameSerializer;
import com.manywords.softworks.tafl.test.TaflTest;

import java.io.File;

/**
 * Created by jay on 7/31/16.
 */
public class ReplayGameTest extends TaflTest {
    @Override
    public void run() {
        GameSerializer.GameContainer container = GameSerializer.loadGameRecordFile(new File("saved-games/replays/Fish-Nasa-2015-Fetlar.otg"));
        ReplayGame rg = new ReplayGame(container.game, container.moves);

        for(GameState state : rg.getGame().getHistory()) {
            assert state instanceof ReplayGameState;
        }
    }
}
