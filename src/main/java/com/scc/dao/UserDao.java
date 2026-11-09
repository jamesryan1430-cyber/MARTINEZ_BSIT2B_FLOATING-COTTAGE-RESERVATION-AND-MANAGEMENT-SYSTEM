package com.scc.dao;

import com.scc.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserDao {
    private final ActivityLogDao logDao = new ActivityLogDao();

    public UserDao() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            defaultAdmin(conn);
        } catch (SQLException e) {
            System.err.println("Error seeding default admin: " + e.getMessage());
        }
    }

    public void defaultAdmin(Connection conn) {
        String checkSql = "SELECT COUNT(*) FROM users WHERE username = 'admin'";
        String insertsql = "INSERT INTO users (username, password, roles, status) VALUES ('admin', 'admin123', 'ADMIN', 'APPROVED')";

        try (PreparedStatement check = conn.prepareStatement(checkSql);
             ResultSet rs = check.executeQuery()) {

            if (rs.next() && rs.getInt(1) == 0) {
                try (PreparedStatement insert = conn.prepareStatement(insertsql)) {
                    insert.executeUpdate();
                }
                System.out.println("Default admin created successfully.");
            }
        } catch (SQLException e) {
            System.err.println("Error occurred while checking/creating default admin: " + e.getMessage());
        }
    }

    public boolean registerUser(String username, String password, String roles, String status) {
        String sql = "INSERT INTO users (username, password, roles, status) VALUES (?, ?, ?, 'PENDING')";

        int newUserId = -1;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, roles.toUpperCase());
            pstmt.executeUpdate();
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) newUserId = keys.getInt(1);
            }
            System.out.println("User registered successfully, Waiting for Approval.");

        } catch (SQLException e) {
            System.err.println("Error occurred while registering user: " + e.getMessage());
            return false;
        }

        if (newUserId > 0) {
            logDao.logAction(newUserId, "USER_REGISTERED",
                    "User " + username + " (ID: " + newUserId + ") registered as " + roles.toUpperCase()
                            + " and is awaiting approval.");
        }
        return true;
    }

    public User login(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("status");
                    if ("PENDING".equalsIgnoreCase(status)) {
                        System.out.println("Your account is pending approval. Please wait for admin approval.");
                        return null;
                    }
                    return new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("roles"),
                            status);
                } else {
                    System.out.println("Invalid username or password.");
                    return null;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error occurred while logging in: " + e.getMessage());
        }
        return null;
    }

    public boolean updateUserStatus(int adminId, int userId, String status) {
        String sql = "UPDATE users SET status = ? WHERE id = ?";

        boolean updated = false;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status.toUpperCase());
            pstmt.setInt(2, userId);
            updated = pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating user status: " + e.getMessage());
        }

        if (updated) {
            logDao.logAction(adminId, "USER_UPDATED",
                    "Admin " + adminId + " changed status of User " + userId + " to " + status.toUpperCase());
        }
        return updated;
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("roles"),
                rs.getString("status"));
    }


    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, password, roles, status FROM users ORDER BY id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapRowToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving users: " + e.getMessage());
        }
        return users;
    }


    public List<User> getPendingUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, password, roles, status FROM users WHERE status = 'PENDING' ORDER BY id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapRowToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving pending users: " + e.getMessage());
        }
        return users;
    }


    public boolean approveUser(int adminId, int userId) {
        String sql = "UPDATE users SET status = 'APPROVED' WHERE id = ?";
        boolean approved = false;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            approved = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error approving user: " + e.getMessage());
        }

        if (approved) {
            logDao.logAction(adminId, "USER_APPROVED",
                    "Admin " + adminId + " approved account for User ID " + userId);
        }
        return approved;
    }


    public User findById(int userId) {
        String sql = "SELECT id, username, password, roles, status FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding user: " + e.getMessage());
        }
        return null;
    }


    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking username: " + e.getMessage());
        }
        return false;
    }


    public boolean addUser(int adminId, String username, String password, String roles, String status) {
        String sql = "INSERT INTO users (username, password, roles, status) VALUES (?, ?, ?, ?)";
        int newUserId = -1;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, roles.toUpperCase());
            ps.setString(4, status.toUpperCase());
            if (ps.executeUpdate() > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) newUserId = keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding user: " + e.getMessage());
            return false;
        }

        if (newUserId > 0) {
            logDao.logAction(adminId, "USER_CREATED",
                    "Admin " + adminId + " created User " + newUserId + " (" + username + ") as " + roles.toUpperCase());
        }
        return newUserId > 0;
    }



    public boolean updateUserRole(int adminId, int userId, String roles) {
        String sql = "UPDATE users SET roles = ? WHERE id = ?";
        boolean updated = false;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roles.toUpperCase());
            ps.setInt(2, userId);
            updated = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user role: " + e.getMessage());
        }

        if (updated) {
            logDao.logAction(adminId, "USER_UPDATED",
                    "Admin " + adminId + " changed role of User " + userId + " to " + roles.toUpperCase());
        }
        return updated;
    }



    public boolean deleteUser(int adminId, int userId) {
        User target = findById(userId);
        String sql = "DELETE FROM users WHERE id = ?";
        boolean deleted = false;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            deleted = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
        }

        if (deleted) {
            String name = target != null ? " (" + target.username() + ")" : "";
            logDao.logAction(adminId, "USER_DELETED",
                    "Admin " + adminId + " deleted User " + userId + name);
        }
        return deleted;
    }
}