package com.manywords.softworks.tafl.ui.lanterna.window.serverlobby;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.manywords.softworks.tafl.ui.lanterna.component.EnterTerminatedTextBox;
import com.manywords.softworks.tafl.ui.lanterna.component.ScrollingLabel;
import com.manywords.softworks.tafl.ui.lanterna.screen.LogicalScreen;
import com.manywords.softworks.tafl.ui.lanterna.screen.MainMenuScreen;

/**
 * Created by jay on 5/23/16.
 */
public class ChatWindow extends BasicWindow {
    public interface ChatWindowHost {
        public void sendChatMessage(String message);
    }

    private LogicalScreen.TerminalCallback mCallback;
    private ChatWindowHost mHost;
    private ScrollingLabel mChatText;
    private EnterTerminatedTextBox mChatBox;
    private Panel mButtonPanel;

    public ChatWindow(LogicalScreen.TerminalCallback callback, ChatWindowHost host) {
        super("Chat");

        mCallback = callback;
        this.mHost = host;

        Panel p = new Panel();
        p.setLayoutManager(new LinearLayout());

        mChatText = new ScrollingLabel();
        p.addComponent(mChatText);

        mChatBox = new EnterTerminatedTextBox(new EnterTerminatedTextBox.TextBoxCallback() {
            @Override
            public void onEnterPressed(String input) {
                mHost.sendChatMessage(input);
            }

            @Override
            public void onPageKeyPressed(KeyStroke key) {
                mChatText.handleScroll(true, key.getKeyType() == KeyType.PageUp);
            }
        });
        p.addComponent(mChatBox);

        mButtonPanel = new Panel();
        mButtonPanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));

        Button exitButton = new Button("Leave server", () -> {
            mCallback.changeActiveScreen(new MainMenuScreen());
        });
        mButtonPanel.addComponent(exitButton);

        p.addComponent(mButtonPanel);

        setComponent(p);
    }

    public void notifyFocus(boolean focused) {
        if(focused) {
            setTitle("**CHAT**");
        }
        else {
            setTitle("Chat");
        }
    }

    public void onChatMessageReceived(String sender, String message) {
        mChatText.addLine(sender + ": " + message);
    }

    @Override
    public void setSize(TerminalSize size) {
        super.setSize(size);

        System.out.println(size);

        mChatText.setPreferredSize(new TerminalSize(size.getColumns(), size.getRows() - 5));
        mChatBox.setPreferredSize(new TerminalSize(size.getColumns(), 1));
        mButtonPanel.setPreferredSize(new TerminalSize(size.getColumns(), 3));
    }

    @Override
    public boolean handleInput(KeyStroke key) {
        boolean handledByScreen = mCallback.handleKeyStroke(key);
        return handledByScreen || super.handleInput(key);
    }
}
