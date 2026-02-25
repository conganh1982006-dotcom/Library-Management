package org.example;

import org.example.Data_access_object.TransactionDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ReturnPopup extends JDialog {
    private TransactionDAO transactionDAO = new TransactionDAO();

    public ReturnPopup(JFrame parentFrame) {
        super(parentFrame, "Return Book Automatically", true);
        setSize(850, 500);
        setLayout(null);
        setLocationRelativeTo(parentFrame);

        JLabel lblTitle = new JLabel("SELECT A TRANSACTION TO RETURN:");
        lblTitle.setBounds(20, 10, 400, 25);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        add(lblTitle);

        // --- Cột của bảng ---
        String[] cols = {"User ID", "Borrower Name", "Phone", "Book ID", "Book Title", "Borrow Date", "Due Date"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.setRowHeight(25);

        // Căn chỉnh độ rộng cột cho đẹp
        table.getColumnModel().getColumn(1).setPreferredWidth(150); // Tên
        table.getColumnModel().getColumn(4).setPreferredWidth(200); // Tên sách

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(20, 40, 790, 300);
        add(scrollPane);

        // --- Hàm Load Data ---
        Runnable loadData = () -> {
            model.setRowCount(0);
            List<Object[]> transactions = transactionDAO.getActiveTransactionsForTable();
            for (Object[] row : transactions) {
                model.addRow(row);
            }
        };

        // Tự động load khi mở Popup
        loadData.run();

        // --- Nút Chốt Trả ---
        JButton btnConfirm = new JButton("✅ RETURN SELECTED BOOK");
        btnConfirm.setBounds(270, 360, 300, 50);
        btnConfirm.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnConfirm.setBackground(new Color(255, 69, 0)); // Màu Đỏ Cam cảnh báo
        btnConfirm.setForeground(Color.WHITE);
        add(btnConfirm);

        btnConfirm.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();

            // Bắt lỗi quên chọn
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "⚠️ Please select a transaction from the table first!");
                return;
            }

            // Móc data từ dòng được click
            int borrowerId = (int) table.getValueAt(selectedRow, 0);
            String borrowerName = (String) table.getValueAt(selectedRow, 1);
            int bookId = (int) table.getValueAt(selectedRow, 3);
            String bookTitle = (String) table.getValueAt(selectedRow, 4);

            // Xác nhận lại cho chắc cú
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to process return for:\nUser: " + borrowerName + "\nBook: " + bookTitle,
                    "Confirm Return", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                transactionDAO.returnBook(borrowerId, bookId);
                JOptionPane.showMessageDialog(this, "✅ Book returned successfully!");
                this.dispose(); // Tắt popup
            }
        });
    }
}