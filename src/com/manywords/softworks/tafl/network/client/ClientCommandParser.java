package com.manywords.softworks.tafl.network.client;

import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.network.packet.ingame.GameChatPacket;
import com.manywords.softworks.tafl.network.packet.ingame.MovePacket;
import com.manywords.softworks.tafl.network.packet.ingame.MoveResultPacket;
import com.manywords.softworks.tafl.network.packet.pregame.StartGamePacket;
import com.manywords.softworks.tafl.network.packet.utility.ErrorPacket;
import com.manywords.softworks.tafl.network.packet.pregame.GameListPacket;
import com.manywords.softworks.tafl.network.packet.pregame.LobbyChatPacket;
import com.manywords.softworks.tafl.network.packet.utility.SuccessPacket;

/**
 * Created by jay on 5/23/16.
 */
public class ClientCommandParser {
    // TODO: handle clock updates
    // TODO: handle victory packets, game finished packets
    public static void handlePacket(ClientServerConnection.ClientServerCallback callback, String data) {
        if(data.startsWith("lobby-chat")) {
            LobbyChatPacket packet = LobbyChatPacket.parse(data);
            callback.onChatMessageReceived(ClientServerConnection.ChatType.LOBBY, packet.sender, packet.message);
        }
        else if(data.startsWith("error")) {
            ErrorPacket packet = ErrorPacket.parse(data);
            callback.onErrorReceived(packet.error);
        }
        else if(data.startsWith("success")) {
            SuccessPacket packet = SuccessPacket.parse(data);
            callback.onSuccessReceived(packet.message);
        }
        else if(data.startsWith("game-list")) {
            GameListPacket packet = GameListPacket.parse(data);
            callback.onGameListReceived(packet.games);
        }
        else if(data.startsWith("start-game")) {
            StartGamePacket packet = StartGamePacket.parse(data);
            callback.onStartGame(packet.rules);
        }
        else if(data.startsWith("move-result")) {
            MoveResultPacket packet = MoveResultPacket.parse(data);
            if(packet.moveResult != GameState.GOOD_MOVE) {
                callback.onErrorReceived(ErrorPacket.DESYNC);
            }
        }
        else if(data.startsWith("move")) {
            MovePacket packet = MovePacket.parse(data);
            callback.onServerMoveReceived(packet.move);
        }
        else if(data.startsWith("game-chat")) {
            GameChatPacket packet = GameChatPacket.parse(data);
            callback.onChatMessageReceived(ClientServerConnection.ChatType.GAME, packet.sender, packet.message);
        }
    }
}
