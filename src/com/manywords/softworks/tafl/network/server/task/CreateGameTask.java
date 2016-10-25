package com.manywords.softworks.tafl.network.server.task;

import com.manywords.softworks.tafl.network.packet.pregame.CreateGamePacket;
import com.manywords.softworks.tafl.network.packet.utility.ErrorPacket;
import com.manywords.softworks.tafl.network.packet.utility.SuccessPacket;
import com.manywords.softworks.tafl.network.server.GameRole;
import com.manywords.softworks.tafl.network.server.NetworkServer;
import com.manywords.softworks.tafl.network.server.ServerClient;
import com.manywords.softworks.tafl.network.server.thread.PriorityTaskQueue;
import com.manywords.softworks.tafl.notation.NotationParseException;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.Rules;

/**
 * Created by jay on 5/26/16.
 */
public class CreateGameTask implements Runnable {
    private NetworkServer mServer;
    private ServerClient mClient;
    private CreateGamePacket mPacket;

    public CreateGameTask(NetworkServer server, ServerClient client, CreateGamePacket packet) {
        mServer = server;
        mClient = client;
        mPacket = packet;
    }

    @Override
    public void run() {
        boolean result;
        Rules rules = null;
        try {
            rules = RulesSerializer.loadRulesRecord(mPacket.otnRulesString);
        }
        catch(NotationParseException e) {
            mServer.sendPacketToClient(mClient, new ErrorPacket(ErrorPacket.BAD_SAVE), PriorityTaskQueue.Priority.LOW);
            return;

        }
        if(mPacket.timeSpec.isEnabled()) {
            result = mServer.createGame(mClient, mPacket.uuid, mPacket.passwordHash, rules, mPacket.attackingSide, mPacket.combineChat, mPacket.allowReplay, mPacket.timeSpec);
        }
        else {
            result = mServer.createGame(mClient, mPacket.uuid, mPacket.passwordHash, rules, mPacket.attackingSide, mPacket.combineChat, mPacket.allowReplay);
        }

        if(result) {
            mServer.sendPacketToClient(
                    mClient,
                    new SuccessPacket(mClient.getGameRole() == GameRole.ATTACKER ? SuccessPacket.JOINED_ATTACKERS : SuccessPacket.JOINED_DEFENDERS),
                    PriorityTaskQueue.Priority.STANDARD);
        }
        else {
            mServer.sendPacketToClient(mClient, new ErrorPacket(ErrorPacket.ALREADY_HOSTING), PriorityTaskQueue.Priority.LOW);
        }
    }
}
