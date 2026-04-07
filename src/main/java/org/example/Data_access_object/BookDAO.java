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

    // BASIC OMNI-SEARCH (Reused by BorrowPopup)
    public List<Book> searchOmni(String keyword) {
        // Reuse the overloaded method below with categoryId = 0 (No category filtering)
        return searchOmni(keyword, 0);
    }

    // ADVANCED OMNI-SEARCH + FILTER (Combines Keyword Search & Category Filtering)
    public List<Book> searchOmni(String keyword, long categoryId) {
        List<Book> list = new ArrayList<>();
        // Dynamic SQL query with conditional category filtering
        String sql = "SELECT b.*, a.name AS author_name, c.name AS category_name " +
                "FROM books b " +
                "LEFT JOIN authors a ON b.author_id = a.author_id " +
                "LEFT JOIN categories c ON b.category_id = c.category_id " +
                "WHERE (b.title LIKE ? OR a.name LIKE ? OR b.book_code LIKE ? OR a.author_code LIKE ?) ";

        // If categoryId > 0, append the category filter condition
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

            // Bind the category ID parameter if category filtering is applied
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

    // AUTHOR CODE GENERATION ALGORITHM (STANDARDIZED 3-CHARACTER PREFIX)

    private String generateAuthorCode(String name, int birthYear) {
        // Split the author's name into individual words based on whitespace
        String[] words = name.trim().split("\\s+");
        StringBuilder prefix = new StringBuilder();

        if (words.length >= 3) {
            // SCENARIO 1: Long names (>= 3 words) -> Extract initials from the last 3 words
            prefix.append(words[words.length - 3].substring(0, 1));
            prefix.append(words[words.length - 2].substring(0, 1));
            prefix.append(words[words.length - 1].substring(0, 1));
        } else if (words.length == 2) {
            // SCENARIO 2: 2-word names -> Extract 2 initials and pad with "0"
            prefix.append(words[0].substring(0, 1));
            prefix.append(words[1].substring(0, 1));
            prefix.append("0");
        } else if (words.length == 1 && !words[0].isEmpty()) {
            // SCENARIO 3: Single-word names -> Extract 1 initial and pad with "00"
            prefix.append(words[0].substring(0, 1));
            prefix.append("00");
        } else {
            // Fallback for edge cases (Although prevented by UI validation)
            prefix.append("UNK");
        }

        // Convert prefix to UPPERCASE and append the birth year
        return prefix.toString().toUpperCase() + birthYear;
    }

    // Retrieve existing author ID or create a new author based on the generated code
    private long getOrCreateAuthorId(Connection conn, String authorName, int birthYear) throws SQLException {
        String authorCode = generateAuthorCode(authorName, birthYear);

        // 1. Search utilizing the unique Author Code rather than the name
        String checkSql = "SELECT author_id FROM authors WHERE author_code = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, authorCode);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) return rs.getLong("author_id");
        }

        // 2. If no match is found, insert a new author record with Code, Name, and Birth Year
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

    // UPDATED TO ACCEPT BIRTH YEAR FOR ADVANCED AUTHOR VALIDATION
    public boolean addBookSmart(String title, String authorName, int birthYear, long categoryId, String categoryName, int quantity) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Retrieve or auto-generate author ID
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

    // RETRIEVE AUTHOR LIST FOR UI COMBOBOX POPULATION

    public List<String> getAllAuthorsForUI() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT author_id, name, birth_year FROM authors";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                // Format output as: "1 - Stephen Hawking (1942)"
                list.add(rs.getLong("author_id") + " - " + rs.getString("name") + " (" + rs.getInt("birth_year") + ")");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ADD BOOK USING AN EXISTING AUTHOR ID (Bypasses author creation)

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