package com.manywords.softworks.tafl.network.packet;

/**
 * Created by jay on 6/2/16.
 */
public class ClientInformation {
    private final String username;

    public ClientInformation(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return username;
    }
}
