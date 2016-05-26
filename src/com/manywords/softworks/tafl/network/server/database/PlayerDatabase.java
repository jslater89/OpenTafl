package com.manywords.softworks.tafl.network.server.database;

import com.manywords.softworks.tafl.network.PasswordHasher;
import com.manywords.softworks.tafl.network.server.NetworkServer;
import com.manywords.softworks.tafl.network.server.task.interval.IntervalTask;
import com.manywords.softworks.tafl.network.server.task.interval.IntervalTaskHolder;
import com.manywords.softworks.tafl.network.server.thread.PriorityTaskQueue;
import com.manywords.softworks.tafl.network.server.thread.ServerTickThread;

import java.util.Date;

/**
 * Created by jay on 5/26/16.
 */
public abstract class PlayerDatabase {
    protected NetworkServer mServer;

    public PlayerDatabase(NetworkServer server) {
        mServer = server;
    }

    /**
     * Get the player record for the given username.
     * @param username
     * @return A PlayerRecord object or null.
     */
    public abstract PlayerRecord getPlayer(String username);

    /**
     * Write a player record to the underlying database, without regard to whether the player already exists.
     * @param dataHolder
     * @return true if data is written, false if it is not.
     */
    public abstract boolean writePlayer(PlayerRecord dataHolder);

    /**
     * For databases which do not constantly update an on-disk representation, flush the current player DB
     * to disk.
     */
    public abstract void flushDatabase();

    /**
     * For databases which do not constantly read from an on-disk representation, verify that the DB contents
     * in memory match the contents in the file.
     */
    public abstract void updateDatabase();

    public void addUpdateTasks(ServerTickThread thread, PriorityTaskQueue queue) {
        IntervalTaskHolder updateTask = new IntervalTaskHolder(queue, 3600 * 1000, new IntervalTask() {
            @Override
            public void reset() {

            }

            @Override
            public void run() {
                updateDatabase();
            }
        }, PriorityTaskQueue.Priority.LOW);

        IntervalTaskHolder flushTask = new IntervalTaskHolder(queue, 600 * 1000, new IntervalTask() {
            @Override
            public void reset() {

            }

            @Override
            public void run() {
                flushDatabase();
            }
        }, PriorityTaskQueue.Priority.LOW);

        thread.addTaskHolder(updateTask);
        thread.addTaskHolder(flushTask);
    }

    public boolean registerPlayer(String username, String password) {
        if(playerExists(username)) return false;

        GenericPlayerRecord record = new GenericPlayerRecord();
        record.username = username;
        record.lastLoggedIn = new Date();
        record.salt = PasswordHasher.generateSalt();
        record.hashedPassword = PasswordHasher.hashPassword(record.salt, password);

        return writePlayer(record);
    }

    private boolean updatePlayerLoggedIn(PlayerRecord player) {
        GenericPlayerRecord record = new GenericPlayerRecord(player);
        record.lastLoggedIn = new Date();

        return writePlayer(record);
    }

    public boolean playerExists(String username) {
        return getPlayer(username) != null;
    }

    public boolean allowLoginFor(String username, String password) {
        // Register the player if this one doesn't exist.
        if(!playerExists(username)) return registerPlayer(username, password);

        // Don't allow double logins.
        if(mServer.hasClientNamed(username)) return false;

        PlayerRecord player = getPlayer(username);
        if(validatePassword(player, password)) {
            updatePlayerLoggedIn(player);
            return true;
        }

        return false;
    }

    public boolean validatePassword(PlayerRecord player, String incomingPassword) {
        String hashedIncomingPassword = PasswordHasher.hashPassword(player.getSalt(), incomingPassword);
        return player.getSaltedPasswordHash().equals(hashedIncomingPassword);
    }

    public class GenericPlayerRecord implements PlayerRecord {
        public GenericPlayerRecord() {

        }

        public GenericPlayerRecord(PlayerRecord other) {
            username = other.getUsername();
            hashedPassword = other.getSaltedPasswordHash();
            salt = other.getSalt();
            lastLoggedIn = other.getLastLoggedIn();
        }

        public String username;
        public String hashedPassword;
        public String salt;
        public Date lastLoggedIn;

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public String getSalt() {
            return salt;
        }

        @Override
        public String getSaltedPasswordHash() {
            return hashedPassword;
        }

        @Override
        public Date getLastLoggedIn() {
            return lastLoggedIn;
        }
    }
}
