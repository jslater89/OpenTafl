package com.manywords.softworks.tafl.engine.ai.evaluators;

import com.manywords.softworks.tafl.OpenTafl;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Taflman;

import java.util.ArrayList;
import java.util.List;

import static com.manywords.softworks.tafl.rules.Rules.*;

/**
 * Created by jay on 12/4/16.
 */
public class PieceSquareTable {
    private float[][] mTable;
    private Rules mRules;

    private int PIECE_DEFENDING_TAFLMAN = 0;
    private int PIECE_ATTACKING_TAFLMEN = 1;
    private int PIECE_KING = 2;

    public PieceSquareTable(Rules r, Game g) {
        mRules = r;

        int dimension = mRules.boardSize * mRules.boardSize;
        mTable = new float[3][dimension];

        initialize();
    }

    /*
     * Split the board into four main regions, with some subregions.
     * 1. Center: moderately bad for both sides, to discourage the attackers from making
     *    incautious center dives, and to encourage the defenders to spread out. The
     *    center is a diamond of diameter centerSize.
     *    a. Throne: good for the king to be on or next to the throne, if he is weak.
     * 2. Gutter: the region between the corners. Bad for defenders, good for king, good for attackers.
     * 3. Crucial: the region in between the center and the gutter. Generally controls movement
     *    around the board.
     * 4. Corners: a 3x3 grid with the corner at the corner (or a 5x5 grid for games with size-two corners)
     *    Good for defenders, although most of the
     *    a. The corner itself: great for the king, irrelevant for everyone else.
     *    b. The spaces next to the corner: bad for any piece which is captured on two sides.
     *    c. The diagonally adjacent to the corner: good for the attacker
     *    d. The space diagonally adjacent to c: good for both sides
     *    e. The spaces which make up the minimal blockoff: great for the attacker. (3 pieces in 3x3, 5 pieces in 5x5 corners)
     */
    private void initialize() {
        int dimension = mRules.boardSize;
        boolean hasCorners = mRules.getEscapeType() == Rules.CORNERS;

        int centerSize = 3;
        switch(dimension) {
            case 9:
                centerSize = 5;
                break;
            case 11:
                centerSize = 5;
                break;
            case 13:
                centerSize = 7;
                break;
            case 15:
                centerSize = 7;
                break;
            case 17:
                centerSize = 9;
                break;
            case 19:
                centerSize = 11;
                break;
        }

        int gutterSize = (dimension == 19 ? 2: 1);

        int crucialSize = dimension - centerSize - gutterSize;
        int cornerSize = (dimension == 19 ? 5 : 3);

        int centerLimit = (dimension / 2) - (centerSize / 2);

        List<Coord> centerSpaces = new ArrayList<>();
        List<Coord> centerCutouts = new ArrayList<>();
        List<Coord> crucialSpaces = new ArrayList<>();
        List<Coord> gutterSpaces = new ArrayList<>();
        List<Coord> cornerSpaces = new ArrayList<>();

        for(int y = 0; y < dimension / 2 + 1; y++) {
            for(int x = 0; x < dimension / 2 + 1; x++) {
                if(hasCorners && x < cornerSize && y < cornerSize) cornerSpaces.add(Coord.get(x, y));
                else if(x < gutterSize || y < gutterSize) gutterSpaces.add(Coord.get(x, y));
                else if(x >= centerLimit && y >= centerLimit) {
                    if((x+y) >= 3 * centerLimit) centerSpaces.add(Coord.get(x, y));
                    else centerCutouts.add(Coord.get(x, y));
                }
                else crucialSpaces.add(Coord.get(x, y));
            }
        }

//        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, centerSpaces);
//        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, crucialSpaces);
//        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, gutterSpaces);
//        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, cornerSpaces);

        setValues(dimension, hasCorners, gutterSize, crucialSize, centerSpaces, centerCutouts, crucialSpaces, gutterSpaces, cornerSpaces);
        mirrorValues(dimension);
    }

    private void setValues(int dimension, boolean hasCorners, int gutterSize, int crucialSize, List<Coord> centerSpaces, List<Coord> centerCutouts, List<Coord> crucialSpaces, List<Coord> gutterSpaces, List<Coord> cornerSpaces) {
        int centerIndex = dimension / 2 + 1;

        // Center stuff
        for(Coord c : centerSpaces) setValue(dimension, c, -0.1f);
        Coord center = Coord.get(centerIndex, centerIndex);
        mTable[PIECE_KING][Coord.getIndex(dimension, center)] = (mRules.getKingStrengthMode() == KING_STRONG ? -0.1f : 0f);

        for(Coord c: centerCutouts) {
            mTable[PIECE_DEFENDING_TAFLMAN][Coord.getIndex(dimension, c)] = 0.05f;
            mTable[PIECE_KING][Coord.getIndex(dimension, c)] = 0.05f;
            mTable[PIECE_ATTACKING_TAFLMEN][Coord.getIndex(dimension, c)] = 0.15f;
        }

        // Gutter stuff
        for(Coord c : gutterSpaces) {
            if(hasCorners) {
                mTable[PIECE_DEFENDING_TAFLMAN][Coord.getIndex(dimension, c)] = -0.1f;
                mTable[PIECE_ATTACKING_TAFLMEN][Coord.getIndex(dimension, c)] = -0.15f;
            }
            else {
                mTable[PIECE_DEFENDING_TAFLMAN][Coord.getIndex(dimension, c)] = 0.1f;
                mTable[PIECE_ATTACKING_TAFLMEN][Coord.getIndex(dimension, c)] = 0.0f;
            }
            mTable[PIECE_KING][Coord.getIndex(dimension, c)] = 0.25f;
        }

        // Crucial stuff
        int crucialStart = gutterSize;

        for(Coord c : crucialSpaces) {
            if(crucialSize > 1) {
                if(c.x == crucialStart || c.y == crucialStart) setValue(dimension, c, 0.25f);
                else if(dimension > 9 && (c.x == crucialStart + 1 || c.y == crucialStart + 1)) setValue(dimension, c, 0.33f);
                else setValue(dimension, c, 0.33f);
            }
            else setValue(dimension, c, 0.33f);
        }

        // Corner stuff

        // It's good to be close to the corner
        for(Coord c : cornerSpaces) {
            setValue(dimension, c, 0.25f);
        }

        // It's bad to be right next to the corner, unless you're a non-weak king.
        if(hasCorners) {
            List<Coord> cornerNeighbors = new ArrayList<>();
            if (dimension == 19) {
                cornerNeighbors.add(Coord.get(0, 2));
                cornerNeighbors.add(Coord.get(2, 0));
                cornerNeighbors.add(Coord.get(1, 2));
                cornerNeighbors.add(Coord.get(2, 1));
            }
            else {
                cornerNeighbors.add(Coord.get(0, 1));
                cornerNeighbors.add(Coord.get(1, 0));
            }

            for(Coord space : cornerNeighbors) {
                mTable[PIECE_ATTACKING_TAFLMEN][Coord.getIndex(dimension, space)] = -0.5f;
                mTable[PIECE_DEFENDING_TAFLMAN][Coord.getIndex(dimension, space)] = -0.5f;

                if (mRules.getKingStrengthMode() != KING_WEAK && mRules.getKingStrengthMode() != KING_STRONG_CENTER)
                    mTable[PIECE_KING][Coord.getIndex(dimension, space)] = 0.5f;
                else
                    mTable[PIECE_KING][Coord.getIndex(dimension, space)] = -0.5f;

            }

            // It's good to be on the corner diagonal point for everyone, but it's also the lynchpin of the attacker's
            // corner defense
            Coord cornerDiagonal;
            if (dimension == 19) cornerDiagonal = Coord.get(2, 2);
            else cornerDiagonal = Coord.get(1, 1);

            setValue(dimension, cornerDiagonal, 0.4f);
            mTable[PIECE_ATTACKING_TAFLMEN][Coord.getIndex(dimension, cornerDiagonal)] = 0.75f;

            List<Coord> cornerDefenseCoords = new ArrayList<>();
            if (dimension == 19) {
                cornerDefenseCoords.add(Coord.get(4, 0));
                cornerDefenseCoords.add(Coord.get(3, 1));
                cornerDefenseCoords.add(Coord.get(1, 3));
                cornerDefenseCoords.add(Coord.get(0, 4));
            }
            else {
                cornerDefenseCoords.add(Coord.get(2, 0));
                cornerDefenseCoords.add(Coord.get(0, 2));
            }

            if(dimension > 7) {
                for (Coord c : cornerDefenseCoords) {
                    mTable[PIECE_ATTACKING_TAFLMEN][Coord.getIndex(dimension, c)] = 0.66f;
                }
            }
        }
    }

    private void setValue(int dimension, Coord c, float value) {
        mTable[PIECE_ATTACKING_TAFLMEN][Coord.getIndex(dimension, c)] = value;
        mTable[PIECE_DEFENDING_TAFLMAN][Coord.getIndex(dimension, c)] = value;
        mTable[PIECE_KING][Coord.getIndex(dimension, c)] = value;
    }

    private void mirrorValues(int dimension) {
        int axis = dimension / 2;
        for(int y = 0; y < axis + 1; y++) {
            for(int x = 0; x < axis + 1; x++) {
                int xDiff = x - axis;
                int yDiff = y - axis;

                // x and y diff are always negative
                int noMirror = y * mRules.boardSize + x;
                int mirror1 = y * mRules.boardSize + (axis - xDiff);
                int mirror2 = (axis - yDiff) * mRules.boardSize + x;
                int mirror3 = (axis - yDiff) * mRules.boardSize + (axis - xDiff);

                for(int type = 0; type < 3; type++) {
                    mTable[type][mirror1] = mTable[type][noMirror];
                    mTable[type][mirror2] = mTable[type][noMirror];
                    mTable[type][mirror3] = mTable[type][noMirror];
                }
            }
        }
    }

    public void logTable(int type) {
        for(int y = 0; y < mRules.boardSize; y++) {
            for(int x = 0; x < mRules.boardSize; x++) {
                OpenTafl.logPrint(OpenTafl.LogLevel.CHATTY, String.format("%+.2f ", mTable[type][y * mRules.boardSize + x]));
            }
            OpenTafl.logPrint(OpenTafl.LogLevel.CHATTY, "\n");
        }
    }

    public float getMultiplier(char taflman, Coord space) {
        switch(Taflman.getPackedType(taflman)) {
            case Taflman.TYPE_COMMANDER:
            case Taflman.TYPE_KNIGHT:
            case Taflman.TYPE_TAFLMAN:
                if(Taflman.getPackedSide(taflman) == Taflman.SIDE_ATTACKERS) {
                    return mTable[PIECE_ATTACKING_TAFLMEN][Coord.getIndex(mRules.boardSize, space)];
                }
                else {
                    return mTable[PIECE_DEFENDING_TAFLMAN][Coord.getIndex(mRules.boardSize, space)];
                }
            case Taflman.TYPE_KING:
                return mTable[PIECE_KING][Coord.getIndex(mRules.boardSize, space)];
            default:
                return 0;
        }
    }
}
