package org.example.app.controllers;

import org.example.app.handlers.CachedHandler;
import org.example.app.models.User;
import org.example.app.repositories.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@RestController
public class AuthenticationController {

    public static final int MAX_AGE = 30 * 60;
    public static final String JSESSION = "JSESSION";

    @Autowired
    @Qualifier("userRepository")
    private Repository<User> userRepository;

    @Autowired
    @Qualifier("memCachedHandler")
    private CachedHandler memCachedHandler;

    @PostMapping("/registration")
    public Cookie register(@RequestBody User user, HttpServletResponse response) {
        if (userRepository.contains(user)) {
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
            return null;
        }

        userRepository.insert(user);
        UUID uuid = getUUID(user.getEmail());
        memCachedHandler.add(uuid.toString(), true, MAX_AGE);
        Cookie cookie = getCookie(uuid);
        response.setStatus(HttpServletResponse.SC_CREATED);
        return cookie;
    }

    private Cookie getCookie(UUID uuid) {
        Cookie cookie = new Cookie(JSESSION, uuid.toString());
        cookie.setHttpOnly(true);
        cookie.setMaxAge(MAX_AGE);
        cookie.setPath("/");
        cookie.setDomain("localhost");

        return cookie;
    }

    @GetMapping("/sign")
    public Cookie signIn(@RequestParam String email, @RequestParam String password, HttpServletResponse response) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);

        if (!userRepository.check(user)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        UUID uuid = getUUID(email);
        String key = uuid.toString();
        if (memCachedHandler.contains(email)) {
            memCachedHandler.set(key, true, MAX_AGE);
        } else {
            memCachedHandler.add(key, true, MAX_AGE);
        }

        return getCookie(uuid);
    }

    @GetMapping("authentication")
    public void authenticate(@CookieValue(value = JSESSION, defaultValue = " ") String cookie, HttpServletResponse response) {
        System.out.println(cookie);
        if (memCachedHandler.contains(cookie)) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private UUID getUUID(String key) {
        return UUID.nameUUIDFromBytes(key.getBytes());
    }
}
