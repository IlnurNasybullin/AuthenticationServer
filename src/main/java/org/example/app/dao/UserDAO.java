package org.example.app.dao;

import org.example.app.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import java.lang.reflect.Type;
import java.sql.Types;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@PropertySource("database.properties")
public class UserDAO {
    private static Map<User, String> passwords;

    @Value("${database.tableName}")
    private String tableName;

    @Value("${database.userNameColumn}")
    private String userNameColumn;

    @Value("${database.emailColumn}")
    private String emailColumn;

    @Value("${database.passwordColumn}")
    private String passwordColumn;

    private static final String selectQuery = "SELECT * FROM \"%s\";";

    private static final String insertQuery = "INSERT INTO \"%s\" (\"%s\", \"%s\", \"%s\") VALUES (?, ?, ?)";

    private JdbcOperations jdbcOperations;

    @Autowired
    public UserDAO(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @PostConstruct
    public void fillMap() {
        if (Objects.nonNull(passwords)) {
            return;
        }

        passwords = new ConcurrentHashMap<>();

        String query = String.format(selectQuery, tableName);
        SqlRowSet rowSet = jdbcOperations.queryForRowSet(query);
        User user;
        while (rowSet.next()) {
            user = new User();
            user.setName(rowSet.getString(userNameColumn));
            user.setEmail(rowSet.getString(emailColumn));
            String password = rowSet.getString(passwordColumn);
            user.setPassword(password);

            passwords.put(user, password);
        }

        System.out.println(passwords);
    }

    public void add(User user) {
        passwords.put(user, user.getPassword());
        addInDatabase(user);
    }

    private void addInDatabase(User user) {
        String query = String.format(insertQuery, tableName, userNameColumn, emailColumn, passwordColumn);
        Object[] params = {user.getName(), user.getEmail(), user.getPassword()};
        int[] types = {Types.VARCHAR, Types.VARCHAR, Types.VARCHAR};;

        jdbcOperations.update(query, params, types);
    }

    public boolean containsUniqueUser(User user) {
        return passwords.containsKey(user);
    }

    public boolean contains(User user) {
        return Objects.equals(passwords.getOrDefault(user, null), user.getPassword());
    }
}
