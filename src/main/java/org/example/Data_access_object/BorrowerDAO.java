package org.example.Data_access_object;

import org.example.DatabaseConnection;
import org.example.models.Borrower;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BorrowerDAO {

    // 1. STRICT PHONE VALIDATION (7-8 Digits)

    public boolean isValidPhoneNumber(String phone) {
        // Regex to check if the string contains only digits (0-9) and has an exact length of 7 or 8 characters
        return phone != null && phone.matches("^[0-9]{7,8}$");
    }

    // 1.5 STRICT EMAIL VALIDATION (Must be @gmail.com)

    public boolean isValidEmail(String email) {
        // Regex to ensure the string starts with alphanumeric characters and ends strictly with @gmail.com
        return email != null && email.matches("^[a-zA-Z0-9._]+@gmail\\.com$");
    }

    // 2. SMART BORROWER CODE GENERATOR (e.g., Le Nguyen Van A -> NVA6879)

    private String generateBorrowerCode(String fullName, String phone) {
        String[] words = fullName.trim().split("\\s+");
        StringBuilder prefix = new StringBuilder();

        // Process the first 3 characters based on the name length
        if (words.length >= 3) {
            prefix.append(words[words.length - 3].substring(0, 1));
            prefix.append(words[words.length - 2].substring(0, 1));
            prefix.append(words[words.length - 1].substring(0, 1));
        } else if (words.length == 2) {
            prefix.append(words[0].substring(0, 1));
            prefix.append(words[1].substring(0, 1));
            prefix.append("0");
        } else if (words.length == 1 && !words[0].isEmpty()) {
            prefix.append(words[0].substring(0, 1)).append("00");
        } else {
            prefix.append("UNK");
        }

        // Extract the last 4 digits of the phone number
        String suffix = phone.length() >= 4 ? phone.substring(phone.length() - 4) : phone;

        return prefix.toString().toUpperCase() + suffix;
    }

    // 3. CREATE NEW BORROWER (Integrated with automatic code generation)

    public boolean addBorrower(String name, String email, String phone) {
        String code = generateBorrowerCode(name, phone);

        // Insert borrower_code into the SQL statement
        String sql = "INSERT INTO borrowers (borrower_code, name, email, phone_number) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code);
            ps.setString(2, name);
            ps.setString(3, email);
            ps.setString(4, phone);

            int rows = ps.executeUpdate();
            System.out.println("New customer added: " + name + " (ID: " + code + ")");
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("Error adding borrower: " + e.getMessage());
            return false;
        }
    }

    // 4. OMNI-SEARCH BORROWERS (PRIORITY FROM LEFT TO RIGHT)

    public List<Object[]> searchBorrowers(String keyword) {
        List<Object[]> list = new ArrayList<>();

        // ADVANCED SQL TECHNIQUES:
        // Search everywhere (WHERE LIKE '%...%')
        // BUT prioritize (ORDER BY) the first matching results (LIKE '...%') to the top.
        String sql = "SELECT * FROM borrowers " +
                "WHERE borrower_code LIKE ? OR name LIKE ? OR phone_number LIKE ? OR email LIKE ? " +
                "ORDER BY " +
                "(borrower_code LIKE ?) DESC, " + // Prioritize matching IDs from the start
                "(name LIKE ?) DESC, " +          // Prioritize matching Name from the start
                "(phone_number LIKE ?) DESC";     // Prioritize matching Phone from the start

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String containsPat = "%" + keyword + "%"; // For WHERE clauses
            String startsWithPat = keyword + "%";     // For ORDER BY clauses

            // 4 parameters for the WHERE condition (Search everywhere)
            ps.setString(1, containsPat);
            ps.setString(2, containsPat);
            ps.setString(3, containsPat);
            ps.setString(4, containsPat);

            // 3 parameters for the ORDER BY condition (Prioritize to the top)
            ps.setString(5, startsWithPat);
            ps.setString(6, startsWithPat);
            ps.setString(7, startsWithPat);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                            rs.getLong("borrower_id"),
                            rs.getString("borrower_code"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("phone_number")
                    });
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // 5. SAFE DELETE (Block deletion if the user is currently holding borrowed books)

    public boolean deleteBorrower(long borrowerId) {
        String checkSql = "SELECT COUNT(*) FROM transactions WHERE borrower_id = ? AND status = 'BORROWED'";
        String deleteSql = "DELETE FROM borrowers WHERE borrower_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkSql);
             PreparedStatement deletePs = conn.prepareStatement(deleteSql)) {

            // Check if the user has any active borrowed books
            checkPs.setLong(1, borrowerId);
            ResultSet rs = checkPs.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return false; // Block the deletion process immediately
            }

            // If no books are owed, proceed with deletion
            deletePs.setLong(1, borrowerId);
            return deletePs.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // LEGACY CODE: RETAINED TO PREVENT APPLICATION ERRORS

    public List<Borrower> getAllBorrowers() {
        List<Borrower> list = new ArrayList<>();
        String sql = "SELECT * FROM borrowers";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Borrower(
                        rs.getLong("borrower_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone_number")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Object[]> searchBorrower(String keyword) {
        List<Object[]> list = new ArrayList<>();

        String sql = "SELECT b.borrower_id, b.name, b.phone_number, " +
                "(SELECT COUNT(*) FROM transactions t WHERE t.borrower_id = b.borrower_id AND t.status = 'BORROWED') AS borrowing_count " +
                "FROM borrowers b WHERE b.name LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String phoneStr = rs.getString("phone_number");
                if (phoneStr == null || phoneStr.trim().isEmpty()) {
                    phoneStr = "Not updated";
                }

                list.add(new Object[]{
                        rs.getLong("borrower_id"),
                        rs.getString("name"),
                        phoneStr,
                        rs.getInt("borrowing_count")
                });
            }
        } catch (Exception e) {
            System.out.println("Error finding members: " + e.getMessage());
        }

        return list;
    }

    public void addBorrower(Borrower borrower) {
        String sql = "INSERT INTO borrowers (name, phone_number, email) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, borrower.getName());
            pstmt.setString(2, borrower.getPhoneNumber());
            pstmt.setString(3, borrower.getEmail());

            pstmt.executeUpdate();
            System.out.println("New customer added to the database: " + borrower.getName());

        } catch (Exception e) {
            System.out.println("Error adding new borrower: " + e.getMessage());
            e.printStackTrace();
        }
    }
}