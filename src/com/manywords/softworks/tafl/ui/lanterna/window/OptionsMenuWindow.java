package com.manywords.softworks.tafl.ui.lanterna.window;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TerminalTextUtils;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.*;
import com.manywords.softworks.tafl.rules.BuiltInVariants;
import com.manywords.softworks.tafl.ui.lanterna.screen.UiScreen;
import com.manywords.softworks.tafl.ui.lanterna.settings.TerminalSettings;
import com.manywords.softworks.tafl.ui.player.external.engine.ExternalEngineHost;

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

    private Interactable mLastFocused;

    private UiScreen.TerminalCallback mTerminalCallback;
    public OptionsMenuWindow(UiScreen.TerminalCallback callback) {
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

        Button variantSelect = new Button("Variant", new Runnable() {
            @Override
            public void run() {
                showVariantSelectDialog();
            }
        });
        mVariantLabel = new Label(BuiltInVariants.rulesDescriptions.get(TerminalSettings.variant));
        optionsPanel.addComponent(variantSelect);
        optionsPanel.addComponent(newSpacer());
        optionsPanel.addComponent(mVariantLabel);


        Button clockSettingSelect = new Button("Clock setting", new Runnable() {
            @Override
            public void run() {
                showTimeSpecDialog();
            }
        });
        mClockLabel = new Label(TerminalSettings.timeSpec.toString());
        optionsPanel.addComponent(clockSettingSelect);
        optionsPanel.addComponent(newSpacer());
        optionsPanel.addComponent(mClockLabel);

        // Blank line
        optionsPanel.addComponent(newSpacer());
        optionsPanel.addComponent(newSpacer());
        optionsPanel.addComponent(newSpacer());

        Button attackerSelect = new Button("Attackers", new Runnable() {
            @Override
            public void run() {
                showPlayerSelectDialog(true);
            }
        });
        mAttackerLabel = new Label(TerminalSettings.labelForPlayerType(TerminalSettings.attackers));
        optionsPanel.addComponent(attackerSelect);
        optionsPanel.addComponent(newSpacer());
        optionsPanel.addComponent(mAttackerLabel);

        Button attackerFileSelect = new Button("Attacker config", () -> {
           showFileSelectDialog(EngineType.ATTACKER);
        });

        if(TerminalSettings.attackerEngineFile == null) {
            mAttackerConfigLabel = new Label("<none>");
        }
        else {
            mAttackerConfigLabel = new Label(TerminalSettings.attackerEngineFile.getName());
        }

        optionsPanel.addComponent(attackerFileSelect);
        optionsPanel.addComponent(newSpacer());
        optionsPanel.addComponent(mAttackerConfigLabel);

        // Blank line
        optionsPanel.addComponent(newSpacer());
        optionsPanel.addComponent(newSpacer());
        optionsPanel.addComponent(newSpacer());

        Button defenderSelect = new Button("Defenders", new Runnable() {
            @Override
            public void run() {
                showPlayerSelectDialog(false);
            }
        });
        mDefenderLabel = new Label(TerminalSettings.labelForPlayerType(TerminalSettings.defenders));
        optionsPanel.addComponent(defenderSelect);
        optionsPanel.addComponent(newSpacer());
        optionsPanel.addComponent(mDefenderLabel);

        Button defenderFileSelect = new Button("Defender config", () -> {
            showFileSelectDialog(EngineType.DEFENDER);
        });

        if(TerminalSettings.defenderEngineFile == null) {
            mDefenderConfigLabel = new Label("<none>");
        }
        else {
            mDefenderConfigLabel = new Label(TerminalSettings.defenderEngineFile.getName());
        }

        optionsPanel.addComponent(defenderFileSelect);
        optionsPanel.addComponent(newSpacer());
        optionsPanel.addComponent(mDefenderConfigLabel);

        // Blank line
        optionsPanel.addComponent(newSpacer());
        optionsPanel.addComponent(newSpacer());
        optionsPanel.addComponent(newSpacer());

        Button analysisButton = new Button("Analysis engine", new Runnable() {
            @Override
            public void run() {
                TerminalSettings.analysisEngine = !TerminalSettings.analysisEngine;
                refreshSettings();
            }
        });
        mAnalysisLabel = new Label(TerminalSettings.analysisEngine ? "On" : "Off");

        optionsPanel.addComponent(analysisButton);
        optionsPanel.addComponent(newSpacer());
        optionsPanel.addComponent(mAnalysisLabel);

        Button analysisFileSelect = new Button("Analysis config", () -> {
            showFileSelectDialog(EngineType.ANALYSIS);
        });

        if(TerminalSettings.analysisEngineFile == null) {
            mAnalysisConfigLabel = new Label("<none>");
        }
        else {
            mAnalysisConfigLabel = new Label(TerminalSettings.analysisEngineFile.getName());
        }

        optionsPanel.addComponent(analysisFileSelect);
        optionsPanel.addComponent(newSpacer());
        optionsPanel.addComponent(mAnalysisConfigLabel);


        // Blank line
        optionsPanel.addComponent(newSpacer());
        optionsPanel.addComponent(newSpacer());
        optionsPanel.addComponent(newSpacer());

        Button aiDepthSelect = new Button("AI think time", new Runnable() {
            @Override
            public void run() {
                showAiDepthEntryDialog();
            }
        });
        mThinkTimeLabel = new Label("" + TerminalSettings.aiThinkTime);
        optionsPanel.addComponent(aiDepthSelect);
        optionsPanel.addComponent(newSpacer());
        optionsPanel.addComponent(mThinkTimeLabel);

        Button backButton = new Button("Back", new Runnable() {
            @Override
            public void run() {
                TerminalSettings.saveToFile();
                mTerminalCallback.onMenuNavigation(new MainMenuWindow(mTerminalCallback));
            }
        });
        optionsPanel.addComponent(backButton);

        optionsPanel.addComponent(newSpacer());
        optionsPanel.addComponent(newSpacer());

        p.addComponent(optionsPanel);

        this.setComponent(p);
    }

    private void refreshSettings() {
        mVariantLabel.setText(BuiltInVariants.rulesDescriptions.get(TerminalSettings.variant));
        mClockLabel.setText(TerminalSettings.timeSpec.toString());
        mAttackerLabel.setText(TerminalSettings.labelForPlayerType(TerminalSettings.attackers));

        if(TerminalSettings.attackerEngineFile == null) {
            mAttackerConfigLabel.setText("<none>");
        }
        else {
            mAttackerConfigLabel.setText(TerminalSettings.attackerEngineFile.getName());
        }

        mDefenderLabel.setText(TerminalSettings.labelForPlayerType(TerminalSettings.defenders));

        if(TerminalSettings.defenderEngineFile == null) {
            mDefenderConfigLabel.setText("<none>");
        }
        else {
            mDefenderConfigLabel.setText(TerminalSettings.defenderEngineFile.getName());
        }

        mAnalysisLabel.setText(TerminalSettings.analysisEngine ? "On" : "Off");

        if(TerminalSettings.analysisEngineFile == null) {
            mAnalysisConfigLabel.setText("<none>");
        }
        else {
            mAnalysisConfigLabel.setText(TerminalSettings.analysisEngineFile.getName());
        }

        mThinkTimeLabel.setText("" + TerminalSettings.aiThinkTime);
    }

    private EmptySpace newSpacer() {
        return new EmptySpace(new TerminalSize(4, 1));
    }

    private void showFileSelectDialog(EngineType type) {
        FileDialogBuilder b = new FileDialogBuilder();
        b.setActionLabel("Select");

        File f = new File("engines");
        if(f.exists()) b.setSelectedFile(f);

        FileDialog d = b.build();
        File configFile = d.showDialog(getTextGUI());

        if(ExternalEngineHost.validateEngineFile(configFile)) {
            switch(type) {

                case ATTACKER:
                    TerminalSettings.attackerEngineFile = configFile;
                    break;
                case DEFENDER:
                    TerminalSettings.defenderEngineFile = configFile;
                    break;
                case ANALYSIS:
                    TerminalSettings.analysisEngineFile = configFile;
                    break;
            }
        }
        else {
            MessageDialog.showMessageDialog(getTextGUI(), "Invalid file", "File does not contain required engine elements.");
        }

        refreshSettings();
    }

    private void showTimeSpecDialog() {
        new TimeEntryDialog("Clock settings").showDialog(getTextGUI());

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
                        if(attackers) TerminalSettings.attackers = TerminalSettings.HUMAN;
                        else TerminalSettings.defenders = TerminalSettings.HUMAN;

                        refreshSettings();
                    }

                    @Override
                    public String toString() {
                        return "Network";
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
