package org.example.app;

import com.whalin.MemCached.MemCachedClient;
import com.whalin.MemCached.SockIOPool;
import org.example.app.models.User;

import java.util.Date;

public class Test {

    private final static MemCachedClient client;
    public static final String CACHE = "cache";
    private static final int MAX_AGE = 30 * 60;

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

    public static void main(String[] args) throws InterruptedException {
        User user = new User();
        user.setPassword("a");
        user.setEmail("b");

        Date date = new Date();
        Date newDate = new Date(1_000 * MAX_AGE);

        System.out.println(newDate);

        client.add("b", true, newDate);
        System.out.println(client.get("b"));
        client.set("b", "st", newDate);
        System.out.println(client.get("b"));
    }
}
