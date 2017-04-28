package com.manywords.softworks.tafl.network.client;

import com.manywords.softworks.tafl.Log;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.network.packet.ingame.*;
import com.manywords.softworks.tafl.network.packet.pregame.ClientListPacket;
import com.manywords.softworks.tafl.network.packet.pregame.StartGamePacket;
import com.manywords.softworks.tafl.network.packet.utility.ErrorPacket;
import com.manywords.softworks.tafl.network.packet.pregame.GameListPacket;
import com.manywords.softworks.tafl.network.packet.pregame.LobbyChatPacket;
import com.manywords.softworks.tafl.network.packet.utility.SuccessPacket;
import com.manywords.softworks.tafl.rules.Rules;

/**
 * Created by jay on 5/23/16.
 */
public class ClientCommandParser {

    public static void handlePacket(ClientServerConnection.ClientServerCallback callback, String data) {
        if(data.startsWith(LobbyChatPacket.PREFIX)) {
            LobbyChatPacket packet = LobbyChatPacket.parse(data);
            callback.onChatMessageReceived(ClientServerConnection.ChatType.LOBBY, packet.sender, packet.message);
        }
        else if(data.startsWith(ErrorPacket.PREFIX)) {
            ErrorPacket packet = ErrorPacket.parse(data);
            callback.onErrorReceived(packet.error);
        }
        else if(data.startsWith(SuccessPacket.PREFIX)) {
            SuccessPacket packet = SuccessPacket.parse(data);
            callback.onSuccessReceived(packet.message);
        }
        else if(data.startsWith(GameListPacket.PREFIX)) {
            GameListPacket packet = GameListPacket.parse(data);
            callback.onGameListReceived(packet.games);
        }
        else if(data.startsWith(ClientListPacket.PREFIX)) {
            ClientListPacket packet = ClientListPacket.parse(data);
            callback.onClientListReceived(packet.clients);
        }
        else if(data.startsWith(StartGamePacket.PREFIX)) {
            StartGamePacket packet = StartGamePacket.parse(data);
            callback.onStartGame(packet.rules, packet.history);
        }
        else if(data.startsWith(MoveResultPacket.PREFIX)) {
            MoveResultPacket packet = MoveResultPacket.parse(data);
            if(packet.moveResult != GameState.GOOD_MOVE) {
                callback.onErrorReceived(ErrorPacket.DESYNC);
            }
        }
        else if(data.startsWith(AwaitMovePacket.PREFIX)) {
            // Do nothing
        }
        else if(data.startsWith(MovePacket.PREFIX) && callback.getGame() != null) {
            Rules r = callback.getGame().getRules();
            MovePacket packet = MovePacket.parse(r.boardSize, data);
            callback.onServerMoveReceived(packet.move);
        }
        else if(data.startsWith(HistoryPacket.PREFIX)) {
            // History packet in game: refresh based on state.
            HistoryPacket h = HistoryPacket.parse(data);
            callback.onHistoryReceived(h.moves);
        }
        else if(data.startsWith(GameChatPacket.PREFIX)) {
            GameChatPacket packet = GameChatPacket.parse(data);
            callback.onChatMessageReceived(ClientServerConnection.ChatType.GAME, packet.sender, packet.message);
        }
        else if(data.startsWith(ClockUpdatePacket.PREFIX)) {
            ClockUpdatePacket packet = ClockUpdatePacket.parse(data);
            callback.onClockUpdateReceived(packet.attackerClock, packet.defenderClock);
        }
        else if(data.startsWith(VictoryPacket.PREFIX)) {
            VictoryPacket packet = VictoryPacket.parse(data);
            callback.onVictory(packet.victory);
        }
        else if(data.startsWith(GameEndedPacket.PREFIX)) {
            // Do nothing
        }
        else {
            Log.println(Log.Level.NORMAL, "Unknown packet! " + data);
        }
    }
}
