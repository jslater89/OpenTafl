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
    public static class GameContainer {
        public Game game;
        public List<DetailedMoveRecord> moves;

        public GameContainer(Game g, List<DetailedMoveRecord> m) {
            game = g;
            moves = m;
        }
    }

    public static boolean writeGameToFile(Game g, File f, boolean comments) {
        String gameRecord = getGameRecord(g, comments);

        try {
            PrintWriter pw = new PrintWriter(f);
            pw.print(gameRecord);
            pw.flush();
            pw.close();
        } catch (FileNotFoundException e) {
            return false;
        }
        return true;
    }
    
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
                        g.getClock().getClockEntry(g.getCurrentState().getAttackers()).toTimeSpec().toGameNotationString() + ", " +
                                g.getClock().getClockEntry(g.getCurrentState().getDefenders()).toTimeSpec().toGameNotationString();
                tagString += "[time-remaining:" + timeRemainingString + "]\n";
            }

            tagString += "[rules:" + g.getRules().getOTRString() + "]\n\n";

        }

        String movesString = (comments ? g.getCommentedHistoryString() : g.getHistoryString());

        return tagString + movesString;
    }

    public static GameContainer loadGameRecordFile(File gameFile) {
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

    public static GameContainer loadGameRecord(String gameRecord) {
        Map<String, String> tagMap = parseTags(gameRecord);
        //System.out.println(tagMap);

        Rules r = RulesSerializer.loadRulesRecord(tagMap.get("rules"));
        Game g = new Game(r, null);
        g.setTagMap(tagMap);
        g.loadClock();

        List<DetailedMoveRecord> moves = parseMoves(r.getBoard().getBoardDimension(), gameRecord);

        return new GameContainer(g, moves);
    }

    public static List<DetailedMoveRecord> parseMoves(int dimension, String gameRecord) {
        List<DetailedMoveRecord> moves = new ArrayList<>();
        String moveStart = "1. ";
        String moveEnd = "\n";
        String commentStart = "[";
        String commentMid = "|";
        String commentEnd = "]";

        int currentMove = 1;
        int matchIndex = -1;
        int lastMatchIndex = -1;
        int lastMovesAdded = -1;
        int commentIndex = -1;
        boolean inGame = false;
        boolean inMove = false;
        boolean inComment = false;
        for(int i = 0; i < gameRecord.length(); i++) {
            if(!inComment && !inMove && gameRecord.regionMatches(i, moveStart, 0, moveStart.length())) {
                lastMatchIndex = matchIndex;
                matchIndex = i;

                inGame = true;
                inMove = true;
                currentMove++;
                moveStart = currentMove + ".";
            }
            else if(inGame && !inComment && inMove && gameRecord.regionMatches(i, moveEnd, 0, moveEnd.length())) {
                lastMatchIndex = matchIndex;
                matchIndex = i;

                inMove = false;

                String moveString = gameRecord.substring(lastMatchIndex, matchIndex).replace(currentMove - 1 + ". ", "").trim();
                String[] moveStrings = moveString.split(" ");
                lastMovesAdded = 0;
                commentIndex = moves.size();
                for(String move : moveStrings) {
                    DetailedMoveRecord m = MoveSerializer.loadMoveRecord(dimension, move);
                    moves.add(m);
                    lastMovesAdded++;
                }
            }
            else if(inGame && !inComment && gameRecord.regionMatches(i, commentStart, 0, commentStart.length())) {
                lastMatchIndex = matchIndex;
                matchIndex = i;

                inComment = true;
            }
            else if(inGame && inComment && gameRecord.regionMatches(i, commentMid, 0, commentMid.length())) {
                lastMatchIndex = matchIndex;
                matchIndex = i;

                if(commentIndex < moves.size()) {
                    DetailedMoveRecord m = moves.get(commentIndex++);

                    // last match index + 1 to skip the opening brace
                    m.setComment(gameRecord.substring(lastMatchIndex + 1, matchIndex));
                }

            }
            else if(inGame && inComment && gameRecord.regionMatches(i, commentEnd, 0, commentEnd.length())) {
                lastMatchIndex = matchIndex;
                matchIndex = i;

                if(commentIndex < moves.size()) {
                    DetailedMoveRecord m = moves.get(commentIndex++);

                    // last match index + 1 to skip the separator pipe
                    m.setComment(gameRecord.substring(lastMatchIndex + 1, matchIndex));
                }

                inComment = false;
            }
        }

        //System.out.println(moves);
        return moves;
    }

    public static Map<String, String> parseTags(String gameRecord) {
        Map<String, String> tags = new LinkedHashMap<>();

        String gameStart = "1.";
        String tagStart = "[";
        String tagSeparator = ":";
        String tagEnd = "]";

        int matchIndex = -1;
        int lastMatchIndex = -1;
        boolean inTagTitle = false;
        boolean inTagBody = false;

        String currentTagTitle = "";
        String currentTagBody = "";

        for(int i = 0; i < gameRecord.length(); i++) {
            if(!inTagTitle && !inTagBody && gameRecord.regionMatches(i, tagStart, 0, tagStart.length())) {
                lastMatchIndex = matchIndex;
                matchIndex = i;

                inTagTitle = true;
                inTagBody = false;
            }
            else if(inTagTitle && !inTagBody && gameRecord.regionMatches(i, tagSeparator, 0, tagSeparator.length())) {
                lastMatchIndex = matchIndex;
                matchIndex = i;

                inTagTitle = false;
                inTagBody = true;

                currentTagTitle = gameRecord.substring(lastMatchIndex + 1, matchIndex);
            }
            else if(!inTagTitle && inTagBody && gameRecord.regionMatches(i, tagEnd, 0, tagEnd.length())) {
                lastMatchIndex = matchIndex;
                matchIndex = i;

                inTagTitle = false;
                inTagBody = false;

                currentTagBody = gameRecord.substring(lastMatchIndex + 1, matchIndex);
                tags.put(currentTagTitle, currentTagBody);
            }
            else if(!inTagTitle && !inTagBody && gameRecord.regionMatches(i, gameStart, 0, gameStart.length())) {
                break;
            }
        }

        return tags;
    }
}
