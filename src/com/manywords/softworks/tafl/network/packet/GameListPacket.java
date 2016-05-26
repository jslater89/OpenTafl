package com.manywords.softworks.tafl.network.packet;

import com.manywords.softworks.tafl.network.client.ClientGameInformation;
import com.manywords.softworks.tafl.network.server.ServerGame;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by jay on 5/24/16.
 */
public class GameListPacket extends NetworkPacket {
    public final List<ClientGameInformation> games;

    public static GameListPacket parse(String data) {
        data = data.replaceFirst("game-list", "");
        String[] records = data.split("\\|");

        List<ClientGameInformation> games = new ArrayList<>();

        if(data.trim().isEmpty()) return new GameListPacket(games);

        for(String record : records) {
            String[] elements = record.split("\"");
            String uuid = elements[0].trim();
            String rulesName = elements[1];
            String attackerName = elements[3];
            String defenderName = elements[5];

            boolean password = elements[6].contains("password:true");
            int spectators = Integer.parseInt(elements[6].replaceAll("[a-z]|\\s|:", ""));

            games.add(new ClientGameInformation(uuid, rulesName, attackerName, defenderName, password, spectators));
        }

        return new GameListPacket(games);
    }

    public static GameListPacket parse(List<ServerGame> serverGames) {
        List<ClientGameInformation> games = new ArrayList<>(serverGames.size());

        for(ServerGame g : serverGames) {
            String uuidString = g.uuid.toString();
            String rulesName = g.getGame().getRules().getName();
            String attackerUsername = g.getAttackerClient().getUsername();
            String defenderUsername = g.getDefenderClient().getUsername();
            boolean password = g.isPassworded();
            int spectators = g.getSpectators().size();

            games.add(new ClientGameInformation(uuidString, rulesName, attackerUsername, defenderUsername, password, spectators));
        }

        return new GameListPacket(games);
    }

    public GameListPacket(List<ClientGameInformation> clientGames) {
        games = clientGames;
    }

    public String toString() {
        String result = "game-list ";
        for(ClientGameInformation g : games) {
            result += g.toString() + "|";
        }

        return result;
    }
}
