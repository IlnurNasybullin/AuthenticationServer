package org.example.app;

import java.util.UUID;

public class Test {
    public static void main(String[] args) {
        String email = "ilnur";
        UUID uuid = UUID.nameUUIDFromBytes(email.getBytes());
        System.out.println(uuid);
    }
}
