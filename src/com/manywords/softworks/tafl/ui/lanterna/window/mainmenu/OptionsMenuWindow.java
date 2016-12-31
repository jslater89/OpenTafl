package com.manywords.softworks.tafl.ui.lanterna.window.mainmenu;

import com.googlecode.lanterna.TerminalTextUtils;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.*;
import com.manywords.softworks.tafl.command.player.external.engine.EngineSpec;
import com.manywords.softworks.tafl.rules.BuiltInVariants;
import com.manywords.softworks.tafl.ui.lanterna.TerminalUtils;
import com.manywords.softworks.tafl.ui.lanterna.screen.LogicalScreen;
import com.manywords.softworks.tafl.ui.lanterna.settings.TerminalSettings;

import java.io.File;
import java.math.BigInteger;
import java.util.List;

/**
 * Created by jay on 2/15/16.
 */
public class OptionsMenuWindow extends BasicWindow {
    private enum EngineType {
        ATTACKER,
        DEFENDER,
        ANALYSIS
    }

    private Label mVariantLabel;
    private Label mClockLabel;
    private Label mAttackerLabel;
    private Label mAttackerConfigLabel;
    private Label mDefenderLabel;
    private Label mDefenderConfigLabel;
    private Label mAnalysisLabel;
    private Label mAnalysisConfigLabel;
    private Label mThinkTimeLabel;
    private Label mShrinkLabel;
    private Label mDestinationLabel;
    private Label mFontSizeLabel;
    private Label mNetworkAddressLabel;

    private Interactable mLastFocused;

    private LogicalScreen.TerminalCallback mTerminalCallback;
    public OptionsMenuWindow(LogicalScreen.TerminalCallback callback) {
        mTerminalCallback = callback;

        buildSettings();
    }

    private void buildSettings() {
        Panel p = new Panel();
        p.setLayoutManager(new LinearLayout());

        Label l1 = new Label("OpenTafl Options");
        p.addComponent(l1);

        Label l2 = new Label("The old Norse board game,");
        p.addComponent(l2);

        Label l3 = new Label("In an old computer style.");
        p.addComponent(l3);

        Panel optionsPanel = new Panel();
        optionsPanel.setLayoutManager(new GridLayout(3));

        Button variantSelect = new Button("Variant", this::showVariantSelectDialog);
        mVariantLabel = new Label(BuiltInVariants.rulesDescriptions.get(TerminalSettings.variant));
        optionsPanel.addComponent(variantSelect);
        optionsPanel.addComponent(TerminalUtils.newSpacer());
        optionsPanel.addComponent(mVariantLabel);


        Button clockSettingSelect = new Button("Clock setting", this::showTimeSpecDialog);
        mClockLabel = new Label(TerminalSettings.timeSpec.toString());
        optionsPanel.addComponent(clockSettingSelect);
        optionsPanel.addComponent(TerminalUtils.newSpacer());
        optionsPanel.addComponent(mClockLabel);

        Button aiDepthSelect = new Button("AI think time", this::showAiDepthEntryDialog);
        mThinkTimeLabel = new Label("" + TerminalSettings.aiThinkTime);
        optionsPanel.addComponent(aiDepthSelect);
        optionsPanel.addComponent(TerminalUtils.newSpacer());
        optionsPanel.addComponent(mThinkTimeLabel);

        // Blank line
        optionsPanel.addComponent(TerminalUtils.newSpacer());
        optionsPanel.addComponent(TerminalUtils.newSpacer());
        optionsPanel.addComponent(TerminalUtils.newSpacer());

        Button attackerSelect = new Button("Attackers", () -> showPlayerSelectDialog(true));
        mAttackerLabel = new Label(TerminalSettings.labelForPlayerType(TerminalSettings.attackers));
        optionsPanel.addComponent(attackerSelect);
        optionsPanel.addComponent(TerminalUtils.newSpacer());
        optionsPanel.addComponent(mAttackerLabel);

        Button attackerFileSelect = new Button("Attacker config", () -> {
           showFileSelectDialog(EngineType.ATTACKER);
        });

        if(TerminalSettings.attackerEngineSpec == null) {
            mAttackerConfigLabel = new Label("<none>");
        }
        else {
            mAttackerConfigLabel = new Label(TerminalSettings.attackerEngineSpec.toString());
        }

        optionsPanel.addComponent(attackerFileSelect);
        optionsPanel.addComponent(TerminalUtils.newSpacer());
        optionsPanel.addComponent(mAttackerConfigLabel);

        // Blank line
        optionsPanel.addComponent(TerminalUtils.newSpacer());
        optionsPanel.addComponent(TerminalUtils.newSpacer());
        optionsPanel.addComponent(TerminalUtils.newSpacer());

        Button defenderSelect = new Button("Defenders", () -> showPlayerSelectDialog(false));
        mDefenderLabel = new Label(TerminalSettings.labelForPlayerType(TerminalSettings.defenders));
        optionsPanel.addComponent(defenderSelect);
        optionsPanel.addComponent(TerminalUtils.newSpacer());
        optionsPanel.addComponent(mDefenderLabel);

        Button defenderFileSelect = new Button("Defender config", () -> {
            showFileSelectDialog(EngineType.DEFENDER);
        });

        if(TerminalSettings.defenderEngineSpec == null) {
            mDefenderConfigLabel = new Label("<none>");
        }
        else {
            mDefenderConfigLabel = new Label(TerminalSettings.defenderEngineSpec.toString());
        }

        optionsPanel.addComponent(defenderFileSelect);
        optionsPanel.addComponent(TerminalUtils.newSpacer());
        optionsPanel.addComponent(mDefenderConfigLabel);

        // Blank line
        optionsPanel.addComponent(TerminalUtils.newSpacer());
        optionsPanel.addComponent(TerminalUtils.newSpacer());
        optionsPanel.addComponent(TerminalUtils.newSpacer());

        Button analysisButton = new Button("Analysis engine", () -> {
            TerminalSettings.analysisEngine = !TerminalSettings.analysisEngine;
            refreshSettings();
        });
        mAnalysisLabel = new Label(TerminalSettings.analysisEngine ? "On" : "Off");

        optionsPanel.addComponent(analysisButton);
        optionsPanel.addComponent(TerminalUtils.newSpacer());
        optionsPanel.addComponent(mAnalysisLabel);

        Button analysisFileSelect = new Button("Analysis config", () -> {
            showFileSelectDialog(EngineType.ANALYSIS);
        });

        if(TerminalSettings.analysisEngineSpec == null) {
            mAnalysisConfigLabel = new Label("<none>");
        }
        else {
            mAnalysisConfigLabel = new Label(TerminalSettings.analysisEngineSpec.toString());
        }

        optionsPanel.addComponent(analysisFileSelect);
        optionsPanel.addComponent(TerminalUtils.newSpacer());
        optionsPanel.addComponent(mAnalysisConfigLabel);

        // Blank line
        optionsPanel.addComponent(TerminalUtils.newSpacer());
        optionsPanel.addComponent(TerminalUtils.newSpacer());
        optionsPanel.addComponent(TerminalUtils.newSpacer());

        Button shrinkLargeBoardsButton = new Button("Shrink large boards", () -> {
            TerminalSettings.shrinkLargeBoards = !TerminalSettings.shrinkLargeBoards;
            refreshSettings();
        });
        mShrinkLabel = new Label(TerminalSettings.shrinkLargeBoards ? "On" : "Off");

        optionsPanel.addComponent(shrinkLargeBoardsButton);
        optionsPanel.addComponent(TerminalUtils.newSpacer());
        optionsPanel.addComponent(mShrinkLabel);

        Button destinationButton = new Button("Show destination name", () -> {
            TerminalSettings.advancedDestinationRendering = !TerminalSettings.advancedDestinationRendering;
            refreshSettings();
        });
        mDestinationLabel = new Label(TerminalSettings.advancedDestinationRendering ? "On" : "Off");

        optionsPanel.addComponent(destinationButton);
        optionsPanel.addComponent(TerminalUtils.newSpacer());
        optionsPanel.addComponent(mDestinationLabel);

        Button fontSizeButton = new Button("Font size", this::showFontSizeDialog);
        mFontSizeLabel = new Label(TerminalSettings.fontSize + "pt");

        optionsPanel.addComponent(fontSizeButton);
        optionsPanel.addComponent(TerminalUtils.newSpacer());
        optionsPanel.addComponent(mFontSizeLabel);

        // Blank line
        optionsPanel.addComponent(TerminalUtils.newSpacer());
        optionsPanel.addComponent(TerminalUtils.newSpacer());
        optionsPanel.addComponent(TerminalUtils.newSpacer());

        final Button networkAddressButton = new Button("Server address", () -> {
            String newAddress = TextInputDialog.showDialog(getTextGUI(), "Server address", "Enter server address", TerminalSettings.onlineServerHost);
            if(newAddress != null) TerminalSettings.onlineServerHost = newAddress;
            refreshSettings();
        });
        mNetworkAddressLabel = new Label(TerminalSettings.onlineServerHost);

        optionsPanel.addComponent(networkAddressButton);
        optionsPanel.addComponent(TerminalUtils.newSpacer());
        optionsPanel.addComponent(mNetworkAddressLabel);

        Button backButton = new Button("Back", () -> {
            TerminalSettings.saveToFile();
            mTerminalCallback.onMenuNavigation(new MainMenuWindow(mTerminalCallback));
        });
        optionsPanel.addComponent(backButton);

        optionsPanel.addComponent(TerminalUtils.newSpacer());
        optionsPanel.addComponent(TerminalUtils.newSpacer());

        p.addComponent(optionsPanel);

        this.setComponent(p);
    }

    private void refreshSettings() {
        mVariantLabel.setText(BuiltInVariants.rulesDescriptions.get(TerminalSettings.variant));
        mClockLabel.setText(TerminalSettings.timeSpec.toString());
        mAttackerLabel.setText(TerminalSettings.labelForPlayerType(TerminalSettings.attackers));

        if(TerminalSettings.attackerEngineSpec == null) {
            mAttackerConfigLabel.setText("<none>");
        }
        else {
            mAttackerConfigLabel.setText(TerminalSettings.attackerEngineSpec.toString());
        }

        mDefenderLabel.setText(TerminalSettings.labelForPlayerType(TerminalSettings.defenders));

        if(TerminalSettings.defenderEngineSpec == null) {
            mDefenderConfigLabel.setText("<none>");
        }
        else {
            mDefenderConfigLabel.setText(TerminalSettings.defenderEngineSpec.toString());
        }

        mAnalysisLabel.setText(TerminalSettings.analysisEngine ? "On" : "Off");

        if(TerminalSettings.analysisEngineSpec == null) {
            mAnalysisConfigLabel.setText("<none>");
        }
        else {
            mAnalysisConfigLabel.setText(TerminalSettings.analysisEngineSpec.toString());
        }

        mThinkTimeLabel.setText("" + TerminalSettings.aiThinkTime);

        mShrinkLabel.setText(TerminalSettings.shrinkLargeBoards ? "On" : "Off");
        mDestinationLabel.setText(TerminalSettings.advancedDestinationRendering ? "On" : "Off");
        mFontSizeLabel.setText(TerminalSettings.fontSize + "pt");

        mNetworkAddressLabel.setText(TerminalSettings.onlineServerHost);
    }

    private void showFileSelectDialog(EngineType type) {
        FileDialogBuilder b = new FileDialogBuilder();
        b.setActionLabel("Select");

        File f = new File("engines");
        if(f.exists()) b.setSelectedFile(f);

        FileDialog d = b.build();
        File configFile = d.showDialog(getTextGUI());

        if(EngineSpec.validateEngineFile(configFile)) {
            EngineSpec spec = new EngineSpec(configFile);
            switch(type) {

                case ATTACKER:
                    TerminalSettings.attackerEngineSpec = spec;
                    break;
                case DEFENDER:
                    TerminalSettings.defenderEngineSpec = spec;
                    break;
                case ANALYSIS:
                    TerminalSettings.analysisEngineSpec = spec;
                    break;
            }
        }
        else {
            MessageDialog.showMessageDialog(getTextGUI(), "Invalid file", "File does not contain required engine elements.");
        }

        refreshSettings();
    }

    private void showTimeSpecDialog() {
        TimeEntryDialog d = new TimeEntryDialog("Clock settings");
        d.showDialog(getTextGUI());
        TerminalSettings.timeSpec = d.timeSpec;

        refreshSettings();
    }

    private void showAiDepthEntryDialog() {
        List<String> lines = TerminalTextUtils.getWordWrappedText(50,
                "The maximum time in seconds per AI move, or 0 to let the AI" +
                        " decide its timing based on the game clock.");
        String descriptionString = "";
        for(String s : lines) {
            descriptionString += s + "\n";
        }
        BigInteger searchdepth = TextInputDialog.showNumberDialog(
                getTextGUI(),
                "AI think time",
                descriptionString,
                "" + TerminalSettings.aiThinkTime);
        int intDepth = searchdepth.intValue();
        TerminalSettings.aiThinkTime = intDepth;

        refreshSettings();
    }

    private void showFontSizeDialog() {
        List<String> lines = TerminalTextUtils.getWordWrappedText(50,
                "The terminal font size. (Setting takes effect only after restart.)");
        String descriptionString = "";
        for(String s : lines) {
            descriptionString += s + "\n";
        }
        BigInteger fontSize = TextInputDialog.showNumberDialog(
                getTextGUI(),
                "Font size (pt):",
                descriptionString,
                "" + TerminalSettings.fontSize);
        TerminalSettings.fontSize = fontSize.intValue();

        refreshSettings();
    }

    private void showVariantSelectDialog() {
        Runnable[] variants = new Runnable[BuiltInVariants.rulesDescriptions.size()];
        for(int i = 0; i < variants.length; i++) {
            final String name = BuiltInVariants.rulesDescriptions.get(i);
            final int index = i;
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    TerminalSettings.variant = index;

                    refreshSettings();
                }

                @Override
                public String toString() {
                    return name;
                }
            };

            variants[i] = r;
        }

        ActionListDialog.showDialog(this.getTextGUI(), "Select variant", "", variants);
    }

    private void showPlayerSelectDialog(final boolean attackers) {
        ActionListDialog.showDialog(this.getTextGUI(), "Select player type", "",
                new Runnable() {

                    @Override
                    public void run() {
                        if(attackers) TerminalSettings.attackers = TerminalSettings.HUMAN;
                        else TerminalSettings.defenders = TerminalSettings.HUMAN;

                        refreshSettings();
                    }

                    @Override
                    public String toString() {
                        return "Human";
                    }
                },

                new Runnable() {

                    @Override
                    public void run() {
                        if(attackers) TerminalSettings.attackers = TerminalSettings.AI;
                        else TerminalSettings.defenders = TerminalSettings.AI;

                        refreshSettings();
                    }

                    @Override
                    public String toString() {
                        return "AI";
                    }
                },

                new Runnable() {

                    @Override
                    public void run() {
                        if(attackers) TerminalSettings.attackers = TerminalSettings.ENGINE;
                        else TerminalSettings.defenders = TerminalSettings.ENGINE;

                        refreshSettings();
                    }

                    @Override
                    public String toString() {
                        return "External Engine";
                    }
                }
        );
    }
}
