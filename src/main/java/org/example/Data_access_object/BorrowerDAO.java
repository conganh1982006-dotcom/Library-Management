package org.example.Data_access_object;

import org.example.DatabaseConnection; // Gọi cầu nối xịn
import org.example.models.Borrower;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BorrowerDAO {

    //LẤY TẤT CẢ NGƯỜI MƯỢN (Dành cho các bảng danh sách tiêu chuẩn)
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

    //TÌM KIẾM THÀNH VIÊN VÀ ĐẾM SÁCH MƯỢN (Dành cho UI Dashboard sếp viết sẵn)
    public List<Object[]> searchBorrower(String keyword) {
        List<Object[]> list = new ArrayList<>();

        // FIX: Đổi b.id thành b.borrower_id cho khớp 100% với Database
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
                        rs.getLong("borrower_id"), // Đã sửa thành borrower_id
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

    //THÊM KHÁCH MỚI (Đã chuẩn hóa dùng thẳng Model Borrower)
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