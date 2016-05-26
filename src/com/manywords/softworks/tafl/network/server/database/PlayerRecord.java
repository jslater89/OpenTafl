package com.manywords.softworks.tafl.network.server.database;

import java.util.Date;
import java.util.UUID;

/**
 * Created by jay on 5/26/16.
 */
public interface PlayerRecord {
    public String getUsername();
    public String getSalt();
    public String getSaltedPasswordHash();
    public Date getLastLoggedIn();
}
