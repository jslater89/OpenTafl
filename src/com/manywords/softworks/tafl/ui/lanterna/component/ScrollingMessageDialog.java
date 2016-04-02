package com.manywords.softworks.tafl.ui.lanterna.component;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.manywords.softworks.tafl.ui.lanterna.theme.TerminalThemeConstants;

/**
 * Created by jay on 2/17/16.
 */
public class ScrollingMessageDialog extends DialogWindow {
    private ScrollingLabel mLabel;
    private MessageDialogButton mResult;
    public ScrollingMessageDialog(String title, String text, MessageDialogButton... buttons) {
        super(title);
        this.setHints(TerminalThemeConstants.CENTERED_MODAL);

        mLabel = new ScrollingLabel(text);

        getLabel().scrollToStart();
        getLabel().forceScrollCount(true);
        getLabel().setScrollCountDirection(ScrollingLabel.Direction.DOWN);

        Panel p = new Panel();
        p.setLayoutManager(new LinearLayout());
        p.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Center));

        Panel buttonPanel = new Panel();
        buttonPanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
        buttonPanel.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.End));

        for(MessageDialogButton buttonName : buttons) {
            Button b = new Button(buttonName.toString(), new Runnable() {
                @Override
                public void run() {
                    ScrollingMessageDialog.this.mResult = buttonName;
                    ScrollingMessageDialog.this.close();
                }
            });
            buttonPanel.addComponent(b);
        }
        p.addComponent(mLabel);
        p.addComponent(buttonPanel);
        setComponent(p);
    }

    public MessageDialogButton showDialog(WindowBasedTextGUI textGUI) {
        this.mResult = null;
        super.showDialog(textGUI);
        return this.mResult;
    }

    @Override
    public boolean handleInput(KeyStroke key) {
        if(key.getKeyType() == KeyType.PageUp) {
            mLabel.handleScroll(true, true);
            return true;
        }
        else if (key.getKeyType() == KeyType.PageDown) {
            mLabel.handleScroll(true, false);
            return true;
        }

        else if(key.getKeyType() == KeyType.ArrowUp) {
            mLabel.handleScroll(false, true);
            return true;
        }
        else if (key.getKeyType() == KeyType.ArrowDown) {
            mLabel.handleScroll(false, false);
            return true;
        }

        return super.handleInput(key);
    }

    public ScrollingLabel getLabel() {
        return mLabel;
    }

    @Override
    public void setSize(TerminalSize size) {
        super.setSize(size);

        mLabel.setPreferredSize(new TerminalSize(size.getColumns(), size.getRows()));
    }
}
