package com.scc.model;

public record User(int id, String username, String password, String roles, String status) {
    @Override
    public String toString() {
        return String.format("[%d] %s (%s) - Status: %s", id, username, roles, status);
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return roles;
    }

    public String getStatus() {
        return status;
    }
}