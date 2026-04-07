package org.example;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginPage {

    public static void main(String[] args) {
        JFrame frame = new JFrame("Library Management System - Security Login");
        frame.setSize(400, 250);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);
        frame.setLocationRelativeTo(null);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(50, 40, 80, 25);
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        frame.add(userLabel);

        JTextField userText = new JTextField();
        userText.setBounds(140, 40, 180, 25);
        frame.add(userText);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(50, 80, 80, 25);
        passwordLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        frame.add(passwordLabel);

        JPasswordField passwordText = new JPasswordField();
        passwordText.setBounds(140, 80, 180, 25);
        frame.add(passwordText);

        JButton loginButton = new JButton("Sign in");
        loginButton.setBounds(140, 130, 120, 35);
        loginButton.setBackground(new Color(30, 144, 255));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        frame.add(loginButton);

        // LOGIN EVENT
        loginButton.addActionListener(e -> {
            String username = userText.getText().trim();
            String pass = new String(passwordText.getPassword()).trim();

            if (username.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please enter both username and password!");
                return;
            }

            // Call the guards down to MySQL to check
            String sql = "SELECT full_name, role FROM users WHERE username = ? AND password = ?";

            // Use the created database connection
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, username);
                pstmt.setString(2, pass);

                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    String fullName = rs.getString("full_name");
                    String role = rs.getString("role"); // Biến role này chứa chữ "ADMIN" hoặc "STAFF" lấy từ MySQL

                    JOptionPane.showMessageDialog(frame, "Welcome " + role + ": " + fullName + "!\nHave a good day.");

                    frame.dispose(); // Close login form

                    // JUST THIS ONE LINE: Transfer the newly acquired position to the Dashboard!
                    new MainDashBoard(role);

                } else {
                    JOptionPane.showMessageDialog(frame, "Incorrect Username or Password!", "Login Failed", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Database Connection Error: " + ex.getMessage());
            }
        });

        //Allows you to type your password and press the ENTER key on your keyboard to log in instantly!
        frame.getRootPane().setDefaultButton(loginButton);

        frame.setVisible(true);
    }
}