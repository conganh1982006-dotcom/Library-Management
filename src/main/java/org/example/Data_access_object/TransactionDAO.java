package org.example.Data_access_object;

import org.example.DatabaseConnection; // Import the robust database connection bridge
import org.example.services.FineService; // Retain the fine calculation service
import org.example.SystemClock; // IMPORT THE TIME MACHINE (Simulated System Clock)

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit; // Import ChronoUnit for date/time calculations
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    private FineService fineService = new FineService();

    // BORROW BOOKS (Automatic inventory deduction & ACID transaction protection)

    public void borrowBook(long borrowerId, long bookId) {
        String insertTransSql = "INSERT INTO transactions (borrower_id, book_id, borrow_date, due_date, status) VALUES (?, ?, ?, ?, 'BORROWED')";
        String updateBookSql = "UPDATE books SET available_quantity = available_quantity - 1 WHERE book_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            // DISABLE AUTO-COMMIT: Ensure data integrity by preventing partial updates in case of errors
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt1 = conn.prepareStatement(insertTransSql);
                 PreparedStatement pstmt2 = conn.prepareStatement(updateBookSql)) {

                // Step 1: Create a borrow ticket (transaction record)
                pstmt1.setLong(1, borrowerId);
                pstmt1.setLong(2, bookId);
                // USE SIMULATED TIME TO CREATE THE TICKET
                pstmt1.setDate(3, Date.valueOf(SystemClock.now()));
                pstmt1.setDate(4, Date.valueOf(SystemClock.now().plusDays(14))); // 14-day borrowing period
                pstmt1.executeUpdate();

                // Step 2: Deduct book quantity from inventory
                pstmt2.setLong(1, bookId);
                pstmt2.executeUpdate();

                // IF BOTH STEPS ARE SUCCESSFUL -> Commit the transaction to the database
                conn.commit();
                System.out.println("Transaction created and inventory updated successfully!");

            } catch (SQLException ex) {
                conn.rollback(); // An error occurred, rollback the entire transaction!
                System.out.println("Rollback triggered! Error: " + ex.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Database Connection Error: " + e.getMessage());
        }
    }

    // RETURN BOOKS (Restore inventory & Apply ACID transaction protection)

    public void returnBook(long borrowerId, long bookId) {
        String updateTransSql = "UPDATE transactions SET status = 'RETURNED', return_date = ? WHERE borrower_id = ? AND book_id = ? AND status = 'BORROWED' LIMIT 1";
        String updateBookSql = "UPDATE books SET available_quantity = available_quantity + 1 WHERE book_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt1 = conn.prepareStatement(updateTransSql)) {

                // Step 1: Update the transaction status to RETURNED
                // RECORD THE RETURN DATE USING SIMULATED TIME
                pstmt1.setDate(1, Date.valueOf(SystemClock.now()));
                pstmt1.setLong(2, borrowerId);
                pstmt1.setLong(3, bookId);
                int updatedRows = pstmt1.executeUpdate();

                // Step 2: Only restore the inventory IF the transaction was successfully updated
                if (updatedRows > 0) {
                    try (PreparedStatement pstmt2 = conn.prepareStatement(updateBookSql)) {
                        pstmt2.setLong(1, bookId);
                        pstmt2.executeUpdate();
                    }
                    conn.commit();
                    System.out.println("Book returned successfully!");
                } else {
                    conn.rollback();
                    System.out.println("No valid book loan transaction found to return!");
                }
            } catch (SQLException ex) {
                conn.rollback();
                System.out.println("Rollback triggered! Error: " + ex.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Database Connection Error: " + e.getMessage());
        }
    }

    // DISPLAY LIST ON THE MAIN DASHBOARD

    public String getBorrowerListForUI() {
        StringBuilder result = new StringBuilder();
        // Standardize ID column names for joined tables
        String sql = "SELECT br.name AS borrower_name, br.phone_number, bk.title AS book_title, t.borrow_date, t.due_date " +
                "FROM transactions t " +
                "JOIN borrowers br ON t.borrower_id = br.borrower_id " +
                "JOIN books bk ON t.book_id = bk.book_id " +
                "WHERE t.status = 'BORROWED'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            // RETRIEVE SIMULATED TIME FOR CALCULATIONS
            LocalDate today = SystemClock.now();
            while (rs.next()) {
                String borrowerName = rs.getString("borrower_name");
                String phoneStr = rs.getString("phone_number");
                String bookTitle = rs.getString("book_title");
                LocalDate borrowDate = rs.getDate("borrow_date").toLocalDate();
                LocalDate dueDate = rs.getDate("due_date").toLocalDate();

                if (phoneStr == null || phoneStr.trim().isEmpty()) {
                    phoneStr = "N/A";
                }

                // Calculate the difference in days between the due date and today
                long daysLate = ChronoUnit.DAYS.between(dueDate, today);

                // "DAYS LEFT" CALCULATION ALGORITHM
                String daysStatus;
                if (daysLate < 0) { // If today is before due date -> STILL WITHIN LIMIT
                    daysStatus = "Late " + Math.abs(daysLate) + " days left";
                } else if (daysLate == 0) { // If today equals due date -> DUE TODAY
                    daysStatus = "DUE TODAY";
                } else { // If today is after due date -> OVERDUE
                    daysStatus = "OVERDUE by " + daysLate + " days!";
                }

                long fineAmount = fineService.calculateFine(daysLate);
                String fineText = (fineAmount > 0) ? (fineAmount + " VND ") : "0 VND";

                result.append("Borrower: ").append(borrowerName).append(" - [Phone: ").append(phoneStr).append("]\n")
                        .append("   Book: ").append(bookTitle).append("\n")
                        .append("   Borrowed: ").append(borrowDate)
                        .append("  | Due: ").append(dueDate).append("\n")
                        .append("   ").append(daysStatus).append("\n") // Insert status message line here
                        .append("   Fine: ").append(fineText).append("\n")
                        .append("---------------------------------------------------\n");
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }

        if (result.length() == 0) return "There are currently no borrowed books.";
        return result.toString();
    }

    // FETCH DATA FOR THE RETURN POPUP TABLE (Includes Search & Fine Calculation)
    // Add a keyword parameter to search by borrower name
    public List<Object[]> getActiveTransactionsForTable(String keyword) {
        List<Object[]> list = new ArrayList<>();
        // Use LIKE operator to find borrower names
        String sql = "SELECT t.borrower_id, br.name, br.phone_number, t.book_id, bk.title, t.borrow_date, t.due_date " +
                "FROM transactions t " +
                "JOIN borrowers br ON t.borrower_id = br.borrower_id " +
                "JOIN books bk ON t.book_id = bk.book_id " +
                "WHERE t.status = 'BORROWED' AND br.name LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = prepareStatement(conn, sql)) {

            pstmt.setString(1, "%" + keyword + "%"); // Bind the search keyword

            try (ResultSet rs = pstmt.executeQuery()) {
                // RETRIEVE SIMULATED TIME FOR CALCULATIONS
                LocalDate today = SystemClock.now();

                while (rs.next()) {
                    String phoneStr = rs.getString("phone_number");
                    if (phoneStr == null || phoneStr.trim().isEmpty()) {
                        phoneStr = "N/A";
                    }

                    LocalDate dueLocal = rs.getDate("due_date").toLocalDate();

                    // Calculate day difference
                    long daysLate = ChronoUnit.DAYS.between(dueLocal, today);
                    String statusStr;
                    long fine = 0;

                    // Classify status to display on the table
                    if (daysLate < 0) {
                        statusStr = Math.abs(daysLate) + " days left";
                    } else if (daysLate == 0) {
                        statusStr = "Due Today";
                    } else {
                        statusStr = "Late " + daysLate + " days";
                        fine = fineService.calculateFine(daysLate); // Calculate fine if overdue
                    }

                    list.add(new Object[]{
                            rs.getLong("borrower_id"),
                            rs.getString("name"),
                            phoneStr,
                            rs.getLong("book_id"),
                            rs.getString("title"),
                            rs.getDate("borrow_date"),
                            rs.getDate("due_date"),
                            statusStr, // NEW COLUMN: Day status
                            fine       // NEW COLUMN: Fine amount
                    });
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading loan list: " + e.getMessage());
        }
        return list;
    }

    // Helper method to resolve IDE syntax highlighting issues for prepareStatement
    private PreparedStatement prepareStatement(Connection conn, String sql) throws SQLException {
        return conn.prepareStatement(sql);
    }

    // RENEW BOOK (Extend borrowing period)
    public boolean renewBook(long borrowerId, long bookId, int extraDays) {
        // Use MySQL's DATE_ADD to directly increment the due date in the database
        String sql = "UPDATE transactions SET due_date = DATE_ADD(due_date, INTERVAL ? DAY) " +
                "WHERE borrower_id = ? AND book_id = ? AND status = 'BORROWED'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, extraDays);   // Number of days to extend (e.g., 7)
            pstmt.setLong(2, borrowerId);
            pstmt.setLong(3, bookId);

            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0; // Return true if the renewal was successful

        } catch (SQLException e) {
            System.out.println("Database Error during renewal: " + e.getMessage());
            return false;
        }
    }
}