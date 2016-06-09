package com.manywords.softworks.tafl.network.packet.pregame;

import com.manywords.softworks.tafl.engine.clock.TimeSpec;
import com.manywords.softworks.tafl.network.packet.GameInformation;
import com.manywords.softworks.tafl.network.packet.NetworkPacket;
import com.manywords.softworks.tafl.network.server.ServerGame;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jay on 5/24/16.
 */
public class GameListPacket extends NetworkPacket {
    public static final String PREFIX = "game-list";
    public final List<GameInformation> games;

    public static GameListPacket parse(String data) {
        data = data.replaceFirst("game-list", "");
        String[] records = data.split("\\|\\|");

        List<GameInformation> games = new ArrayList<>();

        if(data.trim().isEmpty()) return new GameListPacket(games);

        for(String record : records) {
            String[] elements = record.split("\"");
            String uuid = elements[0].trim();
            String rulesName = elements[1];
            String attackerName = elements[3];
            String defenderName = elements[5];

            String[] unquotedElements = elements[6].trim().split(" ");

            boolean password = unquotedElements[0].contains("password:true");
            boolean started = unquotedElements[1].contains("started:true");
            int spectators = Integer.parseInt(unquotedElements[2].replaceAll("[a-z]|\\s|:", ""));

            TimeSpec ts = TimeSpec.parseMachineReadableString(unquotedElements[3]);

            games.add(new GameInformation(uuid, rulesName, attackerName, defenderName, password, started, spectators, ts.toMachineReadableString()));
        }

        return new GameListPacket(games);
    }

    public static GameListPacket parse(List<ServerGame> serverGames) {
        List<GameInformation> games = new ArrayList<>(serverGames.size());

        for(ServerGame g : serverGames) {
            String uuidString = g.uuid.toString();
            String rulesName = g.getGame().getRules().getName() + " " + g.getGame().getRules().getBoard().getBoardDimension() + "x" + g.getGame().getRules().getBoard().getBoardDimension();
            String attackerUsername = g.getAttackerClient() == null ? "<none>" :g.getAttackerClient().getUsername();
            String defenderUsername = g.getDefenderClient() == null ? "<none>" :g.getDefenderClient().getUsername();
            boolean password = g.isPassworded();
            boolean started = g.hasGameStarted();
            int spectators = g.getSpectators().size();
            TimeSpec ts;
            if(g.getGame().getClock() != null) {
                ts = g.getGame().getClock().toTimeSpec();
            }
            else {
                ts = new TimeSpec(0, 0, 0, 0);
            }

            games.add(new GameInformation(uuidString, rulesName, attackerUsername, defenderUsername, password, started, spectators, ts.toMachineReadableString()));
        }

        return new GameListPacket(games);
    }

    public GameListPacket(List<GameInformation> clientGames) {
        games = clientGames;
    }

    public String toString() {
        String result = PREFIX + " ";
        for(GameInformation g : games) {
            result += g.toString() + "||";
        }

        return result;
    }
}
