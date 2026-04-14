package org.example;

import org.example.Data_access_object.BookDAO;
import org.example.Data_access_object.TransactionDAO;
import org.example.Data_access_object.BorrowerDAO;
import org.example.models.Book;
import org.example.models.Category;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MainDashBoard {
    private final BookDAO bookTool = new BookDAO();
    private final TransactionDAO transactionTool = new TransactionDAO();
    private final BorrowerDAO borrowerTool = new BorrowerDAO();
    private JButton btnLoadBooksTab1;

    private String userRole;

    public MainDashBoard(String role) {
        this.userRole = role;

        JFrame frame = new JFrame("Library Management System - Role: " + role.toUpperCase());
        frame.setSize(1200, 750);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 14));
        tabbedPane.addTab("  Book Inventory  ", createBookPanel());
        tabbedPane.addTab("  Borrow & Return  ", createTransactionPanel());

        tabbedPane.addTab("  User Management  ", createBorrowerPanel());

        frame.add(tabbedPane);
        frame.setVisible(true);
    }

    private JPanel createBookPanel() {
        JPanel panel = new JPanel(null);

        btnLoadBooksTab1 = new JButton("Load Books");
        btnLoadBooksTab1.setBounds(20, 10, 120, 35);
        panel.add(btnLoadBooksTab1);

        JLabel lblCategory = new JLabel("Category:");
        lblCategory.setBounds(150, 10, 70, 35);
        lblCategory.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(lblCategory);

        List<Category> dbCategories = bookTool.getAllCategories();
        JComboBox<String> cbFilter = new JComboBox<>();
        cbFilter.addItem("All Categories");
        for (Category c : dbCategories) {
            cbFilter.addItem(c.getName());
        }
        cbFilter.setBounds(230, 10, 150, 35);
        panel.add(cbFilter);

        JLabel lblSearch = new JLabel("Search by:");
        lblSearch.setBounds(400, 10, 80, 35);
        lblSearch.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(lblSearch);

        String[] searchTypes = {"All Fields", "Title", "Author", "ID (Book/Author)"};
        JComboBox<String> cbSearchType = new JComboBox<>(searchTypes);
        cbSearchType.setBounds(480, 10, 140, 35);
        panel.add(cbSearchType);

        JTextField txtSearchBook = new JTextField();
        txtSearchBook.setBounds(630, 10, 380, 35);
        panel.add(txtSearchBook);

        JButton btnSearchAll = new JButton("Search");
        btnSearchAll.setBounds(1020, 10, 140, 35);
        panel.add(btnSearchAll);

        JLabel lblSearchHint = new JLabel("Tip: Select specific filters (Title, Author, or ID) to narrow down your search results.");
        lblSearchHint.setBounds(500, 45, 700, 25);
        lblSearchHint.setFont(new Font("SansSerif", Font.ITALIC, 13));
        lblSearchHint.setForeground(Color.GRAY);
        panel.add(lblSearchHint);

        JButton btnAdd = new JButton("Add New Book");
        btnAdd.setBounds(20, 55, 150, 35);
        panel.add(btnAdd);

        JButton btnUpdateQty = new JButton("Update Qty (+/-)");
        btnUpdateQty.setBounds(180, 55, 170, 35);
        panel.add(btnUpdateQty);

        JButton btnDelete = new JButton("Delete Book");
        btnDelete.setBounds(360, 55, 130, 35);
        panel.add(btnDelete);

        if ("STAFF".equalsIgnoreCase(userRole)) {
            btnAdd.setEnabled(false);
            btnUpdateQty.setEnabled(false);
            btnDelete.setEnabled(false);
        }

        String[] columnNames = {"Book ID", "Code", "Title", "Author", "Category", "Total Qty", "Available Qty"};
        javax.swing.table.DefaultTableModel tableModel = new javax.swing.table.DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFont(new Font("SansSerif", Font.PLAIN, 15));
        table.setRowHeight(30);

        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        table.getColumnModel().getColumn(2).setPreferredWidth(350);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(20, 100, 1140, 560);
        panel.add(scrollPane);

        btnSearchAll.addActionListener(e -> {
            String keyword = txtSearchBook.getText().trim();
            String searchType = (String) cbSearchType.getSelectedItem();

            int selectedIndex = cbFilter.getSelectedIndex();
            long categoryId = 0;
            if (selectedIndex > 0) {
                categoryId = dbCategories.get(selectedIndex - 1).getCategoryId();
            }

            tableModel.setRowCount(0);
            List<Book> books = bookTool.searchOmni(keyword, searchType, categoryId);
            for (Book b : books) {
                tableModel.addRow(new Object[]{ b.getBookId(), b.getBookCode(), b.getTitle(), b.getAuthorName(), b.getCategoryName(), b.getTotalQuantity(), b.getAvailableQuantity() });
            }
        });

        cbFilter.addActionListener(e -> btnSearchAll.doClick());
        cbSearchType.addActionListener(e -> btnSearchAll.doClick());

        // Trigger search automatically when the Enter key is pressed in the text field
        txtSearchBook.addActionListener(e -> btnSearchAll.doClick());

        btnLoadBooksTab1.addActionListener(e -> {
            txtSearchBook.setText("");
            cbFilter.setSelectedIndex(0);
            cbSearchType.setSelectedIndex(0);
            btnSearchAll.doClick();
        });

        btnUpdateQty.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(panel, "Please select a book from the table first!");
                return;
            }
            long bookId = ((Number) table.getValueAt(selectedRow, 0)).longValue();
            String bookTitle = (String) table.getValueAt(selectedRow, 2);
            int currentAvailableQty = ((Number) table.getValueAt(selectedRow, 6)).intValue();

            String input = JOptionPane.showInputDialog(panel, "Book: " + bookTitle + "\nCurrent Available: " + currentAvailableQty + "\n\nEnter amount (+ or -):", "Update Quantity", JOptionPane.QUESTION_MESSAGE);
            if (input != null && !input.trim().isEmpty()) {
                try {
                    int amountChange = Integer.parseInt(input.trim());
                    if (amountChange == 0) return;
                    if (amountChange < 0 && Math.abs(amountChange) > currentAvailableQty) {
                        JOptionPane.showMessageDialog(panel, "Invalid Quantity!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (bookTool.updateBookQuantity(bookId, amountChange)) {
                        btnLoadBooksTab1.doClick();
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(panel, "Invalid number!");
                }
            }
        });

        btnAdd.addActionListener(e -> {
            JTextField txtTitle = new JTextField();
            JComboBox<String> cbAuthor = new JComboBox<>();
            cbAuthor.addItem("CREATE A NEW AUTHOR");
            for (String a : bookTool.getAllAuthorsForUI()) cbAuthor.addItem(a);

            JTextField txtNewAuthorName = new JTextField();
            JTextField txtNewAuthorYear = new JTextField();
            JTextField txtQuantity = new JTextField();

            List<Category> dbCategoriesAdd = bookTool.getAllCategories();
            String[] categoryOptions = new String[dbCategoriesAdd.size()];
            for (int i = 0; i < dbCategoriesAdd.size(); i++) categoryOptions[i] = dbCategoriesAdd.get(i).getName();
            JComboBox<String> cbCategoryAdd = new JComboBox<>(categoryOptions);

            Object[] formFields = { "Book Title:", txtTitle, "Authors:", cbAuthor, "New Author Name:", txtNewAuthorName, "Birth Year:", txtNewAuthorYear, "Category:", cbCategoryAdd, "Quantity:", txtQuantity };

            while (true) {
                int option = JOptionPane.showConfirmDialog(panel, formFields, "Add Book", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    try {
                        String title = txtTitle.getText().trim();
                        int qty = Integer.parseInt(txtQuantity.getText().trim());
                        if (qty <= 0) { JOptionPane.showMessageDialog(panel, "Quantity > 0!"); continue; }

                        long catId = dbCategoriesAdd.get(cbCategoryAdd.getSelectedIndex()).getCategoryId();
                        String catName = dbCategoriesAdd.get(cbCategoryAdd.getSelectedIndex()).getName();

                        if (cbAuthor.getSelectedIndex() == 0) {
                            String aName = txtNewAuthorName.getText().trim();
                            int aYear = Integer.parseInt(txtNewAuthorYear.getText().trim());
                            bookTool.addBookSmart(title, aName, aYear, catId, catName, qty);
                        } else {
                            long aId = Long.parseLong(((String)cbAuthor.getSelectedItem()).split(" - ")[0]);
                            bookTool.addBookWithExistingAuthor(title, aId, catId, catName, qty);
                        }
                        btnLoadBooksTab1.doClick();
                        break;
                    } catch (Exception ex) { JOptionPane.showMessageDialog(panel, "Invalid input!"); }
                } else break;
            }
        });

        btnDelete.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) return;
            long bookId = ((Number) table.getValueAt(selectedRow, 0)).longValue();
            if (JOptionPane.showConfirmDialog(panel, "Delete?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (bookTool.deleteBook(bookId)) btnLoadBooksTab1.doClick();
                else JOptionPane.showMessageDialog(panel, "Cannot delete borrowed book!");
            }
        });

        SwingUtilities.invokeLater(() -> btnLoadBooksTab1.doClick());
        return panel;
    }

    private JPanel createTransactionPanel() {
        JPanel panel = new JPanel(null);

        JButton btnLoad = new JButton("View Fines & Dues");
        btnLoad.setBounds(20, 20, 160, 35);
        panel.add(btnLoad);

        JButton btnBorrow = new JButton("Borrow Book");
        btnBorrow.setBounds(190, 20, 160, 35);
        panel.add(btnBorrow);

        JButton btnReturn = new JButton("Return / Renew Book");
        btnReturn.setBounds(360, 20, 190, 35);
        panel.add(btnReturn);

        JLabel lblDate = new JLabel("System Date:" + SystemClock.now());
        lblDate.setBounds(680, 20, 230, 35);
        lblDate.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblDate.setForeground(new Color(25, 28, 27));
        panel.add(lblDate);

        JButton btnTimeTravel = new JButton("Skip Time(+)");
        btnTimeTravel.setBounds(920, 20, 110, 35);
        panel.add(btnTimeTravel);

        JButton btnResetTime = new JButton("Reset Time");
        btnResetTime.setBounds(1040, 20, 110, 35);
        panel.add(btnResetTime);

        JTextArea displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 15));

        JScrollPane scrollPane = new JScrollPane(displayArea);
        scrollPane.setBounds(20, 70, 1140, 590);
        panel.add(scrollPane);

        btnTimeTravel.addActionListener(e -> {
            try {
                int days = Integer.parseInt(JOptionPane.showInputDialog("Days to skip:"));
                SystemClock.addDays(days);
                lblDate.setText("System Date: " + SystemClock.now());
                btnLoad.doClick();
            } catch (Exception ex) {}
        });

        btnResetTime.addActionListener(e -> {
            SystemClock.reset();
            lblDate.setText("System Date: " + SystemClock.now());
            btnLoad.doClick();
        });

        btnLoad.addActionListener(e -> {
            displayArea.setText("===BORROWED BOOKS & FINES LIST===\n\n" + transactionTool.getBorrowerListForUI());
        });

        btnBorrow.addActionListener(e -> {
            new BorrowPopup((JFrame) SwingUtilities.getWindowAncestor(panel)).setVisible(true);
            btnLoad.doClick();
        });

        btnReturn.addActionListener(e -> {
            new ReturnPopup((JFrame) SwingUtilities.getWindowAncestor(panel)).setVisible(true);
            btnLoad.doClick();
        });

        return panel;
    }

    // ==========================================
    // USER MANAGEMENT TAB (BORROWERS)
    // ==========================================
    private JPanel createBorrowerPanel() {
        JPanel panel = new JPanel(null);

        JButton btnLoad = new JButton("Load Users");
        btnLoad.setBounds(20, 10, 130, 35);
        panel.add(btnLoad);

        JLabel lblSearch = new JLabel("Search (ID, Name, Phone, Email):");
        lblSearch.setBounds(170, 10, 260, 35);
        lblSearch.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(lblSearch);

        JTextField txtSearch = new JTextField();
        txtSearch.setBounds(440, 10, 300, 35);
        panel.add(txtSearch);

        JButton btnSearch = new JButton("Search");
        btnSearch.setBounds(750, 10, 100, 35);
        panel.add(btnSearch);

        JButton btnAdd = new JButton("Add User");
        btnAdd.setBounds(20, 55, 130, 35);
        panel.add(btnAdd);

        JButton btnDelete = new JButton("Delete User");
        btnDelete.setBounds(160, 55, 130, 35);
        panel.add(btnDelete);

        String[] cols = {"DB_ID", "User ID (Code)", "Full Name", "Email Address", "Phone Number"};
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFont(new Font("SansSerif", Font.PLAIN, 15));
        table.setRowHeight(30);

        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(250);
        table.getColumnModel().getColumn(3).setPreferredWidth(250);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(20, 100, 1140, 560);
        panel.add(scrollPane);

        Runnable loadData = () -> {
            model.setRowCount(0);
            List<Object[]> users = borrowerTool.searchBorrowers(txtSearch.getText().trim());
            for (Object[] row : users) model.addRow(row);
        };

        btnLoad.addActionListener(e -> { txtSearch.setText(""); loadData.run(); });
        btnSearch.addActionListener(e -> loadData.run());
        txtSearch.addActionListener(e -> loadData.run());

        btnAdd.addActionListener(e -> {
            JTextField txtName = new JTextField();
            JTextField txtEmail = new JTextField();
            JTextField txtPhone = new JTextField();

            Object[] form = { "Full Name:", txtName, "Email (@gmail.com):", txtEmail, "Phone Number (7-8 digits):", txtPhone };

            while (true) {
                int opt = JOptionPane.showConfirmDialog(panel, form, "Register New Borrower", JOptionPane.OK_CANCEL_OPTION);
                if (opt == JOptionPane.OK_OPTION) {
                    String name = txtName.getText().trim();
                    String email = txtEmail.getText().trim();
                    String phone = txtPhone.getText().trim();

                    if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
                        JOptionPane.showMessageDialog(panel, "All fields are required!");
                        continue;
                    }

                    // BLOCK IF PHONE NUMBER IS INVALID
                    if (!borrowerTool.isValidPhoneNumber(phone)) {
                        JOptionPane.showMessageDialog(panel, "Invalid Phone Number!\nMust contain exactly 7 or 8 digits (No letters/symbols).", "Validation Error", JOptionPane.ERROR_MESSAGE);
                        continue;
                    }

                    // BLOCK IF EMAIL IS NOT @GMAIL.COM
                    if (!borrowerTool.isValidEmail(email)) {
                        JOptionPane.showMessageDialog(panel, "Invalid Email!\nThe email must end with '@gmail.com' (e.g., john@gmail.com).", "Validation Error", JOptionPane.ERROR_MESSAGE);
                        continue;
                    }

                    if (borrowerTool.addBorrower(name, email, phone)) {
                        JOptionPane.showMessageDialog(panel, "User registered successfully!");
                        loadData.run();
                        break;
                    } else {
                        JOptionPane.showMessageDialog(panel, "Database error!");
                        break;
                    }
                } else break;
            }
        });

        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(panel, "Select a user first!"); return;
            }
            long id = ((Number) table.getValueAt(row, 0)).longValue();
            String name = (String) table.getValueAt(row, 2);

            int confirm = JOptionPane.showConfirmDialog(panel, "Are you sure you want to delete user: " + name + "?\nAll their return history will be erased.", "Delete User", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (borrowerTool.deleteBorrower(id)) {
                    JOptionPane.showMessageDialog(panel, "User deleted successfully!");
                    loadData.run();
                } else {
                    JOptionPane.showMessageDialog(panel, "Cannot delete!\nThis user is currently borrowing books. They must return all books first.", "Delete Blocked", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        SwingUtilities.invokeLater(loadData);
        return panel;
    }
}