package com.scc.dao;

import com.scc.model.Reservation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ReservationDao {
    public static final List<String> STATUSES =
            List.of("PENDING", "CONFIRMED", "CHECKED_IN", "COMPLETED", "CANCELLED");

    private final ActivityLogDao logDao = new ActivityLogDao();


    public int createReservation(int staffId, String guestName, String cottageName,
                                 String reservationDate, double totalAmount) {
        String sql = "INSERT INTO reservations (guest_name, cottage_name, reservation_date, total_amount, status, created_by) " +
                "VALUES (?, ?, ?, ?, 'PENDING', ?)";
        int newId = -1;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, guestName);
            ps.setString(2, cottageName);
            ps.setString(3, reservationDate);
            ps.setDouble(4, totalAmount);
            ps.setInt(5, staffId);
            if (ps.executeUpdate() > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) newId = keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating reservation: " + e.getMessage());
            return -1;
        }

        if (newId > 0) {
            logDao.logAction(staffId, "RESERVATION_MADE",
                    String.format("Created Reservation #%d for %s - %s on %s - Total: P%.2f",
                            newId, guestName, cottageName, reservationDate, totalAmount));
        }
        return newId;
    }


    public boolean updateStatus(int staffId, int reservationId, String newStatus) {
        Reservation current = findById(reservationId);
        if (current == null) return false;
        String oldStatus = current.status();
        String status = newStatus.toUpperCase();
        if (oldStatus.equals(status)) return false;

        String sql = "UPDATE reservations SET status = ? WHERE id = ?";
        boolean updated = false;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, reservationId);
            updated = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating reservation status: " + e.getMessage());
        }

        if (updated) {
            logDao.logAction(staffId, "RESERVATION_STATUS",
                    "Updated Reservation #" + reservationId + " status from " + oldStatus + " to " + status);
        }
        return updated;
    }

    public Reservation findById(int reservationId) {
        String sql = "SELECT id, guest_name, cottage_name, reservation_date, total_amount, status " +
                "FROM reservations WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding reservation: " + e.getMessage());
        }
        return null;
    }

    public List<Reservation> getAllReservations() {
        String sql = "SELECT id, guest_name, cottage_name, reservation_date, total_amount, status " +
                "FROM reservations ORDER BY id";
        List<Reservation> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving reservations: " + e.getMessage());
        }
        return list;
    }

    private Reservation mapRow(ResultSet rs) throws SQLException {
        return new Reservation(
                rs.getInt("id"),
                rs.getString("guest_name"),
                rs.getString("cottage_name"),
                rs.getString("reservation_date"),
                rs.getDouble("total_amount"),
                rs.getString("status"));
    }
}