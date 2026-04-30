package org.example.Data_access_object;

import org.example.DatabaseConnection;
import org.example.models.Borrower;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BorrowerDAO {

    public boolean isValidPhoneNumber(String phone) {
        return phone != null && phone.matches("^[0-9]{7,8}$");
    }

    public boolean isValidEmail(String email) {
        return email != null && email.matches("^[a-zA-Z0-9._]+@gmail\\.com$");
    }

    private String generateBorrowerCode(String fullName, String phone) {
        String[] words = fullName.trim().split("\\s+");
        StringBuilder prefix = new StringBuilder();

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

        String suffix = phone.length() >= 4 ? phone.substring(phone.length() - 4) : phone;
        return prefix.toString().toUpperCase() + suffix;
    }

    public boolean addBorrower(String name, String email, String phone) {
        String code = generateBorrowerCode(name, phone);
        String sql = "INSERT INTO borrowers (borrower_code, name, email, phone_number) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code);
            ps.setString(2, name);
            ps.setString(3, email);
            ps.setString(4, phone);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Object[]> searchBorrowers(String keyword) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT * FROM borrowers " +
                "WHERE borrower_code LIKE ? OR name LIKE ? OR phone_number LIKE ? OR email LIKE ? " +
                "ORDER BY " +
                "(borrower_code LIKE ?) DESC, " +
                "(name LIKE ?) DESC, " +
                "(phone_number LIKE ?) DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String containsPat = "%" + keyword + "%";
            String startsWithPat = keyword + "%";

            ps.setString(1, containsPat);
            ps.setString(2, containsPat);
            ps.setString(3, containsPat);
            ps.setString(4, containsPat);
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean deleteBorrower(long borrowerId) {
        String checkSql = "SELECT COUNT(*) FROM transactions WHERE borrower_id = ? AND status = 'BORROWED'";
        String deleteSql = "DELETE FROM borrowers WHERE borrower_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkSql);
             PreparedStatement deletePs = conn.prepareStatement(deleteSql)) {

            checkPs.setLong(1, borrowerId);
            ResultSet rs = checkPs.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) return false;

            deletePs.setLong(1, borrowerId);
            return deletePs.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Object[]> searchBorrowerForUI(String keyword) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT b.borrower_id, b.borrower_code, b.name, b.phone_number, " +
                "(SELECT COUNT(*) FROM transactions t WHERE t.borrower_id = b.borrower_id AND t.status = 'BORROWED') AS borrowing_count " +
                "FROM borrowers b WHERE b.name LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String phoneStr = rs.getString("phone_number");
                if (phoneStr == null || phoneStr.trim().isEmpty()) phoneStr = "Not updated";

                list.add(new Object[]{
                        rs.getLong("borrower_id"),
                        rs.getString("borrower_code"),
                        rs.getString("name"),
                        phoneStr,
                        rs.getInt("borrowing_count")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    // Upgraded: Updates Borrower Information (Name, Email, Phone)
    public boolean updateBorrowerInfo(long borrowerId, String newName, String newEmail, String newPhone) {
        String sql = "UPDATE borrowers SET name = ?, email = ?, phone_number = ? WHERE borrower_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newName);
            ps.setString(2, newEmail);
            ps.setString(3, newPhone);
            ps.setLong(4, borrowerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}