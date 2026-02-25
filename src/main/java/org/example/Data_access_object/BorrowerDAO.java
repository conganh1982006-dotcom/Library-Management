package org.example.Data_access_object;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BorrowerDAO {
    private final String url = "jdbc:mysql://localhost:3306/library_management";
    private final String user = "root";
    private final String password = "123456"; // Đã fix đúng pass của máy bên kia khi dat pass cho MySQL

    // 1. HÀM TÌM KIẾM THÀNH VIÊN (Kéo được số điện thoại thực tế từ DB lên)
    public List<Object[]> searchBorrower(String keyword) {
        List<Object[]> list = new ArrayList<>();

        // Sử dụng đúng tên cột 'phone_number' trong DB của bạn
        String sql = "SELECT b.id, b.name, b.phone_number, " +
                "(SELECT COUNT(*) FROM transactions t WHERE t.borrower_id = b.id AND t.status = 'BORROWED') AS dang_muon " +
                "FROM borrowers b WHERE b.name LIKE ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt =prepareStatement(sql, keyword)) {

            // Truyền từ khóa tìm kiếm vào (Tìm gần đúng với LIKE)
            pstmt.setString(1, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                // Xử lý SĐT: Nếu khách cũ chưa có SĐT thì để chữ "Chưa cập nhật" cho đẹp UI
                String phoneStr = rs.getString("phone_number");
                if (phoneStr == null || phoneStr.trim().isEmpty()) {
                    phoneStr = "Not updated";
                }

                list.add(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        phoneStr, // Đổ SĐT thật vào cột số 3 trên giao diện
                        rs.getInt("borrowing")
                });
            }
        } catch (Exception e) {
            System.out.println("❌ Error finding members: " + e.getMessage());
        }

        return list;
    }

    // 2. HÀM THÊM KHÁCH MỚI (Full Option: Tên, SĐT, Email)
    public void addBorrower(String name, String phone, String email) {
        // Nhét cả 3 thông tin vào đúng 3 cột trong Database
        String sql = "INSERT INTO borrowers (name, phone_number, email) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, phone);
            pstmt.setString(3, email);

            pstmt.executeUpdate();
            System.out.println("✅ New customer added to the database.: " + name + " | Phone: " + phone);

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    // (Hàm phụ trợ hỗ trợ kết nối)
    private PreparedStatement prepareStatement(String sql, String keyword) throws SQLException {
        Connection conn = DriverManager.getConnection(url, user, password);
        return conn.prepareStatement(sql);
    }
}