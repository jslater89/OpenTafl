package com.manywords.softworks.tafl.network.packet.pregame;

import com.manywords.softworks.tafl.network.packet.ClientInformation;
import com.manywords.softworks.tafl.network.packet.NetworkPacket;
import com.manywords.softworks.tafl.network.server.ServerClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jay on 5/24/16.
 */
public class ClientListPacket extends NetworkPacket {
    public static final String PREFIX = "client-list";
    public final List<ClientInformation> clients;

    public static ClientListPacket parse(String data) {
        data = data.replaceFirst(PREFIX, "");
        String[] records = data.split("\\|\\|");

        List<ClientInformation> clients = new ArrayList<>();

        if(data.trim().isEmpty()) return new ClientListPacket(clients);

        for(String record : records) {
            clients.add(new ClientInformation(record.trim()));
        }

        return new ClientListPacket(clients);
    }

    public static ClientListPacket parse(List<ServerClient> serverClients) {
        List<ClientInformation> clients = new ArrayList<>(serverClients.size());

        for(ServerClient c : serverClients) {
            if(c.getUsername() != null) clients.add(new ClientInformation(c.getUsername()));
        }

        return new ClientListPacket(clients);
    }

    public ClientListPacket(List<ClientInformation> clientGames) {
        clients = clientGames;
    }

    public String toString() {
        String result = PREFIX + " ";
        for(ClientInformation c : clients) {
            result += c.toString() + "||";
        }

        return result;
    }
}
