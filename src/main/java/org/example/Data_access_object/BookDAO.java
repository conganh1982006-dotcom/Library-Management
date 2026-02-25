
package org.example.Data_access_object;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.example.models.Book;

public class BookDAO {
    private final String url = "jdbc:mysql://localhost:3306/library_management";
    private final String user = "root";
    private final String password = "123456";

    // =========================================================================
    // 1. DÀNH CHO MAIN DASHBOARD (Có lọc Thể loại bằng JOIN)
    // =========================================================================
    public List<Book> getBooks(String categoryFilter) {
        List<Book> books = new ArrayList<>();

        // Dùng LEFT JOIN để móc tên Tác giả và Thể loại từ 2 bảng khác lên
        String sql = "SELECT b.id, b.title, a.name AS author_name, c.name AS category_name, b.total_quantity, b.available_quantity " +
                "FROM books b " +
                "LEFT JOIN authors a ON b.author_id = a.id " +
                "LEFT JOIN categories c ON b.category_id = c.id ";

        // Nếu người dùng chọn lọc một thể loại cụ thể (không phải "All")
        if (!categoryFilter.equals("All")) {
            sql += "WHERE c.name = ? ";
        }

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (!categoryFilter.equals("All")) {
                pstmt.setString(1, categoryFilter);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String author = rs.getString("author_name") != null ? rs.getString("author_name") : "N/A";
                    String category = rs.getString("category_name") != null ? rs.getString("category_name") : "N/A";

                    // Đã truyền đủ 6 thông số cho Khuôn Book mới!
                    books.add(new Book(
                            rs.getInt("id"),
                            rs.getString("title"),
                            author,
                            category,
                            rs.getInt("total_quantity"),
                            rs.getInt("available_quantity")
                    ));
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error retrieving the list: " + e.getMessage());
        }
        return books;
    }

    // =========================================================================
    // 2. DÀNH CHO BORROW POPUP (Cần lấy tất cả sách)
    // =========================================================================
    public List<Book> getAllBooks() {
        // Tận dụng lại hàm getBooks ở trên cho code gọn gàng!
        return getBooks("All");
    }

    // =========================================================================
    // 3. TÌM SÁCH THEO TÊN (Dùng cho cả Dashboard và Popup)
    // =========================================================================
    public List<Book> searchBooksForUI(String keyword) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT b.id, b.title, a.name AS author_name, c.name AS category_name, b.total_quantity, b.available_quantity " +
                "FROM books b " +
                "LEFT JOIN authors a ON b.author_id = a.id " +
                "LEFT JOIN categories c ON b.category_id = c.id " +
                "WHERE b.title LIKE ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String author = rs.getString("author_name") != null ? rs.getString("author_name") : "N/A";
                String category = rs.getString("category_name") != null ? rs.getString("category_name") : "N/A";
                books.add(new Book(rs.getInt("id"), rs.getString("title"), author, category, rs.getInt("total_quantity"), rs.getInt("available_quantity")));
            }
        } catch (Exception e) {
            System.out.println("❌ Error searching the book: " + e.getMessage());
        }
        return books;
    }

    // =========================================================================
    // 4. TRÍ KHÔN NHÂN TẠO - QUẢN LÝ TÁC GIẢ & THÊM SÁCH
    // =========================================================================
    private int getOrCreateAuthorId(Connection conn, String authorName) throws SQLException {
        String checkSql = "SELECT id FROM authors WHERE name = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, authorName);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) return rs.getInt("id");
        }
        String insertSql = "INSERT INTO authors (name) VALUES (?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            insertStmt.setString(1, authorName);
            insertStmt.executeUpdate();
            ResultSet keys = insertStmt.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        }
        throw new SQLException("Cannot create author!");
    }

    public void addBook(String title, String authorName, int categoryId, int quantity) {
        String sql = "INSERT INTO books (title, author_id, category_id, total_quantity, available_quantity) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            int authorId = getOrCreateAuthorId(conn, authorName);
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, title);
                pstmt.setInt(2, authorId);
                pstmt.setInt(3, categoryId);
                pstmt.setInt(4, quantity);
                pstmt.setInt(5, quantity);
                pstmt.executeUpdate();
                System.out.println("✅ Added complete: " + title);
            }
        } catch (Exception e) {
            System.out.println("❌ Error cannot added: " + e.getMessage());
        }
    }

    // 5. XÓA SÁCH (CÓ LÍNH GÁC CHỐNG XÓA KHI ĐANG CHO MƯỢN)

    public boolean deleteBook(int id) {
        // LÍNH GÁC: Kiểm tra xem sách có đang nằm trong phiếu mượn nào chưa trả không
        String checkSql = "SELECT COUNT(*) FROM transactions WHERE book_id = ? AND status = 'BORROWED'";
        String deleteSql = "DELETE FROM books WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            // Bước 1: Check hóa đơn
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, id);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    return false; // Phát hiện đang có người mượn -> Lập tức chặn lại (Trả về false)
                }
            }

            // Bước 2: Nếu an toàn, tiến hành xóa
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, id);
                deleteStmt.executeUpdate();
                return true; // Xóa thành công (Trả về true)
            }
        } catch (Exception e) {
            System.out.println("❌ Error delete incomplete: " + e.getMessage());
            return false;
        }
    }

    // 6. CẬP NHẬT SỐ LƯỢNG SÁCH (THÊM / BỚT)

    public boolean updateBookQuantity(int bookId, int amountChange) {
        // LÍNH GÁC: Kiểm tra số lượng hiện tại trước khi cho phép bớt
        String checkSql = "SELECT total_quantity, available_quantity FROM books WHERE id = ?";
        String updateSql = "UPDATE books SET total_quantity = total_quantity + ?, available_quantity = available_quantity + ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            // Bước 1: Kiểm tra xem nếu bớt sách (số âm) thì có bị lố không
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, bookId);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    int currentAvail = rs.getInt("available_quantity");

                    // Nếu muốn bớt sách, mà số lượng bớt lại nhiều hơn số sách đang nằm trên kệ -> CHẶN!
                    if (currentAvail + amountChange < 0) {
                        return false;
                    }
                } else {
                    return false; // Không tìm thấy sách
                }
            }

            // Bước 2: An toàn rồi thì cho phép cập nhật số lượng
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setInt(1, amountChange);
                updateStmt.setInt(2, amountChange);
                updateStmt.setInt(3, bookId);
                updateStmt.executeUpdate();
                return true;
            }
        } catch (Exception e) {
            System.out.println("❌ Quantity update error: " + e.getMessage());
            return false;
        }
    }
}