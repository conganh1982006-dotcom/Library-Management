package org.example;

import org.example.Data_access_object.TransactionDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ReturnPopup extends JDialog {
    private final TransactionDAO transactionDAO = new TransactionDAO();

    public ReturnPopup(JFrame parentFrame) {
        super(parentFrame, "Manage Loans - Return & Renew", true);
        setSize(1000, 550);
        setLayout(null);
        setLocationRelativeTo(parentFrame);

        // SEARCH AREA

        JLabel lblSearch = new JLabel("Search Borrower:");
        lblSearch.setBounds(20, 10, 150, 30);
        lblSearch.setFont(new Font("SansSerif", Font.BOLD, 14));
        add(lblSearch);

        JTextField txtSearch = new JTextField();
        txtSearch.setBounds(170, 10, 250, 30);
        add(txtSearch);

        JButton btnSearch = new JButton("Search");
        btnSearch.setBounds(430, 10, 100, 30);
        add(btnSearch);

        // TABLE COLUMNS CONFIGURATION

        String[] cols = {"User ID", "Borrower Name", "Phone", "Book ID", "Book Title", "Borrow Date", "Due Date", "Status", "Fine (VND)"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Prevent cells from being edited by double-clicking
            }
        };
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.setRowHeight(25);

        // ID Stealth Technique (Hide column 0 and column 3 from UI but keep data accessible)
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(3).setMinWidth(0);
        table.getColumnModel().getColumn(3).setMaxWidth(0);

        // Column width configurations
        table.getColumnModel().getColumn(1).setPreferredWidth(150); // Borrower Name
        table.getColumnModel().getColumn(4).setPreferredWidth(200); // Book Title
        table.getColumnModel().getColumn(7).setPreferredWidth(100); // Status
        table.getColumnModel().getColumn(8).setPreferredWidth(80);  // Fine

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(20, 50, 940, 380);
        add(scrollPane);

        // DYNAMIC DATA LOADER

        Runnable loadData = () -> {
            model.setRowCount(0);
            String keyword = txtSearch.getText().trim();
            // Call the upgraded DAO method to fetch real-time transactions
            List<Object[]> transactions = transactionDAO.getActiveTransactionsForTable(keyword);
            for (Object[] row : transactions) {
                model.addRow(row);
            }
        };

        // Automatically load all active transactions when the popup is opened
        loadData.run();

        // Search event listeners
        btnSearch.addActionListener(e -> loadData.run());
        txtSearch.addActionListener(e -> loadData.run()); // Type and press Enter

        // BUTTON 1: CONFIRM RETURN

        JButton btnConfirmReturn = new JButton("CONFIRM RETURN");
        btnConfirmReturn.setBounds(230, 440, 250, 50);
        btnConfirmReturn.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnConfirmReturn.setBackground(new Color(46, 204, 113)); // Emerald Green
        btnConfirmReturn.setForeground(Color.WHITE);
        add(btnConfirmReturn);

        btnConfirmReturn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a transaction from the table first!");
                return;
            }

            // Retrieve data from columns
            long borrowerId = ((Number) table.getValueAt(selectedRow, 0)).longValue();
            String borrowerName = (String) table.getValueAt(selectedRow, 1);
            long bookId = ((Number) table.getValueAt(selectedRow, 3)).longValue();
            String bookTitle = (String) table.getValueAt(selectedRow, 4);
            String status = (String) table.getValueAt(selectedRow, 7);
            long fine = ((Number) table.getValueAt(selectedRow, 8)).longValue();

            // Display a warning popup about potential fines
            String message = "Are you sure you want to process return for:\nUser: " + borrowerName + "\nBook: " + bookTitle;
            if (fine > 0) {
                message += "\n\nATTENTION: This book is " + status + "!\nFINE TO COLLECT: " + fine + " VND";
            }

            int confirm = JOptionPane.showConfirmDialog(this, message, "Confirm Return", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                transactionDAO.returnBook(borrowerId, bookId);

                String successMsg = "Book returned successfully!";
                if (fine > 0) successMsg += "\nCollected Fine: " + fine + " VND";
                JOptionPane.showMessageDialog(this, successMsg);

                loadData.run(); // Reload table data
            }
        });

        // BUTTON 2: RENEW (+7 DAYS)

        JButton btnRenew = new JButton("RENEW (+7 DAYS)");
        btnRenew.setBounds(520, 440, 250, 50);
        btnRenew.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnRenew.setBackground(new Color(52, 152, 219)); // Peter River Blue
        btnRenew.setForeground(Color.WHITE);
        add(btnRenew);

        btnRenew.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a transaction from the table first!");
                return;
            }

            // Retrieve necessary data
            long borrowerId = ((Number) table.getValueAt(selectedRow, 0)).longValue();
            long bookId = ((Number) table.getValueAt(selectedRow, 3)).longValue();
            String bookTitle = (String) table.getValueAt(selectedRow, 4);
            long fine = ((Number) table.getValueAt(selectedRow, 8)).longValue();

            // STRICT LIBRARY RULE: Block renewal if the user has an unpaid fine
            if (fine > 0) {
                JOptionPane.showMessageDialog(this,
                        "Cannot renew! The borrower has an unpaid fine of " + fine + " VND for this book.\nPlease return it and collect the fine.",
                        "Renewal Blocked", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Confirm renewal action
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Renew the book '" + bookTitle + "' for an additional 7 days?",
                    "Confirm Renewal", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                // Call the DAO to extend the due date by 7 days
                boolean success = transactionDAO.renewBook(borrowerId, bookId, 7);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Book successfully renewed for 7 days!");
                    loadData.run(); // Refresh the table to show the new Due Date and Status
                } else {
                    JOptionPane.showMessageDialog(this, "System error! Cannot renew the book.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}