package org.example;

import org.example.Data_access_object.TransactionDAO;
import org.example.Data_access_object.DamagedBookDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ReturnPopup extends JDialog {
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final DamagedBookDAO damagedTool = new DamagedBookDAO(); // Added Damaged Tool

    public ReturnPopup(JFrame parentFrame) {
        super(parentFrame, "Manage Loans - Return & Renew", true);
        setSize(1000, 550);
        setLayout(null);
        setLocationRelativeTo(parentFrame);

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

        String[] cols = {"User ID", "Borrower Name", "Phone", "Book ID", "Book Title", "Borrow Date", "Due Date", "Status", "Fine (VND)"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.setRowHeight(25);

        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(3).setMinWidth(0);
        table.getColumnModel().getColumn(3).setMaxWidth(0);

        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(4).setPreferredWidth(200);
        table.getColumnModel().getColumn(7).setPreferredWidth(100);
        table.getColumnModel().getColumn(8).setPreferredWidth(80);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(20, 50, 940, 380);
        add(scrollPane);

        Runnable loadData = () -> {
            model.setRowCount(0);
            String keyword = txtSearch.getText().trim();
            List<Object[]> transactions = transactionDAO.getActiveTransactionsForTable(keyword);
            for (Object[] row : transactions) {
                model.addRow(row);
            }
        };

        loadData.run();
        btnSearch.addActionListener(e -> loadData.run());
        txtSearch.addActionListener(e -> loadData.run());

        // BUTTON 1: NORMAL RETURN
        JButton btnConfirmReturn = new JButton("RETURN (OK)");
        btnConfirmReturn.setBounds(70, 440, 220, 50);
        btnConfirmReturn.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnConfirmReturn.setBackground(new Color(46, 204, 113));
        btnConfirmReturn.setForeground(Color.WHITE);
        add(btnConfirmReturn);

        // BUTTON 2: DAMAGED RETURN
        JButton btnReturnDamaged = new JButton("RETURN (DAMAGED)");
        btnReturnDamaged.setBounds(330, 440, 260, 50);
        btnReturnDamaged.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnReturnDamaged.setBackground(new Color(231, 76, 60)); // Alizarin Red
        btnReturnDamaged.setForeground(Color.WHITE);
        add(btnReturnDamaged);

        // BUTTON 3: RENEW
        JButton btnRenew = new JButton("RENEW (+7 DAYS)");
        btnRenew.setBounds(630, 440, 220, 50);
        btnRenew.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnRenew.setBackground(new Color(52, 152, 219));
        btnRenew.setForeground(Color.WHITE);
        add(btnRenew);

        // Logic for Normal Return
        btnConfirmReturn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a transaction!"); return;
            }

            try {
                long borrowerId = Long.parseLong(String.valueOf(table.getValueAt(selectedRow, 0)));
                String borrowerName = String.valueOf(table.getValueAt(selectedRow, 1));
                long bookId = Long.parseLong(String.valueOf(table.getValueAt(selectedRow, 3)));
                String bookTitle = String.valueOf(table.getValueAt(selectedRow, 4));
                String status = String.valueOf(table.getValueAt(selectedRow, 7));

                String fineStr = String.valueOf(table.getValueAt(selectedRow, 8)).replace(".0", "");
                long fine = Long.parseLong(fineStr);

                String message = "Process NORMAL return for:\nUser: " + borrowerName + "\nBook: " + bookTitle;
                if (fine > 0) message += "\n\nATTENTION: This book is " + status + "!\nFINE TO COLLECT: " + fine + " VND";

                if (JOptionPane.showConfirmDialog(this, message, "Confirm Normal Return", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    transactionDAO.returnBook(borrowerId, bookId);
                    String successMsg = "Book returned successfully in good condition!";
                    if (fine > 0) successMsg += "\nCollected Fine: " + fine + " VND";
                    JOptionPane.showMessageDialog(this, successMsg);
                    loadData.run();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Data formatting error! " + ex.getMessage());
            }
        });

        // Logic for Damaged Return
        btnReturnDamaged.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a transaction!"); return;
            }

            try {
                long borrowerId = Long.parseLong(String.valueOf(table.getValueAt(selectedRow, 0)));
                String borrowerName = String.valueOf(table.getValueAt(selectedRow, 1));
                long bookId = Long.parseLong(String.valueOf(table.getValueAt(selectedRow, 3)));
                String bookTitle = String.valueOf(table.getValueAt(selectedRow, 4));
                String status = String.valueOf(table.getValueAt(selectedRow, 7));

                String fineStr = String.valueOf(table.getValueAt(selectedRow, 8)).replace(".0", "");
                long fine = Long.parseLong(fineStr);

                String warnMsg = "Warning: You are about to return a DAMAGED book.\nUser: " + borrowerName + "\nBook: " + bookTitle;
                if (fine > 0) warnMsg += "\n\nNOTE: You still need to collect a fine of " + fine + " VND.";

                if (JOptionPane.showConfirmDialog(this, warnMsg, "Confirm Damaged Return", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    // Prompt for damage details
                    String[] damageTypes = {"Torn Pages", "Water Damage", "Lost/Missing", "Other"};
                    JComboBox<String> cbDamageType = new JComboBox<>(damageTypes);
                    JTextArea txtNotes = new JTextArea(3, 20);
                    txtNotes.setLineWrap(true);

                    Object[] form = {
                            "Select Damage Type:", cbDamageType,
                            "Additional Notes:", new JScrollPane(txtNotes)
                    };

                    if (JOptionPane.showConfirmDialog(this, form, "Report Damage Details", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                        String type = (String) cbDamageType.getSelectedItem();
                        String notes = txtNotes.getText().trim();

                        // 1. Process Return (Moves book back to inventory)
                        transactionDAO.returnBook(borrowerId, bookId);

                        // 2. Process Damage (Takes book out of inventory and logs it)
                        damagedTool.reportDamageById(bookId, type, notes);

                        String successMsg = "Book returned and marked as DAMAGED!";
                        if (fine > 0) successMsg += "\nCollected Fine: " + fine + " VND";
                        JOptionPane.showMessageDialog(this, successMsg);
                        loadData.run();
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Data formatting error! " + ex.getMessage());
            }
        });

        // Logic for Renew
        btnRenew.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a transaction!"); return;
            }

            try {
                long borrowerId = Long.parseLong(String.valueOf(table.getValueAt(selectedRow, 0)));
                long bookId = Long.parseLong(String.valueOf(table.getValueAt(selectedRow, 3)));
                String bookTitle = String.valueOf(table.getValueAt(selectedRow, 4));
                String fineStr = String.valueOf(table.getValueAt(selectedRow, 8)).replace(".0", "");
                long fine = Long.parseLong(fineStr);

                if (fine > 0) {
                    JOptionPane.showMessageDialog(this, "Cannot renew! Unpaid fine: " + fine + " VND.", "Renewal Blocked", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (JOptionPane.showConfirmDialog(this, "Renew '" + bookTitle + "' for 7 days?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    if (transactionDAO.renewBook(borrowerId, bookId, 7)) {
                        JOptionPane.showMessageDialog(this, "Book renewed for 7 days!");
                        loadData.run();
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Data formatting error! " + ex.getMessage());
            }
        });
    }
}