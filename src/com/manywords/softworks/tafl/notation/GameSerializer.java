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
import com.manywords.softworks.tafl.engine.replay.MoveAddress;
import com.manywords.softworks.tafl.engine.replay.ReplayGame;
import com.manywords.softworks.tafl.rules.Rules;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jay on 3/22/16.
 */
public class GameSerializer {
    public static class GameContainer {
        public Game game;
        public List<DetailedMoveRecord> moves;
        public List<VariationContainer> variations;

        public GameContainer(Game g, List<DetailedMoveRecord> m) {
            game = g;
            moves = m;
            variations = new ArrayList<>();
        }

        public GameContainer(Game g, List<DetailedMoveRecord> m, List<VariationContainer> v) {
            game = g;
            moves = m;
            variations = v;
        }
    }

    public static class VariationContainer {
        public MoveAddress address;
        public List<DetailedMoveRecord> moves;

        public VariationContainer(MoveAddress a, List<DetailedMoveRecord> m) {
            address = a;
            moves = m;
        }

        @Override
        public String toString() {
            return "V:" + address;
        }
    }

    private static class MoveParseResult {
        List<DetailedMoveRecord> moves;
        List<VariationContainer> variations;

        public MoveParseResult(List<DetailedMoveRecord> m, List<VariationContainer> v) {
            moves = m;
            variations = v;
        }
    }

    public static boolean writeGameToFile(Game g, File f, boolean comments) {
        String gameRecord = getGameRecord(g, comments);
        return writeGameRecordToFile(f, gameRecord);
    }

    public static boolean writeReplayToFile(ReplayGame rg, File f, boolean comments) {
        String gameRecord = getReplayGameRecord(rg, comments);
        return writeGameRecordToFile(f, gameRecord);
    }

    private static boolean writeGameRecordToFile(File f, String record) {
        try {
            PrintWriter pw = new PrintWriter(f);
            pw.print(record);
            pw.flush();
            pw.close();
        } catch (FileNotFoundException e) {
            return false;
        }
        return true;
    }

    public static String getReplayGameRecord(ReplayGame rg, boolean comments) {
        String tagString = getTagString(rg.getGame());
        String movesString = (comments ? rg.getCommentedHistoryString(true) : rg.getUncommentedHistoryString(true));

        return tagString + movesString;
    }
    
    public static String getGameRecord(Game g, boolean comments) {
        String tagString = getTagString(g);
        String movesString = (comments ? g.getCommentedHistoryString() : g.getHistoryString());

        return tagString + movesString;
    }

    private static String getTagString(Game g) {
        String tagString = "";

        if(g.getTagMap() == null || !g.getTagMap().containsKey("rules")) {
            g.setDefaultTags();
        }

        for(Map.Entry<String, String> entry : g.getTagMap().entrySet()) {
            tagString += "[" + entry.getKey() + ":" + entry.getValue() + "]\n";
        }

        tagString += "\n";

        return tagString;
    }

    public static GameContainer loadGameRecordFile(File gameFile) throws NotationParseException {
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

    public static GameContainer loadGameRecord(String gameRecord) throws NotationParseException {
        Map<String, String> tagMap = parseTags(gameRecord);
        //System.out.println(tagMap);

        Rules r = RulesSerializer.loadRulesRecord(tagMap.get("rules"));
        Game g = new Game(r, null);
        g.setTagMap(tagMap);
        g.loadClock();

        MoveParseResult result = parseMoves(r.getBoard().getBoardDimension(), gameRecord);

        return new GameContainer(g, result.moves, result.variations);
    }

    public static MoveParseResult parseMoves(int dimension, String gameRecord) throws NotationParseException {
        List<DetailedMoveRecord> moves = new ArrayList<>();
        List<VariationContainer> variations = new ArrayList<>();
        String moveStart = "1. ";
        String moveEnd = "\n";
        String commentStart = "[";
        String commentMid = "|";
        String commentEnd = "]";
        String variationStart = "";

        int currentMove = 1;
        int matchIndex = -1;
        int lastMatchIndex = -1;
        int lastMovesAdded = -1;
        int commentIndex = -1;
        boolean inGame = false;
        boolean inMove = false;
        boolean inVariation = false;
        boolean inComment = false;
        boolean lastMoveWasVariation = false;
        List<DetailedMoveRecord> lastTurn = new ArrayList<>();
        Pattern variationStartPattern = Pattern.compile("([0-9]+[a-z]\\.)([0-9]+\\.[0-9]+[a-z]\\.)+");

        for(int i = 0; i < gameRecord.length(); i++) {
            try {
                if (inGame && !inMove && !inComment && !inVariation) {
                    String remainingRecord = gameRecord.substring(i, gameRecord.length());
                    Matcher variationMatcher = variationStartPattern.matcher(remainingRecord);
                    boolean isVariationStart = variationMatcher.lookingAt();

                    if (isVariationStart) {
                        variationStart = variationMatcher.group(0);
                    }
                }

                // If not in anything, and now in a move
                if (!inComment && !inMove && !inVariation && gameRecord.regionMatches(i, moveStart, 0, moveStart.length())) {
                    lastMatchIndex = matchIndex;
                    matchIndex = i;

                    inGame = true;
                    inMove = true;
                    currentMove++;
                    moveStart = currentMove + ".";
                    lastTurn.clear();
                }
                // If in a move and leaving
                else if (inGame && !inComment && !inVariation && inMove && gameRecord.regionMatches(i, moveEnd, 0, moveEnd.length())) {
                    lastMatchIndex = matchIndex;
                    matchIndex = i;

                    String moveString = gameRecord.substring(lastMatchIndex, matchIndex).replace(currentMove - 1 + ". ", "").trim();
                    String[] moveStrings = moveString.split(" ");
                    lastMovesAdded = 0;
                    commentIndex = 0;
                    for (String move : moveStrings) {
                        DetailedMoveRecord m = MoveSerializer.loadMoveRecord(dimension, move);
                        moves.add(m);
                        lastTurn.add(m);
                        lastMovesAdded++;
                    }

                    lastMoveWasVariation = false;
                    inMove = false;
                }
                // If nowhere and entering a comment
                else if (inGame && !inComment && gameRecord.regionMatches(i, commentStart, 0, commentStart.length())) {
                    lastMatchIndex = matchIndex;
                    matchIndex = i;

                    inComment = true;
                }
                // If in a comment and encountering a comment separator
                else if (inGame && inComment && gameRecord.regionMatches(i, commentMid, 0, commentMid.length())) {
                    lastMatchIndex = matchIndex;
                    matchIndex = i;

                    List<DetailedMoveRecord> commentMoves;
                    if (lastMoveWasVariation) {
                        commentMoves = variations.get(variations.size() - 1).moves;
                    }
                    else {
                        commentMoves = lastTurn;
                    }

                    if (commentIndex < commentMoves.size()) {
                        DetailedMoveRecord m = commentMoves.get(commentIndex++);

                        // last match index + 1 to skip the opening brace
                        m.setComment(gameRecord.substring(lastMatchIndex + 1, matchIndex));
                    }

                }
                // If in a comment and finishing a comment
                else if (inGame && inComment && gameRecord.regionMatches(i, commentEnd, 0, commentEnd.length())) {
                    lastMatchIndex = matchIndex;
                    matchIndex = i;

                    List<DetailedMoveRecord> commentMoves;
                    if (lastMoveWasVariation) {
                        commentMoves = variations.get(variations.size() - 1).moves;
                    }
                    else {
                        commentMoves = lastTurn;
                    }

                    if (commentIndex < commentMoves.size()) {
                        DetailedMoveRecord m = commentMoves.get(commentIndex++);

                        // last match index + 1 to skip the separator pipe
                        m.setComment(gameRecord.substring(lastMatchIndex + 1, matchIndex));
                    }

                    inComment = false;
                }
                // If nowhere and entering a variation
                else if (inGame && !inComment && !inVariation && !variationStart.isEmpty() && gameRecord.regionMatches(i, variationStart, 0, variationStart.length())) {
                    lastMatchIndex = matchIndex;
                    matchIndex = i;

                    //variationStart is set by the regex at the top
                    inVariation = true;
                    lastTurn.clear();
                }
                // If in a variation and leaving
                else if (inGame && !inComment && !inMove && inVariation && gameRecord.regionMatches(i, moveEnd, 0, moveEnd.length())) {
                    lastMatchIndex = matchIndex;
                    matchIndex = i;

                    String moveString = gameRecord.substring(lastMatchIndex, matchIndex).replace(variationStart, "").trim();
                    String[] moveStrings = moveString.split(" ");
                    lastMovesAdded = 0;
                    commentIndex = 0;
                    int variationStartOffset = 0;
                    for (String move : moveStrings) {
                        // If the move matches this pattern, then it's a ..... dummy move, used to make the typesetting
                        // for variations beginning on the second or later move in a turn make a little more sense.
                        if (!move.matches("\\.+")) {
                            DetailedMoveRecord m = MoveSerializer.loadMoveRecord(dimension, move);
                            lastTurn.add(m);
                            lastMovesAdded++;
                        }
                        else {
                            variationStartOffset++;
                        }
                    }

                    MoveAddress a = MoveAddress.parseAddress(variationStart);
                    for (int j = 0; j < variationStartOffset; j++) {
                        a.increment(false);
                    }

                    VariationContainer v = new VariationContainer(a, new ArrayList<>(lastTurn));
                    variations.add(v);

                    lastMoveWasVariation = true;
                    inVariation = false;
                }
            }
            catch(Exception e) {
                int index = i;
                String context = gameRecord.substring(Math.max(index - 10, 0), Math.min(index + 10, gameRecord.length()));
                throw new NotationParseException(index, context, "Failed to parse game record");
            }
        }

        //System.out.println(moves);
        return new MoveParseResult(moves, variations);
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
