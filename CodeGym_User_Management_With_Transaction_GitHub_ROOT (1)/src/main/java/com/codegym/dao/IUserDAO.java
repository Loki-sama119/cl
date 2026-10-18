package com.codegym.dao;

import com.codegym.model.User;
import java.sql.SQLException;
import java.util.List;

/** Declares CRUD operations for MySQL users. */
public interface IUserDAO {
    void insertUser(User user) throws SQLException;
    User selectUser(int id) throws SQLException;
    List<User> selectAllUsers() throws SQLException;
    /** Country substring search; sort is empty, name_asc or name_desc. */
    List<User> searchUsers(String country, String sort) throws SQLException;
    boolean deleteUser(int id) throws SQLException;
    boolean updateUser(User user) throws SQLException;

    /** Calls get_user_by_id stored procedure via JDBC CallableStatement. */
    User getUserById(int id) throws SQLException;

    /** Calls insert_user stored procedure via JDBC CallableStatement. */
    void insertUserStore(User user) throws SQLException;

    /** Atomic insert of user and permission assignments via JDBC transaction. */
    void addUserTransaction(User user, int[] permissionIds) throws SQLException;

    /** Demonstrates partial writes without transaction; resets Employee table. */
    void insertUpdateWithoutTransaction() throws SQLException;

    /** Transaction demo; failUpdate=true simulates a faulty UPDATE and triggers rollback. */
    void insertUpdateUseTransaction(boolean failUpdate) throws SQLException;

    /** Default demo: deliberately fail UPDATE to verify rollback. */
    default void insertUpdateUseTransaction() throws SQLException {
        insertUpdateUseTransaction(true);
    }
}
