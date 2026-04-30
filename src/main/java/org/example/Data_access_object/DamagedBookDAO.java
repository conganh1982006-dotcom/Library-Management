package org.example.Data_access_object;

import org.example.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DamagedBookDAO {

    // 1. Get the list of damaged books to display on the table
    public List<Object[]> getAllDamagedBooks(String keyword) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT d.issue_id, b.book_id, b.book_code, b.title, d.damage_type, d.notes, d.status " +
                "FROM damaged_books d " +
                "JOIN books b ON d.book_id = b.book_id " +
                "WHERE b.book_code LIKE ? OR b.title LIKE ? ORDER BY d.status DESC, d.issue_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{
                        rs.getInt("issue_id"),
                        rs.getLong("book_id"),
                        rs.getString("book_code"),
                        rs.getString("title"),
                        rs.getString("damage_type"),
                        rs.getString("notes"),
                        rs.getString("status")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 2. Report a damaged book (Automatically deduct 1 from available inventory)
    public boolean reportDamage(String bookCode, String damageType, String notes) {
        String checkSql = "SELECT book_id, available_quantity FROM books WHERE book_code = ?";
        String updateSql = "UPDATE books SET available_quantity = available_quantity - 1 WHERE book_id = ?";
        String insertSql = "INSERT INTO damaged_books (book_id, damage_type, notes, status) VALUES (?, ?, ?, 'UNFIXED')";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Enable manual transaction protection
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setString(1, bookCode);
                ResultSet rs = psCheck.executeQuery();
                if (rs.next()) {
                    long bookId = rs.getLong("book_id");
                    int qty = rs.getInt("available_quantity");

                    if (qty <= 0) {
                        return false; // No available books left to report as damaged
                    }

                    // Deduct 1 book from inventory
                    try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                        psUpdate.setLong(1, bookId);
                        psUpdate.executeUpdate();
                    }

                    // Create damage report record
                    try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                        psInsert.setLong(1, bookId);
                        psInsert.setString(2, damageType);
                        psInsert.setString(3, notes);
                        psInsert.executeUpdate();
                    }
                    conn.commit(); // Commit transaction
                    return true;
                }
            } catch (SQLException ex) {
                conn.rollback(); // Rollback on error to prevent database corruption
                ex.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 3. Mark as repaired (Automatically add 1 back to available inventory)
    public boolean markAsRepaired(int issueId, long bookId) {
        String updateDamageSql = "UPDATE damaged_books SET status = 'REPAIRED' WHERE issue_id = ?";
        String updateBookSql = "UPDATE books SET available_quantity = available_quantity + 1 WHERE book_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Change status to REPAIRED
                try (PreparedStatement ps1 = conn.prepareStatement(updateDamageSql)) {
                    ps1.setInt(1, issueId);
                    ps1.executeUpdate();
                }
                // Add 1 book back to available inventory
                try (PreparedStatement ps2 = conn.prepareStatement(updateBookSql)) {
                    ps2.setLong(1, bookId);
                    ps2.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 4. Report damage directly by Book ID (Used when returning a book)
    public boolean reportDamageById(long bookId, String damageType, String notes) {
        String checkSql = "SELECT available_quantity FROM books WHERE book_id = ?";
        String updateSql = "UPDATE books SET available_quantity = available_quantity - 1 WHERE book_id = ?";
        String insertSql = "INSERT INTO damaged_books (book_id, damage_type, notes, status) VALUES (?, ?, ?, 'UNFIXED')";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setLong(1, bookId);
                ResultSet rs = psCheck.executeQuery();
                if (rs.next()) {
                    int qty = rs.getInt("available_quantity");
                    if (qty <= 0) return false;

                    try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                        psUpdate.setLong(1, bookId);
                        psUpdate.executeUpdate();
                    }
                    try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                        psInsert.setLong(1, bookId);
                        psInsert.setString(2, damageType);
                        psInsert.setString(3, notes);
                        psInsert.executeUpdate();
                    }
                    conn.commit();
                    return true;
                }
            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 5. Delete Repaired History
    public boolean deleteRepairedIssues() {
        String sql = "DELETE FROM damaged_books WHERE status = 'REPAIRED'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}