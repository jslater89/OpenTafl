package com.manywords.softworks.tafl.ui.lanterna.window.varianteditor;

import com.googlecode.lanterna.TerminalTextUtils;
import com.googlecode.lanterna.gui2.*;
import com.manywords.softworks.tafl.rules.GenericRules;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.ui.lanterna.TerminalUtils;
import com.manywords.softworks.tafl.ui.lanterna.component.FocusableBasicWindow;
import com.manywords.softworks.tafl.ui.lanterna.component.ScrollingLabel;
import com.manywords.softworks.tafl.ui.lanterna.screen.LogicalScreen;

/**
 * Created by jay on 4/13/17.
 */
public class RulesWindow extends FocusableBasicWindow {
    public RulesWindow(LogicalScreen.TerminalCallback callback) {
        super("Rules", callback);
        buildWindow();
    }

    private TextBox mName;

    private RadioBoxList<String> mEscapeRadio;
    private String[] mEscapeTypes = {"Corner", "Edge"};

    private CheckBox mSurroundingCheckBox;
    private CheckBox mAttackersFirstCheckBox;
    private CheckBox mEdgeFortEscapeCheckBox;

    private RadioBoxList<String> mThreefoldResultRadio;
    private String[] mThreefoldResultModes = {"Draw", "Win", "Loss", "None"};

    private RadioBoxList<String> mKingArmedRadio;
    private String[] mKingArmedModes = {"Armed", "Hammer", "Anvil", "Unarmed"};
    private RadioBoxList<String> mKingStrongRadio;
    private String[] mKingStrongModes = {"Strong", "Center", "Middleweight", "Weak"};

    private String[] mJumpModes = {"None", "Jump", "Capture", "Restricted"};
    private RadioBoxList<String> mKingJumpRadio;
    private RadioBoxList<String> mKnightJumpRadio;
    private RadioBoxList<String> mCommanderJumpRadio;

    private RadioBoxList<String> mShieldwallModeRadio;
    private String[] mShieldwallModes = {"None", "Weak", "Strong"};
    private CheckBox mShieldwallFlankingCheckBox;

    private RadioBoxList<String> mBerserkRadio;
    private String[] mBerserkModes = {"None", "Capture", "Any move"};

    private Button mSpeedLimitsButton;
    private int[] mTaflmanSpeedLimits = new int[Rules.TAFLMAN_TYPE_COUNT];

    private SpacePropertiesDialog mCenterPropertiesDialog;
    private SpacePropertiesDialog mCornerPropertiesDialog;
    private SpacePropertiesDialog mAttackerFortPropertiesDialog;
    private SpacePropertiesDialog mDefenderFortPropertiesDialog;


    private ScrollingLabel mLabel = new ScrollingLabel();

    private void buildWindow() {
        Panel p = new Panel();

        p.addComponent(mLabel);

        Panel firstRow = new Panel(new LinearLayout(Direction.HORIZONTAL));

        firstRow.addComponent(new Label("Name: "));
        mName = new TextBox("Unnamed tafl variant");
        firstRow.addComponent(mName);

        firstRow.addComponent(new Label("Escape: "));
        mEscapeRadio = new RadioBoxList<>();
        addItems(mEscapeRadio, mEscapeTypes);
        firstRow.addComponent(mEscapeRadio);

        p.addComponent(firstRow);
        p.addComponent(TerminalUtils.newSpacer());

        Panel secondRow = new Panel(new LinearLayout(Direction.HORIZONTAL));

        mAttackersFirstCheckBox = new CheckBox("Attackers first?");
        secondRow.addComponent(mAttackersFirstCheckBox);

        mSurroundingCheckBox = new CheckBox("Surrounding fatal?");
        secondRow.addComponent(mSurroundingCheckBox);

        mEdgeFortEscapeCheckBox = new CheckBox("Edge fort escape?");
        secondRow.addComponent(mEdgeFortEscapeCheckBox);

        p.addComponent(secondRow);
        p.addComponent(TerminalUtils.newSpacer());

        Panel thirdRow = new Panel(new LinearLayout(Direction.HORIZONTAL));

        thirdRow.addComponent(new Label("Shieldwall captures?"));

        mShieldwallModeRadio = new RadioBoxList<>();
        addItems(mShieldwallModeRadio, mShieldwallModes);
        thirdRow.addComponent(mShieldwallModeRadio);

        mShieldwallFlankingCheckBox = new CheckBox("Flanking only?");
        thirdRow.addComponent(mShieldwallFlankingCheckBox);

        thirdRow.addComponent(new Label("Threefold result?"));
        mThreefoldResultRadio = new RadioBoxList<>();
        addItems(mThreefoldResultRadio, mThreefoldResultModes);
        thirdRow.addComponent(mThreefoldResultRadio);

        p.addComponent(thirdRow);
        p.addComponent(TerminalUtils.newSpacer());

        Panel fourthRow = new Panel(new LinearLayout(Direction.HORIZONTAL));

        fourthRow.addComponent(new Label("King strength?"));
        mKingStrongRadio = new RadioBoxList<>();
        addItems(mKingStrongRadio, mKingStrongModes);
        fourthRow.addComponent(mKingStrongRadio);

        fourthRow.addComponent(new Label("King armed?"));
        mKingArmedRadio = new RadioBoxList<>();
        addItems(mKingArmedRadio, mKingArmedModes);
        fourthRow.addComponent(mKingArmedRadio);

        fourthRow.addComponent(new Label("King jumps?"));
        mKingJumpRadio = new RadioBoxList<>();
        addItems(mKingJumpRadio, mJumpModes);
        fourthRow.addComponent(mKingJumpRadio);

        p.addComponent(fourthRow);
        p.addComponent(TerminalUtils.newSpacer());

        Panel fifthRow = new Panel(new LinearLayout(Direction.HORIZONTAL));

        fifthRow.addComponent(new Label("Berserk?"));
        mBerserkRadio = new RadioBoxList<>();
        addItems(mBerserkRadio, mBerserkModes);
        fifthRow.addComponent(mBerserkRadio);

        fifthRow.addComponent(new Label("Knight jumps?"));
        mKnightJumpRadio = new RadioBoxList<>();
        addItems(mKnightJumpRadio, mJumpModes);
        fifthRow.addComponent(mKnightJumpRadio);

        fifthRow.addComponent(new Label("Commander jumps?"));
        mCommanderJumpRadio = new RadioBoxList<>();
        addItems(mCommanderJumpRadio, mJumpModes);
        fifthRow.addComponent(mCommanderJumpRadio);

        p.addComponent(fifthRow);
        p.addComponent(TerminalUtils.newSpacer());

        Panel sixthRow = new Panel(new LinearLayout(Direction.HORIZONTAL));

        mSpeedLimitsButton = new Button("Speed limits", () -> {
            int[] result = SpeedLimitDialog.show(getTextGUI(), mTaflmanSpeedLimits);
            if(result != null) {
                mTaflmanSpeedLimits = result;
            }
        });
        sixthRow.addComponent(mSpeedLimitsButton);

        mCenterPropertiesDialog = new SpacePropertiesDialog("center", true);
        mCornerPropertiesDialog = new SpacePropertiesDialog("corner", true);
        mAttackerFortPropertiesDialog = new SpacePropertiesDialog("attacker fort", true);
        mDefenderFortPropertiesDialog = new SpacePropertiesDialog("defender fort", true);

        Button mCenterSpacePropertiesButton = new Button("Center properties", () -> {
            mCenterPropertiesDialog.showDialog(getTextGUI());
        });
        sixthRow.addComponent(mCenterSpacePropertiesButton);

        Button mCornerSpacePropertiesButton = new Button("Corner properties", () -> {
            mCornerPropertiesDialog.showDialog(getTextGUI());
        });
        sixthRow.addComponent(mCornerSpacePropertiesButton);

        p.addComponent(sixthRow);

        Panel seventhRow = new Panel(new LinearLayout(Direction.HORIZONTAL));

        Button mAttackerFortPropertiesButton = new Button("Attacker fort properties", () -> {
            mAttackerFortPropertiesDialog.showDialog(getTextGUI());
        });
        seventhRow.addComponent(mAttackerFortPropertiesButton);

        Button mDefenderFortPropertiesButton = new Button("Defender fort properties", () -> {
            mDefenderFortPropertiesDialog.showDialog(getTextGUI());
        });
        seventhRow.addComponent(mDefenderFortPropertiesButton);
        p.addComponent(seventhRow);


        setComponent(p);
    }

    public void setLabel(String text) {
        if(getSize() != null)
            mLabel.setText(TerminalUtils.linesToString(TerminalTextUtils.getWordWrappedText(getSize().getColumns(), text)));
    }

    private static void addItems(RadioBoxList<String> box, String[] items) {
        for(String i : items) {
            box.addItem(i);
        }
    }

    /**
     * Update a given set of generic rules to match the state of the UI.
     * @param rules
     */
    public void updateRules(GenericRules rules) {
        rules.setName(mName.getText());

        // Checkboxes
        rules.setSurroundingFatal(mSurroundingCheckBox.isChecked());
        rules.setAttackersFirst(mAttackersFirstCheckBox.isChecked());
        rules.setEdgeFortEscape(mEdgeFortEscapeCheckBox.isChecked());
        rules.setShieldwallFlankingRequired(mShieldwallFlankingCheckBox.isChecked());

        // Radios
        rules.setEscapeType(escapeStringToType(mEscapeRadio.getCheckedItem()));
        rules.setThreefoldResult(threefoldStringToType(mThreefoldResultRadio.getCheckedItem()));
        rules.setKingArmed(kingArmedStringToType(mKingArmedRadio.getCheckedItem()));
        rules.setKingStrength(kingStrongStringToType(mKingStrongRadio.getCheckedItem()));

        rules.setKingJumpMode(jumpStringToType(mKingJumpRadio.getCheckedItem()));
        rules.setKnightJumpMode(jumpStringToType(mKnightJumpRadio.getCheckedItem()));
        rules.setCommanderJumpMode(jumpStringToType(mCommanderJumpRadio.getCheckedItem()));

        rules.setShieldwallMode(shieldwallStringToType(mShieldwallModeRadio.getCheckedItem()));
        rules.setBerserkMode(berserkStringToType(mBerserkRadio.getCheckedItem()));

        // Dialogs
        rules.setSpeedLimits(mTaflmanSpeedLimits);
        rules.setCenterParameters(
                mCenterPropertiesDialog.passable,
                mCenterPropertiesDialog.stoppable,
                mCenterPropertiesDialog.hostile,
                mCenterPropertiesDialog.reenterable,
                mCenterPropertiesDialog.hostileEmpty
        );

        rules.setCornerParameters(
                mCornerPropertiesDialog.passable,
                mCornerPropertiesDialog.stoppable,
                mCornerPropertiesDialog.hostile,
                mCornerPropertiesDialog.reenterable
        );

        rules.setAttackerFortParameters(
                mAttackerFortPropertiesDialog.passable,
                mAttackerFortPropertiesDialog.stoppable,
                mAttackerFortPropertiesDialog.hostile,
                mAttackerFortPropertiesDialog.reenterable
        );

        rules.setDefenderFortParameters(
                mDefenderFortPropertiesDialog.passable,
                mDefenderFortPropertiesDialog.stoppable,
                mDefenderFortPropertiesDialog.hostile,
                mDefenderFortPropertiesDialog.reenterable
        );
    }

    /**
     * Set the UI widgets to match a given set of rules.
     * @param rules
     */
    public void updateScreen(GenericRules rules) {
        mName.setText(rules.getName());

        // Checkboxes
        mSurroundingCheckBox.setChecked(rules.isSurroundingFatal());
        mAttackersFirstCheckBox.setChecked(rules.getStartingSide().isAttackingSide());
        mEdgeFortEscapeCheckBox.setChecked(rules.allowEdgeFortEscapes());
        mShieldwallFlankingCheckBox.setChecked(rules.allowFlankingShieldwallCapturesOnly());

        // Radios
        mEscapeRadio.setCheckedItemIndex(escapeTypeToIndex(rules.getEscapeType()));
        mThreefoldResultRadio.setCheckedItemIndex(threefoldTypeToIndex(rules.threefoldRepetitionResult()));
        mKingArmedRadio.setCheckedItemIndex(kingArmedTypeToIndex(rules.getKingArmedMode()));
        mKingStrongRadio.setCheckedItemIndex(kingStrongTypeToIndex(rules.getKingStrengthMode()));

        mKingJumpRadio.setCheckedItemIndex(jumpTypeToIndex(rules.getKingJumpMode()));
        mKnightJumpRadio.setCheckedItemIndex(jumpTypeToIndex(rules.getKnightJumpMode()));
        mCommanderJumpRadio.setCheckedItemIndex(jumpTypeToIndex(rules.getCommanderJumpMode()));

        mShieldwallModeRadio.setCheckedItemIndex(shieldwallTypeToIndex(rules.allowShieldWallCaptures()));
        mBerserkRadio.setCheckedItemIndex(berserkTypeToindex(rules.getBerserkMode()));

        // Dialogs
        System.arraycopy(rules.getSpeedLimits(), 0, mTaflmanSpeedLimits, 0, mTaflmanSpeedLimits.length);

        mCenterPropertiesDialog.loadRules(
                rules.centerPassableFor,
                rules.centerStoppableFor,
                rules.centerHostileTo,
                rules.centerReenterableFor,
                rules.emptyCenterHostileTo);

        mCornerPropertiesDialog.loadRules(
                rules.cornerPassableFor,
                rules.cornerStoppableFor,
                rules.cornerHostileTo,
                rules.cornerReenterableFor,
                new boolean[0]);

        mAttackerFortPropertiesDialog.loadRules(
                rules.attackerFortPassableFor,
                rules.attackerFortStoppableFor,
                rules.attackerFortHostileTo,
                rules.attackerFortReenterableFor,
                new boolean[0]);

        mDefenderFortPropertiesDialog.loadRules(
                rules.defenderFortPassableFor,
                rules.defenderFortStoppableFor,
                rules.defenderFortHostileTo,
                rules.defenderFortReenterableFor,
                new boolean[0]);

    }

    private int getIndex(String[] a, String s) {
        for(int i = 0; i < a.length; i++) {
            if(s.toLowerCase().equals(a[i].toLowerCase())) return i;
        }
        return -1;
    }

    private int escapeStringToType(String escape) {
        if("corner".equals(escape.toLowerCase())) return Rules.CORNERS;
        else return Rules.EDGES;
    }

    private int escapeTypeToIndex(int mode) {
        if(mode == Rules.CORNERS) return getIndex(mEscapeTypes, "corner");
        else return getIndex(mEscapeTypes, "edge");
    }

    private int threefoldStringToType(String threefold) {
        if("draw".equals(threefold.toLowerCase())) return Rules.THIRD_REPETITION_DRAWS;
        else if("win".equals(threefold.toLowerCase())) return Rules.THIRD_REPETITION_WINS;
        else if("loss".equals(threefold.toLowerCase())) return Rules.THIRD_REPETITION_DRAWS;
        else return Rules.THIRD_REPETITION_IGNORED;
    }

    private int threefoldTypeToIndex(int mode) {
        if(mode == Rules.THIRD_REPETITION_DRAWS) return getIndex(mThreefoldResultModes, "draw");
        else if(mode == Rules.THIRD_REPETITION_WINS) return getIndex(mThreefoldResultModes, "win");
        else if(mode == Rules.THIRD_REPETITION_LOSES) return getIndex(mThreefoldResultModes, "loss");
        else return getIndex(mThreefoldResultModes, "none");
    }

    // Happily, this one is in the same order.
    private int kingArmedStringToType(String armed) {
        return getIndex(mKingArmedModes, armed);
    }

    private int kingArmedTypeToIndex(int mode) {
        return mode;
    }

    private int kingStrongStringToType(String strong) {
        if("strong".equals(strong.toLowerCase())) return Rules.KING_STRONG;
        else if("center".equals(strong.toLowerCase())) return Rules.KING_STRONG_CENTER;
        else if("middleweight".equals(strong.toLowerCase())) return Rules.KING_MIDDLEWEIGHT;
        else return Rules.KING_WEAK;
    }

    private int kingStrongTypeToIndex(int mode) {
        if(mode == Rules.KING_STRONG) return getIndex(mKingStrongModes, "strong");
        else if(mode == Rules.KING_STRONG_CENTER) return getIndex(mKingStrongModes, "center");
        else if(mode == Rules.KING_MIDDLEWEIGHT) return getIndex(mKingStrongModes, "middleweight");
        else return getIndex(mKingStrongModes, "weak");
    }

    private int jumpStringToType(String jump) {
        return getIndex(mJumpModes, jump);
    }

    private int jumpTypeToIndex(int mode) {
        return mode;
    }

    private int shieldwallStringToType(String shieldwall) {
        return getIndex(mShieldwallModes, shieldwall);
    }

    private int shieldwallTypeToIndex(int mode) {
        return mode;
    }

    private int berserkStringToType(String berserk) {
        return getIndex(mBerserkModes, berserk);
    }

    private int berserkTypeToindex(int mode) {
        return mode;
    }
}
