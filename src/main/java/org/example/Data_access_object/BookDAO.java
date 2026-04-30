package org.example.Data_access_object;

import org.example.DatabaseConnection;
import org.example.models.Book;
import org.example.models.Category;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    /**
     * Retrieves all categories from the database.
     * @return A list of Category objects.
     */
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

    /**
     * Retrieves all books from the database, including author and category names.
     * @return A list of Book objects.
     */
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

    /**
     * Performs a basic omni-search for books without category filtering.
     * @param keyword The search keyword.
     * @return A list of Book objects matching the keyword.
     */
    public List<Book> searchOmni(String keyword) {
        return searchOmni(keyword, "All Fields", 0);
    }

    /**
     * Performs an advanced dynamic search for books with keyword, search type, and category filtering.
     * @param keyword The search keyword.
     * @param searchType The type of search (e.g., "Title", "Author", "Book Code", "Author Code", "All Fields").
     * @param categoryId The category ID to filter by (0 for no filter).
     * @return A list of Book objects matching the criteria.
     */
    public List<Book> searchOmni(String keyword, String searchType, long categoryId) {
        List<Book> list = new ArrayList<>();

        // DYNAMIC SQL TECHNIQUE: Start with '1=1' to easily append subsequent AND conditions
        StringBuilder sql = new StringBuilder(
                "SELECT b.*, a.name AS author_name, c.name AS category_name " +
                        "FROM books b " +
                        "LEFT JOIN authors a ON b.author_id = a.author_id " +
                        "LEFT JOIN categories c ON b.category_id = c.category_id " +
                        "WHERE 1=1 "
        );

        // Append Category filter condition if applicable
        if (categoryId > 0) {
            sql.append(" AND b.category_id = ? ");
        }

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();

        // Append Keyword Search condition based on the selected Search Type dropdown
        if (hasKeyword) {
            sql.append(" AND (");
            // Synchronized: Separate Book Code and Author Code to match MainDashBoard
            if ("Title".equals(searchType)) {
                sql.append("b.title LIKE ?");
            } else if ("Author".equals(searchType)) {
                sql.append("a.name LIKE ?");
            } else if ("Book Code".equals(searchType)) {
                sql.append("b.book_code LIKE ?"); // Search only book code
            } else if ("Author Code".equals(searchType)) {
                sql.append("a.author_code LIKE ?"); // Search only author code
            } else { // Fallback to "All Fields"
                sql.append("b.title LIKE ? OR a.name LIKE ? OR b.book_code LIKE ? OR a.author_code LIKE ?");
            }
            sql.append(") ");

            // PRIORITY RANKING: Push exact prefix matches ('keyword%') to the top of the result list
            sql.append("ORDER BY ");
            if ("Title".equals(searchType)) {
                sql.append("(b.title LIKE ?) DESC");
            } else if ("Author".equals(searchType)) {
                sql.append("(a.name LIKE ?) DESC");
            } else if ("Book Code".equals(searchType)) {
                sql.append("(b.book_code LIKE ?) DESC");
            } else if ("Author Code".equals(searchType)) {
                sql.append("(a.author_code LIKE ?) DESC");
            } else { // Priority for "All Fields"
                sql.append("(b.title LIKE ?) DESC, (b.book_code LIKE ?) DESC, (a.name LIKE ?) DESC");
            }
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1; // Parameter index counter for dynamic binding

            // Bind Category ID parameter
            if (categoryId > 0) {
                ps.setLong(paramIndex++, categoryId);
            }

            // Bind Keyword parameter(s) dynamically
            if (hasKeyword) {
                String containsPat = "%" + keyword + "%"; // Used for WHERE (Search everywhere)
                String startsWithPat = keyword + "%";     // Used for ORDER BY & Code Search (Prioritize prefix)

                // A. Bind parameters for the WHERE clause
                if ("Title".equals(searchType) || "Author".equals(searchType)) {
                    ps.setString(paramIndex++, containsPat); // Search contains
                } else if ("Book Code".equals(searchType) || "Author Code".equals(searchType)) {
                    // FIX: Code search should match from the beginning to avoid data noise
                    ps.setString(paramIndex++, startsWithPat);
                } else { // "All Fields" requires 4 parameter bindings
                    ps.setString(paramIndex++, containsPat);
                    ps.setString(paramIndex++, containsPat);
                    ps.setString(paramIndex++, containsPat);
                    ps.setString(paramIndex++, containsPat);
                }

                // B. Bind parameters for the ORDER BY clause
                if ("Title".equals(searchType) || "Author".equals(searchType) || "Book Code".equals(searchType) || "Author Code".equals(searchType)) {
                    ps.setString(paramIndex++, startsWithPat);
                } else { // "All Fields" requires 3 parameter bindings for Order By
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

    /**
     * Generates a standardized 3-character prefix for author codes.
     * @param name The author's full name.
     * @param birthYear The author's birth year.
     * @return The generated author code.
     */
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

    /**
     * Retrieves an existing author's ID or creates a new author if not found.
     * @param conn The database connection.
     * @param authorName The author's name.
     * @param birthYear The author's birth year.
     * @return The ID of the existing or newly created author.
     * @throws SQLException If a database error occurs.
     */
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

    /**
     * Generates a unique book code based on category and existing books.
     * @param conn The database connection.
     * @param categoryId The category ID of the book.
     * @param categoryName The name of the category.
     * @return The generated book code.
     * @throws SQLException If a database error occurs.
     */
    private String generateBookCode(Connection conn, long categoryId, String categoryName) throws SQLException {
        String prefix;
        // Remove spaces to handle compound names (e.g., "Sci Fi" -> "SciFi")
        String cleanName = categoryName.trim().replaceAll("\\s+", "");

        if (cleanName.length() >= 3) {
            prefix = cleanName.substring(0, 3).toUpperCase();
        } else {
            // If the category name is too short (less than 3 characters), pad with 'X'
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

    /**
     * Adds a new book to the database, creating a new author if necessary.
     * Uses a transaction for atomicity.
     * @param title The book's title.
     * @param authorName The author's name.
     * @param birthYear The author's birth year.
     * @param categoryId The category ID.
     * @param categoryName The category name.
     * @param quantity The total quantity of the book.
     * @return True if the book was added successfully, false otherwise.
     */
    public boolean addBookSmart(String title, String authorName, int birthYear, long categoryId, String categoryName, int quantity) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Enable manual transaction control
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
                conn.commit(); // Finalize transaction
                return true;
            } catch (SQLException e) {
                conn.rollback(); // Prevent data corruption on failure
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a book from the database. Deletion is blocked if the book is currently borrowed.
     * @param bookId The ID of the book to delete.
     * @return True if the book was deleted, false if it's currently borrowed or an error occurred.
     */
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

    /**
     * Updates a book's title and adjusts its total and available quantities.
     * @param bookId The ID of the book to update.
     * @param newTitle The new title for the book.
     * @param amountChange The amount to change the total and available quantities by.
     * @return True if the book was updated successfully, false otherwise.
     */
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

    /**
     * Retrieves a list of all authors for display in the UI.
     * @return A list of strings, each representing an author with their ID, name, and birth year.
     */
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

    /**
     * Adds a new book to the database using an existing author.
     * Uses a transaction for atomicity.
     * @param title The book's title.
     * @param authorId The ID of the existing author.
     * @param categoryId The category ID.
     * @param categoryName The category name.
     * @param quantity The total quantity of the book.
     * @return True if the book was added successfully, false otherwise.
     */
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