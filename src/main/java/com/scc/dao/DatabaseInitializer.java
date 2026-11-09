package com.scc.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    public static void initializeDatabase() {
        String sqlUsers = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "roles TEXT NOT NULL, " +
                "status TEXT DEFAULT 'PENDING');";

        String sqlActivityLogs = "CREATE TABLE IF NOT EXISTS activity_logs (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "action_type TEXT NOT NULL, " +
                "description TEXT NOT NULL, " +
                "timestamp DATETIME DEFAULT (datetime('now', 'localtime')), " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE);";

        String sqlReservations = "CREATE TABLE IF NOT EXISTS reservations (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "guest_name TEXT NOT NULL, " +
                "cottage_name TEXT NOT NULL, " +
                "reservation_date TEXT NOT NULL, " +
                "total_amount REAL NOT NULL, " +
                "status TEXT NOT NULL DEFAULT 'PENDING', " +
                "created_by INTEGER, " +
                "FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL);";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlUsers);
            stmt.execute(sqlActivityLogs);
            stmt.execute(sqlReservations);
            System.out.println("[DatabaseInitializer] -> Tables initialized successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}