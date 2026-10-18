package com.codegym.dao;

import com.codegym.model.User;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** JDBC implementation of CRUD, country search/name sort and stored procedures. */
public class UserDAO implements IUserDAO {
    private static final String INSERT_SQL =
            "INSERT INTO users (name, email, country) VALUES (?, ?, ?)";
    private static final String FIND_BY_ID_SQL =
            "SELECT id, name, email, country FROM users WHERE id = ?";
    private static final String FIND_ALL_SQL =
            "SELECT id, name, email, country FROM users ORDER BY id ASC";
    private static final String USER_COLUMNS = "SELECT id, name, email, country FROM users";
    private static final String UPDATE_SQL =
            "UPDATE users SET name = ?, email = ?, country = ? WHERE id = ?";
    private static final String DELETE_SQL =
            "DELETE FROM users WHERE id = ?";
    private static final String GET_USER_BY_ID_PROCEDURE = "{CALL get_user_by_id(?)}";
    private static final String INSERT_USER_PROCEDURE = "{CALL insert_user(?, ?, ?)}";

    private final String jdbcUrl;
    private final String jdbcUsername;
    private final String jdbcPassword;

    public UserDAO() {
        this(
            setting("JDBC_URL", "jdbc:mysql://localhost:3306/demo?useUnicode=true&characterEncoding=UTF-8"),
            setting("JDBC_USERNAME", "root"),
            setting("JDBC_PASSWORD", "")
        );
    }

    public UserDAO(String jdbcUrl, String jdbcUsername, String jdbcPassword) {
        this.jdbcUrl = jdbcUrl;
        this.jdbcUsername = jdbcUsername;
        this.jdbcPassword = jdbcPassword;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("Missing MySQL Connector/J. Check pom.xml.", ex);
        }
    }

    private static String setting(String name, String fallback) {
        String value = System.getenv(name);
        return value == null ? fallback : value;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcPassword);
    }

    @Override
    public void insertUser(User user) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getCountry());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getInt(1));
                }
            }
        }
    }

    @Override
    public User selectUser(int id) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? mapUser(rs) : null;
            }
        }
    }

    @Override
    public List<User> selectAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                users.add(mapUser(rs));
            }
        }
        return users;
    }

    /**
     * Search by a country fragment and optionally sort by user name.
     * All user-controlled text uses PreparedStatement binding; ORDER BY
     * is selected exclusively from fixed, known-safe SQL fragments.
     */
    @Override
    public List<User> searchUsers(String country, String sort) throws SQLException {
        String trimmedCountry = country == null ? "" : country.trim();
        String ordering = sort == null ? "" : sort.trim();

        StringBuilder sql = new StringBuilder(USER_COLUMNS);
        if (!trimmedCountry.isEmpty()) {
            sql.append(" WHERE LOWER(country) LIKE LOWER(?) ESCAPE '!'");
        }
        switch (ordering) {
            case "" -> sql.append(" ORDER BY id ASC");
            case "name_asc" -> sql.append(" ORDER BY name ASC, id ASC");
            case "name_desc" -> sql.append(" ORDER BY name DESC, id ASC");
            default -> throw new IllegalArgumentException("Invalid sort option");
        }

        List<User> users = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            if (!trimmedCountry.isEmpty()) {
                statement.setString(1, "%" + escapeLike(trimmedCountry) + "%");
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    users.add(mapUser(rs));
                }
            }
        }
        return users;
    }

    // Treat %, _ and ! as literal characters in a searched country name.
    private static String escapeLike(String text) {
        return text.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }

    /**
     * Call get_user_by_id(IN user_id INT). The procedure returns the
     * name/email/country fields; the requested ID is supplied by the caller.
     */
    @Override
    public User getUserById(int id) throws SQLException {
        try (Connection connection = getConnection();
             CallableStatement statement = connection.prepareCall(GET_USER_BY_ID_PROCEDURE)) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new User(id, rs.getString("name"), rs.getString("email"), rs.getString("country"));
            }
        }
    }

    /** Call insert_user(IN name, IN email, IN country). */
    @Override
    public void insertUserStore(User user) throws SQLException {
        try (Connection connection = getConnection();
             CallableStatement statement = connection.prepareCall(INSERT_USER_PROCEDURE)) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getCountry());
            statement.executeUpdate();
        }
    }

    /**
     * Atomic insertion: either both user and selected permissions are persisted,
     * or all writes are rolled back on the same JDBC Connection.
     */
    @Override
    public void addUserTransaction(User user, int[] permissionIds) throws SQLException {
        final String addUserSql = "INSERT INTO users (name, email, country) VALUES (?, ?, ?)";
        final String assignSql = "INSERT INTO user_permission (user_id, permission_id) VALUES (?, ?)";
        try (Connection connection = getConnection()) {
            boolean initialAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                int newUserId;
                try (PreparedStatement insert = connection.prepareStatement(addUserSql, Statement.RETURN_GENERATED_KEYS)) {
                    insert.setString(1, user.getName());
                    insert.setString(2, user.getEmail());
                    insert.setString(3, user.getCountry());
                    if (insert.executeUpdate() != 1) {
                        throw new SQLException("Unable to insert user");
                    }
                    try (ResultSet generated = insert.getGeneratedKeys()) {
                        if (!generated.next()) {
                            throw new SQLException("No generated user ID returned");
                        }
                        newUserId = generated.getInt(1);
                    }
                }
                if (permissionIds != null && permissionIds.length > 0) {
                    try (PreparedStatement assign = connection.prepareStatement(assignSql)) {
                        for (int permissionId : permissionIds) {
                            assign.setInt(1, newUserId);
                            assign.setInt(2, permissionId);
                            if (assign.executeUpdate() != 1) {
                                throw new SQLException("Unable to assign permission " + permissionId);
                            }
                        }
                    }
                }
                connection.commit();
                user.setId(newUserId);
            } catch (SQLException | RuntimeException ex) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    ex.addSuppressed(rollbackException);
                }
                throw ex;
            } finally {
                try {
                    connection.setAutoCommit(initialAutoCommit);
                } catch (SQLException restoreException) {
                    // Connection is closed by try-with-resources; no further operations allowed.
                }
            }
        }
    }

    @Override
    public boolean updateUser(User user) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getCountry());
            statement.setInt(4, user.getId());
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteUser(int id) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    private static User mapUser(ResultSet rs) throws SQLException {
        return new User(rs.getInt("id"), rs.getString("name"), rs.getString("email"), rs.getString("country"));
    }
}
