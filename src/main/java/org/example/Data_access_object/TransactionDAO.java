package org.example.Data_access_object;

import org.example.services.FineService;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class TransactionDAO {
    private final String url = "jdbc:mysql://localhost:3306/library_management";
    private final String user = "root";
    private final String password = "123456";
    private FineService fineService = new FineService();

    // =========================================================================
    // 1. MƯỢN SÁCH (Tự động trừ kho)
    // =========================================================================
    public void borrowBook(int borrowerId, int bookId) {
        String insertTransSql = "INSERT INTO transactions (borrower_id, book_id, borrow_date, due_date, status) VALUES (?, ?, ?, ?, 'BORROWED')";
        String updateBookSql = "UPDATE books SET available_quantity = available_quantity - 1 WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            try (PreparedStatement pstmt1 = conn.prepareStatement(insertTransSql)) {
                pstmt1.setInt(1, borrowerId);
                pstmt1.setInt(2, bookId);
                pstmt1.setDate(3, Date.valueOf(LocalDate.now()));
                pstmt1.setDate(4, Date.valueOf(LocalDate.now().plusDays(14)));
                pstmt1.executeUpdate();
            }
            try (PreparedStatement pstmt2 = conn.prepareStatement(updateBookSql)) {
                pstmt2.setInt(1, bookId);
                pstmt2.executeUpdate();
            }
            System.out.println("✅ Transaction created and inventory updated successfully!");
        } catch (Exception e) {
            System.out.println("❌ Error borrowing book: " + e.getMessage());
        }
    }

    // =========================================================================
    // 2. TRẢ SÁCH (Có lính gác chống nổ kho)
    // =========================================================================
    public void returnBook(int borrowerId, int bookId) {
        String updateTransSql = "UPDATE transactions SET status = 'RETURNED', return_date = ? WHERE borrower_id = ? AND book_id = ? AND status = 'BORROWED' LIMIT 1";
        String updateBookSql = "UPDATE books SET available_quantity = available_quantity + 1 WHERE id = ? AND available_quantity < total_quantity";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            try (PreparedStatement pstmt1 = conn.prepareStatement(updateTransSql)) {
                pstmt1.setDate(1, Date.valueOf(LocalDate.now()));
                pstmt1.setInt(2, borrowerId);
                pstmt1.setInt(3, bookId);
                int rows = pstmt1.executeUpdate();

                if (rows > 0) {
                    try (PreparedStatement pstmt2 = conn.prepareStatement(updateBookSql)) {
                        pstmt2.setInt(1, bookId);
                        int bookUpdated = pstmt2.executeUpdate();
                        if (bookUpdated == 0) {
                            System.out.println("⚠️ The borrowing period has ended, but the book storage is full");
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error returning book: " + e.getMessage());
        }
    }

    // 3. HIỂN THỊ DANH SÁCH GIAO DIỆN CHÍNH
    public String getBorrowerListForUI() {
        StringBuilder result = new StringBuilder();
        String sql = "SELECT br.name AS borrower_name, br.phone_number, bk.title AS book_title, t.borrow_date, t.due_date " +
                "FROM transactions t " +
                "JOIN borrowers br ON t.borrower_id = br.id " +
                "JOIN books bk ON t.book_id = bk.id " +
                "WHERE t.status = 'BORROWED'";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            LocalDate today = LocalDate.now();
            while (rs.next()) {
                String borrowerName = rs.getString("borrower_name");
                String phoneStr = rs.getString("phone_number");
                String bookTitle = rs.getString("book_title");
                LocalDate borrowDate = rs.getDate("borrow_date").toLocalDate();
                LocalDate dueDate = rs.getDate("due_date").toLocalDate();

                if (phoneStr == null || phoneStr.trim().isEmpty()) {
                    phoneStr = "N/A";
                }

                long daysLate = ChronoUnit.DAYS.between(dueDate, today);
                long fineAmount = fineService.calculateFine(daysLate);
                String fineText = (fineAmount > 0) ? (fineAmount + " VND ⚠️") : "0 VND";

                result.append("👤 Borrower: ").append(borrowerName).append(" - [Phone: ").append(phoneStr).append("]\n")
                        .append("   📖 Book: ").append(bookTitle).append("\n")
                        .append("   ➡️ Borrowed: ").append(borrowDate)
                        .append("  |  📅 Due: ").append(dueDate).append("\n")
                        .append("   💸 Fine: ").append(fineText).append("\n")
                        .append("---------------------------------------------------\n");
            }
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }

        if (result.length() == 0) return "There are currently no borrowed books.";
        return result.toString();
    }

    // =========================================================================
    // 4. LẤY DỮ LIỆU ĐỔ VÀO BẢNG POPUP TRẢ SÁCH
    // =========================================================================
    public java.util.List<Object[]> getActiveTransactionsForTable() {
        java.util.List<Object[]> list = new java.util.ArrayList<>();
        String sql = "SELECT t.borrower_id, br.name, br.phone_number, t.book_id, bk.title, t.borrow_date, t.due_date " +
                "FROM transactions t " +
                "JOIN borrowers br ON t.borrower_id = br.id " +
                "JOIN books bk ON t.book_id = bk.id " +
                "WHERE t.status = 'BORROWED'";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String phoneStr = rs.getString("phone_number");
                if (phoneStr == null || phoneStr.trim().isEmpty()) {
                    phoneStr = "N/A";
                }
                list.add(new Object[]{
                        rs.getInt("borrower_id"),
                        rs.getString("name"),
                        phoneStr,
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getDate("borrow_date"),
                        rs.getDate("due_date")
                });
            }
        } catch (Exception e) {
            System.out.println("❌ Error loading loan list: " + e.getMessage());
        }
        return list;
    }
}