package com.manywords.softworks.tafl.ui.lanterna.window.selfplay;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.manywords.softworks.tafl.ui.lanterna.TerminalUtils;
import com.manywords.softworks.tafl.ui.lanterna.screen.LogicalScreen;
import com.manywords.softworks.tafl.ui.lanterna.window.mainmenu.MainMenuWindow;
import com.manywords.softworks.tafl.ui.selfplay.MatchResult;
import com.manywords.softworks.tafl.ui.selfplay.SelfplayRunner;

/**
 * Created by jay on 3/28/16.
 */
public class SelfplayResultWindow extends BasicWindow {
    private LogicalScreen.TerminalCallback mTerminalCallback;
    private SelfplayRunner mRunner;

    public SelfplayResultWindow(LogicalScreen.TerminalCallback terminalCallback, SelfplayRunner selfplayRunner) {
        mTerminalCallback = terminalCallback;
        mRunner = selfplayRunner;

        setupUi();
    }

    private void setupUi() {
        Panel p = new Panel();
        p.setLayoutManager(new GridLayout(3));
        Label l1 = new Label("AI Self-Play Results");
        p.addComponent(TerminalUtils.newSpacer());
        p.addComponent(l1);
        p.addComponent(TerminalUtils.newSpacer());

        Label l = new Label("Winner");
        p.addComponent(l);
        l = new Label("Game 1");
        p.addComponent(l);
        l = new Label("Game 2");
        p.addComponent(l);

        for(MatchResult m : mRunner.getMatchResults()) {
            String winner = SelfplayRunner.drawOrName(m.getMatchWinner());
            String game1Result = SelfplayRunner.drawOrName(m.getWinner(0)) + " (" + m.getLength(0) + ")";
            String game2Result = SelfplayRunner.drawOrName(m.getWinner(1)) + " (" + m.getLength(1) + ")";

            p.addComponent(new Label(winner));
            p.addComponent(new Label(game1Result));
            p.addComponent(new Label(game2Result));
        }

        Button b = new Button("Main Menu", () -> {
            mTerminalCallback.onMenuNavigation(new MainMenuWindow(mTerminalCallback));
        });
        p.addComponent(TerminalUtils.newSpacer());
        p.addComponent(TerminalUtils.newSpacer());
        p.addComponent(b);

        setComponent(p);
    }
}
