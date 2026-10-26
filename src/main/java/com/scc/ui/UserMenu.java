package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;
import java.util.Scanner;

public class UserMenu {
    private final UserDao userDao;
    private final Scanner scanner;

    public UserMenu(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void display(User user) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n========================================");
            System.out.println("            CUSTOMER / GUEST MENU       ");
            System.out.println("========================================");
            System.out.println("1. View Available Cottages");
            System.out.println("2. Make a Reservation");
            System.out.println("3. View My Reservations");
            System.out.println("4. Cancel My Reservation");
            System.out.println("0. Logout");
            System.out.println("----------------------------------------");
            System.out.print("Choose an option (0-4): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    System.out.println("\nView Available Cottages");
                    break;
                case "2":
                    System.out.println("\nMake a Reservation");
                    break;
                case "3":
                    System.out.println("\nView My Reservations");
                    break;
                case "4":
                    System.out.println("\nCancel My Reservation");
                    break;
                case "0":
                    System.out.println("\nYou have been logged out.");
                    loggedIn = false;
                    break;
                default:
                    System.out.println("\nInvalid choice. Please select an option between 0 and 4.");
            }
        }
    }
}