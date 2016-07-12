package com.manywords.softworks.tafl.ui.lanterna.window.serverlobby;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.manywords.softworks.tafl.ui.Ansi;
import com.manywords.softworks.tafl.ui.lanterna.component.EnterTerminatedTextBox;
import com.manywords.softworks.tafl.ui.lanterna.component.ScrollingLabel;
import com.manywords.softworks.tafl.ui.lanterna.screen.LogicalScreen;
import com.manywords.softworks.tafl.ui.lanterna.screen.MainMenuScreen;
import com.manywords.softworks.tafl.ui.lanterna.theme.TerminalThemeConstants;

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

        EmptySpace space = new EmptySpace();
        p.addComponent(space);

        Label enterTextLabel = new Label("Enter message:");
        p.addComponent(enterTextLabel);

        mChatBox = new EnterTerminatedTextBox(new EnterTerminatedTextBox.TextBoxCallback() {
            @Override
            public void onEnterPressed(String input) {
                if(!input.trim().isEmpty()) {
                    mHost.sendChatMessage(input);
                }
            }

            @Override
            public void onPageKeyPressed(KeyStroke key) {
                mChatText.handleScroll(true, key.getKeyType() == KeyType.PageUp);
            }
        });
        p.addComponent(mChatBox);

        setComponent(p);
    }

    public void notifyFocus(boolean focused) {
        if(focused) {
            setTitle(Ansi.UNDERLINE + "CHAT" + Ansi.UNDERLINE_OFF);
        }
        else {
            setTitle("Chat");
        }
    }

    public void onChatMessageReceived(String sender, String message) {
        if(!message.trim().isEmpty()) {
            mChatText.addLine(Ansi.UNDERLINE + sender + Ansi.UNDERLINE_OFF + ": " + message);
        }
    }

    @Override
    public void setSize(TerminalSize size) {
        super.setSize(size);

        mChatText.setPreferredSize(new TerminalSize(size.getColumns(), size.getRows() - 3));
        mChatBox.setPreferredSize(new TerminalSize(size.getColumns(), 1));
    }

    @Override
    public boolean handleInput(KeyStroke key) {
        boolean handledByScreen = mCallback.handleKeyStroke(key);
        return handledByScreen || super.handleInput(key);
    }
}
