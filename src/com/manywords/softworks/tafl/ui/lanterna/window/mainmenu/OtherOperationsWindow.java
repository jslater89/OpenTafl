package com.manywords.softworks.tafl.ui.lanterna.window.mainmenu;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.manywords.softworks.tafl.OpenTafl;
import com.manywords.softworks.tafl.ui.lanterna.screen.LogicalScreen;
import com.manywords.softworks.tafl.ui.lanterna.theme.TerminalThemeConstants;
import com.manywords.softworks.tafl.ui.lanterna.window.selfplay.SelfplayWindow;

/**
 * Created by jay on 2/10/17.
 */
public class OtherOperationsWindow extends BasicWindow {
    private LogicalScreen.TerminalCallback mTerminalCallback;
    public OtherOperationsWindow(LogicalScreen.TerminalCallback callback) {
        mTerminalCallback = callback;

        buildWindow();
    }

    private void buildWindow() {
        Panel p = new Panel();
        p.setLayoutManager(new LinearLayout(Direction.VERTICAL));

        Label l1 = new Label("OpenTafl " + OpenTafl.CURRENT_VERSION);
        p.addComponent(l1);

        Label l2 = new Label("The old Norse board game,");
        p.addComponent(l2);

        Label l3 = new Label("In an old computer style.");
        p.addComponent(l3);

        EmptySpace e1 = new EmptySpace(new TerminalSize(0, 1));
        p.addComponent(e1);

        Button downloadButton = new Button("Download PlayTaflOnline game", () -> new DownloadPlayTaflOnlineDialog(mTerminalCallback).showDialog(getTextGUI()));
        p.addComponent(downloadButton);

        Button loadNotationButton = new Button("Load notation", () -> {
            LoadNotationDialog d = new LoadNotationDialog(mTerminalCallback, null);
            d.setHints(TerminalThemeConstants.CENTERED_MODAL);
            d.showLoadNotationDialog(getTextGUI());
        });
        p.addComponent(loadNotationButton);

        Button tourneyButton = new Button("AI selfplay", () -> mTerminalCallback.onMenuNavigation(new SelfplayWindow(mTerminalCallback)));
        p.addComponent(tourneyButton);

        Button closeButton = new Button("Back", () -> mTerminalCallback.onMenuNavigation(new MainMenuWindow(mTerminalCallback)));
        p.addComponent(closeButton);

        setComponent(p);
    }
}
