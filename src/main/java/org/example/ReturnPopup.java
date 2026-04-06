package org.example;

import org.example.Data_access_object.TransactionDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ReturnPopup extends JDialog {
    private final TransactionDAO transactionDAO = new TransactionDAO();

    public ReturnPopup(JFrame parentFrame) {
        super(parentFrame, "Process Book Return", true);
        setSize(1000, 550); // Nới rộng cửa sổ để chứa cột mới
        setLayout(null);
        setLocationRelativeTo(parentFrame);

        // --- KHU VỰC TÌM KIẾM ---
        JLabel lblSearch = new JLabel("🔍 Search Borrower:");
        lblSearch.setBounds(20, 10, 150, 30);
        lblSearch.setFont(new Font("SansSerif", Font.BOLD, 14));
        add(lblSearch);

        JTextField txtSearch = new JTextField();
        txtSearch.setBounds(170, 10, 250, 30);
        add(txtSearch);

        JButton btnSearch = new JButton("Search");
        btnSearch.setBounds(430, 10, 100, 30);
        add(btnSearch);

        // --- CỘT CỦA BẢNG --- (Thêm Status và Fine)
        String[] cols = {"User ID", "Borrower Name", "Phone", "Book ID", "Book Title", "Borrow Date", "Due Date", "Status", "Fine (VND)"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.setRowHeight(25);

        // 🌟 KỸ THUẬT TÀNG HÌNH ID (Giấu cột 0 và cột 3)
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(3).setMinWidth(0);
        table.getColumnModel().getColumn(3).setMaxWidth(0);

        // Căn chỉnh cho đẹp
        table.getColumnModel().getColumn(1).setPreferredWidth(150); // Tên
        table.getColumnModel().getColumn(4).setPreferredWidth(200); // Tên sách
        table.getColumnModel().getColumn(7).setPreferredWidth(100); // Status
        table.getColumnModel().getColumn(8).setPreferredWidth(80);  // Fine

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(20, 50, 940, 380);
        add(scrollPane);

        // --- HÀM LOAD DATA ---
        Runnable loadData = () -> {
            model.setRowCount(0);
            String keyword = txtSearch.getText().trim();
            // Gọi hàm DAO mới được nâng cấp
            List<Object[]> transactions = transactionDAO.getActiveTransactionsForTable(keyword);
            for (Object[] row : transactions) {
                model.addRow(row);
            }
        };

        // Tự động load tất cả khi mở Popup
        loadData.run();

        // Sự kiện nút Search
        btnSearch.addActionListener(e -> loadData.run());
        // Mẹo UX: Gõ xong Enter là search luôn
        txtSearch.addActionListener(e -> loadData.run());

        // --- NÚT CHỐT TRẢ ---
        JButton btnConfirm = new JButton("CONFIRM RETURN");
        btnConfirm.setBounds(350, 440, 300, 50);
        btnConfirm.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnConfirm.setBackground(new Color(46, 204, 113)); // Đổi sang màu Xanh Lá cho thân thiện
        btnConfirm.setForeground(Color.WHITE);
        add(btnConfirm);

        btnConfirm.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a transaction from the table first!");
                return;
            }

            // Lấy data từ các cột (Kể cả cột tàng hình)
            long borrowerId = ((Number) table.getValueAt(selectedRow, 0)).longValue();
            String borrowerName = (String) table.getValueAt(selectedRow, 1);
            long bookId = ((Number) table.getValueAt(selectedRow, 3)).longValue();
            String bookTitle = (String) table.getValueAt(selectedRow, 4);
            String status = (String) table.getValueAt(selectedRow, 7);
            long fine = ((Number) table.getValueAt(selectedRow, 8)).longValue();

            // Hiển thị Popup cảnh báo tiền phạt nếu có
            String message = "Are you sure you want to process return for:\n👤 User: " + borrowerName + "\n📖 Book: " + bookTitle;
            if (fine > 0) {
                message += "\n\nATTENTION: This book is " + status + "!\n💸 FINE TO COLLECT: " + fine + " VND";
            }

            int confirm = JOptionPane.showConfirmDialog(this, message, "Confirm Return", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                transactionDAO.returnBook(borrowerId, bookId);

                String successMsg = "Book returned successfully!";
                if (fine > 0) successMsg += "\nCollected Fine: " + fine + " VND";
                JOptionPane.showMessageDialog(this, successMsg);

                loadData.run(); // Load lại bảng để dòng vừa trả biến mất
            }
        });
    }
}