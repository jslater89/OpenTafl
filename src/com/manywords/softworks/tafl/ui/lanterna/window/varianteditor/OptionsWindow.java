package com.manywords.softworks.tafl.ui.lanterna.window.varianteditor;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.ListSelectDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.gui2.dialogs.TextInputDialog;
import com.manywords.softworks.tafl.Log;
import com.manywords.softworks.tafl.notation.NotationParseException;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.*;
import com.manywords.softworks.tafl.ui.Ansi;
import com.manywords.softworks.tafl.ui.HumanReadableRulesPrinter;
import com.manywords.softworks.tafl.ui.lanterna.TerminalUtils;
import com.manywords.softworks.tafl.ui.lanterna.component.FocusableBasicWindow;
import com.manywords.softworks.tafl.ui.lanterna.component.ScrollingMessageDialog;
import com.manywords.softworks.tafl.ui.lanterna.screen.LogicalScreen;
import com.manywords.softworks.tafl.ui.lanterna.theme.TerminalThemeConstants;

import java.io.*;
import java.math.BigInteger;
import java.util.List;

/**
 * Created by jay on 4/13/17.
 */
public class OptionsWindow extends FocusableBasicWindow {
    private Label mStatus;
    private OptionsWindowHost mHost;

    public OptionsWindow(LogicalScreen.TerminalCallback callback, OptionsWindowHost host) {
        super("Options", callback);
        mHost = host;
        buildWindow();
    }

    private void buildWindow() {
        Panel p = new Panel();
        p.setLayoutManager(new LinearLayout(Direction.VERTICAL));

        Panel firstRow = new Panel();
        firstRow.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
        mStatus = new Label("Status: ");
        firstRow.addComponent(mStatus);
        p.addComponent(firstRow);

//        Panel rowSpacer = new Panel();
//        rowSpacer.addComponent(TerminalUtils.newSpacer());
//        p.addComponent(rowSpacer);

        Panel secondRow = new Panel();
        secondRow.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));

        Button newLayout = new Button("New rules", () -> {
            BigInteger newSize = TextInputDialog.showNumberDialog(
                    getTextGUI(),
                    "New board size",
                    "Enter the side length of the new board.",
                    "9"
            );

            if(newSize != null) {
                int size = newSize.intValue();
                if(size % 2 != 1) size += 1;
                size = Math.max(5, Math.min(17, size));
                mHost.newLayout(size);
            }
        });
        secondRow.addComponent(newLayout);

        Button loadStatic = new Button("Load static", () -> {
            List<Rules> builtins = Variants.builtinRules;
            Rules[] r = new Rules[builtins.size()];

            ListSelectDialogBuilder<Rules> builder = new ListSelectDialogBuilder<>();
            builder.addListItems(builtins.toArray(r));
            builder.setTitle("Select variant");
            builder.setCanCancel(true);
            builder.setListBoxSize(new TerminalSize(40, 20));

            Rules selected = builder.build().showDialog(getTextGUI());
            mHost.loadRules(selected);
        });
        secondRow.addComponent(loadStatic);

        Button loadUser = new Button("Load user", () -> {
            File f = TerminalUtils.showFileChooserDialog(getTextGUI(), "Load rule set", "Load", new File("user-rules"));

            if(f != null) {
                if(f.exists()) {
                    try {
                        BufferedReader r = new BufferedReader(new FileReader(f));
                        String rulesString = r.readLine();

                        Rules rules = RulesSerializer.loadRulesRecord(rulesString);
                        mHost.loadRules(rules);
                    }
                    catch (FileNotFoundException e) {
                        // Already caught below
                    }
                    catch (IOException e) {
                        MessageDialog.showMessageDialog(getTextGUI(), "Error", "Failed to read file");
                        Log.stackTrace(Log.Level.NORMAL, e);
                    }
                    catch (NotationParseException e) {
                        MessageDialog.showMessageDialog(getTextGUI(), "Error", "Failed to parse file");
                        Log.stackTrace(Log.Level.NORMAL, e);
                    }
                }
                else {
                    MessageDialog.showMessageDialog(getTextGUI(), "Error", "File not found");
                }
            }
        });
        secondRow.addComponent(loadUser);

        p.addComponent(secondRow);

        Panel thirdRow = new Panel();
        thirdRow.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));

        Button save = new Button("Save rules", () -> mHost.saveRules());
        thirdRow.addComponent(save);

        Button help = new Button("Help", () -> {
            ScrollingMessageDialog dialog = new ScrollingMessageDialog("OpenTafl Variant Editor Help", HELP, MessageDialogButton.Close);
            dialog.setSize(new TerminalSize(Math.min(70, getTextGUI().getScreen().getTerminalSize().getColumns() - 2), 30));
            dialog.setHints(TerminalThemeConstants.CENTERED_MODAL);
            dialog.showDialog(getTextGUI());
        });
        thirdRow.addComponent(help);

        Button quit = new Button("Quit", () -> mHost.quit());
        thirdRow.addComponent(quit);

        p.addComponent(thirdRow);

        Panel fourthRow = new Panel();
        fourthRow.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
        Button readableRules = new Button("Human-readable rules", () -> {
            ScrollingMessageDialog d = new ScrollingMessageDialog(
                    "Rules",
                    HumanReadableRulesPrinter.getHumanReadableRules(mHost.getRules()),
                    MessageDialogButton.Close);
            d.setSize(new TerminalSize(Math.min(70, getTextGUI().getScreen().getTerminalSize().getColumns() - 2), 30));
            d.setHints(TerminalThemeConstants.CENTERED_MODAL);
            d.showDialog(getTextGUI());
        });
        fourthRow.addComponent(readableRules);

        Button deleteRules = new Button("Delete rules", () -> {
            File f = TerminalUtils.showFileChooserDialog(getTextGUI(), "Delete rules", "Delete", new File("user-rules"));

            if(f != null) {
                if(f.exists()) {
                    if(f.getName().endsWith(".otr")) {
                        MessageDialogButton result = MessageDialog.showMessageDialog(getTextGUI(), "Confirm delete", "This cannot be undone. Are you sure?", MessageDialogButton.Yes, MessageDialogButton.No);

                        if (result.equals(MessageDialogButton.Yes)) {
                            f.delete();
                            Variants.reloadRules();
                        }
                    }
                    else {
                        MessageDialog.showMessageDialog(getTextGUI(), "Wrong file type", "Only OpenTafl Rules (.otr) files can be deleted here.", MessageDialogButton.OK);
                    }
                }
                else {
                    MessageDialog.showMessageDialog(getTextGUI(), "File not found", "Cannot delete a non-existent file.", MessageDialogButton.OK);
                }
            }
        });
        fourthRow.addComponent(deleteRules);

        p.addComponent(fourthRow);


        setComponent(p);
    }

    public void updateStatus(Coord space, SpaceType spaceType, char taflman) {
        String status = "Status: ";
        status += space;

        if(spaceType != SpaceType.NONE || taflman != Taflman.EMPTY) {
            status += " ";

            if(spaceType != SpaceType.NONE) {
                status += "(" + spaceType + ") ";
            }

            if(taflman != Taflman.EMPTY) {
                status += (Taflman.getPackedSide(taflman) == Taflman.SIDE_ATTACKERS ? "atk " : "def ");

                switch(Taflman.getPackedType(taflman)) {
                    case Taflman.TYPE_KING:
                        status += "king";
                        break;
                    case Taflman.TYPE_KNIGHT:
                        status += "knight";
                        break;
                    case Taflman.TYPE_COMMANDER:
                        status += "commander";
                        break;
                    default:
                        status += "taflman";
                }
            }
        }

        mStatus.setText(status);
    }

    public interface OptionsWindowHost {
        void newLayout(int dimension);
        void loadRules(Rules r);
        void saveRules();
        void quit();
        Rules getRules();
    }

    private static final String HELP =
            "The OpenTafl variant editor allows end users to combine OpenTafl's supported rules " +
                    "in any combination, to create their own tafl variants. The interface consists " +
                    "of three windows. The Tab key cycles forward. Shift+Tab cycles backward. The " +
                    "currently focused window's title is capitalized and underlined." +
                    "\n\n" +
                    "The first window, in the top left, is the board editor window. With the board editor, " +
                    "users may place taflmen and change the layout of special spaces on the board. Use the arrow " +
                    "keys or WASD to navigate. Use the 'p' key to place a taflman. Repeated presses of the " +
                    "'p' key over a placed taflman will change its type. Pressing the 'i' key over a placed " +
                    "taflman will change its side. Use the 't' key to cycle spaces between the five possible " +
                    "space types: normal, corner, center, attacker fort, and defender fort." +
                    "\n\n" +
                    "The next window is the rules editor window. A full description of its properties can be found " +
                    "in the OpenTafl Notation specification, a link to which is located in your README file. A " +
                    "short description can be found at the end of this help message." +
                    "\n\n" +
                    "The final window is the options window. At its top, the status line indicates where the board " +
                    "editing cursor is positioned, and what kind of taflman and special space are below the cursor " +
                    "position. The other buttons in the options window allow for loading built-in or user-defined " +
                    "rules, saving the currently-displayed set of rules as a user-defined rule set, showing this " +
                    "help message, returning to the main menu, and displaying the human-readable version of the " +
                    "currently-displayed rules. Finally, using the 'Delete rules' button, the user may select " +
                    "unwanted rules variant files for deletion." +
                    "\n\n" +
                    Ansi.UNDERLINE + "Rules window help" + Ansi.UNDERLINE_OFF +
                    "\n\n" +
                    "Name: the name of the variant, which will be displayed above the board during games." +
                    "\n\n" +
                    "Escape: whether the king's objective is to reach the corners or the edges." +
                    "\n\n" +
                    "Attackers first: whether the attacking side moves first." +
                    "\n\n" +
                    "Surrounding fatal: whether surrounding a side causes that side to lose." +
                    "\n\n" +
                    "Edge fort escape: if checked, the king may escape if he has at least one potential " +
                    "move while surrounded by an invincible fort against the edge of the board." +
                    "\n\n" +
                    "Shieldwall captures: whether to allow Copenhagen-style shieldwall captures. Weak shieldwalls " +
                    "must be capped on both sides by a taflman. Strong shieldwalls may be capped by corners." +
                    "\n\n" +
                    "Flanking only: if checked, shieldwall captures may only be closed by a move to the edge of the " +
                    "board. (One of the capping pieces must be the final move.)" +
                    "\n\n" +
                    "Threefold result: the result when a a board position is repeated for the third time. 'Win' and " +
                    "'Loss' mean that the player who made the move to enter the repeated board position wins or loses, " +
                    "respectively." +
                    "\n\n" +
                    "King strength: how difficult the king is to capture. A strong king must be surrounded on four " +
                    "sides by hostile spaces. A (strong) center king is strong when adjacent to a center space, and " +
                    "weak otherwise. A middleweight king must be surrounded on all sides by hostile spaces, even if the " +
                    "number of adjacent spaces is smaller than four. A weak king is captured as an ordinary taflman." +
                    "\n\n" +
                    "King armed: whether the king can participate in captures. Armed kings may take part in captures " +
                    "at all times. Hammer kings may only take part in captures if they are the moving piece. Anvil kings " +
                    "may be captured against, but cannot make captures by moving. Unarmed kings do not take part in " +
                    "captures." +
                    "\n\n" +
                    "King, knight, commander jumps: the jump mode for special piece types. 'Jump' denotes a piece which" +
                    "may jump over enemy pieces without capturing them. 'Capture' denotes a piece which may jump over " +
                    "enemy pieces, capturing them in the process. 'Restricted' denotes a piece which may jump over enemy " +
                    "pieces, but only if jumping from or landing on a throne, corner, or fort space." +
                    "\n\n" +
                    "Berserk mode: in berserk mode, the player who makes a capture has a chance to move again. 'Capture' " +
                    "means that the capturing player must make another move if and only if another capture is possible. " +
                    "'Any move' means that the capturing player must make some move after a capture, whether a second " +
                    "capture or not." +
                    "\n\n" +
                    "Help for speed limits and special space properties is available inside the respective dialogs.";
}
