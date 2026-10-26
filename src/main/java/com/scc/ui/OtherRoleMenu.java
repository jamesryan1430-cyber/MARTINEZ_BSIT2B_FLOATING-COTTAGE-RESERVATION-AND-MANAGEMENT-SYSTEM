package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class OtherRoleMenu {
    private final UserDao userDao;
    private final Scanner scanner;
    private final User currentUser;

    public OtherRoleMenu(UserDao userDao, Scanner scanner, User currentUser) {
        this.userDao = userDao;
        this.scanner = scanner;
        this.currentUser = currentUser;
    }

    public void showMenu() {
        boolean active = true;
        while (active) {
            System.out.println("\n=== " + currentUser.getRole() + " DASHBOARD (" + currentUser.getUsername() + ") ===");
            System.out.println("1. View Profile Info");
            System.out.println("2. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    System.out.println("Username: " + currentUser.getUsername());
                    System.out.println("Assigned Role: " + currentUser.getRole());
                    System.out.println("Account Status: " + currentUser.getStatus());
                    break;
                case "2":
                    System.out.println("Logging out...");
                    active = false;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
}