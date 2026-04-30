package org.example.services;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class FineService {

    // The core "processor": The fine calculation algorithm
    public long calculateFine(long daysLate) {
        if (daysLate <= 0) {
            return 0; // Paid on time or early, no penalty
        }

        if (daysLate <= 10) {
            // Delays from 1 to 10 days: 1,000 VND/day
            return daysLate * 1000;
        } else {
            // Late by more than 10 days: 1,000 VND for the first 10 days, 2,000 VND for each subsequent day
            return (10 * 1000) + ((daysLate - 10) * 2000);
        }
    }

    // Function for Admin: Calculate and display penalty invoices
    // (Use LocalDate to simulate the scheduled payment date and the actual payment date)
    public void adminViewFineReport(LocalDate dueDate, LocalDate returnDate) {
        System.out.println("\n=======================================");
        System.out.println("         BOOK RETURN REPORT            ");
        System.out.println("=======================================");
        System.out.println("Due date: " + dueDate);
        System.out.println("Actual return date: " + returnDate);

        // Calculate the difference between the two dates
        long daysLate = ChronoUnit.DAYS.between(dueDate, returnDate);

        if (daysLate <= 0) {
            System.out.println("Status: GOOD. Borrower returned the book on time.");
            System.out.println("Fine amount: 0 VND");
        } else {
            long fineAmount = calculateFine(daysLate);
            System.out.println("Status: VIOLATION (Late " + daysLate + " days)");
            System.out.println("TOTAL FINES: " + fineAmount + " VND");

            // Hint
            if (daysLate > 10) {
                System.out.println("Note: An increased penalty (2,000 VND/day) has been applied for " + (daysLate - 10) + " day(s) beyond the initial frame.");
            }
        }
        System.out.println("=======================================\n");
    }
}