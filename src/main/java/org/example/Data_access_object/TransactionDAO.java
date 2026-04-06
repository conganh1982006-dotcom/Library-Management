package org.example.Data_access_object;

import org.example.DatabaseConnection; // Gọi cầu nối xịn
import org.example.services.FineService; // Giữ nguyên Service tính tiền phạt của sếp
import org.example.SystemClock; // 🌟 IMPORT CỖ MÁY THỜI GIAN

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit; // import này để Java biết cách tính toán ngày tháng:
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    private FineService fineService = new FineService();

    // =======================================================
    // MƯỢN SÁCH (Tự động trừ kho & Có bảo vệ giao dịch ACID)
    // =======================================================
    public void borrowBook(long borrowerId, long bookId) {
        String insertTransSql = "INSERT INTO transactions (borrower_id, book_id, borrow_date, due_date, status) VALUES (?, ?, ?, ?, 'BORROWED')";
        String updateBookSql = "UPDATE books SET available_quantity = available_quantity - 1 WHERE book_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            // TẮT AUTO COMMIT: Đảm bảo nếu lỗi thì không ghi bậy vào Database
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt1 = conn.prepareStatement(insertTransSql);
                 PreparedStatement pstmt2 = conn.prepareStatement(updateBookSql)) {

                // Bước 1: Tạo phiếu mượn
                pstmt1.setLong(1, borrowerId);
                pstmt1.setLong(2, bookId);
                // 🌟 DÙNG GIỜ GIẢ LẬP ĐỂ TẠO PHIẾU
                pstmt1.setDate(3, Date.valueOf(SystemClock.now()));
                pstmt1.setDate(4, Date.valueOf(SystemClock.now().plusDays(14))); // Hạn 14 ngày
                pstmt1.executeUpdate();

                // Bước 2: Trừ sách trong kho
                pstmt2.setLong(1, bookId);
                pstmt2.executeUpdate();

                // NẾU CẢ 2 BƯỚC ĐỀU ỔN -> Chốt lưu vào Database
                conn.commit();
                System.out.println("Transaction created and inventory updated successfully!");

            } catch (SQLException ex) {
                conn.rollback(); // Có biến là hoàn tác lại toàn bộ!
                System.out.println("Rollback triggered! Error: " + ex.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Database Connection Error: " + e.getMessage());
        }
    }

    // =========================================================================
    // TRẢ SÁCH (Cộng lại kho & Có bảo vệ giao dịch ACID)
    // =========================================================================
    public void returnBook(long borrowerId, long bookId) {
        String updateTransSql = "UPDATE transactions SET status = 'RETURNED', return_date = ? WHERE borrower_id = ? AND book_id = ? AND status = 'BORROWED' LIMIT 1";
        String updateBookSql = "UPDATE books SET available_quantity = available_quantity + 1 WHERE book_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt1 = conn.prepareStatement(updateTransSql)) {

                // Bước 1: Cập nhật trạng thái phiếu mượn thành RETURNED
                // 🌟 GHI NHẬN NGÀY TRẢ LÀ NGÀY GIẢ LẬP
                pstmt1.setDate(1, Date.valueOf(SystemClock.now()));
                pstmt1.setLong(2, borrowerId);
                pstmt1.setLong(3, bookId);
                int updatedRows = pstmt1.executeUpdate();

                // Bước 2: Chỉ cộng lại kho NẾU phiếu mượn thực sự được cập nhật
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

    // =====================================
    // HIỂN THỊ DANH SÁCH GIAO DIỆN CHÍNH
    // =====================================
    public String getBorrowerListForUI() {
        StringBuilder result = new StringBuilder();
        // Sửa lại chuẩn tên cột ID của các bảng
        String sql = "SELECT br.name AS borrower_name, br.phone_number, bk.title AS book_title, t.borrow_date, t.due_date " +
                "FROM transactions t " +
                "JOIN borrowers br ON t.borrower_id = br.borrower_id " +
                "JOIN books bk ON t.book_id = bk.book_id " +
                "WHERE t.status = 'BORROWED'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            // LẤY GIỜ GIẢ LẬP ĐỂ TÍNH TOÁN
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

                // Tính số ngày chênh lệch giữa ngày trả và ngày hôm nay
                long daysLate = ChronoUnit.DAYS.between(dueDate, today);

                // THUẬT TOÁN TÍNH "DAY LEFT"
                String daysStatus;
                if (daysLate < 0) { // Nếu today nhỏ hơn dueDate -> CÒN HẠN
                    daysStatus = "⏳ " + Math.abs(daysLate) + " days left";
                } else if (daysLate == 0) { // Nếu today bằng dueDate -> ĐẾN HẠN HÔM NAY
                    daysStatus = "DUE TODAY";
                } else { // Nếu today lớn hơn dueDate -> TRỄ HẠN
                    daysStatus = "OVERDUE by " + daysLate + " days!";
                }

                long fineAmount = fineService.calculateFine(daysLate);
                String fineText = (fineAmount > 0) ? (fineAmount + " VND ⚠️") : "0 VND";

                result.append("👤 Borrower: ").append(borrowerName).append(" - [Phone: ").append(phoneStr).append("]\n")
                        .append("   Book: ").append(bookTitle).append("\n")
                        .append("   Borrowed: ").append(borrowDate)
                        .append("  | Due: ").append(dueDate).append("\n")
                        .append("   ").append(daysStatus).append("\n") // Chèn dòng báo trạng thái vào đây
                        .append("   Fine: ").append(fineText).append("\n")
                        .append("---------------------------------------------------\n");
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }

        if (result.length() == 0) return "There are currently no borrowed books.";
        return result.toString();
    }

    // =================================================================
    // LẤY DỮ LIỆU ĐỔ VÀO BẢNG POPUP TRẢ SÁCH (Đã có Search & Tính phạt)
    // =================================================================
    // Thêm tham số keyword để tìm kiếm theo tên
    public List<Object[]> getActiveTransactionsForTable(String keyword) {
        List<Object[]> list = new ArrayList<>();
        // Dùng LIKE để tìm tên người mượn
        String sql = "SELECT t.borrower_id, br.name, br.phone_number, t.book_id, bk.title, t.borrow_date, t.due_date " +
                "FROM transactions t " +
                "JOIN borrowers br ON t.borrower_id = br.borrower_id " +
                "JOIN books bk ON t.book_id = bk.book_id " +
                "WHERE t.status = 'BORROWED' AND br.name LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt =prepareStatement(conn, sql)) {

            pstmt.setString(1, "%" + keyword + "%"); // Gắn từ khóa tìm kiếm

            try (ResultSet rs = pstmt.executeQuery()) {
                // LẤY GIỜ GIẢ LẬP ĐỂ TÍNH TOÁN
                LocalDate today = SystemClock.now();

                while (rs.next()) {
                    String phoneStr = rs.getString("phone_number");
                    if (phoneStr == null || phoneStr.trim().isEmpty()) {
                        phoneStr = "N/A";
                    }

                    LocalDate dueLocal = rs.getDate("due_date").toLocalDate();

                    // Tính số ngày chênh lệch
                    long daysLate = ChronoUnit.DAYS.between(dueLocal, today);
                    String statusStr;
                    long fine = 0;

                    // Phân loại trạng thái đưa lên bảng
                    if (daysLate < 0) {
                        statusStr = Math.abs(daysLate) + " days left";
                    } else if (daysLate == 0) {
                        statusStr = "Due Today";
                    } else {
                        statusStr = "Late " + daysLate + " days";
                        fine = fineService.calculateFine(daysLate); // Tính tiền phạt nếu trễ
                    }

                    list.add(new Object[]{
                            rs.getLong("borrower_id"),
                            rs.getString("name"),
                            phoneStr,
                            rs.getLong("book_id"),
                            rs.getString("title"),
                            rs.getDate("borrow_date"),
                            rs.getDate("due_date"),
                            statusStr, //CỘT MỚI: Trạng thái ngày
                            fine       //CỘT MỚI: Tiền phạt
                    });
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading loan list: " + e.getMessage());
        }
        return list;
    }

    // Helper method (phụ trợ) để cho code nó không lỗi báo đỏ ở hàm prepareStatement bên trên
    private PreparedStatement prepareStatement(Connection conn, String sql) throws SQLException {
        return conn.prepareStatement(sql);
    }
}