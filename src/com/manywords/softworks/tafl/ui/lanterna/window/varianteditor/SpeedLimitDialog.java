package com.manywords.softworks.tafl.ui.lanterna.window.varianteditor;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import com.googlecode.lanterna.input.KeyType;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.ui.lanterna.TerminalUtils;
import com.manywords.softworks.tafl.ui.lanterna.theme.TerminalThemeConstants;

/**
 * Created by jay on 4/14/17.
 */
public class SpeedLimitDialog extends DialogWindow {
    private boolean mSave = false;

    private int[] mSpeedLimits = new int[Rules.TAFLMAN_TYPE_COUNT];
    private TextBox[] mSpeedLimitInputs;

    protected SpeedLimitDialog() {
        super("Speed limits");

        buildWindow();
    }

    private void buildWindow() {
        Panel container = new Panel();
        container.addComponent(new Label("Enter speed limit, or 0 or -1 for no speed limit."));
        container.addComponent(TerminalUtils.newSpacer());

        Panel p = new Panel();
        p.setLayoutManager(new GridLayout(5));

        mSpeedLimitInputs = new TextBox[] {
                new TextBox(new TerminalSize(3, 1)),
                new TextBox(new TerminalSize(3, 1)),
                new TextBox(new TerminalSize(3, 1)),
                new TextBox(new TerminalSize(3, 1)),
                new TextBox(new TerminalSize(3, 1)),
                new TextBox(new TerminalSize(3, 1)),
                new TextBox(new TerminalSize(3, 1)),
                new TextBox(new TerminalSize(3, 1)),
        };

        for (TextBox input : mSpeedLimitInputs) {
            input.setInputFilter((interactable, keyStroke) ->
                    !(keyStroke.getKeyType() == KeyType.Character && Character.isAlphabetic(keyStroke.getCharacter())));
        }

        p.addComponent(TerminalUtils.newSpacer());
        p.addComponent(new Label("Taflman"));
        p.addComponent(new Label("Commander"));
        p.addComponent(new Label("Knight"));
        p.addComponent(new Label("King"));

        p.addComponent(new Label("Attacker"));
        p.addComponent(mSpeedLimitInputs[0]);
        p.addComponent(mSpeedLimitInputs[1]);
        p.addComponent(mSpeedLimitInputs[2]);
        p.addComponent(mSpeedLimitInputs[3]);

        p.addComponent(new Label("Defender"));
        p.addComponent(mSpeedLimitInputs[4]);
        p.addComponent(mSpeedLimitInputs[5]);
        p.addComponent(mSpeedLimitInputs[6]);
        p.addComponent(mSpeedLimitInputs[7]);

        container.addComponent(p);

        Panel buttons = new Panel(new LinearLayout(Direction.HORIZONTAL));

        Button save = new Button("Save", () -> {
            mSave = true;

            for(int i = 0; i < mSpeedLimits.length; i++) {
                mSpeedLimits[i] = Integer.parseInt(mSpeedLimitInputs[i].getText());
            }

            close();
        });
        buttons.addComponent(save);

        Button cancel = new Button("Cancel", () -> {
           close();
        });
        buttons.addComponent(cancel);

        container.addComponent(buttons);

        setComponent(container);
    }

    @Override
    public Object showDialog(WindowBasedTextGUI textGUI) {
        super.showDialog(textGUI);

        if(mSave) return mSpeedLimits;
        else return null;
    }

    public static int[] show(WindowBasedTextGUI gui, int[] speedLimits) {
        SpeedLimitDialog d = new SpeedLimitDialog();
        d.setHints(TerminalThemeConstants.CENTERED_MODAL);


        System.arraycopy(speedLimits, 0, d.mSpeedLimits, 0, speedLimits.length);

        for(int i = 0; i < d.mSpeedLimits.length; i++) {
            d.mSpeedLimitInputs[i].setText("" + d.mSpeedLimits[i]);
        }


        return (int[]) d.showDialog(gui);
    }
}
