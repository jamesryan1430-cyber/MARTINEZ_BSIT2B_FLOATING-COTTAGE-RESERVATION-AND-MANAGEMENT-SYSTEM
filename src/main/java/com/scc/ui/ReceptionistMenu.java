package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;
import java.util.Scanner;

public class ReceptionistMenu {
    private final UserDao userDao;
    private final Scanner scanner;

    public ReceptionistMenu(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void display(User staff) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n========================================");
            System.out.println("       RECEPTIONIST         ");
            System.out.println("========================================");
            System.out.println("1. Create Reservation");
            System.out.println("2. Create Walk-in Reservation");
            System.out.println("3. View / Edit Reservations");
            System.out.println("4. Record Payment");
            System.out.println("5. Check-In Guest");
            System.out.println("6. Check-Out Guest");
            System.out.println("0. Logout");
            System.out.println("----------------------------------------");
            System.out.print("Choose an option (0-6): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    System.out.println("\nCreate Reservation");
                    break;
                case "2":
                    System.out.println("\nCreate Walk-in Reservation");
                    break;
                case "3":
                    System.out.println("\nView / Edit Reservations");
                    break;
                case "4":
                    System.out.println("\nRecord Payment");
                    break;
                case "5":
                    System.out.println("\nCheck-In Guest");
                    break;
                case "6":
                    System.out.println("\nCheck-Out Guest");
                    break;
                case "0":
                    System.out.println("\nYou have been logged out.");
                    loggedIn = false;
                    break;
                default:
                    System.out.println("\nInvalid choice. Please select an option between 0 and 6.");
            }
        }
    }
}