package com.manywords.softworks.tafl.network;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.clock.TimeSpec;
import com.manywords.softworks.tafl.network.packet.GameInformation;
import com.manywords.softworks.tafl.network.server.NetworkServer;
import com.manywords.softworks.tafl.network.server.ServerGame;
import com.manywords.softworks.tafl.rules.brandub.Brandub;
import com.manywords.softworks.tafl.rules.copenhagen.Copenhagen;
import com.manywords.softworks.tafl.rules.fetlar.Fetlar;
import com.manywords.softworks.tafl.rules.seabattle.SeaBattle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Created by jay on 5/25/16.
 */
public class NetworkDummyDataGenerator {
    public static List<GameInformation> generateDebugClientGames(int count) {
        List<GameInformation> games = new ArrayList<>();
        for(int i = 0; i < count; i++) {
            switch(new Random().nextInt(3)) {
                case 0:
                    games.add(new GameInformation(UUID.randomUUID().toString(), "Brandub 7x7", "Fishbreath", "otherguy", true, true, 0, new TimeSpec(0, 0, 0, 0).toMachineReadableString()));
                    break;
                case 1:
                    games.add(new GameInformation(UUID.randomUUID().toString(), "Tablut 15x15", "Shenmage", "parvusimperator", false, true, 2, new TimeSpec(0, 0, 0, 0).toMachineReadableString()));
                    break;
                case 2:
                    games.add(new GameInformation(UUID.randomUUID().toString(), "Foteviken Tablut 9x9", "Nasa", "OpenTafl AI", false, false, 28, new TimeSpec(0, 0, 0, 0).toMachineReadableString()));
                    break;
            }
        }

        return games;
    }

    public static List<ServerGame> getDummyGames(NetworkServer server, int count) {
        ArrayList<ServerGame> games = new ArrayList<>();

        for(int i = 0; i < count; i++) {
            switch(new Random().nextInt(4)) {
                case 0:
                    games.add(ServerGame.getDummyGame(server, "Fishbreath", "Shenmage", new Game(Brandub.newBrandub7(), null)));
                    break;
                case 1:
                    games.add(ServerGame.getDummyGame(server, "Nasa", "Shenmage", new Game(Fetlar.newFetlar11(), null)));
                    break;
                case 2:
                    games.add(ServerGame.getDummyGame(server, "Fishbreath", "parvusimperator", new Game(Copenhagen.newCopenhagen11(), null)));
                    break;
                case 3:
                    games.add(ServerGame.getDummyGame(server, "Nasa", "Shenmage", new Game(SeaBattle.newSeaBattle9(), null)));
                    break;
            }
        }

        return games;
    }
}
