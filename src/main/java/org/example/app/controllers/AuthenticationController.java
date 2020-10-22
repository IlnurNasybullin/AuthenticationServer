package org.example.app.controllers;

import org.example.app.dao.UserDAO;
import org.example.app.handlers.MemCachedHandler;
import org.example.app.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@RestController
public class AuthenticationController {

    public static final String JSESSION = "JSESSION";
    private static final int MAX_AGE = 30 * 60;
    public static final ZoneId ZONE_ID;

    private final UserDAO userDAO;

    static {
        ZONE_ID = ZoneId.systemDefault();
    }

    @Autowired
    public AuthenticationController(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @PostMapping("/registration")
    public Cookie registry(@RequestBody User user, HttpServletResponse response) throws IOException {
        if (userDAO.containsUniqueUser(user)) {
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
            return null;
        } else {
            UUID uuid = UUID.randomUUID();
            Cookie cookie = getCookie(uuid);
            userDAO.add(user);
            MemCachedHandler.add(uuid.toString(), user, getExpiryDate());
            response.setStatus(HttpServletResponse.SC_CREATED);
            return cookie;
        }
    }

    private Date getExpiryDate() {
        LocalDateTime date = LocalDateTime.now().plusSeconds(MAX_AGE);
        return Date.from(date.atZone(ZONE_ID).toInstant());
    }

    private Cookie getCookie(UUID uuid) {
        Cookie cookie = new Cookie(JSESSION, uuid.toString());
        cookie.setHttpOnly(true);
        cookie.setMaxAge(MAX_AGE);
        return cookie;
    }

    @GetMapping("/authentication")
    public void authenticate(@CookieValue(value = JSESSION, defaultValue = " ") String cookie, HttpServletResponse response) {
        System.out.println(cookie);
        if (MemCachedHandler.contains(cookie)) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    @GetMapping("/sign")
    public Cookie signIn(@RequestParam String email, @RequestParam String password, HttpServletResponse response) throws IOException {
        User user = new User();
        user.setPassword(password);
        user.setEmail(email);

        UUID uuid;
        boolean cache;

        if ((cache = MemCachedHandler.containsUser(user)) || userDAO.contains(user)) {
            if (cache) {
                uuid = MemCachedHandler.replaceUserDate(user, getExpiryDate());
            } else {
                uuid = MemCachedHandler.addUser(user, getExpiryDate());
            }
            Cookie cookie = getCookie(uuid);
            response.setStatus(HttpServletResponse.SC_OK);
            return cookie;
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }
    }
}
