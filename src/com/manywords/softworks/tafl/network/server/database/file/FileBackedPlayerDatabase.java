package com.manywords.softworks.tafl.network.server.database.file;

import com.manywords.softworks.tafl.network.server.NetworkServer;
import com.manywords.softworks.tafl.network.server.database.PlayerDatabase;
import com.manywords.softworks.tafl.network.server.database.PlayerRecord;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jay on 5/26/16.
 */
public class FileBackedPlayerDatabase extends PlayerDatabase {
    private File mDatabase;

    private List<FilePlayerRecord> mPlayerRecords = new ArrayList<>();

    public FileBackedPlayerDatabase(NetworkServer server, File database) {
        super(server);
        mDatabase = database;

        boolean success = true;
        if(!mDatabase.exists()) {
            if(!mDatabase.getParentFile().exists()) {
                success = mDatabase.getParentFile().mkdirs();
            }
            try {
                success &= mDatabase.createNewFile();
            } catch (IOException e) {
                success = false;
            }
        }

        if(!success) throw new RuntimeException("Could not create database!");
    }

    private synchronized int getPlayerIndex(String username) {
        int i = 0;
        for(PlayerRecord pr : mPlayerRecords) {
            if(username.equals(pr.getUsername())) return i;
            i++;
        }
        return -1;
    }

    @Override
    public synchronized PlayerRecord getPlayer(String username) {
        int index = getPlayerIndex(username);
        return (index == -1 ? null : mPlayerRecords.get(index));
    }

    @Override
    public synchronized boolean writePlayer(PlayerRecord dataHolder) {
        int index = getPlayerIndex(dataHolder.getUsername());
        if(index != -1) {
            mPlayerRecords.remove(index);
            mPlayerRecords.add(index, new FilePlayerRecord(dataHolder));
        }
        else {
            mPlayerRecords.add(new FilePlayerRecord(dataHolder));
        }

        flushDatabaseInternal();
        return true;
    }

    private synchronized void flushDatabaseInternal() {
        try {
            PrintWriter pw = new PrintWriter(mDatabase);

            for(FilePlayerRecord pr : mPlayerRecords) {
                pw.println(pr.toString());
            }

            pw.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Failed to write to database file");
        }
    }

    @Override
    public synchronized void flushDatabase() {
        // no-op: database is always flushed after writing
    }

    @Override
    public synchronized void updateDatabase() {
        mPlayerRecords.clear();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(mDatabase));

            String in;
            while((in = reader.readLine()) != null) {
                FilePlayerRecord r = new FilePlayerRecord(in);

                long time = System.currentTimeMillis();
                long offset = 3600 * 24 * 7 * 1000; // One week

                if(time - r.getLastLoggedIn().getTime() < offset) {
                    // Only load players who are less than a week old.
                    mPlayerRecords.add(r);
                }
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException("Failed to open database file");
        } catch (IOException e) {
            throw new RuntimeException("Failed to read database file");
        }

        // Flush the database to clear out any unused players.
        flushDatabaseInternal();
    }

    private class FilePlayerRecord extends GenericPlayerRecord {
        public FilePlayerRecord(PlayerRecord record) {
            super(record);
        }

        public FilePlayerRecord(String data) {
            String[] parts = data.split("\\|");

            username = parts[0];
            salt = parts[1];
            hashedPassword = parts[2];
            lastLoggedIn = new Date(Long.parseLong(parts[3]));
        }

        @Override
        public String toString() {
            return username + "|" + salt + "|" + hashedPassword + "|" + lastLoggedIn.getTime();
        }
    }
}
