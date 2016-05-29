package com.manywords.softworks.tafl.network.packet.pregame;

import com.manywords.softworks.tafl.engine.clock.TimeSpec;
import com.manywords.softworks.tafl.network.client.ClientGameInformation;
import com.manywords.softworks.tafl.network.packet.NetworkPacket;
import com.manywords.softworks.tafl.network.server.ServerGame;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jay on 5/24/16.
 */
public class GameListPacket extends NetworkPacket {
    public final List<ClientGameInformation> games;

    public static GameListPacket parse(String data) {
        data = data.replaceFirst("game-list", "");
        String[] records = data.split("\\|\\|");

        List<ClientGameInformation> games = new ArrayList<>();

        if(data.trim().isEmpty()) return new GameListPacket(games);

        for(String record : records) {
            String[] elements = record.split("\"");
            String uuid = elements[0].trim();
            String rulesName = elements[1];
            String attackerName = elements[3];
            String defenderName = elements[5];

            String[] unquotedElements = elements[6].trim().split(" ");

            boolean password = unquotedElements[0].contains("password:true");
            int spectators = Integer.parseInt(unquotedElements[1].replaceAll("[a-z]|\\s|:", ""));

            TimeSpec ts = TimeSpec.parseMachineReadableString(unquotedElements[2]);

            games.add(new ClientGameInformation(uuid, rulesName, attackerName, defenderName, password, spectators, ts.toMachineReadableString()));
        }

        return new GameListPacket(games);
    }

    public static GameListPacket parse(List<ServerGame> serverGames) {
        List<ClientGameInformation> games = new ArrayList<>(serverGames.size());

        for(ServerGame g : serverGames) {
            String uuidString = g.uuid.toString();
            String rulesName = g.getGame().getRules().getName() + " " + g.getGame().getRules().getBoard().getBoardDimension() + "x" + g.getGame().getRules().getBoard().getBoardDimension();
            String attackerUsername = g.getAttackerClient() == null ? "<none>" :g.getAttackerClient().getUsername();
            String defenderUsername = g.getDefenderClient() == null ? "<none>" :g.getDefenderClient().getUsername();
            boolean password = g.isPassworded();
            int spectators = g.getSpectators().size();
            TimeSpec ts;
            if(g.getGame().getClock() != null) {
                ts = g.getGame().getClock().toTimeSpec();
            }
            else {
                ts = new TimeSpec(0, 0, 0, 0);
            }

            games.add(new ClientGameInformation(uuidString, rulesName, attackerUsername, defenderUsername, password, spectators, ts.toMachineReadableString()));
        }

        return new GameListPacket(games);
    }

    public GameListPacket(List<ClientGameInformation> clientGames) {
        games = clientGames;
    }

    public String toString() {
        String result = "game-list ";
        for(ClientGameInformation g : games) {
            result += g.toString() + "||";
        }

        return result;
    }
}
