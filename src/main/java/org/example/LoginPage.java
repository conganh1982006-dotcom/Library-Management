//package org.example;
//
//import javax.swing.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//
//public class LoginPage {
//    public static void main(String[] args) {
//        // 1. Tạo cái khung cửa sổ (JFrame)
//        JFrame frame = new JFrame("Library management system - Login Page");
//        frame.setSize(400, 250);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setLayout(null);
//        frame.setLocationRelativeTo(null); // Lệnh này giúp cửa sổ luôn hiện ra ở chính giữa màn hình
//
//        // 2. Tạo chữ "Tài khoản" và Ô nhập tài khoản (JLabel & JTextField)
//        JLabel userLabel = new JLabel("Account:");
//        userLabel.setBounds(50, 40, 80, 25);
//        frame.add(userLabel);
//
//        JTextField userText = new JTextField();
//        userText.setBounds(140, 40, 160, 25);
//        frame.add(userText);
//
//        // 3. Tạo chữ "Mật khẩu" và Ô nhập mật khẩu (JPasswordField)
//        JLabel passwordLabel = new JLabel("Password:");
//        passwordLabel.setBounds(50, 80, 80, 25);
//        frame.add(passwordLabel);
//
//        JPasswordField passwordText = new JPasswordField();
//        passwordText.setBounds(140, 80, 160, 25); // Dấu mật khẩu tự động thành dấu sao (*)
//        frame.add(passwordText);
//
//        // 4. Tạo nút bấm "Đăng Nhập" (JButton)
//        JButton loginButton = new JButton("Sign in");
//        loginButton.setBounds(140, 130, 120, 30);
//        frame.add(loginButton);
//
//        // 5. Thêm "Linh hồn" cho nút bấm (Sự kiện khi click chuột)
//        loginButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                // Lấy chữ người dùng vừa gõ
//                String user = userText.getText();
//                String pass = new String(passwordText.getPassword());
//
//                // Kiểm tra đúng sai (Đang fix cứng tài khoản Admin để test hệ thống)
//                if (user.equals("admin") && pass.equals("123")) {
//                    JOptionPane.showMessageDialog(frame, "✅ Welcome Admin! Have a good day!");
//
//                    // Đóng cửa sổ Login hiện tại
//                    frame.dispose();
//
//                    // Kích hoạt Trạm điều khiển trung tâm (Hệ thống Tabs PRO)
//                    new MainDashboard();
//                } else {
//                    JOptionPane.showMessageDialog(frame, "❌ User of password are incorrect!");
//                }
//            }
//        });
//
//        // 6. Hiển thị cửa sổ lên màn hình
//        frame.setVisible(true);
//    }
//}

package org.example;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginPage {
    // Thông tin kết nối chiếc "Két sắt" Database
    private static final String url = "jdbc:mysql://localhost:3306/library_management";
    private static final String userDB = "root";
    private static final String passwordDB = "123456";

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
        loginButton.setBackground(new Color(30, 144, 255)); // Đổi màu xanh cho chuyên nghiệp
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        frame.add(loginButton);

        // 🌟 SỰ KIỆN ĐĂNG NHẬP (Nối thẳng vào MySQL)
        loginButton.addActionListener(e -> {
            String username = userText.getText().trim();
            String pass = new String(passwordText.getPassword()).trim();

            if (username.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "⚠️ Please enter both username and password!");
                return;
            }

            // Gọi lính gác xuống MySQL kiểm tra thẻ căn cước
            String sql = "SELECT full_name, role FROM users WHERE username = ? AND password = ?";

            try (Connection conn = DriverManager.getConnection(url, userDB, passwordDB);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, username);
                pstmt.setString(2, pass);

                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    // Nếu tìm thấy khớp tài khoản và mật khẩu
                    String fullName = rs.getString("full_name");
                    String role = rs.getString("role");

                    // Chào đúng Tên thật và Chức vụ của người đó
                    JOptionPane.showMessageDialog(frame, "✅ Welcome " + role + ": " + fullName + "!\nHave a productive day.");

                    frame.dispose(); // Đóng cửa sổ Login
                    new MainDashboard(); // Mở Hệ thống chính
                } else {
                    // Nếu sai pass hoặc user không tồn tại
                    JOptionPane.showMessageDialog(frame, "❌ Incorrect Username or Password!", "Login Failed", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "❌ Database Connection Error: " + ex.getMessage());
            }
        });

        // ĐỈNH CAO UX: Cho phép gõ xong pass bấm nút ENTER trên bàn phím là đăng nhập luôn!
        frame.getRootPane().setDefaultButton(loginButton);

        frame.setVisible(true);
    }
}