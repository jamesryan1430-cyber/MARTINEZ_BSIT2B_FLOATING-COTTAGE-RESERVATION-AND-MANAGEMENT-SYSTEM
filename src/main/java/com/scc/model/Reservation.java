package com.scc.model;

public record Reservation(int id, String guestName, String cottageName, String reservationDate,
                          double totalAmount, String status) {
}