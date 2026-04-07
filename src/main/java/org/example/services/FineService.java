package org.example.services;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class FineService {

    //The core "processor": The fine calculation algorithm
    public long calculateFine(long daysLate) {
        if (daysLate <= 0) {
            return 0; // Pay on time or early, no penalty
        }

        if (daysLate <= 10) {
            // Delays from 1 to 10 days: 1k/day
            return daysLate * 1000;
        } else {
            // Late by more than 10 days: 1k for the first 10 days, 2k for each subsequent day
            return (10 * 1000) + ((daysLate - 10) * 2000);
        }
    }

    // Function for Admin: Calculate and display penalty invoices
    // (Use LocalDate to simulate the scheduled payment date and the actual payment date)
    public void adminViewFineReport(LocalDate dueDate, LocalDate returnDate) {
        System.out.println("\n=======================================");
        System.out.println("         BOOK RETURN REPORT     ");
        System.out.println("=======================================");
        System.out.println("Payment appointment date: " + dueDate);
        System.out.println("Actual payment date: " + returnDate);

        // Calculate the distance between two days
        long daysLate = ChronoUnit.DAYS.between(dueDate, returnDate);

        if (daysLate <= 0) {
            System.out.println(" Status: GOOD. Reader returned the book on time.");
            System.out.println(" Fine amount: 0 VND");
        } else {
            long fineAmount = calculateFine(daysLate);
            System.out.println("Status: VIOLATION (Late " + daysLate + " day)");
            System.out.println("TOTAL FINES " + fineAmount + " VNĐ");

            // Hint
            if (daysLate > 10) {
                System.out.println("Note: An increased penalty (2000 VND/day) has been applied. " + (daysLate - 10) + " day beyond frame.");
            }
        }
        System.out.println("=======================================\n");
    }
}