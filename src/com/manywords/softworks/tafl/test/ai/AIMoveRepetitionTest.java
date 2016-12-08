package com.manywords.softworks.tafl.test.ai;

import com.manywords.softworks.tafl.OpenTafl;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.ai.AiWorkspace;
import com.manywords.softworks.tafl.engine.ai.evaluators.Evaluator;
import com.manywords.softworks.tafl.notation.NotationParseException;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.test.TaflTest;
import com.manywords.softworks.tafl.ui.UiCallback;

public class AIMoveRepetitionTest extends TaflTest implements UiCallback {

    @Override
    public void statusText(String text) {
        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, text);
    }

    @Override
    public void run() {
        AiWorkspace.resetTranspositionTable();

        Rules rules = null;
        try {
             rules = RulesSerializer.loadRulesRecord("dim:7 name:Brandub surf:n atkf:n ks:w tfr:l cor:a1,b1 corh: cenh: cenhe: dfor:c1,c2,c3,c4,b4,a4 dforp: dfors: dforh: start:/7/t6/K6/7/7/7/");
        }
        catch (NotationParseException e) {
            assert false;
        }

        Game g = new Game(rules, this);
        //RawTerminal.renderGameState(g.getCurrentState());

        AiWorkspace workspace = new AiWorkspace(this, g, g.getCurrentState(), 0);
        workspace.explore(30);
        workspace.printSearchStats();

        assert workspace.getTreeRoot().getValue() >= Evaluator.ATTACKER_WIN;

        try {
            rules = RulesSerializer.loadRulesRecord("dim:7 name:Brandub surf:n atkf:n ks:w tfr:w cor:a1,b1 corh: cenh: cenhe: dfor:c1,c2,c3,c4,b4,a4 dforp: dfors: dforh: start:/7/t6/K6/7/7/7/");
        }
        catch (NotationParseException e) {
            assert false;
        }

        g = new Game(rules, this);
        //RawTerminal.renderGameState(g.getCurrentState());

        workspace = new AiWorkspace(this, g, g.getCurrentState(), 0);
        workspace.explore(30);
        //workspace.printSearchStats();

        assert workspace.getTreeRoot().getValue() <= Evaluator.DEFENDER_WIN;
    }

}
