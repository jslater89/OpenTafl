package com.manywords.softworks.tafl.notation;

import com.manywords.softworks.tafl.rules.Board;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Taflman;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jay on 2/13/16.
 */
public class PositionSerializer {
    public static String getPositionRecord(Board b) {
        char[][] board = b.getBoardArray();

        return getPositionRecord(board);
    }

    public static String getPositionRecord(char[][] board) {
        String otnString = "/";
        for(int y = 0; y < board.length; y++) {
            int emptyCount = 0;

            for(int x = 0; x < board.length; x++) {
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
}
