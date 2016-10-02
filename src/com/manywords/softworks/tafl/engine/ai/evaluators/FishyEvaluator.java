package com.manywords.softworks.tafl.engine.ai.evaluators;

import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.ai.GameTreeState;
import com.manywords.softworks.tafl.rules.*;
import com.manywords.softworks.tafl.ui.RawTerminal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jay on 12/24/15.
 */
public class FishyEvaluator implements Evaluator {
    private static final int LOW = 0;
    private static final int HIGH = 1;
    private static final int DEFENDER = 0;
    private static final int ATTACKER = 1;
    private static int debugId = 0;
    public static boolean debug = false;
    public static String debugString;
    public static short debugValue;

    public short evaluate(GameState state, int maxDepth, int depth) {
        short value = 0;
        debugString = "";

        int victory = state.checkVictory();

        Board board = state.getBoard();


        /*
        if(depth == 5 && Math.random() < 0.001) {
            debug = true;
        }
        if(depth == 4 && Math.random() < 0.0025) {
            debug = true;
        }
		if(depth == 3 && Math.random() < 0.005) {
			debug = true;
		}
		if(depth == 2 && Math.random() < 0.01) {
			debug = true;
		}
		if(depth == 1 && Math.random() < 0.1) {
			debug = true;
		}
		*/

        int remainingDepth = maxDepth - depth;

        if (debug) {
            debugValue = 0;
            RawTerminal.disableColor();
            debugString = RawTerminal.getGameStateString(state);
            RawTerminal.enableColor();
        }

        // The concept: 5000 is an attacker win, -5000 is a defender win.
        // Closer victories are worth more. Give wins a little edge over
        // states that evaluate to almost-win to make them tastier.

        if (victory == GameState.ATTACKER_WIN) {
            if (debug) debugString += "Attacker win at depth " + depth + "/" + remainingDepth + "\n";
            value = (short)(Evaluator.ATTACKER_WIN + (500 * (remainingDepth + 1)));
            if (debug) printDebug(value, state.getCurrentSide().isAttackingSide(), depth);
            return value;
        } else if (victory == GameState.DEFENDER_WIN) {
            if (debug) debugString += "Defender win at depth " + depth + "/" + remainingDepth + "\n";
            value = (short)(Evaluator.DEFENDER_WIN - (500 * (remainingDepth + 1)));
            if (debug) printDebug(value, state.getCurrentSide().isAttackingSide(), depth);
            return value;
        } else if (victory == GameState.DRAW) {
            if (debug) debugString += "Draw at depth " + depth + "/" + remainingDepth + "\n";

            // Treat draws as losses
            if (state.getCurrentSide().isAttackingSide()) value = (short)(Evaluator.DEFENDER_WIN - (500 * (remainingDepth + 1)));
            else value = (short)(Evaluator.ATTACKER_WIN + (500 * (remainingDepth + 1)));

            if (debug) printDebug(value, state.getCurrentSide().isAttackingSide(), depth);
        }

        value = 0;

        // Values add to 500--if everything is going one team's way, it's as good
        // as a win (except not really).
        final int KING_FREEDOM_VALUE = 1250;
        final int KING_RISK_VALUE = 1150;
        final int RANK_AND_FILE_VALUE = 500;
        final int TAFLMAN_RISK_VALUE = 1400;
        final int MATERIAL_VALUE = 500;

        List<Character> defendingTaflmen = state.getDefenders().getTaflmen();
        int defendingTaflmenCount = defendingTaflmen.size();
        int startingDefendingTaflmenCount = state.getDefenders().getStartingTaflmen().size();
        List<Character> attackingTaflmen = state.getAttackers().getTaflmen();
        int attackingTaflmenCount = attackingTaflmen.size();
        int startingAttackingTaflmenCount = state.getAttackers().getStartingTaflmen().size();
        int boardSize = board.getBoardDimension();

        List<Character> allTaflmen = new ArrayList<Character>(defendingTaflmenCount + attackingTaflmenCount);
        allTaflmen.addAll(defendingTaflmen);
        allTaflmen.addAll(attackingTaflmen);

        char king = Taflman.EMPTY;

        // precalc rank/file information

        // these store the presence of one side's pieces on each rank and
        // file. e.g. rankPresence[ATTACKERS][1] == true means that each
        // defender
        boolean[][] rankPresence = new boolean[2][board.getBoardDimension()];
        boolean[][] filePresence = new boolean[2][board.getBoardDimension()];

        // these store the position of the highest/lowest piece on
        // each rank and file, positive for attacker and negative
        // for defender. e.g. rankControlTmp[LOW][0] == -1 means that
        // the low piece on rank 0 is a defender on file 1.
        byte[][] rankControlTmp = new byte[2][board.getBoardDimension()];
        byte[][] fileControlTmp = new byte[2][board.getBoardDimension()];

        byte[] rankControl = new byte[board.getBoardDimension()];
        byte[] fileControl = new byte[board.getBoardDimension()];

        for(char taflman : allTaflmen) {

            int side = (Taflman.getPackedSide(taflman) > 0 ? ATTACKER : DEFENDER);
            int mul = (side > 0 ? 1 : -1);
            Coord coord = board.findTaflmanSpace(taflman);

            rankPresence[side][coord.y] = true;
            filePresence[side][coord.x] = true;

            if(coord.x < rankControlTmp[LOW][coord.y]) rankControlTmp[LOW][coord.y] = (byte)(coord.x * mul);
            else if(coord.x > rankControlTmp[HIGH][coord.y]) rankControlTmp[HIGH][coord.y] = (byte)(coord.x * mul);

            if(coord.y < fileControlTmp[LOW][coord.x]) fileControlTmp[LOW][coord.x] = (byte)(coord.y * mul);
            else if(coord.y > fileControlTmp[HIGH][coord.x]) fileControlTmp[HIGH][coord.x] = (byte)(coord.y * mul);


            if(king == Taflman.EMPTY && Taflman.isKing(taflman)) {
                king = taflman;
            }
        }


        for(int i = 0; i < board.getBoardDimension(); i++) {
            if (rankControlTmp[LOW][i] > 0 && rankControlTmp[HIGH][i] > 0) rankControl[i] = ATTACKER;
            else if (rankControlTmp[LOW][i] < 0 && rankControlTmp[HIGH][i] < 0) rankControl[i] = DEFENDER;

            if (fileControlTmp[LOW][i] > 0 && fileControlTmp[HIGH][i] > 0) fileControl[i] = ATTACKER;
            else if (fileControlTmp[LOW][i] < 0 && fileControlTmp[HIGH][i] < 0) fileControl[i] = DEFENDER;
        }

        Coord kingCoord = board.findTaflmanSpace(king);

        boolean armedKing = state.mGame.getRules().getKingArmedMode() == Rules.KING_ARMED || state.mGame.getRules().getKingArmedMode() == Rules.KING_ANVIL_ONLY;
        boolean strongKing = state.mGame.getRules().getKingStrengthMode() == Rules.KING_STRONG;
        boolean edgeEscape = state.mGame.getRules().getEscapeType() == Rules.EDGES;
        boolean cornerEscape = state.mGame.getRules().getEscapeType() == Rules.CORNERS;

        // ==================== 1. KING FREEDOM ====================
        // If the king can get to an edge in two ways (edge win) or the corner
        // two ways (corner win) this is a victory, and we can report it as such.
        List<Coord> kingDestinations = Taflman.getAllowableDestinations(state, king);
        int kingCorners = 0;
        int kingEdges = 0;
        for (Coord dest : kingDestinations) {
            if (board.isEdgeSpace(dest)) kingEdges++;
            if (board.getSpaceTypeFor(dest) == SpaceType.CORNER) kingCorners++;
        }

        if (edgeEscape) { // block max: defender 0.75, attacker 0.0
            if (kingEdges >= 2) {
                if (debug) debugString += "Defender win--two ways to an edge\n";
                value = (short)(Evaluator.DEFENDER_WIN - (250 * (remainingDepth + 1)));
                if (debug) printDebug(value, state.getCurrentSide().isAttackingSide(), depth);
                return value;
            }

            if (kingEdges == 1) {
                if (debug) debugString += "King has edge access: " + KING_FREEDOM_VALUE * GameTreeState.DEFENDER * 0.75  + "\n";
                value += KING_FREEDOM_VALUE * GameTreeState.DEFENDER * 0.75;
            }
        } else if (cornerEscape) { // block max: defender 0.4, attacker 0.0
            if (kingCorners >= 2) {
                if (debug) debugString += "Defender win--two ways to a corner\n";
                value = (short)(Evaluator.DEFENDER_WIN - (250 * (remainingDepth + 1)));
                if (debug) printDebug(value, state.getCurrentSide().isAttackingSide(), depth);
                return value;
            }

            if (kingCorners == 1) {
                if (debug) debugString += "King has corner access: " + KING_FREEDOM_VALUE * GameTreeState.DEFENDER * 0.2  + "\n";
                value += KING_FREEDOM_VALUE * GameTreeState.DEFENDER * 0.2;
            }

            if (kingEdges >= 1) {
                if (debug) debugString += "King has edge access: " + KING_FREEDOM_VALUE * GameTreeState.DEFENDER * 0.1  + "\n";
                value += KING_FREEDOM_VALUE * GameTreeState.DEFENDER * 0.1;
            }
        }

        // If the king is on a rank or file controlled by us, that's good.
        if(rankControl[kingCoord.y] == DEFENDER) {
            if (debug) debugString += "King on one of our ranks: " + KING_FREEDOM_VALUE * GameTreeState.DEFENDER * 0.1  + "\n";
            value += KING_FREEDOM_VALUE * GameTreeState.DEFENDER * 0.1;
        }
        if(fileControl[kingCoord.x] == DEFENDER) {
            if (debug) debugString += "King on one of our files: " + KING_FREEDOM_VALUE * GameTreeState.DEFENDER * 0.1  + "\n";
            value += KING_FREEDOM_VALUE * GameTreeState.DEFENDER * 0.1;
        }

        // ==================== 2. KING RISK ====================
        // If the king is in check, an enemy taflman can close the trap, and
        // none of our pieces can block it, this is a victory, and we can report
        // it as such.

        List<Character> kingAdjacentTaflmen = board.getAdjacentNeighbors(kingCoord);
        List<Character> enemyKingAdjacentTaflmen = new ArrayList<Character>(4);
        for (char c : kingAdjacentTaflmen) {
            if (Taflman.getPackedSide(c) == Taflman.SIDE_ATTACKERS) enemyKingAdjacentTaflmen.add(c);
        }

        if (armedKing && strongKing) { // block max: defender 1, attacker 1
            if (enemyKingAdjacentTaflmen.size() <= 2) {
                if (debug) debugString += "Strong armed king adjacent to " + enemyKingAdjacentTaflmen.size() + " taflmen, which is cool: " + KING_RISK_VALUE * GameTreeState.DEFENDER * 0.1 * enemyKingAdjacentTaflmen.size() + "\n";
                // There's value in having an armed, strong king next to one or two enemy taflmen--leads to captures.
                value += KING_RISK_VALUE * GameTreeState.DEFENDER * 0.1 * enemyKingAdjacentTaflmen.size();
            }
            else if (enemyKingAdjacentTaflmen.size() > 2) {
                // Having a king in check is risky.
                if (debug) debugString += "Strong armed king adjacent to " + enemyKingAdjacentTaflmen.size() + " taflmen, which is check!: " + KING_RISK_VALUE * GameTreeState.ATTACKER * 1 + "\n";
                value += KING_RISK_VALUE * GameTreeState.ATTACKER * 1;
            }

            if (kingAdjacentTaflmen.size() > enemyKingAdjacentTaflmen.size()) {
                if (debug) debugString += "King has a bodyguard: " + KING_RISK_VALUE * GameTreeState.DEFENDER * 0.8  + "\n";
                value += KING_RISK_VALUE * GameTreeState.DEFENDER * 0.8;
            }
        }
        else if (strongKing) { // block max: defender 1, attacker 1
            // if unarmed strong king, or weak armed or unarmed king
            if (enemyKingAdjacentTaflmen.size() > 2) {
                // Having a king in check is risky.
                if (debug) debugString += "Strong king adjacent to " + enemyKingAdjacentTaflmen.size() + " taflmen, which is check!: " + KING_RISK_VALUE * GameTreeState.ATTACKER * 1  + "\n";
                value += KING_RISK_VALUE * GameTreeState.ATTACKER * 1;
            }

            // A piece next to a king means no check.
            if (kingAdjacentTaflmen.size() > enemyKingAdjacentTaflmen.size()) {
                if (debug) debugString += "King has a bodyguard: " + KING_RISK_VALUE * GameTreeState.DEFENDER * 1  + "\n";
                value += KING_RISK_VALUE * GameTreeState.DEFENDER * 1;
            }
        }
        else { // block max: defender 0.0, attacker 1
            // armed/unarmed weak kings
            if (enemyKingAdjacentTaflmen.size() == 1) {
                // Having a king in check is risky.
                if(debug) debugString += "King in check: " + KING_RISK_VALUE * GameTreeState.ATTACKER * 1 + "\n";
                value += KING_RISK_VALUE * GameTreeState.ATTACKER * 1;
            }
        }

        // 3. ==================== CONTROL OF IMPORTANT RANKS AND FILES ====================
        // Attacker max this section: 1
        // Defender max this section: 1

        if(debug) debugValue = value;

        // The points diagonally adjacent to the corners are important, and it's good for the
        // attackers to occupy them.
        // attacker max: 0.4
        List<Coord> cornerPoints = new ArrayList<Coord>(4);
        for (Coord corner : board.getRules().getCornerSpaces()) {
            cornerPoints.addAll(board.getDiagonalSpaces(corner));
        }

        for (Coord cornerPoint : cornerPoints) {
            char occupier = board.getOccupier(cornerPoint);
            if (occupier != Taflman.EMPTY && Taflman.getPackedSide(occupier) == Taflman.SIDE_ATTACKERS) {
                value += RANK_AND_FILE_VALUE * GameTreeState.ATTACKER * (0.4 / 4);
            }
        }

        if(debug) {
            debugString += "Control of corner diagonals: " + (value - debugValue) + "\n";
            debugValue = value;
        }

        // It's good for the attacker to control ranks and files. It's good for the defender to be alone
        // on ranks and files.
        // attacker max: 0.4
        // defender max: 0.8
        final double attackerValuePerRank = RANK_AND_FILE_VALUE * (0.4 / (2 * boardSize));
        final double defenderValuePerRank = RANK_AND_FILE_VALUE * (0.8 / (2 * boardSize));
        for(int i = 0; i < boardSize; i++) {
            if(rankControl[i] == DEFENDER && !rankPresence[ATTACKER][i]) value += defenderValuePerRank * GameTreeState.DEFENDER;
            else if(rankControl[i] == ATTACKER) value += attackerValuePerRank * GameTreeState.ATTACKER;

            if(fileControl[i] == DEFENDER && !filePresence[ATTACKER][i]) value += defenderValuePerRank * GameTreeState.DEFENDER;
            else if(fileControl[i] == ATTACKER) value += attackerValuePerRank * GameTreeState.ATTACKER;
        }

        if(debug) {
            debugString += "Control/aloneness on ranks and files: " + (value - debugValue) + "\n";
            debugValue = value;
        }

        // It's better to have more developed pieces than fewer developed pieces.
        // attacker max, defender max: 0.2
        int attackerDeveloped = 0;
        int defenderDeveloped = 0;
        final double valuePerAttacker = RANK_AND_FILE_VALUE * 0.2 / startingAttackingTaflmenCount;
        final double valuePerDefender = RANK_AND_FILE_VALUE * 0.2 / startingAttackingTaflmenCount;

        for(char taflman : allTaflmen) {
            if(Taflman.getPackedSide(taflman) == Taflman.SIDE_ATTACKERS) {
                if(Taflman.getDeveloped(taflman)) {
                    attackerDeveloped++;
                }
            }
            else {
                if(Taflman.getDeveloped(taflman)) {
                    defenderDeveloped++;
                }
            }
        }

        value += attackerDeveloped * valuePerAttacker * GameTreeState.ATTACKER;
        value += defenderDeveloped * valuePerDefender * GameTreeState.DEFENDER;

        if(debug) {
            debugString += "Taflman development: " + (value - debugValue) + "\n";
            debugValue = value;
        }

        // 4. ==================== TAFLMAN RISK ====================

        // 5. ==================== MATERIAL COMPARISON ====================
        int defendersLost = startingDefendingTaflmenCount - defendingTaflmenCount;
        int attackersLost = startingAttackingTaflmenCount - attackingTaflmenCount;

        double attackerValue = MATERIAL_VALUE / startingAttackingTaflmenCount;
        // Don't count the king unless there's no other choice
        double defenderValue = MATERIAL_VALUE / Math.max(1, startingDefendingTaflmenCount - 1);

        value += attackersLost * attackerValue * GameTreeState.DEFENDER;
        value += defendersLost * defenderValue * GameTreeState.ATTACKER;

        if(debug) {
            debugString += "Material: " + (value - debugValue) + "\n";
            debugValue = value;
        }

        if (debug) printDebug(value, state.getCurrentSide().isAttackingSide(), depth);
        return value;
    }

    private static void printDebug(short value, boolean isAttackingSide, int depth) {
        debugString += "\n\nFinal evaluation " + value + "\n";


        //OpenTafl.logPrint(OpenTafl.LogLevel.CHATTY, debugString);

        // The sequence justifying this logic:
        // 1. getCurrentSide().isAttackingSide() explores moves.
        // 2. mBranch.getCurrentSide().isAttackingSide() == false! (because the turn advances).
        // 3. The move we're evaluating is an attacker move.
        String filename = (isAttackingSide ? "position-defenders-" : "position-attackers-") + "depth" + depth + "-" + ++debugId + ".txt";
        File f = new File("debug-positions/" + filename);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(f))) {
            writer.write(debugString, 0, debugString.length());
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }

    }
}
