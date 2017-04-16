package com.manywords.softworks.tafl.ui;

import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.notation.TaflmanCodes;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Taflman;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by jay on 4/8/16.
 */
public class HumanReadableRulesPrinter {
    public static String getHumanReadableRules(Rules r) {
        String rules = "";
        String otrString = r.getOTRString(false);
        int ruleNumber = 1;

        rules += "The tafl games are a collection of Norse board games dating to the Viking age and earlier. They were commonly played in " +
                "the time before chess arrived in Northern Europe in the 1100s. All tafl games have a few features in common: they are played " +
                "on square boards of odd-numbered size, with asymmetric forces. The defenders, or the king's side, start in the center of the " +
                "board, and have escape as their objective: the king must reach either an edge space or a corner space, depending on the rules " +
                "in question. The attackers, or the besieging side, start around the edges of the board, and must prevent the king's escape. The " +
                "following rules are for this variant: " + r.getName() + " " + r.boardSize + "x" + r.boardSize + ".\n\n";

        rules += ruleNumber++ +". The game is played on a board of " + r.boardSize + "x" + r.boardSize + " spaces, with " + r.getDefenders().getStartingTaflmen().size() +
                " defenders (including one king, marked by the '+' symbol) and " +
                r.getAttackers().getStartingTaflmen().size() + " attackers.\n\n";

        Set<Coord> centers = r.getCenterSpaces();
        Set<Coord> corners = r.getCornerSpaces();
        Set<Coord> attackerForts = r.getAttackerForts();
        Set<Coord> defenderForts = r.getDefenderForts();

        String centerString = "center space";
        String centerVerb = "is ";
        if(centers.size() > 1) {
            centerString = "center spaces";
            centerVerb = "are ";
        }

        if(centers.size() > 0) {
            rules += ruleNumber++ + ". The " + centerString + " " + centerVerb + "known as the throne. The throne is marked on the board with " +
                    "circles. ";

            List<Character> unallocatedTypes = new ArrayList<>();
            unallocatedTypes.add('t');
            unallocatedTypes.add('c');
            unallocatedTypes.add('n');
            unallocatedTypes.add('k');
            unallocatedTypes.add('T');
            unallocatedTypes.add('C');
            unallocatedTypes.add('N');
            unallocatedTypes.add('K');
            List<Character> allocatedTypes = new ArrayList<>();

            // The following taflmen may freely move past and stop on the throne.
            // if in cenp, cens, and cenre.
            boolean[] unrestrictedTaflmen = new boolean[Rules.TAFLMAN_TYPE_COUNT];

            for(char c : unallocatedTypes) {
                if(allocatedTypes.contains(c)) continue;

                String cString = "" + c;
                if(getValueForTag(otrString, "cenp").contains(cString)
                        && getValueForTag(otrString, "cens").contains(cString)
                        && getValueForTag(otrString, "cenre").contains(cString)) {
                    allocatedTypes.add(c);
                    unrestrictedTaflmen[TaflmanCodes.getIndexForChar(c)] = true;
                }
            }

            // The following taflmen may freely move past and stop on the throne, if and only if they begin their turn
            // on a throne space.
            // if in cenp and cens, but not cenre.
            boolean[] moveStopNoEntryTaflmen = new boolean[Rules.TAFLMAN_TYPE_COUNT];

            for(char c : unallocatedTypes) {
                if(allocatedTypes.contains(c)) continue;

                String cString = "" + c;
                if(getValueForTag(otrString, "cenp").contains(cString)
                        && getValueForTag(otrString, "cens").contains(cString)
                        && !getValueForTag(otrString, "cenre").contains(cString)) {
                    allocatedTypes.add(c);
                    moveStopNoEntryTaflmen[TaflmanCodes.getIndexForChar(c)] = true;
                }
            }

            // The following taflmen may freely move past the throne, but may not stop on it.
            // if in cenp and cenre, but not cens.
            boolean[] moveEntryTaflmen = new boolean[Rules.TAFLMAN_TYPE_COUNT];

            for(char c : unallocatedTypes) {
                if(allocatedTypes.contains(c)) continue;

                String cString = "" + c;
                if(getValueForTag(otrString, "cenp").contains(cString)
                        && !getValueForTag(otrString, "cens").contains(cString)
                        && getValueForTag(otrString, "cenre").contains(cString)) {
                    allocatedTypes.add(c);
                    moveEntryTaflmen[TaflmanCodes.getIndexForChar(c)] = true;
                }
            }

            // The following taflmen may freely move past the throne, if and only if they begin their turn on a throne
            // space. They may never stop on the throne.
            // if in cenp, but not cenre or cens.
            boolean[] moveNoEntryTaflmen = new boolean[Rules.TAFLMAN_TYPE_COUNT];
            for(char c : unallocatedTypes) {
                if(allocatedTypes.contains(c)) continue;

                String cString = "" + c;
                if(getValueForTag(otrString, "cenp").contains(cString)
                        && !getValueForTag(otrString, "cens").contains(cString)
                        && !getValueForTag(otrString, "cenre").contains(cString)) {
                    allocatedTypes.add(c);
                    moveNoEntryTaflmen[TaflmanCodes.getIndexForChar(c)] = true;
                }
            }

            // The following taflmen may not move past or stop on the throne.
            // if in neither cenp or cens.
            boolean[] restrictedTaflmen = new boolean[Rules.TAFLMAN_TYPE_COUNT];
            for(char c : unallocatedTypes) {
                if(allocatedTypes.contains(c)) continue;

                String cString = "" + c;
                if(!getValueForTag(otrString, "cenp").contains(cString)
                        && !getValueForTag(otrString, "cens").contains(cString)
                        && !getValueForTag(otrString, "cenre").contains(cString)) {
                    allocatedTypes.add(c);
                    restrictedTaflmen[TaflmanCodes.getIndexForChar(c)] = true;
                }
            }

            String unrestrictedString = getTaflmanTypeStringFromSpec(r, RulesSerializer.getStringForTaflmanTypeList(unrestrictedTaflmen));
            String moveStopNoEntryString = getTaflmanTypeStringFromSpec(r, RulesSerializer.getStringForTaflmanTypeList(moveStopNoEntryTaflmen));
            String moveEntryString = getTaflmanTypeStringFromSpec(r, RulesSerializer.getStringForTaflmanTypeList(moveEntryTaflmen));
            String moveNoEntryString = getTaflmanTypeStringFromSpec(r, RulesSerializer.getStringForTaflmanTypeList(moveNoEntryTaflmen));
            String restrictedString = getTaflmanTypeStringFromSpec(r, RulesSerializer.getStringForTaflmanTypeList(restrictedTaflmen));

            if(!unrestrictedString.contains("no taflmen")) {
                rules += "The following taflmen may freely move through or stop on the throne: " + unrestrictedString + ". ";
            }

            if(!moveStopNoEntryString.contains("no taflmen")) {
                rules += "The following taflmen may move through or stop on the throne, but may not enter it from the outside: " + moveStopNoEntryString + ". ";
            }

            if(!moveEntryString.contains("no taflmen")) {
                rules += "The following taflmen may freely move through the throne, but may not stop on it: " + moveEntryString + ". ";
            }

            if(!moveNoEntryString.contains("no taflmen")) {
                rules += "The following taflmen may move through the throne, but may not stop on a throne space or enter the throne from the outside: " + moveNoEntryString + ". ";
            }

            if(!restrictedString.contains("no taflmen")) {
                rules += "The following taflmen may neither move through nor stop on the throne: " + restrictedString + ". ";
            }

            String hostile = getTaflmanTypeStringForTag(r, "cenh");
            String hostileEmpty = getTaflmanTypeStringForTag(r, "cenhe");

            if(!hostile.isEmpty() && !hostile.contains("no taflmen")) {
                rules += "The throne is hostile to the following taflmen: " + hostile + ". ";
            }

            if(!hostileEmpty.isEmpty() && !hostileEmpty.contains("no taflmen")) {
                rules += "Additionally, the throne is hostile to the following taflmen when empty: " + hostileEmpty + ". ";
            }

            rules += "\n\n";
        }

        if(corners.size() > 0) {
            rules += ruleNumber++ + ". The corner spaces are marked on the board with asterisks. Only the king may stop on a corner space. ";

            String hostile = getTaflmanTypeStringForTag(r, "corh");

            if(!hostile.isEmpty() && !hostile.contains("no taflmen")) {
                rules += "The corners are hostile to the following taflmen: " + hostile + ". ";
            }

            rules += "\n\n";
        }

        if(attackerForts.size() > 0) {
            rules += ruleNumber++ + ". Certain spaces are attacker fortresses. They are marked on the board with " +
                    "dots. ";

            List<Character> unallocatedTypes = new ArrayList<>();
            unallocatedTypes.add('t');
            unallocatedTypes.add('c');
            unallocatedTypes.add('n');
            unallocatedTypes.add('k');
            unallocatedTypes.add('T');
            unallocatedTypes.add('C');
            unallocatedTypes.add('N');
            unallocatedTypes.add('K');
            List<Character> allocatedTypes = new ArrayList<>();

            // The following taflmen may freely move past and stop on the throne.
            // if in cenp, cens, and cenre.
            boolean[] unrestrictedTaflmen = new boolean[Rules.TAFLMAN_TYPE_COUNT];

            for(char c : unallocatedTypes) {
                if(allocatedTypes.contains(c)) continue;

                String cString = "" + c;
                if(getValueForTag(otrString, "aforp").contains(cString)
                        && getValueForTag(otrString, "afors").contains(cString)
                        && getValueForTag(otrString, "aforre").contains(cString)) {
                    allocatedTypes.add(c);
                    unrestrictedTaflmen[TaflmanCodes.getIndexForChar(c)] = true;
                }
            }

            // The following taflmen may freely move past and stop on the throne, if and only if they begin their turn
            // on a throne space.
            // if in cenp and cens, but not cenre.
            boolean[] moveStopNoEntryTaflmen = new boolean[Rules.TAFLMAN_TYPE_COUNT];

            for(char c : unallocatedTypes) {
                if(allocatedTypes.contains(c)) continue;

                String cString = "" + c;
                if(getValueForTag(otrString, "aforp").contains(cString)
                        && getValueForTag(otrString, "afors").contains(cString)
                        && !getValueForTag(otrString, "aforre").contains(cString)) {
                    allocatedTypes.add(c);
                    moveStopNoEntryTaflmen[TaflmanCodes.getIndexForChar(c)] = true;
                }
            }

            // The following taflmen may freely move past the throne, but may not stop on it.
            // if in cenp and cenre, but not cens.
            boolean[] moveEntryTaflmen = new boolean[Rules.TAFLMAN_TYPE_COUNT];

            for(char c : unallocatedTypes) {
                if(allocatedTypes.contains(c)) continue;

                String cString = "" + c;
                if(getValueForTag(otrString, "aforp").contains(cString)
                        && !getValueForTag(otrString, "afors").contains(cString)
                        && getValueForTag(otrString, "aforre").contains(cString)) {
                    allocatedTypes.add(c);
                    moveEntryTaflmen[TaflmanCodes.getIndexForChar(c)] = true;
                }
            }

            // The following taflmen may freely move past the throne, if and only if they begin their turn on a throne
            // space. They may never stop on the throne.
            // if in cenp, but not cenre or cens.
            boolean[] moveNoEntryTaflmen = new boolean[Rules.TAFLMAN_TYPE_COUNT];
            for(char c : unallocatedTypes) {
                if(allocatedTypes.contains(c)) continue;

                String cString = "" + c;
                if(getValueForTag(otrString, "aforp").contains(cString)
                        && !getValueForTag(otrString, "afors").contains(cString)
                        && !getValueForTag(otrString, "aforre").contains(cString)) {
                    allocatedTypes.add(c);
                    moveNoEntryTaflmen[TaflmanCodes.getIndexForChar(c)] = true;
                }
            }

            // The following taflmen may not move past or stop on the throne.
            // if in neither cenp or cens.
            boolean[] restrictedTaflmen = new boolean[Rules.TAFLMAN_TYPE_COUNT];
            for(char c : unallocatedTypes) {
                if(allocatedTypes.contains(c)) continue;

                String cString = "" + c;
                if(!getValueForTag(otrString, "aforp").contains(cString)
                        && !getValueForTag(otrString, "afors").contains(cString)
                        && !getValueForTag(otrString, "aforre").contains(cString)) {
                    allocatedTypes.add(c);
                    restrictedTaflmen[TaflmanCodes.getIndexForChar(c)] = true;
                }
            }

            String unrestrictedString = getTaflmanTypeStringFromSpec(r, RulesSerializer.getStringForTaflmanTypeList(unrestrictedTaflmen));
            String moveStopNoEntryString = getTaflmanTypeStringFromSpec(r, RulesSerializer.getStringForTaflmanTypeList(moveStopNoEntryTaflmen));
            String moveEntryString = getTaflmanTypeStringFromSpec(r, RulesSerializer.getStringForTaflmanTypeList(moveEntryTaflmen));
            String moveNoEntryString = getTaflmanTypeStringFromSpec(r, RulesSerializer.getStringForTaflmanTypeList(moveNoEntryTaflmen));
            String restrictedString = getTaflmanTypeStringFromSpec(r, RulesSerializer.getStringForTaflmanTypeList(restrictedTaflmen));

            if(!unrestrictedString.contains("no taflmen")) {
                rules += "The following taflmen may freely move through or stop on the attacker forts: " + unrestrictedString + ". ";
            }

            if(!moveStopNoEntryString.contains("no taflmen")) {
                rules += "The following taflmen may move through or stop on the attacker forts, but may not enter them from the outside: " + moveStopNoEntryString + ". ";
            }

            if(!moveEntryString.contains("no taflmen")) {
                rules += "The following taflmen may freely move through the attacker forts, but may not stop on them: " + moveEntryString + ". ";
            }

            if(!moveNoEntryString.contains("no taflmen")) {
                rules += "The following taflmen may move through the attacker forts, but may not stop on an attacker fort space or enter the attacker forts from the outside: " + moveNoEntryString + ". ";
            }

            if(!restrictedString.contains("no taflmen")) {
                rules += "The following taflmen may neither move through nor stop on the attacker forts: " + restrictedString + ". ";
            }

            String hostile = getTaflmanTypeStringForTag(r, "aforh");

            if(!hostile.isEmpty() && !hostile.contains("no taflmen")) {
                rules += "The attacker forts are hostile to the following taflmen: " + hostile + ". ";
            }

            rules += "\n\n";
        }

        if(defenderForts.size() > 0) {
            rules += ruleNumber++ + ". Certain spaces are defender fortresses. They are marked on the board with " +
                    "dashes. ";

            List<Character> unallocatedTypes = new ArrayList<>();
            unallocatedTypes.add('t');
            unallocatedTypes.add('c');
            unallocatedTypes.add('n');
            unallocatedTypes.add('k');
            unallocatedTypes.add('T');
            unallocatedTypes.add('C');
            unallocatedTypes.add('N');
            unallocatedTypes.add('K');
            List<Character> allocatedTypes = new ArrayList<>();

            // The following taflmen may freely move past and stop on the throne.
            // if in cenp, cens, and cenre.
            boolean[] unrestrictedTaflmen = new boolean[Rules.TAFLMAN_TYPE_COUNT];

            for(char c : unallocatedTypes) {
                if(allocatedTypes.contains(c)) continue;

                String cString = "" + c;
                if(getValueForTag(otrString, "dforp").contains(cString)
                        && getValueForTag(otrString, "dfors").contains(cString)
                        && getValueForTag(otrString, "dforre").contains(cString)) {
                    allocatedTypes.add(c);
                    unrestrictedTaflmen[TaflmanCodes.getIndexForChar(c)] = true;
                }
            }

            // The following taflmen may freely move past and stop on the throne, if and only if they begin their turn
            // on a throne space.
            // if in cenp and cens, but not cenre.
            boolean[] moveStopNoEntryTaflmen = new boolean[Rules.TAFLMAN_TYPE_COUNT];

            for(char c : unallocatedTypes) {
                if(allocatedTypes.contains(c)) continue;

                String cString = "" + c;
                if(getValueForTag(otrString, "dforp").contains(cString)
                        && getValueForTag(otrString, "dfors").contains(cString)
                        && !getValueForTag(otrString, "dforre").contains(cString)) {
                    allocatedTypes.add(c);
                    moveStopNoEntryTaflmen[TaflmanCodes.getIndexForChar(c)] = true;
                }
            }

            // The following taflmen may freely move past the throne, but may not stop on it.
            // if in cenp and cenre, but not cens.
            boolean[] moveEntryTaflmen = new boolean[Rules.TAFLMAN_TYPE_COUNT];

            for(char c : unallocatedTypes) {
                if(allocatedTypes.contains(c)) continue;

                String cString = "" + c;
                if(getValueForTag(otrString, "dforp").contains(cString)
                        && !getValueForTag(otrString, "dfors").contains(cString)
                        && getValueForTag(otrString, "dforre").contains(cString)) {
                    allocatedTypes.add(c);
                    moveEntryTaflmen[TaflmanCodes.getIndexForChar(c)] = true;
                }
            }

            // The following taflmen may freely move past the throne, if and only if they begin their turn on a throne
            // space. They may never stop on the throne.
            // if in cenp, but not cenre or cens.
            boolean[] moveNoEntryTaflmen = new boolean[Rules.TAFLMAN_TYPE_COUNT];
            for(char c : unallocatedTypes) {
                if(allocatedTypes.contains(c)) continue;

                String cString = "" + c;
                if(getValueForTag(otrString, "dforp").contains(cString)
                        && !getValueForTag(otrString, "dfors").contains(cString)
                        && !getValueForTag(otrString, "dforre").contains(cString)) {
                    allocatedTypes.add(c);
                    moveNoEntryTaflmen[TaflmanCodes.getIndexForChar(c)] = true;
                }
            }

            // The following taflmen may not move past or stop on the throne.
            // if in neither cenp or cens.
            boolean[] restrictedTaflmen = new boolean[Rules.TAFLMAN_TYPE_COUNT];
            for(char c : unallocatedTypes) {
                if(allocatedTypes.contains(c)) continue;

                String cString = "" + c;
                if(!getValueForTag(otrString, "dforp").contains(cString)
                        && !getValueForTag(otrString, "dfors").contains(cString)
                        && !getValueForTag(otrString, "dforre").contains(cString)) {
                    allocatedTypes.add(c);
                    restrictedTaflmen[TaflmanCodes.getIndexForChar(c)] = true;
                }
            }

            String unrestrictedString = getTaflmanTypeStringFromSpec(r, RulesSerializer.getStringForTaflmanTypeList(unrestrictedTaflmen));
            String moveStopNoEntryString = getTaflmanTypeStringFromSpec(r, RulesSerializer.getStringForTaflmanTypeList(moveStopNoEntryTaflmen));
            String moveEntryString = getTaflmanTypeStringFromSpec(r, RulesSerializer.getStringForTaflmanTypeList(moveEntryTaflmen));
            String moveNoEntryString = getTaflmanTypeStringFromSpec(r, RulesSerializer.getStringForTaflmanTypeList(moveNoEntryTaflmen));
            String restrictedString = getTaflmanTypeStringFromSpec(r, RulesSerializer.getStringForTaflmanTypeList(restrictedTaflmen));

            if(!unrestrictedString.contains("no taflmen")) {
                rules += "The following taflmen may freely move through or stop on the defender forts: " + unrestrictedString + ". ";
            }

            if(!moveStopNoEntryString.contains("no taflmen")) {
                rules += "The following taflmen may move through or stop on the defender forts, but may not enter it from the outside: " + moveStopNoEntryString + ". ";
            }

            if(!moveEntryString.contains("no taflmen")) {
                rules += "The following taflmen may freely move through the defender forts, but may not stop on them: " + moveEntryString + ". ";
            }

            if(!moveNoEntryString.contains("no taflmen")) {
                rules += "The following taflmen may move through the defender forts, but may not stop on a defender fort space or enter the defender forts from the outside: " + moveNoEntryString + ". ";
            }

            if(!restrictedString.contains("no taflmen")) {
                rules += "The following taflmen may neither move through nor stop on the defender forts: " + restrictedString + ". ";
            }

            String hostile = getTaflmanTypeStringForTag(r, "dforh");

            if(!hostile.isEmpty() && !hostile.contains("no taflmen")) {
                rules += "The defender forts are hostile to the following taflmen: " + hostile + ". ";
            }

            rules += "\n\n";
        }

        if(centers.size() == 0 && corners.size() == 0 && attackerForts.size() == 0 && defenderForts.size() == 0) {
            rules += ruleNumber++ + ". There are no special spaces on the board. All taflmen may move through or occupy any space.\n\n";
        }

        rules += ruleNumber++ + ". The " + (r.getStartingSide().isAttackingSide() ? "attacking side" : "defending side") + " moves first. Moves alternate " +
                "between sides.\n\n";


        if(r.getSpeedLimitMode() == Rules.SPEED_LIMITS_NONE) {
            rules += ruleNumber++ + ". All taflmen move like the rook in chess: any number of vacant spaces along a row or column. Taflmen may " +
                    "not move onto or through a space occupied by another taflman.\n\n";
        }
        else if(r.getSpeedLimitMode() == Rules.SPEED_LIMITS_IDENTICAL) {
            int speedLimit = r.getTaflmanSpeedLimit(Taflman.ALL_TAFLMAN_TYPES[0]);
            rules += ruleNumber++ + ". All taflmen move along rows or columns, up to " + speedLimit + " spaces per move. Taflmen may " +
                    "not move onto or through a space occupied by another taflman.\n\n";
        }
        else if(r.getSpeedLimitMode() == Rules.SPEED_LIMITS_BY_SIDE) {
            int attackerLimit = r.getTaflmanSpeedLimit(Taflman.ALL_TAFLMAN_TYPES[0]);
            int defenderLimit = r.getTaflmanSpeedLimit(Taflman.ALL_TAFLMAN_TYPES[Taflman.ALL_TAFLMAN_TYPES.length / 2]);
            rules += ruleNumber++ + ". All taflmen move along rows or columns. ";
            rules += "Attacking taflmen move up to " + attackerLimit + " spaces per move, while defending taflmen move up to " + defenderLimit + " spaces. ";
            rules += "Taflmen may not move onto or through a space occupied by another taflman.\n\n";
        }
        else if(r.getSpeedLimitMode() == Rules.SPEED_LIMITS_BY_TYPE) {
            rules += ruleNumber++ + ". All taflmen move along rows or columns. ";

            for(int i = 0; i < Taflman.ALL_TAFLMAN_TYPES.length; i++) {
                boolean isAttacking = Taflman.getPackedSide(Taflman.ALL_TAFLMAN_TYPES[i]) == Taflman.SIDE_ATTACKERS;
                char taflmanType = Taflman.getPackedType(Taflman.ALL_TAFLMAN_TYPES[i]);
                String side = isAttacking ? "attacking" : "defending";

                int speedLimit = r.getTaflmanSpeedLimit(Taflman.ALL_TAFLMAN_TYPES[i]);

                String speedLimitString = (speedLimit == -1 ? "any number of spaces. " : "up to " + speedLimit + (speedLimit > 1 ? " spaces. " : " space. "));

                if(taflmanType == Taflman.TYPE_KING) {
                    if(!isAttacking) rules += "The king moves " + speedLimitString;
                }
                else if(taflmanType == Taflman.TYPE_COMMANDER) {
                    if(isAttacking && r.getAttackers().hasCommanders()) rules += "Attacking commanders move " + speedLimitString;
                    else if(!isAttacking && r.getDefenders().hasCommanders()) rules += "Defending commanders move " + speedLimitString;
                }
                else if(taflmanType == Taflman.TYPE_KNIGHT) {
                    if(isAttacking && r.getAttackers().hasKnights()) rules += "Attacking knights move up to " + speedLimitString;
                    else if(!isAttacking && r.getDefenders().hasKnights()) rules += "Defending knights move up to " + speedLimitString;
                }
                else {
                    side = Character.toUpperCase(side.charAt(0)) + side.substring(1);
                    rules += side + " taflmen move " + speedLimitString;
                }
            }

            rules += "\n\n";
        }

        boolean kingWeak = r.getKingStrengthMode() == Rules.KING_WEAK || (r.getKingStrengthMode() == Rules.KING_STRONG_CENTER && centers.size() == 0);
        rules += ruleNumber++ + ". Taflmen" + (kingWeak ? ", except for the king, " : " ") + "are captured when an " +
                "opponent's move surrounds a taflman on two sides, along a row or a column, with hostile taflmen or spaces. " +
                "Taflmen are only captured if the opponent's move closes the trap. A taflman may therefore safely move in between two enemy taflmen, or " +
                "an enemy taflman and a hostile space. Captured taflmen are removed from the game. ";

        switch(r.getKingArmedMode()) {
            case Rules.KING_ARMED:
                rules += "The king may take part in captures.\n\n";
                break;
            case Rules.KING_HAMMER_ONLY:
                rules += "The king may take part in captures, but only as the moving piece.\n\n";
                break;
            case Rules.KING_ANVIL_ONLY:
                rules += "The king may take part in captures, but only as the stationary piece.\n\n";
                break;
            case Rules.KING_UNARMED:
                rules += "The king may not take part in captures.\n\n";
                break;
        }

        if(r.getKingStrengthMode() == Rules.KING_STRONG) {
            rules += ruleNumber++ + ". The king must be surrounded by enemy taflmen or hostile spaces on all four sides to be captured. " +
                    "The king may not be captured against the edge of the board.";

            rules += "\n\n";
        }
        else if(r.getKingStrengthMode() == Rules.KING_MIDDLEWEIGHT) {
            rules += ruleNumber++ + ". The king must be surrounded by enemy taflmen or hostile spaces on all sides to be captured. " +
                    "The king may be captured against the edge of the board, provided all adjacent spaces are either hostile or contain " +
                    "enemy taflmen.";

            rules += "\n\n";
        }
        else if(r.getKingStrengthMode() == Rules.KING_STRONG_CENTER && centers.size() > 0) {
            rules += ruleNumber++ + ". When the king is on the throne, he must be surrounded by attacking taflmen on all sides to be captured. " +
                    "When the king is adjacent to the throne, he must be surrounded on the other three sides by attacking taflmen to be captured. " +
                    "When the king is elsewhere on the board, he may be captured by two attacking taflmen, in the same manner as a regular taflman.";

            rules += "\n\n";
        }

        rules += ruleNumber++ + ". The defenders win if the king reaches " + (r.getEscapeType() == Rules.EDGES ? "an edge " : "a corner ") + "space. " +
                "The attackers win if the king is captured. If, at any point, a player's turn begins and that player has no legal moves, that player " +
                "loses.\n\n";

        if(r.threefoldRepetitionResult() != Rules.THIRD_REPETITION_IGNORED) {
            rules += ruleNumber++ + ". If a board position is repeated three times,";

            if(r.threefoldRepetitionResult() == Rules.THIRD_REPETITION_DRAWS) {
                rules += " the game is drawn.";
            }
            else if(r.threefoldRepetitionResult() == Rules.THIRD_REPETITION_LOSES) {
                rules += " the player who makes the move which results in the third repetition loses.";
            }
            else {
                rules += " the player who makes the move which results in the third repetition wins.";
            }

            rules += "\n\n";
        }

        if(r.isSurroundingFatal()) {
            rules += ruleNumber++ + ". If the defenders are fully surrounded (that is, no defending taflmen, if given an " +
                    "unlimited number of moves from the current position, without any responses from the attackers, can " +
                    "reach an edge space), the defenders lose.\n\n";
        }

        if(r.allowShieldWallCaptures() != Rules.NO_SHIELDWALL) {
            rules += ruleNumber++ + ". Shieldwall captures are allowed. A row of two or more taflmen on the board edge may " +
                    "be captured by a row of the same number of taflmen one row nearer the center of the board, faced off " +
                    "taflman to taflman, and two flanking taflmen on the board edge, capping the row of taflmen to be captured. ";

            if(r.allowShieldWallCaptures() == Rules.WEAK_SHIELDWALL && corners.size() > 0) {
                rules += "A hostile corner may stand in for one of the flanking taflmen. ";
            }

            if(r.allowFlankingShieldwallCapturesOnly()) {
                rules += "Only shieldwall formations closed by a taflman moving into a flanking position make a capture. ";
            }

            rules += "\n\n";
        }

        if(r.allowEdgeFortEscapes()) {
            rules += ruleNumber++ + ". Edge fort escapes are allowed. If the king is surrounded against the edge of the board " +
                    "by an invincible formation of friendly taflmen (that is, if the edge fort cannot be captured by the attacker, " +
                    "no matter how many moves the attacker is allowed), and the king has at least one legal move inside the fort, " +
                    "the defenders win.\n\n";
        }

        if(r.getBerserkMode() != Rules.BERSERK_NONE) {
            rules += ruleNumber++ + ". The berserk rule is in effect. ";
            if(r.getBerserkMode() == Rules.BERSERK_ANY_MOVE) {
                rules += "After a taflman makes a capture, that taflman is required to make one additional move.";
            }
            else {
                rules += "After a taflman makes a capture, if that taflman is able to make another capture, that taflman " +
                        "must make the second capture.";
            }
            rules += "\n\n";
        }

        if(r.getAttackers().hasCommanders() || r.getDefenders().hasCommanders()) {
            rules += ruleNumber++ + ". Commanders are a special piece, marked by the 'o' symbol. ";
            if(r.getCommanderJumpMode() != Taflman.JUMP_NONE) {
                rules += getJumpModeString("commander", r.getCommanderJumpMode());
            }

            rules += "\n\n";
        }

        if(r.getDefenders().hasKnights() || r.getAttackers().hasKnights()) {
            rules += ruleNumber++ + ". Knights are a special piece, marked by the '?' symbol. ";
            if(r.getKnightJumpMode() != Taflman.JUMP_NONE) {
                rules += getJumpModeString("knight", r.getKnightJumpMode());
            }

            rules += "\n\n";
        }

        if(r.getAttackers().hasCommanders() || r.getAttackers().hasKnights()) {
            rules += ruleNumber++ +
                    ". Attacking commanders or knights may capture the king by surrounding him on two sides" +
                    (corners.size() > 0 ? ", or by surrounding him against a corner. " : ". " ) +
                    "Two commanders or knights may not capture the king while the king " +
                    "is on the throne, nor may one commander or knight capture the king against the throne while the king " +
                    "is adjacent to the throne.\n\n";
        }

        if(r.getKingJumpMode() != Taflman.JUMP_NONE) {
            rules += ruleNumber++ + ". " + getJumpModeString("king", r.getKingJumpMode());
            rules += "\n\n";
        }

        return rules;
    }

    private static String getJumpModeString(String pieceName, int jumpMode) {
        String jumpString = "The " + pieceName + " ";

        if(jumpMode == Taflman.JUMP_STANDARD) {
            jumpString += "may jump over adjacent enemy taflmen (but not enemy knights, commanders, or kings). The jumped taflman " +
                    "is not captured.";
        }
        else if(jumpMode == Taflman.JUMP_CAPTURE) {
            jumpString += "may jump over adjacent enemy taflmen (but not enemy knights, commanders, or kings), capturing the jumped " +
                    "taflman.";
        }
        else if(jumpMode == Taflman.JUMP_RESTRICTED) {
            jumpString += "may jump over adjacent enemy taflmen (but not enemy knights, commanders, or kings), provided that the " +
                    pieceName + " starts from or lands upon a throne space or a corner space. The jumped taflman is not captured.";
        }

        return jumpString;
    }

    private static String getValueForTag(String rules, String tag) {
        String[] rulesTags = rules.split(" ");

        for(String rule : rulesTags) {
            String[] elements = rule.split(":");

            if(elements[0].startsWith(tag)) {
                if(elements.length == 1) {
                    return "";
                }
                else {
                    return elements[1];
                }
            }
        }

        if(RulesSerializer.defaults.containsKey(tag)) {
            return RulesSerializer.defaults.get(tag);
        }

        return "";
    }

    private static String getTaflmanTypeStringForTag(Rules r, String rulesTag) {
        String tagValue = getValueForTag(r.getOTRString(false), rulesTag);

        String attackerString = getTaflmanTypeStringFromSpec(r, true, tagValue);
        String defenderString = getTaflmanTypeStringFromSpec(r,false, tagValue);

        if(attackerString.contains("all") && defenderString.contains("all")) {
            return "all taflmen";
        }
        else if(attackerString.contains("no") && defenderString.contains("no")) {
            return "no taflmen";
        }
        else {
            defenderString += ", and " + attackerString;
            return defenderString;
        }
    }

    private static String getTaflmanTypeStringForTag(Rules r, boolean attackingSide, String rulesTag) {
        String tagValue = getValueForTag(r.getOTRString(false), rulesTag);
        return getTaflmanTypeStringFromSpec(r, attackingSide, tagValue);
    }

    private static String getTaflmanTypeStringFromSpec(Rules r, String spec) {
        String attackerString = getTaflmanTypeStringFromSpec(r, true, spec);
        String defenderString = getTaflmanTypeStringFromSpec(r, false, spec);

        if(attackerString.contains("all") && defenderString.contains("all")) {
            return "all taflmen";
        }
        else if(attackerString.contains("no") && defenderString.contains("no")) {
            return "no taflmen";
        }
        else {
            defenderString += ", and " + attackerString;
            return defenderString;
        }
    }

    private static String getTaflmanTypeStringFromSpec(Rules r, boolean attackingSide, String spec) {
        Pattern taflmanSpecPattern = Pattern.compile("\\s*t?c?n?k?T?C?N?K?\\s*");
        if(!taflmanSpecPattern.matcher(spec).matches()) throw new IllegalArgumentException("Bad taflman spec");

        String taflmenString = (attackingSide ? "attacking " : "defending ");

        if(spec.equals("tcnkTCNK")) {
            return "all " + (attackingSide ? "attacking" : "defending") + " taflmen";
        }

        if(attackingSide) {
            spec = spec.replaceAll("[TCNK]", "");
        }
        else {
            spec = spec.replaceAll("[tcnk]", "");
        }

        if(!r.getAttackers().hasKnights()) {
            spec = spec.replaceAll("n", "");
        }
        if(!r.getDefenders().hasKnights()) {
            spec = spec.replaceAll("N", "");
        }

        if(!r.getAttackers().hasCommanders()) {
            spec = spec.replaceAll("c", "");
        }
        if(!r.getDefenders().hasCommanders()) {
            spec = spec.replaceAll("C", "");
        }
        spec = spec.replaceAll("k", "");

        if(spec.equals("")) {
            return "no " + (attackingSide ? "attacking" : "defending") + " taflmen";
        }

        for(int i = 0; i < spec.length(); i++) {
            char c = spec.charAt(i);

            if(i == spec.length() - 1 && spec.length() > 2) {
                taflmenString += ", and";
            }
            if(i > 0 && spec.length() > 2) {
                taflmenString += ", ";
            }
            else if(i > 0 && spec.length() > 1) {
                taflmenString += " and ";
            }

            switch(c) {
                case 'T':
                case 't':
                    taflmenString += "taflmen";
                    break;
                case 'C':
                case 'c':
                    taflmenString += "commanders";
                    break;
                case 'N':
                case 'n':
                    taflmenString += "knights";
                    break;
                case 'K':
                case 'k':
                    taflmenString += "kings";
            }
        }

        return taflmenString;
    }
}
