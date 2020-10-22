package org.example.app.handlers;

import com.whalin.MemCached.MemCachedClient;
import com.whalin.MemCached.SockIOPool;
import org.example.app.models.User;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class MemCachedHandler {

    private final static MemCachedClient client;
    public static final String CACHE = "cache";

    static {
        String[] servers = {"localhost:11211"};
        SockIOPool pool = SockIOPool.getInstance(CACHE);
        pool.setMinConn(2);
        pool.setMaxConn(20);
        pool.setServers(servers);
        pool.setFailover(true);
        pool.setInitConn(30);
        pool.setMaintSleep(90);
        pool.setSocketTO(3000);
        pool.setAliveCheck(true);
        pool.initialize();

        client = new MemCachedClient(CACHE);
    }

    public static UUID addUser(User user, Date expireDate) {
        UUID uuid = UUID.nameUUIDFromBytes(user.getEmail().getBytes());
        add(uuid.toString(), user, expireDate);
        return uuid;
    }

    public static boolean add(String key, Serializable value, Date expiry) {
        return client.add(key, value, expiry);
    }

    public static boolean containsUser(User user) {
        return contains(getUUID(user).toString());
    }

    private static UUID getUUID(User user) {
        return UUID.nameUUIDFromBytes(user.getEmail().getBytes());
    }

    public static boolean contains(String key) {
        return client.keyExists(key) && Objects.nonNull(client.get(key));
    }

    public static UUID replaceUserDate(User user, Date expiryDate) {
        UUID uuid = getUUID(user);
        client.replace(uuid.toString(), user, expiryDate);
        return uuid;
    }
}
