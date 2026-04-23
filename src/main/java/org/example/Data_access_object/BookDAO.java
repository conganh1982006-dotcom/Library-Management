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

    public List<Book> searchOmni(String keyword) {
        return searchOmni(keyword, "All Fields", 0);
    }

    public List<Book> searchOmni(String keyword, String searchType, long categoryId) {
        List<Book> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT b.*, a.name AS author_name, c.name AS category_name " +
                        "FROM books b " +
                        "LEFT JOIN authors a ON b.author_id = a.author_id " +
                        "LEFT JOIN categories c ON b.category_id = c.category_id " +
                        "WHERE 1=1 "
        );

        if (categoryId > 0) {
            sql.append(" AND b.category_id = ? ");
        }

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();

        if (hasKeyword) {
            sql.append(" AND (");
            if ("Title".equals(searchType)) {
                sql.append("b.title LIKE ?");
            } else if ("Author".equals(searchType)) {
                sql.append("a.name LIKE ?");
            } else if ("Book Code".equals(searchType)) {
                sql.append("b.book_code LIKE ?");
            } else if ("Author Code".equals(searchType)) {
                sql.append("a.author_code LIKE ?");
            } else {
                sql.append("b.title LIKE ? OR a.name LIKE ? OR b.book_code LIKE ? OR a.author_code LIKE ?");
            }
            sql.append(") ");

            sql.append("ORDER BY ");
            if ("Title".equals(searchType)) {
                sql.append("(b.title LIKE ?) DESC");
            } else if ("Author".equals(searchType)) {
                sql.append("(a.name LIKE ?) DESC");
            } else if ("Book Code".equals(searchType)) {
                sql.append("(b.book_code LIKE ?) DESC");
            } else if ("Author Code".equals(searchType)) {
                sql.append("(a.author_code LIKE ?) DESC");
            } else {
                sql.append("(b.title LIKE ?) DESC, (b.book_code LIKE ?) DESC, (a.name LIKE ?) DESC");
            }
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;

            if (categoryId > 0) {
                ps.setLong(paramIndex++, categoryId);
            }

            if (hasKeyword) {
                String containsPat = "%" + keyword + "%";
                String startsWithPat = keyword + "%";

                if ("Title".equals(searchType) || "Author".equals(searchType)) {
                    ps.setString(paramIndex++, containsPat);
                } else if ("Book Code".equals(searchType) || "Author Code".equals(searchType)) {
                    ps.setString(paramIndex++, startsWithPat);
                } else {
                    ps.setString(paramIndex++, containsPat);
                    ps.setString(paramIndex++, containsPat);
                    ps.setString(paramIndex++, containsPat);
                    ps.setString(paramIndex++, containsPat);
                }

                if ("Title".equals(searchType) || "Author".equals(searchType) || "Book Code".equals(searchType) || "Author Code".equals(searchType)) {
                    ps.setString(paramIndex++, startsWithPat);
                } else {
                    ps.setString(paramIndex++, startsWithPat);
                    ps.setString(paramIndex++, startsWithPat);
                    ps.setString(paramIndex++, startsWithPat);
                }
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

    private String generateAuthorCode(String name, int birthYear) {
        String[] words = name.trim().split("\\s+");
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
            prefix.append(words[0].substring(0, 1));
            prefix.append("00");
        } else {
            prefix.append("UNK");
        }
        return prefix.toString().toUpperCase() + birthYear;
    }

    // BUG FIX: Bulletproof method to get Author ID safely
    private long getOrCreateAuthorId(Connection conn, String authorName, int birthYear) throws SQLException {
        String authorCode = generateAuthorCode(authorName, birthYear);

        // 1. Check if the author already exists
        String checkSql = "SELECT author_id FROM authors WHERE author_code = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, authorCode);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) return rs.getLong("author_id");
        }

        // 2. Insert new author (Removed getGeneratedKeys to avoid JDBC bugs with non-AutoIncrement tables)
        String insertSql = "INSERT INTO authors (author_code, name, birth_year) VALUES (?, ?, ?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setString(1, authorCode);
            insertStmt.setString(2, authorName);
            insertStmt.setInt(3, birthYear);
            insertStmt.executeUpdate();
        }

        // 3. Fetch the ID explicitly using the unique author_code we just inserted
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, authorCode);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) return rs.getLong("author_id");
        }

        throw new SQLException("Cannot retrieve new author ID!");
    }

    // BUG FIX: Count by Prefix string to prevent jumping numbers
    private String generateBookCode(Connection conn, long categoryId, String categoryName) throws SQLException {
        String prefix;
        String cleanName = categoryName.trim().replaceAll("\\s+", "");

        if (cleanName.length() >= 3) {
            prefix = cleanName.substring(0, 3).toUpperCase();
        } else {
            prefix = String.format("%-3s", cleanName).replace(' ', 'X').toUpperCase();
        }

        // Count how many books already have this exact prefix
        String countSql = "SELECT COUNT(*) FROM books WHERE book_code LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(countSql)) {
            ps.setString(1, prefix + "%");
            ResultSet rs = ps.executeQuery();
            int currentCount = 0;
            if (rs.next()) currentCount = rs.getInt(1);
            return String.format("%s%04d", prefix, currentCount + 1);
        }
    }

    public boolean addBookSmart(String title, String authorName, int birthYear, long categoryId, String categoryName, int quantity) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
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

    // Upgraded: Updates both Book Title and Quantity simultaneously
    public boolean updateBookInfo(long bookId, String newTitle, int amountChange) {
        String sql = "UPDATE books SET title = ?, total_quantity = total_quantity + ?, available_quantity = available_quantity + ? WHERE book_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newTitle); // Set the new title
            ps.setInt(2, amountChange);
            ps.setInt(3, amountChange);
            ps.setLong(4, bookId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getAllAuthorsForUI() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT author_id, name, birth_year FROM authors";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getLong("author_id") + " - " + rs.getString("name") + " (" + rs.getInt("birth_year") + ")");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

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