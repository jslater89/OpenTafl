package com.manywords.softworks.tafl.ui;

import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Taflman;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by jay on 4/8/16.
 */
public class HumanReadableRulesPrinter {
    public static String getHumanReadableRules(Rules r) {
        String rules = "";
        int ruleNumber = 1;

        rules += "The tafl games are a collection of Norse board games dating to the Viking age and earlier. They were commonly played in " +
                "the time before chess arrived in Northern Europe in the 1100s. All tafl games have a few features in common: they are played " +
                "on square boards of odd-numbered size, with asymmetric forces. The defenders, or the king's side, start in the center of the " +
                "board, and have escape as their objective: the king must reach either an edge space or a corner space, depending on the rules " +
                "in question. The attackers, or the besieging side, start around the edges of the board, and must prevent the king's escape. The " +
                "following rules are for this variant: " + r.getName() + " " + r.boardSize + "x" + r.boardSize + "\n\n";

        rules += ruleNumber++ +". The game is played on a board of " + r.boardSize + "x" + r.boardSize + " spaces, with " + r.getDefenders().getStartingTaflmen().size() +
                " defenders (including one king, marked by the '+' symbol) and " +
                r.getAttackers().getStartingTaflmen().size() + " attackers.\n\n";

        Set<Coord> centers = r.getCenterSpaces();
        Set<Coord> corners = r.getCornerSpaces();

        String centerString = "center space";
        String centerVerb = "is ";
        if(centers.size() > 1) {
            centerString = "center spaces";
            centerVerb = "are ";
        }

        if(centers.size() > 0) {
            rules += ruleNumber++ + ". The " + centerString + " " + centerVerb + "known as the throne. The throne is marked on the board with " +
                    "asterisks. ";

            String defenderStoppable = getTaflmanTypeStringForTag(r, false, "cens");

            if(defenderStoppable != null) {
                rules += "The following taflmen may stop on the " + centerString + ": " + defenderStoppable + ". ";
            }

            String defenderPassable = getTaflmanTypeStringForTag(r, false, "cenp");
            String attackerPassable = getTaflmanTypeStringForTag(r, true, "cenp");

            if(defenderPassable != null && attackerPassable != null) {
                rules += "The following taflmen may move through the " + centerString + ": " + defenderPassable + ", and " + attackerPassable + ". ";
            }
            else if(defenderPassable != null) {
                rules += "The following taflmen may move through the " + centerString + ": " + defenderPassable + ". ";
            }
            else if(attackerPassable != null) {
                rules += "The following taflmen may move through the " + centerString + ": " + attackerPassable + ". ";
            }

            String defenderHostile = getTaflmanTypeStringForTag(r, false, "cenh");
            String attackerHostile = getTaflmanTypeStringForTag(r, true, "cenh");
            String defenderHostileEmpty = getTaflmanTypeStringForTag(r, false, "cenhe");
            String attackerHostileEmpty = getTaflmanTypeStringForTag(r, true, "cenhe");

            if(defenderHostile != null && attackerHostile != null) {
                rules += "The throne is hostile to the following taflmen: " + defenderHostile + ", and " + attackerHostile + ". ";
            }
            else if(defenderHostile != null) {
                rules += "The throne is hostile to the following taflmen: " + defenderHostile + ". ";
            }
            else if(attackerHostile != null) {
                rules += "The throne is hostile to the following taflmen: " + attackerHostile + ". ";
            }

            if(defenderHostileEmpty != null && attackerHostileEmpty != null) {
                rules += "Additionally, when empty, the throne is hostile to the following taflmen: " + defenderHostileEmpty + ", and " + attackerHostileEmpty + ". ";
            }
            else if(defenderHostile != null) {
                rules += "Additionally, when empty, the throne is hostile to the following taflmen: " + defenderHostileEmpty + ". ";
            }
            else if(attackerHostile != null) {
                rules += "Additionally, when empty, the throne is hostile to the following taflmen: " + attackerHostileEmpty + ". ";
            }

            rules += "\n\n";
        }

        if(corners.size() > 0) {
            rules += ruleNumber++ + ". The corner spaces are marked on the board with asterisks. Only the king may stop on a corner space. ";

            String defenderHostile = getTaflmanTypeStringForTag(r, false, "corh");
            String attackerHostile = getTaflmanTypeStringForTag(r, true, "corh");

            if(defenderHostile != null && attackerHostile != null) {
                rules += "The corners are hostile to the following taflmen: " + defenderHostile + ", and " + attackerHostile + ". ";
            }
            else if(defenderHostile != null) {
                rules += "The corners are hostile to the following taflmen: " + defenderHostile + ". ";
            }
            else if(attackerHostile != null) {
                rules += "The corners are hostile to the following taflmen: " + attackerHostile + ". ";
            }

            rules += "\n\n";
        }

        if(centers.size() == 0 && corners.size() == 0) {
            rules += ruleNumber++ + ". There are no special spaces on the board. All taflmen may move through or occupy any space.\n\n";
        }

        rules += ruleNumber++ + ". The " + (r.getStartingSide().isAttackingSide() ? "attacking side" : "defending side") + " moves first. Moves alternate " +
                "between sides.\n\n";

        rules += ruleNumber++ + ". All taflmen move like the rook in chess: any number of vacant spaces along a row or column. Taflmen may " +
                "not move onto or through a space occupied by another taflman.\n\n";

        rules += ruleNumber++ + ". All taflmen " + (r.isKingStrong() ? "except the king " : "") + "are captured when the opposing side moves " +
                "a taflman such that the captured taflman is surrounded on both sides, along a row or a column, by enemy taflmen or hostile spaces. " +
                "A taflman is only captured if the opponent's move closes the trap: a taflman may therefore safely move in between two enemy taflmen, or " +
                "an enemy taflman and a hostile space. Captured taflmen are removed from the game. " + (r.isKingArmed() ? "The king may take part in " +
                "captures." : "The king may not take part in captures.") + "\n\n";

        if(r.isKingStrong()) {
            rules += ruleNumber++ + ". The king must be surrounded by enemy taflmen or hostile spaces on all four sides to be captured. " +
                    "The king cannot be captured against the edge of the board. ";

                    if(centers.size() > 0) rules += "The king may be captured by three attacking pieces against an empty throne.";

                    rules += "\n\n";
        }

        rules += ruleNumber++ + ". The defenders win if the king reaches " + (r.getEscapeType() == Rules.EDGES ? "an edge " : "a corner ") + "space. " +
                "The attackers win if the king is captured. If, at any point, a player's turn begins and that player has no legal moves, that player " +
                "loses.\n\n";

        if(r.threefoldRepetitionResult() != Rules.IGNORE) {
            rules += ruleNumber++ + ". If a board position is repeated three times,";

            if(r.threefoldRepetitionResult() == Rules.DRAW) {
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
            rules += getJumpModeString("king", r.getKingJumpMode());
            rules += "\n\n";
        }

        return rules;
    }

    private static String getJumpModeString(String pieceName, int jumpMode) {
        String jumpString = "The " + pieceName + " ";

        if(jumpMode == Taflman.JUMP_STANDARD) {
            jumpString += "may jump over adjacent enemy taflmen (but not enemy knights, commanders, or kings). A jumped taflman " +
                    "is not captured.";
        }
        else if(jumpMode == Taflman.JUMP_CAPTURE) {
            jumpString += "may jump over adjacent enemy taflmen (but not enemy knights, commanders, or kings), capturing the jumped " +
                    "taflman.";
        }
        else if(jumpMode == Taflman.JUMP_RESTRICTED) {
            jumpString += "may jump over adjacent enemy taflmen (but not enemy knights, commanders, or kings), provided that the " +
                    pieceName + " starts from or lands upon a throne space or a corner space.";
        }

        return jumpString;
    }

    private static String getValueForTag(String rules, String tag) {
        String[] rulesTags = rules.split(" ");

        for(String rule : rulesTags) {
            String[] elements = rule.split(":");

            if(elements[0].startsWith(tag)) {
                if(elements.length == 1) {
                    return null;
                }
                else {
                    return elements[1];
                }
            }
        }

        if(RulesSerializer.defaults.containsKey(tag)) {
            return RulesSerializer.defaults.get(tag);
        }

        return null;
    }

    private static String getTaflmanTypeStringForTag(Rules r, boolean attackingSide, String rulesTag) {
        String tagValue = getValueForTag(r.getOTRString(), rulesTag);

        if(tagValue == null) return null;
        else return getTaflmanTypeStringFromSpec(attackingSide, tagValue);
    }

    private static String getTaflmanTypeStringFromSpec(boolean attackingSide, String spec) {
        Pattern taflmanSpecPattern = Pattern.compile("\\s*t?c?n?k?T?C?N?K?\\s*");
        if(!taflmanSpecPattern.matcher(spec).matches()) return null;

        String taflmenString = (attackingSide ? "attacking " : "defending ");
        if(attackingSide) {
            spec = spec.replaceAll("[TCNK]", "");
        }
        else {
            spec = spec.replaceAll("[tcnk]", "");
            spec = spec.toLowerCase();
        }

        if(spec.equals("tcnk")) {
            return "all " + (attackingSide ? "attacking" : "defending") + " taflmen";
        }
        else if(spec.equals("")) {
            return null;
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
                case 't':
                    taflmenString += "taflmen";
                    break;
                case 'c':
                    taflmenString += "commanders";
                    break;
                case 'n':
                    taflmenString += "knights";
                    break;
                case 'k':
                    taflmenString += "kings";
            }
        }

        return taflmenString;
    }
}
