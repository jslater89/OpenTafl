package com.manywords.softworks.tafl.ui.lanterna.window.varianteditor;

import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.ui.lanterna.TerminalUtils;
import com.manywords.softworks.tafl.ui.lanterna.theme.TerminalThemeConstants;

/**
 * Created by jay on 4/14/17.
 */
public class SpacePropertiesDialog extends DialogWindow {
    boolean[] passable = new boolean[Rules.TAFLMAN_TYPE_COUNT];
    boolean[] stoppable = new boolean[Rules.TAFLMAN_TYPE_COUNT];
    boolean[] hostile = new boolean[Rules.TAFLMAN_TYPE_COUNT];
    boolean[] reenterable = new boolean[Rules.TAFLMAN_TYPE_COUNT];
    boolean[] hostileEmpty = new boolean[Rules.TAFLMAN_TYPE_COUNT];
    boolean save;

    private CheckBox[] mPassableCheckBoxes;
    private CheckBox[] mStoppableCheckBoxes;
    private CheckBox[] mHostileCheckBoxes;
    private CheckBox[] mReenterableCheckBoxes;
    private CheckBox[] mHostileEmptyCheckBoxes;

    private String mSpaceType;
    private boolean mShowHostileEmpty;

    public SpacePropertiesDialog(String spaceType, boolean showHostileEmpty) {
        super("Space properties");
        mSpaceType = spaceType;
        mShowHostileEmpty = showHostileEmpty;
    }

    private void buildWindow() {
        Panel container = new Panel();
        container.addComponent(new Label("Check boxes for " + mSpaceType + " properties."));
        container.addComponent(TerminalUtils.newSpacer());

        setupCheckBoxes();

        Panel p = new Panel(new GridLayout(9));

        p.addComponent(TerminalUtils.newSpacer());
        p.addComponent(new Label("Attacker"));
        p.addComponent(new Label("Attacker"));
        p.addComponent(new Label("Attacker"));
        p.addComponent(new Label("Attacker"));
        p.addComponent(new Label("Defender"));
        p.addComponent(new Label("Defender"));
        p.addComponent(new Label("Defender"));
        p.addComponent(new Label("Defender"));

        p.addComponent(TerminalUtils.newSpacer());
        p.addComponent(new Label("Taflman"));
        p.addComponent(new Label("Commander"));
        p.addComponent(new Label("Knight"));
        p.addComponent(new Label("King"));
        p.addComponent(new Label("Taflman"));
        p.addComponent(new Label("Commander"));
        p.addComponent(new Label("Knight"));
        p.addComponent(new Label("King"));

        p.addComponent(new Label("Passable"));
        for(CheckBox checkBox : mPassableCheckBoxes) p.addComponent(checkBox);

        p.addComponent(new Label("Stoppable"));
        for(CheckBox checkBox : mStoppableCheckBoxes) p.addComponent(checkBox);

        p.addComponent(new Label("Hostile"));
        for(CheckBox checkBox : mHostileCheckBoxes) p.addComponent(checkBox);

        p.addComponent(new Label("Reenterable"));
        for(CheckBox checkBox : mReenterableCheckBoxes) p.addComponent(checkBox);

        if(mShowHostileEmpty) {
            p.addComponent(new Label("Hostile Empty"));
            for (CheckBox checkBox : mHostileEmptyCheckBoxes) p.addComponent(checkBox);
        }

        container.addComponent(p);

        Panel buttons = new Panel(new LinearLayout(Direction.HORIZONTAL));
        Button save = new Button("Save", () -> {
            this.save = true;

            updateData();

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

    void loadRules(boolean[] passable, boolean[] stoppable, boolean[] hostile, boolean[] reenterable, boolean[] hostileEmpty) {
        System.arraycopy(passable, 0, this.passable, 0, passable.length);
        System.arraycopy(stoppable, 0, this.stoppable, 0, stoppable.length);
        System.arraycopy(hostile, 0, this.hostile, 0, hostile.length);
        System.arraycopy(reenterable, 0, this.reenterable, 0, reenterable.length);
        System.arraycopy(hostileEmpty, 0, this.hostileEmpty, 0, hostileEmpty.length);
    }

    private void setupCheckBoxes() {
        mPassableCheckBoxes = new CheckBox[] {
                new CheckBox(""),
                new CheckBox(""),
                new CheckBox(""),
                new CheckBox(""),
                new CheckBox(""),
                new CheckBox(""),
                new CheckBox(""),
                new CheckBox(""),
        };
        for(int i = 0; i < passable.length; i++) {
            mPassableCheckBoxes[i].setChecked(passable[i]);
        }

        mStoppableCheckBoxes = new CheckBox[] {
                new CheckBox(""),
                new CheckBox(""),
                new CheckBox(""),
                new CheckBox(""),
                new CheckBox(""),
                new CheckBox(""),
                new CheckBox(""),
                new CheckBox(""),
        };
        for(int i = 0; i < stoppable.length; i++) {
            mStoppableCheckBoxes[i].setChecked(stoppable[i]);
        }

        mHostileCheckBoxes = new CheckBox[] {
                new CheckBox(""),
                new CheckBox(""),
                new CheckBox(""),
                new CheckBox(""),
                new CheckBox(""),
                new CheckBox(""),
                new CheckBox(""),
                new CheckBox(""),
        };
        for(int i = 0; i < hostile.length; i++) {
            mHostileCheckBoxes[i].setChecked(hostile[i]);
        }

        mReenterableCheckBoxes = new CheckBox[] {
                new CheckBox(""),
                new CheckBox(""),
                new CheckBox(""),
                new CheckBox(""),
                new CheckBox(""),
                new CheckBox(""),
                new CheckBox(""),
                new CheckBox(""),
        };
        for(int i = 0; i < reenterable.length; i++) {
            mReenterableCheckBoxes[i].setChecked(reenterable[i]);
        }

        mHostileEmptyCheckBoxes = new CheckBox[] {
                new CheckBox(""),
                new CheckBox(""),
                new CheckBox(""),
                new CheckBox(""),
                new CheckBox(""),
                new CheckBox(""),
                new CheckBox(""),
                new CheckBox(""),
        };
        for(int i = 0; i < hostileEmpty.length; i++) {
            mHostileEmptyCheckBoxes[i].setChecked(hostileEmpty[i]);
        }
    }

    private void updateData() {
        for(int i = 0; i < mPassableCheckBoxes.length; i++) {
            passable[i] = mPassableCheckBoxes[i].isChecked();
        }

        for(int i = 0; i < mStoppableCheckBoxes.length; i++) {
            stoppable[i] = mStoppableCheckBoxes[i].isChecked();
        }

        for(int i = 0; i < mHostileCheckBoxes.length; i++) {
            hostile[i] = mHostileCheckBoxes[i].isChecked();
        }

        for(int i = 0; i < mReenterableCheckBoxes.length; i++) {
            reenterable[i] = mReenterableCheckBoxes[i].isChecked();
        }

        for(int i = 0; i < mHostileEmptyCheckBoxes.length; i++) {
            hostileEmpty[i] = mHostileEmptyCheckBoxes[i].isChecked();
        }
    }

    @Override
    public Object showDialog(WindowBasedTextGUI textGUI) {
        buildWindow();
        setHints(TerminalThemeConstants.CENTERED_MODAL);
        return super.showDialog(textGUI);
    }
}