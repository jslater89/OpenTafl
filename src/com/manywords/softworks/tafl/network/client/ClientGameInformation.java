package com.manywords.softworks.tafl.network.client;

/**
 * Created by jay on 5/24/16.
 */
public class ClientGameInformation {
    public final String uuid;
    public final String rulesName;
    public final String attackerUsername;
    public final String defenderUsername;
    public final boolean password;
    public final int spectators;

    public ClientGameInformation(String uuid, String rulesName, String attackerUsername, String defenderUsername, boolean password, int spectators) {
        this.uuid = uuid;
        this.rulesName = rulesName;
        this.attackerUsername = attackerUsername;
        this.defenderUsername = defenderUsername;
        this.password = password;
        this.spectators = spectators;
    }

    public boolean hasFreeSide() {
        return attackerUsername.equals("<none>") || defenderUsername.equals("<none>");
    }

    public boolean freeSideAttackers() {
        return attackerUsername.equals("<none>");
    }

    public String toString() {
        return uuid + " \"" + rulesName + "\" " + " \"" + attackerUsername + "\" " + " \"" + defenderUsername + "\" " + "password:" + password + " spectators:" + spectators;
    }
}
