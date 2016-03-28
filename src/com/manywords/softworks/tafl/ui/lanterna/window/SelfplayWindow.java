package com.manywords.softworks.tafl.ui.lanterna.window;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TerminalTextUtils;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.TextInputDialog;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.ui.AdvancedTerminalHelper;
import com.manywords.softworks.tafl.ui.selfplay.MatchResult;
import com.manywords.softworks.tafl.ui.selfplay.SelfplayRunner;

import java.math.BigInteger;
import java.util.List;

/**
 * Created by jay on 3/22/16.
 */
public class SelfplayWindow extends BasicWindow {
    private AdvancedTerminalHelper.TerminalCallback mTerminalCallback;
    private SelfplayRunner mRunner;
    private int mIterations = 10;
    private Label mIterationsLabel;

    public SelfplayWindow(AdvancedTerminalHelper.TerminalCallback callback) {
        mTerminalCallback = callback;
        mRunner = new SelfplayRunner(this, 10);

        setupUi();
    }

    public void setupUi() {
        Panel p = new Panel();
        p.setLayoutManager(new GridLayout(3));

        Button iterationsButton = new Button("Iterations", this::showIterationsDialog);
        p.addComponent(iterationsButton);

        p.addComponent(newSpacer());

        mIterationsLabel = new Label("" + mIterations);
        p.addComponent(mIterationsLabel);

        Button menuButton= new Button("Main Menu", () -> {
            mTerminalCallback.onMenuNavigation(new MainMenuWindow(mTerminalCallback));
        });

        Button startButton = new Button("Start", () -> {
            mRunner.setMatchCount(mIterations);
            mRunner.startTournament();
        });
        p.addComponent(newSpacer());
        p.addComponent(startButton);
        p.addComponent(menuButton);

        setComponent(p);
    }

    public SelfplayRunner getRunner() {
        return mRunner;
    }

    public AdvancedTerminalHelper.TerminalCallback getTerminalCallback() {
        return mTerminalCallback;
    }

    public void notifyGameFinished(Game g) {
        mRunner.notifyGameFinished(g);
    }

    private EmptySpace newSpacer() {
        return new EmptySpace(new TerminalSize(4, 1));
    }

    private void showIterationsDialog() {
        List<String> lines = TerminalTextUtils.getWordWrappedText(50,
                "How many two-game matches to run.");
        String descriptionString = "";
        for(String s : lines) {
            descriptionString += s + "\n";
        }
        BigInteger searchdepth = TextInputDialog.showNumberDialog(
                getTextGUI(),
                "Iterations",
                descriptionString,
                "" + mIterations);
        int intDepth = searchdepth.intValue();
        mIterations = intDepth;
        mIterationsLabel.setText("" + mIterations);
    }
}
