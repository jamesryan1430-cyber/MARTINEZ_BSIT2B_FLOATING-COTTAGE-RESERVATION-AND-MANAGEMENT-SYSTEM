package com.scc.ui;

import com.scc.dao.ReservationDao;
import com.scc.dao.UserDao;
import com.scc.model.Reservation;
import com.scc.model.User;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class ReceptionistMenu {
    private final UserDao userDao;
    private final ReservationDao reservationDao;
    private final Scanner scanner;

    public ReceptionistMenu(UserDao userDao, ReservationDao reservationDao, Scanner scanner) {
        this.userDao = userDao;
        this.reservationDao = reservationDao;
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
                    createReservation(staff, false);
                    break;
                case "2":
                    createReservation(staff, true);
                    break;
                case "3":
                    viewAndEditReservations(staff);
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

    private void createReservation(User staff, boolean walkIn) {
        System.out.println(walkIn ? "\n--- Create Walk-in Reservation ---" : "\n--- Create Reservation ---");

        System.out.print("Guest name (leave blank to cancel): ");
        String guest = scanner.nextLine().trim();
        if (guest.isEmpty()) return;

        System.out.print("Cottage name (leave blank to cancel): ");
        String cottage = scanner.nextLine().trim();
        if (cottage.isEmpty()) return;

        String date;
        if (walkIn) {
            date = LocalDate.now().toString();
        } else {
            date = readDate();
            if (date == null) return;
        }

        Double amount = readAmount();
        if (amount == null) return;

        int id = reservationDao.createReservation(staff.id(), guest, cottage, date, amount);
        if (id > 0) {
            System.out.println("Reservation #" + id + " created (status: PENDING).");
        } else {
            System.out.println("Failed to create reservation.");
        }
    }

    private void viewAndEditReservations(User staff) {
        List<Reservation> all = reservationDao.getAllReservations();
        if (all.isEmpty()) {
            System.out.println("No reservations yet.");
            return;
        }
        printReservationTable(all);

        Integer id = readPositiveInt("Enter Reservation ID to change status (0 to go back): ");
        if (id == null) return;

        if (reservationDao.findById(id) == null) {
            System.out.println("No reservation found with ID " + id + ".");
            return;
        }

        List<String> statuses = ReservationDao.STATUSES;
        for (int i = 0; i < statuses.size(); i++) {
            System.out.println((i + 1) + ". " + statuses.get(i));
        }
        System.out.println("0. Cancel");
        Integer pick = readPositiveInt("Choose new status: ");
        if (pick == null || pick > statuses.size()) {
            System.out.println("Cancelled or invalid status choice.");
            return;
        }

        String newStatus = statuses.get(pick - 1);
        if (reservationDao.updateStatus(staff.id(), id, newStatus)) {
            System.out.println("Reservation #" + id + " status updated to " + newStatus + ".");
        } else {
            System.out.println("Status not changed (same status or update failed).");
        }
    }

    private String readDate() {
        while (true) {
            System.out.print("Reservation date (yyyy-MM-dd, blank to cancel): ");
            String in = scanner.nextLine().trim();
            if (in.isEmpty()) return null;
            try {
                return LocalDate.parse(in).toString();
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date. Use the format yyyy-MM-dd.");
            }
        }
    }

    private Double readAmount() {
        while (true) {
            System.out.print("Total amount in P (blank to cancel): ");
            String in = scanner.nextLine().trim();
            if (in.isEmpty()) return null;
            try {
                double amount = Double.parseDouble(in);
                if (amount > 0 && Double.isFinite(amount)) return amount;
                System.out.println("Amount must be greater than 0.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount. Please enter a number.");
            }
        }
    }

    private Integer readPositiveInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int n = Integer.parseInt(scanner.nextLine().trim());
                return n <= 0 ? null : n;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    private void printReservationTable(List<Reservation> list) {
        String format = "| %-4s | %-15s | %-15s | %-10s | %-10s | %-10s |%n";
        String border = "+------+-----------------+-----------------+------------+------------+------------+";
        System.out.println(border);
        System.out.printf(format, "ID", "GUEST", "COTTAGE", "DATE", "TOTAL", "STATUS");
        System.out.println(border);
        for (Reservation r : list) {
            System.out.printf(format, r.id(), r.guestName(), r.cottageName(), r.reservationDate(),
                    String.format("P%.2f", r.totalAmount()), r.status());
        }
        System.out.println(border);
    }
}