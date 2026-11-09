package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.DatabaseInitializer;
import com.scc.dao.ReservationDao;
import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class Authentication {
    private static final Scanner sc = new Scanner(System.in);
    private static UserDao userDao;
    private static ActivityLogDao activityLogDao;
    private static ReservationDao reservationDao;

    public static void main(String[] args) {
        DatabaseInitializer.initializeDatabase();
        userDao = new UserDao();
        activityLogDao = new ActivityLogDao();
        reservationDao = new ReservationDao();

        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("============================================================");
            System.out.println("    FLOATING COTTAGE RESERVATION AND MANAGEMENT SYSTEM      ");
            System.out.println("============================================================");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            int choice;
            try {
                choice = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1:
                    login();
                    break;
                case 2:
                    System.out.print("Enter a new username: ");
                    String user = sc.nextLine().trim();
                    System.out.print("Enter a new password: ");
                    String pass = sc.nextLine().trim();
                    System.out.print("Select role [1] RECEPTIONIST [2] ADMIN [3] CUSTOMER [4] MAINTENANCE: ");
                    String roleChoice = sc.nextLine().trim();

                    String role;
                    switch (roleChoice) {
                        case "1":
                            role = "RECEPTIONIST";
                            break;
                        case "2":
                            role = "ADMIN";
                            break;
                        case "3":
                            role = "CUSTOMER";
                            break;
                        case "4":
                            role = "MAINTENANCE";
                            break;
                        default:
                            role = null;
                    }

                    if (role != null) {
                        userDao.registerUser(user, pass, role, "PENDING");
                        System.out.println("Registration submitted successfully! Waiting for admin approval.");
                    } else {
                        System.out.println("Invalid role selection. Please try again.");
                    }
                    break;
                case 3:
                    System.out.println("Exiting the system. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void login() {
        System.out.print("Enter username: ");
        String username = sc.nextLine().trim();
        System.out.print("Enter password: ");
        String password = sc.nextLine().trim();

        User user = userDao.login(username, password);

        if (user != null) {
            activityLogDao.logAction(user.id(), "USER_LOGIN",
                    "User " + user.username() + " (ID: " + user.id() + ") logged in.");
            System.out.println("Login successful! Welcome, " + user.username() + " | ROLE: " + user.roles());
            switch (user.roles().toUpperCase()) {
                case "ADMIN" -> new AdminMenu(userDao, activityLogDao, sc).display(user);
                case "RECEPTIONIST", "STAFF" -> new ReceptionistMenu(userDao, reservationDao, sc).display(user);
                case "CUSTOMER", "USER" -> new UserMenu(userDao, sc).display(user);
                default -> System.out.println("Unknown role. Access denied.");
            }
            logout(user);
        } else {
            System.out.println("Login failed. Invalid username, password, or account pending approval.");
        }
    }

    private static void logout(User user) {
        activityLogDao.logAction(user.id(), "USER_LOGOUT",
                "User " + user.username() + " (ID: " + user.id() + ") logged out.");
    }
}