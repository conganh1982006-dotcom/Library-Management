package org.example.Data_access_object;

import org.example.DatabaseConnection;
import org.example.models.Book;
import org.example.models.Category;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    public List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM categories";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Category(rs.getLong("category_id"), rs.getString("name")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Book> getAllBooks() {
        List<Book> list = new ArrayList<>();
        String sql = "SELECT b.*, a.name AS author_name, c.name AS category_name " +
                "FROM books b " +
                "LEFT JOIN authors a ON b.author_id = a.author_id " +
                "LEFT JOIN categories c ON b.category_id = c.category_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Book b = new Book(
                        rs.getLong("book_id"),
                        rs.getString("book_code"),
                        rs.getString("title"),
                        rs.getLong("author_id"),
                        rs.getLong("category_id"),
                        rs.getInt("total_quantity"),
                        rs.getInt("available_quantity")
                );
                b.setAuthorName(rs.getString("author_name"));
                b.setCategoryName(rs.getString("category_name"));
                list.add(b);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // 2. OMNI-SEARCH CƠ BẢN (Dành cho BorrowPopup dùng lại)
    public List<Book> searchOmni(String keyword) {
        // Tái sử dụng hàm bên dưới với categoryId = 0 (Không lọc thể loại)
        return searchOmni(keyword, 0);
    }

    //OMNI-SEARCH + FILTER (Kết hợp Tìm kiếm & Lọc thể loại)
    public List<Book> searchOmni(String keyword, long categoryId) {
        List<Book> list = new ArrayList<>();
        // Câu lệnh SQL linh hoạt, có điều kiện lọc thể loại
        String sql = "SELECT b.*, a.name AS author_name, c.name AS category_name " +
                "FROM books b " +
                "LEFT JOIN authors a ON b.author_id = a.author_id " +
                "LEFT JOIN categories c ON b.category_id = c.category_id " +
                "WHERE (b.title LIKE ? OR a.name LIKE ? OR b.book_code LIKE ? OR a.author_code LIKE ?) ";

        // Nếu categoryId > 0 tức là người dùng có chọn Thể loại để lọc
        if (categoryId > 0) {
            sql += " AND b.category_id = ?";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);
            ps.setString(4, searchPattern);

            // Truyền thêm id thể loại vào câu truy vấn nếu có
            if (categoryId > 0) {
                ps.setLong(5, categoryId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Book b = new Book(
                            rs.getLong("book_id"),
                            rs.getString("book_code"),
                            rs.getString("title"),
                            rs.getLong("author_id"),
                            rs.getLong("category_id"),
                            rs.getInt("total_quantity"),
                            rs.getInt("available_quantity")
                    );
                    b.setAuthorName(rs.getString("author_name"));
                    b.setCategoryName(rs.getString("category_name"));
                    list.add(b);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // =========================================================
    // THUẬT TOÁN TẠO MÃ TÁC GIẢ TỪ TÊN + NĂM SINH (CHUẨN 3 KÝ TỰ)
    // =========================================================
    private String generateAuthorCode(String name, int birthYear) {
        // Cắt tên thành các từ dựa trên khoảng trắng
        String[] words = name.trim().split("\\s+");
        StringBuilder prefix = new StringBuilder();

        if (words.length >= 3) {
            // KỊCH BẢN 1: Tên dài (>= 3 chữ) -> Lấy 3 chữ cuối
            prefix.append(words[words.length - 3].substring(0, 1));
            prefix.append(words[words.length - 2].substring(0, 1));
            prefix.append(words[words.length - 1].substring(0, 1));
        } else if (words.length == 2) {
            // KỊCH BẢN 2: Tên có 2 chữ -> Lấy 2 chữ + thêm "0"
            prefix.append(words[0].substring(0, 1));
            prefix.append(words[1].substring(0, 1));
            prefix.append("0");
        } else if (words.length == 1 && !words[0].isEmpty()) {
            // KỊCH BẢN 3: Tên có 1 chữ -> Lấy 1 chữ + thêm "00"
            prefix.append(words[0].substring(0, 1));
            prefix.append("00");
        } else {
            // Đề phòng trường hợp lỗi (Dù Form đã chặn)
            prefix.append("UNK");
        }

        // Chuyển tất cả thành chữ IN HOA và gắn năm sinh vào đuôi
        return prefix.toString().toUpperCase() + birthYear;
    }

    // Kiểm tra xem tác giả (dựa trên Mã NVA1980) đã có chưa
    private long getOrCreateAuthorId(Connection conn, String authorName, int birthYear) throws SQLException {
        String authorCode = generateAuthorCode(authorName, birthYear);

        // 1. Tìm theo Mã tác giả thay vì Tên
        String checkSql = "SELECT author_id FROM authors WHERE author_code = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, authorCode);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) return rs.getLong("author_id");
        }

        // 2. Nếu chưa có, tạo mới với đầy đủ Mã, Tên, Năm sinh
        String insertSql = "INSERT INTO authors (author_code, name, birth_year) VALUES (?, ?, ?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            insertStmt.setString(1, authorCode);
            insertStmt.setString(2, authorName);
            insertStmt.setInt(3, birthYear);
            insertStmt.executeUpdate();
            ResultSet keys = insertStmt.getGeneratedKeys();
            if (keys.next()) return keys.getLong(1);
        }
        throw new SQLException("Cannot create author!");
    }

    private String generateBookCode(Connection conn, long categoryId, String categoryName) throws SQLException {
        String prefix = categoryName.substring(0, 1).toUpperCase();
        String countSql = "SELECT COUNT(*) FROM books WHERE category_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(countSql)) {
            ps.setLong(1, categoryId);
            ResultSet rs = ps.executeQuery();
            int currentCount = 0;
            if (rs.next()) currentCount = rs.getInt(1);
            return String.format("%s%04d", prefix, currentCount + 1);
        }
    }

    // UPDATE ĐỂ NHẬN THÊM birthYear
    public boolean addBookSmart(String title, String authorName, int birthYear, long categoryId, String categoryName, int quantity) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Gọi hàm tạo tác giả với năm sinh
                long authorId = getOrCreateAuthorId(conn, authorName, birthYear);
                String bookCode = generateBookCode(conn, categoryId, categoryName);

                String sql = "INSERT INTO books (book_code, title, author_id, category_id, total_quantity, available_quantity) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, bookCode);
                    ps.setString(2, title);
                    ps.setLong(3, authorId);
                    ps.setLong(4, categoryId);
                    ps.setInt(5, quantity);
                    ps.setInt(6, quantity);
                    ps.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteBook(long bookId) {
        String checkSql = "SELECT COUNT(*) FROM transactions WHERE book_id = ? AND status = 'BORROWED'";
        String deleteSql = "DELETE FROM books WHERE book_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkSql);
             PreparedStatement deletePs = conn.prepareStatement(deleteSql)) {
            checkPs.setLong(1, bookId);
            ResultSet rs = checkPs.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) return false;
            deletePs.setLong(1, bookId);
            return deletePs.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateBookQuantity(long bookId, int amountChange) {
        String sql = "UPDATE books SET total_quantity = total_quantity + ?, available_quantity = available_quantity + ? WHERE book_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, amountChange);
            ps.setInt(2, amountChange);
            ps.setLong(3, bookId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =======================================
    // Lấy danh sách tác giả đưa lên ComboBox
    // =======================================
    public List<String> getAllAuthorsForUI() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT author_id, name, birth_year FROM authors";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                // Hiển thị dạng: "1 - Stephen Hawking (1942)"
                list.add(rs.getLong("author_id") + " - " + rs.getString("name") + " (" + rs.getInt("birth_year") + ")");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // =====================================================
    //Thêm sách bằng ID tác giả CÓ SẴN (Bỏ qua bước tạo mới)
    // =====================================================
    public boolean addBookWithExistingAuthor(String title, long authorId, long categoryId, String categoryName, int quantity) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String bookCode = generateBookCode(conn, categoryId, categoryName);
                String sql = "INSERT INTO books (book_code, title, author_id, category_id, total_quantity, available_quantity) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, bookCode);
                    ps.setString(2, title);
                    ps.setLong(3, authorId);
                    ps.setLong(4, categoryId);
                    ps.setInt(5, quantity);
                    ps.setInt(6, quantity);
                    ps.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}