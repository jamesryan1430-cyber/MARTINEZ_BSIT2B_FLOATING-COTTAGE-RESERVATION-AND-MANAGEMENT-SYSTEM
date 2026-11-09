package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.UserDao;
import com.scc.model.ActivityLog;
import com.scc.model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class AdminMenu {
    private final UserDao userDao;
    private final ActivityLogDao activityLogDao;
    private final Scanner scanner;

    public AdminMenu(UserDao userDao, ActivityLogDao activityLogDao, Scanner scanner) {
        this.userDao = userDao;
        this.activityLogDao = activityLogDao;
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
            System.out.println("7. View System Audit Logs");
            System.out.println("8. Logout");
            System.out.print("Choose an option (1-8): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    viewAllUsers();
                    break;
                case "2":
                    viewPendingUsers();
                    break;
                case "3":
                    approveUserAccount(admin);
                    break;
                case "4":
                    addNewUser(admin);
                    break;
                case "5":
                    updateUserRoleOrStatus(admin);
                    break;
                case "6":
                    deleteUserAccount(admin);
                    break;
                case "7":
                    auditLogMenu(admin);
                    break;
                case "8":
                    System.out.println("\nYou have been logged out.");
                    loggedIn = false;
                    break;
                default:
                    System.out.println("\nInvalid choice. Please choose between 1 and 8.");
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

    private void approveUserAccount(User admin) {
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

        if (userDao.approveUser(admin.id(), userId)) {
            System.out.println("User ID " + userId + " has been approved.");
        } else {
            System.out.println("Failed to approve user ID " + userId + ".");
        }
    }

    private void addNewUser(User admin) {
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

        if (userDao.addUser(admin.id(), username, password, role, "APPROVED")) {
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
                if (newRole != null && userDao.updateUserRole(admin.id(), userId, newRole)) {
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
                if (newStatus != null && userDao.updateUserStatus(admin.id(), userId, newStatus)) {
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
            if (userDao.deleteUser(admin.id(), userId)) {
                System.out.println("User deleted successfully.");
            } else {
                System.out.println("Failed to delete user.");
            }
        }
    }

    private void auditLogMenu(User admin) {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n=====================================");
            System.out.println("         SYSTEM AUDIT LOGS           ");
            System.out.println("=====================================");
            System.out.println("1. View All System Logs");
            System.out.println("2. Filter Logs by User ID");
            System.out.println("3. Clear All Logs");
            System.out.println("0. Back to Admin Dashboard");
            System.out.print("Choose an option (0-3): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    printLogTable(activityLogDao.getAllLogs());
                    break;
                case "2":
                    filterLogsByUser();
                    break;
                case "3":
                    clearLogs(admin);
                    break;
                case "0":
                    inMenu = false;
                    break;
                default:
                    System.out.println("\nInvalid choice. Please choose between 0 and 3.");
            }
        }
    }

    private void filterLogsByUser() {
        // Re-prompts on non-numeric input instead of failing; 0 returns to the audit menu.
        while (true) {
            System.out.print("Enter User ID to filter by (0 to cancel): ");
            String input = scanner.nextLine().trim();
            int userId;
            try {
                userId = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a numeric User ID.");
                continue;
            }
            if (userId <= 0) return;

            List<ActivityLog> logs = activityLogDao.getLogsByUserId(userId);
            if (logs.isEmpty()) {
                System.out.println("No logs found for User ID " + userId + ".");
            } else {
                printLogTable(logs);
            }
            return;
        }
    }

    private void clearLogs(User admin) {
        System.out.println("WARNING: This permanently deletes ALL audit log entries.");
        System.out.print("Type CLEAR to confirm (anything else cancels): ");
        if (!scanner.nextLine().trim().equals("CLEAR")) {
            System.out.println("Cancelled. No logs were deleted.");
            return;
        }

        int removed = activityLogDao.clearAllLogs();
        if (removed < 0) {
            System.out.println("Failed to clear logs.");
            return;
        }
        System.out.println(removed + " log entries cleared.");
        // Leave a trace that the clear itself happened.
        activityLogDao.logAction(admin.id(), "LOGS_CLEARED",
                "Admin " + admin.id() + " cleared " + removed + " audit log entries");
    }

    private void printLogTable(List<ActivityLog> logs) {
        if (logs == null || logs.isEmpty()) {
            System.out.println("No audit logs recorded yet.");
            return;
        }

        String format = "%-5s | %-8s | %-18s | %-35s | %-20s%n";
        String divider = "-".repeat(5 + 3 + 8 + 3 + 18 + 3 + 35 + 3 + 20);
        System.out.println();
        System.out.println(divider);
        System.out.printf(format, "ID", "USER ID", "ACTION TYPE", "DESCRIPTION", "TIMESTAMP");
        System.out.println(divider);
        for (ActivityLog log : logs) {
            // Long descriptions wrap onto extra lines so no text is cut off.
            List<String> lines = wrap(log.description(), 35);
            System.out.printf(format, log.id(), log.userId(), truncate(log.actionType(), 18), lines.get(0), log.timestamp());
            for (int i = 1; i < lines.size(); i++) {
                System.out.printf(format, "", "", "", lines.get(i), "");
            }
        }
        System.out.println(divider);
        System.out.println(logs.size() + " log entr" + (logs.size() == 1 ? "y" : "ies") + " shown.");
    }

    /** Keeps an over-long value from pushing the table columns out of alignment. */
    private static String truncate(String text, int width) {
        if (text == null) return "";
        return text.length() <= width ? text : text.substring(0, width - 1) + "~";
    }

    private static List<String> wrap(String text, int width) {
        List<String> lines = new ArrayList<>();
        String rest = text == null ? "" : text.trim();
        while (rest.length() > width) {
            int cut = rest.lastIndexOf(' ', width);
            if (cut <= 0) cut = width;
            lines.add(rest.substring(0, cut).trim());
            rest = rest.substring(cut).trim();
        }
        lines.add(rest);
        return lines;
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