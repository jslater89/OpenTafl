package com.manywords.softworks.tafl.notation;

import com.manywords.softworks.tafl.engine.DetailedMoveRecord;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.rules.Rules;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by jay on 3/22/16.
 */
public class GameSerializer {
    public static String getGameRecord(Game g, boolean comments) {
        String tagString = "";

        if(g.getTagMap().containsKey("rules")) {
            for(Map.Entry<String, String> entry : g.getTagMap().entrySet()) {
                tagString += "[" + entry.getKey() + ":" + entry.getValue() + "]\n";
            }

            tagString += "\n";
        }
        else {
            tagString += "[date:";
            String date = new SimpleDateFormat("yyyy.MM.dd").format(new Date());
            tagString += date + "]\n";

            tagString += "[result:";
            if (g.getCurrentState().checkVictory() == GameState.ATTACKER_WIN) tagString += "1";
            else if (g.getCurrentState().checkVictory() == GameState.DEFENDER_WIN) tagString += "-1";
            else if (g.getCurrentState().checkVictory() == GameState.DRAW) tagString += "0";
            else tagString += "?";
            tagString += "]\n";

            tagString += "[compiler:OpenTafl]\n";

            if (g.getClock() != null) {
                String timeString = g.getClock().toTimeSpec().toString();
                tagString += "[time-control:" + timeString + "]\n";

                String timeRemainingString =
                        g.getClock().getClockEntry(g.getCurrentState().getAttackers()).toTimeSpec().toGameNotationString() + " " +
                                g.getClock().getClockEntry(g.getCurrentState().getAttackers()).toTimeSpec().toGameNotationString();
                tagString += "[time-remaining:" + timeRemainingString + "]\n";
            }

            tagString += "[rules:" + g.getGameRules().getOTRString() + "]\n\n";

        }

        String movesString = (comments ? g.getCommentedHistoryString() : g.getHistoryString());

        return tagString + movesString;
    }

    public static Game loadGameRecordFile(File gameFile) {
        String gameString = "";
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(gameFile)));

            String line = "";
            while((line = r.readLine()) != null) {
                gameString += line + "\n";
            }

            return loadGameRecord(gameString);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Game file " + gameFile + " does not exist!");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read from game file " + gameFile);
        }
    }

    public static Game loadGameRecord(String gameRecord) {
        String[] gameLines = gameRecord.split("\n");
        Map<String, String> tagMap = parseTags(gameLines);
        System.out.println(tagMap);

        Rules r = RulesSerializer.loadRulesRecord(tagMap.get("rules"));
        Game g = new Game(r, null);
        g.setTagMap(tagMap);

        List<DetailedMoveRecord> moves = parseMoves(gameRecord);

        return g;
    }

    public static List<DetailedMoveRecord> parseMoves(String gameRecord) {
        List<DetailedMoveRecord> moves = new ArrayList<>();
        String moveStart = "1.";
        String moveEnd = "\n";
        String commentStart = "[";
        String commentMid = "|";
        String commentEnd = "]";

        int currentMove = 1;
        int matchIndex = -1;
        int lastMatchIndex = -1;
        int lastMovesAdded = -1;
        boolean inMove = false;
        boolean inComment = false;
        for(int i = 0; i < gameRecord.length(); i++) {
            if(!inComment && !inMove && gameRecord.regionMatches(i, moveStart, 0, moveStart.length())) {
                lastMatchIndex = matchIndex;
                matchIndex = i;

                inMove = true;
                currentMove++;
                moveStart = currentMove + ".";
            }
            else if(!inComment && inMove && gameRecord.regionMatches(i, moveEnd, 0, moveEnd.length())) {
                lastMatchIndex = matchIndex;
                matchIndex = i;

                inMove = false;

                String[] moveStrings = gameRecord.substring(lastMatchIndex, matchIndex).split(" ");
                DetailedMoveRecord m1 = MoveSerializer.loadMoveRecord(moveStrings[1]);
                moves.add(m1);
                lastMovesAdded = 1;
                if(moveStrings.length > 2) {
                    DetailedMoveRecord m2 = MoveSerializer.loadMoveRecord(moveStrings[2]);
                    moves.add(m2);
                    lastMovesAdded = 2;
                }
            }
            else if(!inComment && gameRecord.regionMatches(i, commentStart, 0, commentStart.length())) {
                lastMatchIndex = matchIndex;
                matchIndex = i;

                inComment = true;
            }
            else if(inComment && gameRecord.regionMatches(i, commentMid, 0, commentMid.length())) {
                lastMatchIndex = matchIndex;
                matchIndex = i;

                DetailedMoveRecord moveForFirstHalfComment = moves.get(moves.size() - lastMovesAdded);

                // last match index + 1 to skip the opening brace
                moveForFirstHalfComment.setComment(gameRecord.substring(lastMatchIndex + 1, matchIndex));
            }
            else if(inComment && gameRecord.regionMatches(i, commentEnd, 0, commentEnd.length())) {
                lastMatchIndex = matchIndex;
                matchIndex = i;

                if(lastMovesAdded == 2) {
                    DetailedMoveRecord moveForSecondHalfComment = moves.get(moves.size() - 1);

                    // last match index + 1 to skip the separator pipe
                    moveForSecondHalfComment.setComment(gameRecord.substring(lastMatchIndex + 1, matchIndex));
                }

                inComment = false;
            }
        }

        System.out.println(moves);
        return moves;
    }

    public static Map<String, String> parseTags(String[] gameLines) {
        Map<String, String> tags = new LinkedHashMap<>();

        for(String line : gameLines) {
            if(line.matches("\\[.*:.*\\]")) {
                line = line.replace("[", "");
                line = line.replace("]", "");

                int separatorIndex = line.indexOf(':');
                tags.put(line.substring(0, separatorIndex), line.substring(separatorIndex + 1, line.length()));
            }
            else {
                break;
            }
        }

        return tags;
    }

    public static int ordinalIndexOf(String str, char c, int n) {
        int pos = str.indexOf(c, 0);
        while (n-- > 0 && pos != -1)
            pos = str.indexOf(c, pos+1);
        return pos;
    }
}
