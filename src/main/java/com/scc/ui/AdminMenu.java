package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class AdminMenu {
    private final UserDao userDao;
    private final Scanner scanner;

    public AdminMenu(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void display(User admin) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n=====================================");
            System.out.println("           ADMIN DASHBOARD           ");
            System.out.println("=====================================");
            System.out.println("1. View All Users");
            System.out.println("2. View Pending User Approvals");
            System.out.println("3. Approve User Registration");
            System.out.println("4. Add New User");
            System.out.println("5. Update User Role / Status");
            System.out.println("6. Delete User Account");
            System.out.println("7. Logout");
            System.out.print("Choose an option (1-7): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    viewAllUsers();
                    break;
                case "2":
                    viewPendingUsers();
                    break;
                case "3":
                    approveUserAccount();
                    break;
                case "4":
                    addNewUser();
                    break;
                case "5":
                    updateUserRoleOrStatus(admin);
                    break;
                case "6":
                    deleteUserAccount(admin);
                    break;
                case "7":
                    System.out.println("\nYou have been logged out.");
                    loggedIn = false;
                    break;
                default:
                    System.out.println("\nInvalid choice. Please choose between 1 and 7.");
            }
        }
    }

    private void viewAllUsers() {
        List<User> users = userDao.getAllUsers();
        printUserTable(users);
    }

    private void viewPendingUsers() {
        List<User> pending = userDao.getPendingUsers();
        if (pending == null || pending.isEmpty()) {
            System.out.println("No pending registrations.");
            return;
        }
        printUserTable(pending);
    }

    private void approveUserAccount() {
        List<User> pending = userDao.getPendingUsers();
        if (pending == null || pending.isEmpty()) {
            System.out.println("No pending registrations to approve.");
            return;
        }

        printUserTable(pending);
        Integer userId = readUserId("Enter Target User ID to approve (0 to cancel): ");
        if (userId == null) return;

        boolean inPendingList = pending.stream().anyMatch(u -> u.id() == userId);
        if (!inPendingList) {
            System.out.println("User ID " + userId + " is not in the pending list.");
            return;
        }

        if (userDao.approveUser(userId)) {
            System.out.println("User ID " + userId + " has been approved.");
        } else {
            System.out.println("Failed to approve user ID " + userId + ".");
        }
    }

    private void addNewUser() {
        System.out.print("Enter username (leave blank to cancel): ");
        String username = scanner.nextLine().trim();
        if (username.isEmpty()) return;

        if (userDao.usernameExists(username)) {
            System.out.println("Username '" + username + "' already exists.");
            return;
        }

        System.out.print("Enter password (leave blank to cancel): ");
        String password = scanner.nextLine().trim();
        if (password.isEmpty()) return;

        String role = chooseRole();
        if (role == null) return;

        if (userDao.addUser(username, password, role, "APPROVED")) {
            System.out.println("User '" + username + "' added successfully as " + role + ".");
        } else {
            System.out.println("Failed to add user.");
        }
    }

    private void updateUserRoleOrStatus(User admin) {
        List<User> users = userDao.getAllUsers();
        if (users == null || users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }

        printUserTable(users);
        Integer userId = readUserId("Enter Target User ID to update (0 to cancel): ");
        if (userId == null) return;

        User target = userDao.findById(userId);
        if (target == null) {
            System.out.println("No user found with ID " + userId + ".");
            return;
        }

        if (Objects.equals(target.id(), admin.id())) {
            System.out.println("You cannot change your own role or status.");
            return;
        }

        System.out.println("\n1. Change Role");
        System.out.println("2. Change Status");
        System.out.println("0. Cancel");
        System.out.print("Choose an option: ");

        String option = scanner.nextLine().trim();
        switch (option) {
            case "1":
                String newRole = chooseRole();
                if (newRole != null && userDao.updateUserRole(userId, newRole)) {
                    System.out.println("Role updated to " + newRole + ".");
                } else {
                    System.out.println("Failed or cancelled role update.");
                }
                break;
            case "2":
                System.out.println("1. APPROVED  2. PENDING  0. Cancel");
                System.out.print("Choose new status: ");
                String statusChoice = scanner.nextLine().trim();
                String newStatus = statusChoice.equals("1") ? "APPROVED" : statusChoice.equals("2") ? "PENDING" : null;
                if (newStatus != null && userDao.updateUserStatus(userId, newStatus)) {
                    System.out.println("Status updated to " + newStatus + ".");
                } else {
                    System.out.println("Failed or cancelled status update.");
                }
                break;
            default:
                System.out.println("Cancelled or invalid option.");
        }
    }

    private void deleteUserAccount(User admin) {
        List<User> users = userDao.getAllUsers();
        if (users == null || users.isEmpty()) return;

        printUserTable(users);
        Integer userId = readUserId("Enter Target User ID to delete (0 to cancel): ");
        if (userId == null) return;

        User target = userDao.findById(userId);
        if (target == null || Objects.equals(target.id(), admin.id())) {
            System.out.println("Action not allowed or user not found.");
            return;
        }

        System.out.print("Delete user '" + target.username() + "'? (y/n): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
            if (userDao.deleteUser(userId)) {
                System.out.println("User deleted successfully.");
            } else {
                System.out.println("Failed to delete user.");
            }
        }
    }

    private Integer readUserId(String prompt) {
        System.out.print(prompt);
        try {
            int userId = Integer.parseInt(scanner.nextLine().trim());
            if (userId == 0) return null;
            return userId;
        } catch (NumberFormatException e) {
            System.out.println("Invalid numeric ID.");
            return null;
        }
    }

    private String chooseRole() {
        System.out.println("Roles: 1. ADMIN  2. RECEPTIONIST  3. CUSTOMER  4. MAINTENANCE  0. Cancel");
        System.out.print("Choose role: ");
        String choice = scanner.nextLine().trim();
        return switch (choice) {
            case "1" -> "ADMIN";
            case "2" -> "RECEPTIONIST";
            case "3" -> "CUSTOMER";
            case "4" -> "MAINTENANCE";
            default -> null;
        };
    }

    private void printUserTable(List<User> users) {
        String format = "| %-5s | %-15s | %-15s | %-10s |%n";
        String border = "+-------+-----------------+-----------------+------------+";
        System.out.println(border);
        System.out.printf(format, "ID", "USERNAME", "ROLE", "STATUS");
        System.out.println(border);
        for (User u : users) {
            System.out.printf(format, u.id(), u.username(), u.roles(), u.status());
        }
        System.out.println(border);
    }
}