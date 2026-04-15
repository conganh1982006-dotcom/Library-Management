package org.example.Data_access_object;

import org.example.DatabaseConnection;
import org.example.models.Author;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuthorDAO {

    // 1. Get a list of all authors (Used to display the interface)
    public List<Author> getAllAuthors() {
        List<Author> list = new ArrayList<>();
        String sql = "SELECT * FROM authors";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Author author = new Author();
                author.setAuthorId(rs.getLong("author_id"));
                author.setName(rs.getString("name"));
                list.add(author);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 2. Add new authors
    public void addAuthor(Author author) {
        String sql = "INSERT INTO authors (name) VALUES (?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, author.getName());
            ps.executeUpdate();
            System.out.println(" Successfully added another author!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 3. Remove author by ID
    public void deleteAuthor(long id) {
        String sql = "DELETE FROM authors WHERE author_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();
            System.out.println(" Author ID successfully removed: " + id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}