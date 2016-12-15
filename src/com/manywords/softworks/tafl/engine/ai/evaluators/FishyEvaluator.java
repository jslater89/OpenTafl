package com.manywords.softworks.tafl.engine.ai.evaluators;

import com.manywords.softworks.tafl.OpenTafl;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.Utilities;
import com.manywords.softworks.tafl.rules.*;
import com.manywords.softworks.tafl.ui.RawTerminal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static com.manywords.softworks.tafl.rules.Taflman.EMPTY;

/**
 * Created by jay on 12/24/15.
 */
public class FishyEvaluator implements Evaluator {
    // Debug options
    private static int debugId = 0;
    public static boolean debug = false;
    public static String debugString;
    public static short debugValue;

    // constants
    private static final int LOW = 0;
    private static final int HIGH = 1;
    private static final int DEFENDER = 0;
    private static final int ATTACKER = 1;

    // System.arraycopy is faster than Arrays.fill(): arraycopy uses memcpy
    // where available
    private static final boolean[] TRUE_ARRAY = {true, true, true, true,
                                                 true, true, true, true,
                                                 true, true, true, true,
                                                 true, true, true, true,
                                                 true, true, true, true};
    private static final boolean[] FALSE_ARRAY = {false, false, false, false,
                                                  false, false, false, false,
                                                  false, false, false, false,
                                                  false, false, false, false,
                                                  false, false, false, false};

    // Values add to 5000--if everything is going one team's way, it's as good
    // as a win (except not really).
    private final int KING_FREEDOM_INDEX = 0;
    private final int KING_RISK_INDEX = 1;
    private final int RANK_AND_FILE_INDEX = 2;
    private final int MATERIAL_INDEX = 3;
    private final int PIECE_SQUARE_INDEX = 4;
    private final int KING_FREEDOM_VALUE = 500;
    private final int KING_RISK_VALUE = 500;
    private final int RANK_AND_FILE_VALUE = 1100;
    private final int MATERIAL_VALUE = 1650;
    private final int PIECE_SQUARE_VALUE = 1100;
    // 1400 for unused TAFLMAN_RISK

    // Losing fewer than taflman-count * LIGHT_LOSSES is not a tragedy.
    // Losing in between LIGHT and HEAVY is getting bad.
    // We should really try to avoid dropping below HEAVY.
    private static final double LIGHT_TAFLMAN_LOSSES = 0.25;
    private static final double HEAVY_TAFLMAN_LOSSES = 0.25;
    private static final double LIGHT_TAFLMAN_VALUE = 0.15;
    private static final double HEAVY_TAFLMAN_VALUE = 0.40;

    // Rules/values things
    private int[] mLightTaflmanCount = new int[2];
    private int[] mStandardTaflmanCount = new int[2];
    private int[] mHeavyTaflmanCount = new int[2];
    private short[] mStandardTaflmanValue = new short[2];
    private short[] mLightTaflmanValue = new short[2];
    private short[] mHeavyTaflmanValue = new short[2];

    private Rules mRules;
    private int mStartingDefenderCount = 0;
    private int mStartingAttackerCount = 0;

    private boolean mArmedKing;
    private int mKingStrength;
    private boolean mEdgeEscape;
    private boolean mCornerEscape;

    // Accumulated values
    private short mAssignedKingFreedom = 0;
    private short mAssignedKingRisk = 0;
    private short mAssignedRankAndFile = 0;
    private short mAssignedMaterial = 0;
    private short mAssignedPieceSquare = 0;

    private PieceSquareTable mPieceSquareTable;

    @Override
    public void initialize(Rules rules) {
        mPieceSquareTable = new PieceSquareTable(rules, null);
        mStartingAttackerCount = rules.getAttackers().getStartingTaflmen().size();
        mStartingDefenderCount = rules.getDefenders().getStartingTaflmen().size();

        mArmedKing = rules.getKingArmedMode() == Rules.KING_ARMED || rules.getKingArmedMode() == Rules.KING_ANVIL_ONLY;
        mKingStrength = rules.getKingStrengthMode();
        mEdgeEscape = rules.getEscapeType() == Rules.EDGES;
        mCornerEscape = rules.getEscapeType() == Rules.CORNERS;

        mRules = rules;

        double standardTaflmanPercentage = 1d - HEAVY_TAFLMAN_LOSSES - LIGHT_TAFLMAN_LOSSES;

        // Set up defender taflman values
        int taflmanTotal = 0;
        mLightTaflmanCount[DEFENDER] = (int) (mStartingDefenderCount * LIGHT_TAFLMAN_LOSSES);
        mStandardTaflmanCount[DEFENDER] = (int) (mStartingDefenderCount * standardTaflmanPercentage);
        mHeavyTaflmanCount[DEFENDER] = (int) (mStartingDefenderCount * HEAVY_TAFLMAN_LOSSES);

        taflmanTotal = mLightTaflmanCount[DEFENDER] + mStandardTaflmanCount[DEFENDER] + mHeavyTaflmanCount[DEFENDER];
        if(taflmanTotal < mStartingDefenderCount) mStandardTaflmanCount[DEFENDER] += (mStartingDefenderCount - taflmanTotal);

        // Set up attacker taflman values
//        taflmanTotal = 0;
//        mLightTaflmanCount[ATTACKER] = (int) (mStartingAttackerCount * LIGHT_TAFLMAN_LOSSES);
//        mStandardTaflmanCount[ATTACKER] = (int) (mStartingAttackerCount * standardTaflmanPercentage);
//        mHeavyTaflmanCount[ATTACKER] = (int) (mStartingAttackerCount * HEAVY_TAFLMAN_LOSSES);
//
//        taflmanTotal = mLightTaflmanCount[ATTACKER] + mStandardTaflmanCount[ATTACKER] + mHeavyTaflmanCount[ATTACKER];
//        if(taflmanTotal < mStartingAttackerCount) mStandardTaflmanCount[ATTACKER] += (mStartingAttackerCount - taflmanTotal);
//
//        short standardTaflmanValue = (short) (MATERIAL_VALUE * (1d - HEAVY_TAFLMAN_VALUE - LIGHT_TAFLMAN_VALUE));
//        short lightTaflmanValue = (short) (MATERIAL_VALUE * LIGHT_TAFLMAN_VALUE);
//        short heavyTaflmanValue = (short) (MATERIAL_VALUE * HEAVY_TAFLMAN_VALUE);
//
//        mStandardTaflmanValue[ATTACKER] = (short) (standardTaflmanValue / mStandardTaflmanCount[ATTACKER]);
//        mStandardTaflmanValue[DEFENDER] = (short) (standardTaflmanValue / mStandardTaflmanCount[DEFENDER]);
//
//        mLightTaflmanValue[ATTACKER] = (short) (lightTaflmanValue / mLightTaflmanCount[ATTACKER]);
//        mLightTaflmanValue[DEFENDER] = (short) (lightTaflmanValue / mLightTaflmanCount[DEFENDER]);
//
//        mHeavyTaflmanValue[ATTACKER] = (short) (heavyTaflmanValue / mHeavyTaflmanCount[ATTACKER]);
//        mHeavyTaflmanValue[DEFENDER] = (short) (heavyTaflmanValue / mHeavyTaflmanCount[DEFENDER]);
    }

    private short getMaxFor(int category) {
        switch(category) {
            case KING_FREEDOM_INDEX:
                return KING_FREEDOM_VALUE;
            case KING_RISK_INDEX:
                return KING_RISK_VALUE;
            case RANK_AND_FILE_INDEX:
                return RANK_AND_FILE_VALUE;
            case MATERIAL_INDEX:
                return MATERIAL_VALUE;
            case PIECE_SQUARE_INDEX:
                return PIECE_SQUARE_VALUE;
        }

        throw new IllegalArgumentException("Bad category index");
    }

    private short getAssignedFor(int category) {
        switch(category) {
            case KING_FREEDOM_INDEX:
                return mAssignedKingFreedom;
            case KING_RISK_INDEX:
                return mAssignedKingRisk;
            case RANK_AND_FILE_INDEX:
                return mAssignedRankAndFile;
            case MATERIAL_INDEX:
                return mAssignedMaterial;
            case PIECE_SQUARE_INDEX:
                return PIECE_SQUARE_INDEX;
        }

        throw new IllegalArgumentException("Bad category index");
    }

    private short assignValue(int side, int category, short amount) {
        int multiplier = (side == ATTACKER ? 1 : -1);

        short available = (short) (multiplier * getMaxFor(category) - getAssignedFor(category));
        short actualAmount = (short) (Math.min(Math.abs(amount), Math.abs(available)) * multiplier);

        if((amount < 0 && actualAmount > 0) || (amount > 0 && actualAmount < 0)) actualAmount = (short) -actualAmount;

        switch(category) {
            case KING_FREEDOM_INDEX:
                mAssignedKingFreedom += actualAmount;
                break;
            case KING_RISK_INDEX:
                mAssignedKingRisk += actualAmount;
                break;
            case RANK_AND_FILE_INDEX:
                mAssignedRankAndFile += actualAmount;
                break;
            case MATERIAL_INDEX:
                mAssignedMaterial += actualAmount;
                break;
            case PIECE_SQUARE_INDEX:
                mAssignedPieceSquare += actualAmount;
                break;
        }

        return actualAmount;
    }

    private short changeEvaluation(int side, int category, float fraction) {
        return changeEvaluation(side, category, fraction, "");
    }

    private short changeEvaluation(int side, int category, float fraction, String debugMessage) {
        short preferredValue = (short) (getMaxFor(category) * fraction * (side == ATTACKER ? 1 : -1));

        short assignedValue = assignValue(side, category, preferredValue);

        if(debug && debugMessage != null) debugString += debugMessage + " (" + assignedValue + "/" + preferredValue + ")\n";

        return assignedValue;
    }

    public short evaluate(GameState state, int maxDepth, int depth) {
        mTaflmanThreatCache.clear();
        mAssignedKingRisk = mAssignedKingFreedom = mAssignedRankAndFile = mAssignedMaterial = 0;

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

        List<Character> defendingTaflmen = state.getDefenders().getTaflmen();
        int defendingTaflmenCount = defendingTaflmen.size();
        List<Character> attackingTaflmen = state.getAttackers().getTaflmen();
        int attackingTaflmenCount = attackingTaflmen.size();
        int boardSize = board.getBoardDimension();

        List<Character> allTaflmen = new ArrayList<Character>(defendingTaflmenCount + attackingTaflmenCount);
        allTaflmen.addAll(defendingTaflmen);
        allTaflmen.addAll(attackingTaflmen);

        char king = EMPTY;

        // precalc rank/file information

        // these store the presence of one side's pieces on each rank and
        // file. e.g. rankPresence[ATTACKERS][1] == true means that each
        // defender
        boolean[][] rankPresence = new boolean[2][boardSize];
        boolean[][] filePresence = new boolean[2][boardSize];

        // these store the position of the highest/lowest piece on
        // each rank and file, positive for attacker and negative
        // for defender. e.g. rankControlTmp[LOW][0] == -1 means that
        // the low piece on rank 0 is a defender on file 1.
        byte[][] rankHighLowTaflmen = new byte[2][boardSize];
        byte[][] fileHighLowTaflmen = new byte[2][boardSize];

        for(int i = 0; i < boardSize; i++) {
            rankHighLowTaflmen[LOW][i] = (byte) boardSize;
            fileHighLowTaflmen[LOW][i] = (byte) boardSize;
        }

        byte[] rankControl = new byte[boardSize];
        byte[] fileControl = new byte[boardSize];

//        byte[][] boardArray = new byte[boardSize][boardSize];
//        byte[] lastTaflmanRankIndex = new byte[boardSize];
//        byte[] lastTaflmanFileIndex = new byte[boardSize];
//
//        boolean[][] attackersCanMoveTo = new boolean[boardSize][boardSize];
//        boolean[][] defendersCanMoveTo = new boolean[boardSize][boardSize];

        Set<Coord> defenderMoves = new HashSet<>(boardSize * boardSize);
        Set<Coord> attackerMoves = new HashSet<>(boardSize * boardSize);

//        for(char taflman: allTaflmen) {
//            if(Taflman.getPackedSide(taflman) > 0)
//                attackerMoves.addAll(Taflman.getAllowableDestinations(state, taflman));
//            else
//                defenderMoves.addAll(Taflman.getAllowableDestinations(state, taflman));
//        }

        // Set up some lists to help us figure out who can move where
        for(char taflman : allTaflmen) {

            int side = (Taflman.getPackedSide(taflman) > 0 ? ATTACKER : DEFENDER);
            int mul = (side == ATTACKER ? 1 : -1);
            Coord coord = board.findTaflmanSpace(taflman);

//            boardArray[coord.y][coord.x] = (byte) mul;

            rankPresence[side][coord.y] = true;
            filePresence[side][coord.x] = true;

            boolean taflmanThreatened = false; //isTaflmanThreatened(coord, side, board, defenderMoves, attackerMoves);

            if(!taflmanThreatened) {
                if (coord.x < Math.abs(rankHighLowTaflmen[LOW][coord.y]))
                    rankHighLowTaflmen[LOW][coord.y] = (byte) (coord.x * mul);
                if (coord.x > Math.abs(rankHighLowTaflmen[HIGH][coord.y]))
                    rankHighLowTaflmen[HIGH][coord.y] = (byte) (coord.x * mul);

                if (coord.y < Math.abs(fileHighLowTaflmen[LOW][coord.x]))
                    fileHighLowTaflmen[LOW][coord.x] = (byte) (coord.y * mul);
                if (coord.y > Math.abs(fileHighLowTaflmen[HIGH][coord.x]))
                    fileHighLowTaflmen[HIGH][coord.x] = (byte) (coord.y * mul);
            }


            if(king == EMPTY && Taflman.isKing(taflman)) {
                king = taflman;
            }

        }

        // For each space along each rank and file,
        /*
        for(int rankFile = 0; rankFile < boardSize; rankFile++) {
            for(int space = 0; space < boardSize; space++) {
                // along the rank
                // boardArray[rankFile][space];

                // along the file
                // boardArray[space][rankFile];
            }
        }
        */


        for(int i = 0; i < boardSize; i++) {
            if (rankHighLowTaflmen[LOW][i] > 0 && rankHighLowTaflmen[HIGH][i] > 0) {
                rankControl[i] = ATTACKER;
            }
            else if (rankHighLowTaflmen[LOW][i] < 0 && rankHighLowTaflmen[HIGH][i] < 0) {
                rankControl[i] = DEFENDER;
            }

            if (fileHighLowTaflmen[LOW][i] > 0 && fileHighLowTaflmen[HIGH][i] > 0) {
                fileControl[i] = ATTACKER;
            }
            else if (fileHighLowTaflmen[LOW][i] < 0 && fileHighLowTaflmen[HIGH][i] < 0) {
                fileControl[i] = DEFENDER;
            }
        }

        Coord kingCoord = board.findTaflmanSpace(king);
        boolean kingCurrentlyStrong = (mKingStrength == Rules.KING_STRONG) ||
                (mKingStrength == Rules.KING_STRONG_CENTER && isCenterOrAdjacentCoord(kingCoord)) ||
                mKingStrength == Rules.KING_MIDDLEWEIGHT;


        // ==================== 1. KING FREEDOM ====================
        // If the king can get to an edge in two ways (edge win) or the corner
        // two ways (corner win) this is a victory, and we can report it as such.
        List<Coord> kingDestinations = Taflman.getAllowableDestinations(state, king);
        int kingCorners = 0;
        int kingEdges = 0;
        for (Coord dest : kingDestinations) {
            if (board.isEdgeSpace(dest)) {
                kingEdges++;
                if (board.getSpaceTypeFor(dest) == SpaceType.CORNER) kingCorners++;
            }
        }

        if (mEdgeEscape) {
            if (kingEdges >= 2) {
                if (debug) debugString += "Defender win--two ways to an edge\n";
                value = (short)(Evaluator.DEFENDER_WIN - (250 * (remainingDepth + 1)));
                if (debug) printDebug(value, state.getCurrentSide().isAttackingSide(), depth);
                return value;
            }

            if (kingEdges == 1) {
                value += changeEvaluation(DEFENDER, KING_FREEDOM_INDEX, 0.75f, "King edges: -0.75 FREEDOM");
            }
        } else if (mCornerEscape) {
            if (kingCorners >= 2) {
                if (debug) debugString += "Defender win--two ways to a corner\n";
                value = (short)(Evaluator.DEFENDER_WIN - (250 * (remainingDepth + 1)));
                if (debug) printDebug(value, state.getCurrentSide().isAttackingSide(), depth);
                return value;
            }

            if (kingCorners == 1) {
                value += changeEvaluation(DEFENDER, KING_FREEDOM_INDEX, 0.2f, "King one corner: -0.2 FREEDOM");
            }

            if (kingEdges >= 1) {
                value +=  changeEvaluation(DEFENDER, KING_FREEDOM_INDEX, 0.1f, "King edge access: -0.1 FREEDOM");
            }
        }

        // If the king is on a rank or file controlled by us, that's good.
        if(rankControl[kingCoord.y] == DEFENDER) {
            value += changeEvaluation(DEFENDER, KING_FREEDOM_INDEX, 0.1f, "King on our rank: -0.1 FREEDOM");
        }
        if(fileControl[kingCoord.x] == DEFENDER) {
            value += changeEvaluation(DEFENDER, KING_FREEDOM_INDEX, 0.1f, "King on our file: -0.1 FREEDOM");
        }

        // If the king has fewer destinations than boardSize, the attackers are doing well. If he has fewer
        // destinations than boardSize / 2, they're doing very well.

        if(kingDestinations.size() < boardSize) {
            value += changeEvaluation(ATTACKER, KING_FREEDOM_INDEX, 0.25f, "King has few destinations: 0.25 FREEDOM");
        }

        if(kingDestinations.size() < (boardSize / 2)) {
            value += changeEvaluation(ATTACKER, KING_FREEDOM_INDEX, 0.25f, "King has very few destinations: 0.25 FREEDOM");
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

        boolean kingThreatened = true; //isTaflmanThreatened(kingCoord,DEFENDER, board, defenderMoves, attackerMoves);
        if (mArmedKing && kingCurrentlyStrong) { // block max: defender 1, attacker 1
            if (enemyKingAdjacentTaflmen.size() <= 2) {
                // There's value in having an armed, strong king next to one or two enemy taflmen--leads to captures.
                value += changeEvaluation(DEFENDER, KING_RISK_INDEX, 0.1f * enemyKingAdjacentTaflmen.size(), "Strong armed king adjacent to attackers at -0.1 RISK per.");
            }
            else if (enemyKingAdjacentTaflmen.size() > 2 && kingThreatened) {
                // Having a king in check is risky.
                value += changeEvaluation(ATTACKER, KING_RISK_INDEX, 1, "Strong armed king in check: 1 RISK");
            }

            if (kingAdjacentTaflmen.size() > enemyKingAdjacentTaflmen.size()) {
                value += changeEvaluation(DEFENDER, KING_RISK_INDEX, 0.1f, "Strong armed king has bodyguard: -0.1 RISK");
            }
        }
        else if (kingCurrentlyStrong) { // block max: defender 1, attacker 1
            // if unarmed strong king, or weak armed or unarmed king
            if (enemyKingAdjacentTaflmen.size() > 2 && kingThreatened) {
                // Having a king in check is risky.
                value += changeEvaluation(ATTACKER, KING_RISK_INDEX, 1, "Strong unarmed king in check: 1 RISK");
            }

            if (kingAdjacentTaflmen.size() > enemyKingAdjacentTaflmen.size()) {
                value += changeEvaluation(DEFENDER, KING_RISK_INDEX, 0.1f, "Strong unarmed king has bodyguard: -0.1 RISK");
            }
        }
        else { // block max: defender 0.0, attacker 1
            // armed/unarmed weak kings
            if (enemyKingAdjacentTaflmen.size() == 1 && kingThreatened) {
                // Having a king in check is risky.
                value += changeEvaluation(ATTACKER, KING_RISK_INDEX, 1, "Weak king in check: 1 RISK");
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
            if (occupier != EMPTY && Taflman.getPackedSide(occupier) == Taflman.SIDE_ATTACKERS) {
                value += changeEvaluation(ATTACKER, RANK_AND_FILE_INDEX, 0.1f, "Corner point control: 0.1 RANK-FILE");
            }
        }

        if(debug) {
            debugString += "Control of corner diagonals: " + (value - debugValue) + "\n";
            debugValue = value;
        }

        // It's good for the attacker to control ranks and files. It's good for the defender to be alone
        // on ranks and files. The defenders like open ranks and files.
        // attacker max: 0.4
        // defender max: 0.8
        float attackerPerRankFile = 0.4f / boardSize / 2; // ranks and files
        float defenderPerRankFile = 0.8f / boardSize / 2; // ranks and files
        float defenderPerOutsideCordon = 0.4f / boardSize / 4; // ranks and files, high and low

        for(int i = 0; i < boardSize; i++) {
            if(rankPresence[DEFENDER][i] && rankControl[i] == DEFENDER && !rankPresence[ATTACKER][i])
                value += changeEvaluation(DEFENDER, RANK_AND_FILE_INDEX, defenderPerRankFile, "Defender rank control: ");
            else if(rankControl[i] == ATTACKER)
                value += changeEvaluation(ATTACKER, RANK_AND_FILE_INDEX, attackerPerRankFile, "Attacker rank control: ");

            if(filePresence[DEFENDER][i] && fileControl[i] == DEFENDER && !filePresence[ATTACKER][i])
                value += changeEvaluation(DEFENDER, RANK_AND_FILE_INDEX, defenderPerRankFile, "Defender file control: ");
            else if(fileControl[i] == ATTACKER)
                value += changeEvaluation(ATTACKER, RANK_AND_FILE_INDEX, attackerPerRankFile, "Attacker file control: ");

            // It's good for the defenders to be the high or low pieces on a rank or file, outside the attacker cordon.
            if(rankHighLowTaflmen[HIGH][i] < 0) value += changeEvaluation(DEFENDER, RANK_AND_FILE_INDEX, defenderPerOutsideCordon, "Defender outside cordon: ");
            if(rankHighLowTaflmen[LOW][i] < 0) value += changeEvaluation(DEFENDER, RANK_AND_FILE_INDEX, defenderPerOutsideCordon, "Defender outside cordon: ");
            if(fileHighLowTaflmen[HIGH][i] < 0) value += changeEvaluation(DEFENDER, RANK_AND_FILE_INDEX, defenderPerOutsideCordon, "Defender outside cordon: ");
            if(fileHighLowTaflmen[LOW][i] < 0) value += changeEvaluation(DEFENDER, RANK_AND_FILE_INDEX, defenderPerOutsideCordon, "Defender outside cordon: ");
        }

        if(debug) {
            debugString += "Control/aloneness on ranks and files: " + (value - debugValue) + "\n";
            debugValue = value;
        }

        // It's better to have more developed pieces than fewer developed pieces.
        // attacker max, defender max: 0.2
        int attackerDeveloped = 0;
        int defenderDeveloped = 0;

        final float fractionPerAttacker = 0.2f / mStartingAttackerCount;
        final float fractionPerDefender = 0.2f / mStartingDefenderCount;

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

        // It's better to develop pieces, but not at the expense of doing anything more interesting.
        value += changeEvaluation(DEFENDER, RANK_AND_FILE_INDEX, fractionPerDefender * defenderDeveloped, "Defender development: ");
        value += changeEvaluation(ATTACKER, RANK_AND_FILE_INDEX, fractionPerAttacker * attackerDeveloped, "Attacker development: ");

        if(debug) {
            debugString += "Taflman development: " + (value - debugValue) + "\n";
            debugValue = value;
        }

        // 5. ==================== MATERIAL COMPARISON ====================
        int defendersLost = mStartingDefenderCount - defendingTaflmenCount;
        int attackersLost = mStartingAttackerCount - attackingTaflmenCount;

        float attackerFraction = 0.7f / mStartingAttackerCount;
        float defenderFraction = 0.8f / mStartingDefenderCount;

        int defenderLossLevel = 0;
        int attackerLossLevel = 0;

        // The actual material count: 1/8, because expressing it in raw terms is super hard
        value += changeEvaluation(DEFENDER, MATERIAL_INDEX, attackerFraction * attackersLost, "Attacker losses: ");
        value += changeEvaluation(ATTACKER, MATERIAL_INDEX, defenderFraction * defendersLost, "Defender losses: ");

        if(defendersLost > mStandardTaflmanCount[DEFENDER]) defenderLossLevel = 2;
        else if(defendersLost > mLightTaflmanCount[DEFENDER]) defenderLossLevel = 1;

        if(attackersLost > mStandardTaflmanCount[ATTACKER]) attackerLossLevel = 2;
        else if(attackersLost > mLightTaflmanCount[ATTACKER]) attackerLossLevel = 1;

        // Relative material is worth a few taflmen, though

        // It's bad for the defenders if they've lost more than 20% more than the attackers
        if(defenderLossLevel > attackerLossLevel) {
            value += changeEvaluation(ATTACKER, MATERIAL_INDEX, 0.2f, "Defender is behind on material: 0.2 MATERIAL");
        }

        // It's bad for the attackers to be behind on the material race.
        if(attackerLossLevel > defenderLossLevel) {
            value += changeEvaluation(DEFENDER, MATERIAL_INDEX, 0.2f, "Attacker is behind on material: -0.2 MATERIAL");
        }

        if(attackerLossLevel == 2) {
            value += changeEvaluation(DEFENDER, MATERIAL_INDEX, 0.1f, "Attacker is on heavy losses: -0.1 MATERIAL");
        }


        if(debug) {
            debugString += "Material: " + (value - debugValue) + "\n";
            debugValue = value;
        }


        // 5. ==================== PIECE-SQUARE TABLE =====================
        if(mPieceSquareTable != null) {
            attackerFraction = 1f / mStartingAttackerCount;
            defenderFraction = 1f / (mStartingDefenderCount - 2);
            float kingFraction = (1f / (mStartingDefenderCount - 2)) * 3;

            for (char taflman : attackingTaflmen) {
                Coord space = board.findTaflmanSpace(taflman);
                value += changeEvaluation(ATTACKER, PIECE_SQUARE_INDEX, attackerFraction * mPieceSquareTable.getMultiplier(taflman, space), "Attacker PST " + space + ": ");
            }

            for (char taflman : defendingTaflmen) {
                Coord space = board.findTaflmanSpace(taflman);
                if (Taflman.isKing(taflman))
                    value += changeEvaluation(DEFENDER, PIECE_SQUARE_INDEX, kingFraction * mPieceSquareTable.getMultiplier(taflman, space), "King PST " + space + ": ");
                else
                    value += changeEvaluation(DEFENDER, PIECE_SQUARE_INDEX, defenderFraction * mPieceSquareTable.getMultiplier(taflman, space), "Defender PST " + space + ": ");
            }
        }

        if (debug) printDebug(value, state.getCurrentSide().isAttackingSide(), depth);
        return value;
    }

    private boolean isCenterOrAdjacentCoord(Coord coord) {
        return mRules.getBoard().getCenterAndAdjacentSpaces().contains(coord);
    }

    private HashMap<Coord, Boolean> mTaflmanThreatCache = new HashMap<>(60);

    public boolean isTaflmanThreatened(Coord taflmanCoord, int side, Board board, Set<Coord> defenderMoves, Set<Coord> attackerMoves) {
        if(mTaflmanThreatCache.containsKey(taflmanCoord)) return mTaflmanThreatCache.get(taflmanCoord);

        char taflman = board.getOccupier(taflmanCoord);
        int boardSize = board.getBoardDimension();
        List<Coord> adjacent = board.getAdjacentSpaces(taflmanCoord);

        int currentKingStrength = mRules.getKingStrengthMode();

        if(Taflman.isKing(taflman) && mKingStrength == Rules.KING_STRONG_CENTER) {
            if(board.getCenterAndAdjacentSpaces().contains(taflmanCoord)) {
                currentKingStrength = Rules.KING_STRONG;
            }
            else currentKingStrength = Rules.KING_WEAK;
        }

        if(!Taflman.isKing(taflman) || currentKingStrength == Rules.KING_WEAK) {
            List<Coord> toCheck = new ArrayList<Coord>();
            for (Coord space : adjacent) {
                char neighbor = EMPTY;
                if (board.getOccupier(space) != EMPTY) {
                    neighbor = board.getOccupier(space);
                }

                if (neighbor != EMPTY && Taflman.getPackedSide(neighbor) != side) {
                    toCheck.add(Coord.getCoordAcrossFrom(boardSize, taflmanCoord, space));
                }
                else if (board.isSpaceHostileTo(space, board.getOccupier(taflmanCoord))) {
                    toCheck.add(Coord.getCoordAcrossFrom(boardSize, taflmanCoord, space));
                }
            }

            if (toCheck.size() > 0) {
                for (Coord check : toCheck) {
                    if (side == ATTACKER) {
                        if (defenderMoves.contains(check)) {
                            mTaflmanThreatCache.put(taflmanCoord, true);
                            return true;
                        }
                    }
                    else {
                        if (attackerMoves.contains(check)) {
                            mTaflmanThreatCache.put(taflmanCoord, true);
                            return true;
                        }
                    }
                }
            }
        }
        else if(Taflman.isKing(taflman) && currentKingStrength == Rules.KING_MIDDLEWEIGHT) {
            int currentlyHostile = 0;
            int threatened = 0;
            for(Coord space : adjacent) {
                if(board.isSpaceHostileTo(space, taflman)) currentlyHostile++;
                else if(attackerMoves.contains(space)) threatened++;
            }

            return (currentlyHostile == adjacent.size() - 1) && threatened == 1;
        }
        else if(Taflman.isKing(taflman) && currentKingStrength == Rules.KING_STRONG) {
            if(adjacent.size() != 4) return false;

            int currentlyHostile = 0;
            int threatened = 0;
            for(Coord space : adjacent) {
                if(board.isSpaceHostileTo(space, taflman)) currentlyHostile++;
                else if(attackerMoves.contains(space)) threatened++;
            }

            return (currentlyHostile == 3) && threatened == 1;
        }
        return false;
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

    public boolean test() {
        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Heavy taflmen counts, values, defender/attacker");
        Utilities.printArray(mHeavyTaflmanCount);
        Utilities.printArray(mHeavyTaflmanValue);

        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Standard taflmen counts, values, defender/attacker");
        Utilities.printArray(mStandardTaflmanCount);
        Utilities.printArray(mStandardTaflmanValue);

        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Light taflmen counts, values, defender/attacker");
        Utilities.printArray(mLightTaflmanCount);
        Utilities.printArray(mLightTaflmanValue);

        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Defender, 1 taflman, KING_FREEDOM: " + changeEvaluation(ATTACKER, KING_FREEDOM_INDEX, 1));
        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Defender, 1 taflman, KING_FREEDOM: " + changeEvaluation(ATTACKER, KING_FREEDOM_INDEX, 1));
        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Defender, 1 taflman, KING_FREEDOM: " + changeEvaluation(ATTACKER, KING_FREEDOM_INDEX, 1));
        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Defender, 1 taflman, KING_FREEDOM: " + changeEvaluation(ATTACKER, KING_FREEDOM_INDEX, 1));
        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Defender, 1 taflman, KING_FREEDOM: " + changeEvaluation(ATTACKER, KING_FREEDOM_INDEX, 1));
        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Defender, 1 taflman, KING_FREEDOM: " + changeEvaluation(ATTACKER, KING_FREEDOM_INDEX, 1));
        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Defender, 1 taflman, KING_FREEDOM: " + changeEvaluation(ATTACKER, KING_FREEDOM_INDEX, 1));
        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Defender, 1 taflman, KING_FREEDOM: " + changeEvaluation(ATTACKER, KING_FREEDOM_INDEX, 1));
        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Defender, 1 taflman, KING_FREEDOM: " + changeEvaluation(ATTACKER, KING_FREEDOM_INDEX, 1));
        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Defender, 1 taflman, KING_FREEDOM: " + changeEvaluation(ATTACKER, KING_FREEDOM_INDEX, 1));
        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Defender, 1 taflman, KING_FREEDOM: " + changeEvaluation(ATTACKER, KING_FREEDOM_INDEX, 1));
        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Defender, 1 taflman, KING_FREEDOM: " + changeEvaluation(ATTACKER, KING_FREEDOM_INDEX, 1));

        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Defender, 1 taflman, KING_FREEDOM: " + changeEvaluation(DEFENDER, KING_FREEDOM_INDEX, 1));
        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Defender, 1 taflman, KING_FREEDOM: " + changeEvaluation(DEFENDER, KING_FREEDOM_INDEX, 1));
        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Defender, 1 taflman, KING_FREEDOM: " + changeEvaluation(DEFENDER, KING_FREEDOM_INDEX, 1));
        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Defender, 1 taflman, KING_FREEDOM: " + changeEvaluation(DEFENDER, KING_FREEDOM_INDEX, 1));
        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Defender, 1 taflman, KING_FREEDOM: " + changeEvaluation(DEFENDER, KING_FREEDOM_INDEX, 1));
        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Defender, 1 taflman, KING_FREEDOM: " + changeEvaluation(DEFENDER, KING_FREEDOM_INDEX, 1));
        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Defender, 1 taflman, KING_FREEDOM: " + changeEvaluation(DEFENDER, KING_FREEDOM_INDEX, 1));
        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Defender, 1 taflman, KING_FREEDOM: " + changeEvaluation(DEFENDER, KING_FREEDOM_INDEX, 1));

        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Defender, 1 taflman, KING_FREEDOM: " + changeEvaluation(ATTACKER, KING_FREEDOM_INDEX, 1));
        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Defender, 1 taflman, KING_FREEDOM: " + changeEvaluation(ATTACKER, KING_FREEDOM_INDEX, 1));
        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Defender, 1 taflman, KING_FREEDOM: " + changeEvaluation(ATTACKER, KING_FREEDOM_INDEX, 1));
        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Defender, 1 taflman, KING_FREEDOM: " + changeEvaluation(ATTACKER, KING_FREEDOM_INDEX, 1));
        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Defender, 1 taflman, KING_FREEDOM: " + changeEvaluation(ATTACKER, KING_FREEDOM_INDEX, 1));
        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Defender, 1 taflman, KING_FREEDOM: " + changeEvaluation(ATTACKER, KING_FREEDOM_INDEX, 1));


        return true;
    }
}
