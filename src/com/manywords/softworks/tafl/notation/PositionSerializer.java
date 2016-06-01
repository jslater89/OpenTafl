/*
Under section 3(b)(ii) of the Free-As-In-Beer License, this file
is exempted from all terms and conditions of the license. It is
released instead under the Apache license 2.0:

Copyright 2015 Jay Slater

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.manywords.softworks.tafl.notation;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.rules.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jay on 2/13/16.
 */
public class PositionSerializer {
    public static String getPositionRecord(Board b) {
        System.out.println("Getting position record");
        char[][] board = b.getBoardArray();

        return getPositionRecord(board);
    }

    public static String getPositionRecord(char[][] board) {
        String otnString = "/";
        for(int y = 0; y < board.length; y++) {
            int emptyCount = 0;

            for(int x = 0; x < board.length; x++) {
                if(Taflman.isKing(board[y][x])) System.out.println(Taflman.getStringSymbol(board[y][x]) + " " + x + "," + y);

                if(board[y][x] == Taflman.EMPTY) {
                    emptyCount++;
                }
                else {
                    if(emptyCount > 0) {
                        otnString += emptyCount;
                        emptyCount = 0;
                    }
                    otnString += Taflman.getOtnStringSymbol(board[y][x]);
                }
            }

            if(emptyCount > 0) otnString += emptyCount;
            otnString += "/";
        }

        System.out.println("Done with position record");
        return otnString;
    }

    public static char[][] loadPositionRecord(String otnPosition) {
        String[] rawRows = otnPosition.split("/");
        List<String> rows = new ArrayList<String>();
        for(String row : rawRows) {
            if(row.length() > 0) rows.add(row);
        }

        char[][] board = new char[rows.size()][rows.size()];

        int currentAttackerId = 0;
        int currentDefenderId = 0;

        int currentRow = 0;
        for(String row : rows) {
            boolean inNumber = false;
            String numberSoFar = "";

            int currentCol = 0;
            for(int i = 0; i < row.length(); i++) {
                char c = row.charAt(i);

                // Catch multi-number digits
                if(Character.isDigit(c)) {
                    inNumber = true;
                    numberSoFar += c;
                    continue;
                }
                else if(inNumber && !Character.isDigit(c)) {
                    currentCol += Integer.parseInt(numberSoFar);
                    numberSoFar = "";
                    inNumber = false;
                }

                char side = 0;
                char id = 0;
                char type = 0;

                if(Character.isUpperCase(c)) {
                    side = Taflman.SIDE_DEFENDERS;
                    id = (char) currentDefenderId++;
                }
                else {
                    side = Taflman.SIDE_ATTACKERS;
                    id = (char) currentAttackerId++;
                }

                type = TaflmanCodes.getTaflmanTypeForCode(c);

                char taflman = Taflman.encode(id, type, side);
                Coord coord = Coord.get(currentCol, currentRow);

                board[coord.y][coord.x] = taflman;
                currentCol++;
            }

            currentRow++;
        }

        return board;
    }

    public static GameState loadPositionRecord(Rules rules, String otnPosition, Game g) {
        //char[][] board = loadPositionRecord(otnPosition);
        Board b = new GenericBoard(rules);
        List<List<Side.TaflmanHolder>> positionTaflmen = parseTaflmenFromPosition(otnPosition);
        Side attackers = new GenericSide(b, true, positionTaflmen.get(0));
        Side defenders = new GenericSide(b, false, positionTaflmen.get(1));
        b.setupTaflmen(attackers, defenders);

        return new GameState(g, g.getRules(), b, attackers, defenders);
    }

    public static List<List<Side.TaflmanHolder>> parseTaflmenFromPosition(String startPosition) {
        List<Side.TaflmanHolder> attackers = new ArrayList<Side.TaflmanHolder>();
        List<Side.TaflmanHolder> defenders = new ArrayList<Side.TaflmanHolder>();
        List<List<Side.TaflmanHolder>> taflmen = new ArrayList<>();
        taflmen.add(attackers);
        taflmen.add(defenders);

        char[][] boardArray = PositionSerializer.loadPositionRecord(startPosition);

        for(int y = 0; y < boardArray.length; y++) {
            for(int x = 0; x < boardArray.length; x++) {
                char taflman = boardArray[y][x];
                if(taflman != Taflman.EMPTY) {
                    Side.TaflmanHolder holder = new Side.TaflmanHolder(taflman, Coord.get(x, y));
                    if(Taflman.getPackedSide(taflman) == Taflman.SIDE_ATTACKERS) {
                        attackers.add(holder);
                    }
                    else {
                        defenders.add(holder);
                    }
                }
            }
        }

        return taflmen;
    }
}
