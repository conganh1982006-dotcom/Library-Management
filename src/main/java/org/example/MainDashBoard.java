package org.example;

import org.example.Data_access_object.BookDAO;
import org.example.Data_access_object.TransactionDAO;
import org.example.Data_access_object.BorrowerDAO;
import org.example.Data_access_object.DamagedBookDAO;
import org.example.models.Book;
import org.example.models.Category;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MainDashBoard {
    private final BookDAO bookTool = new BookDAO();
    private final TransactionDAO transactionTool = new TransactionDAO();
    private final BorrowerDAO borrowerTool = new BorrowerDAO();
    private final DamagedBookDAO damagedTool = new DamagedBookDAO(); // Added DamagedBookDAO
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
        tabbedPane.addTab("  Damaged Books  ", createDamagedPanel()); // Added 4th Tab

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

        String[] searchTypes = {"All Fields", "Title", "Author", "Book Code", "Author Code"};
        JComboBox<String> cbSearchType = new JComboBox<>(searchTypes);
        cbSearchType.setBounds(480, 10, 140, 35);
        panel.add(cbSearchType);

        JTextField txtSearchBook = new JTextField();
        txtSearchBook.setBounds(630, 10, 380, 35);
        panel.add(txtSearchBook);

        JButton btnSearchAll = new JButton("Search");
        btnSearchAll.setBounds(1020, 10, 140, 35);
        panel.add(btnSearchAll);

        JLabel lblSearchHint = new JLabel("Tip: Select specific filters (Title, Author, or Code) to narrow down your search results.");
        lblSearchHint.setBounds(500, 45, 700, 25);
        lblSearchHint.setFont(new Font("SansSerif", Font.ITALIC, 13));
        lblSearchHint.setForeground(Color.GRAY);
        panel.add(lblSearchHint);

        JButton btnAdd = new JButton("Add New Book");
        btnAdd.setBounds(20, 55, 150, 35);
        panel.add(btnAdd);

        JButton btnEditBook = new JButton("Edit Book");
        btnEditBook.setBounds(180, 55, 170, 35);
        panel.add(btnEditBook);

        JButton btnDelete = new JButton("Delete Book");
        btnDelete.setBounds(360, 55, 130, 35);
        panel.add(btnDelete);

        // Role based UI protection for staff
        if ("STAFF".equalsIgnoreCase(userRole)) {
            btnAdd.setEnabled(false);
            btnEditBook.setEnabled(false);
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

        // Execute search query
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
        txtSearchBook.addActionListener(e -> btnSearchAll.doClick());

        btnLoadBooksTab1.addActionListener(e -> {
            txtSearchBook.setText("");
            cbFilter.setSelectedIndex(0);
            cbSearchType.setSelectedIndex(0);
            btnSearchAll.doClick();
        });

        // Edit book title and quantity functionality
        btnEditBook.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(panel, "Please select a book from the table first!");
                return;
            }

            long bookId = ((Number) table.getValueAt(selectedRow, 0)).longValue();
            String currentTitle = (String) table.getValueAt(selectedRow, 2);
            int currentAvailableQty = ((Number) table.getValueAt(selectedRow, 6)).intValue();

            JTextField txtEditTitle = new JTextField(currentTitle);
            JTextField txtQtyChange = new JTextField("0");

            Object[] formFields = {
                    "Edit Book Title:", txtEditTitle,
                    "Quantity Change (+/-) (Available: " + currentAvailableQty + "):", txtQtyChange
            };

            int option = JOptionPane.showConfirmDialog(panel, formFields, "Edit Book Information", JOptionPane.OK_CANCEL_OPTION);

            if (option == JOptionPane.OK_OPTION) {
                try {
                    String newTitle = txtEditTitle.getText().trim();
                    if (newTitle.isEmpty()) {
                        JOptionPane.showMessageDialog(panel, "Title cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    int amountChange = Integer.parseInt(txtQtyChange.getText().trim());

                    if (amountChange < 0 && Math.abs(amountChange) > currentAvailableQty) {
                        JOptionPane.showMessageDialog(panel, "Invalid Quantity! You cannot reduce more than available.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (bookTool.updateBookInfo(bookId, newTitle, amountChange)) {
                        btnLoadBooksTab1.doClick();
                        JOptionPane.showMessageDialog(panel, "Book updated successfully!");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(panel, "Invalid number format!");
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

            // Disable author text fields if an existing author is selected from the combobox
            cbAuthor.addActionListener(event -> {
                boolean isNewAuthor = cbAuthor.getSelectedIndex() == 0;
                txtNewAuthorName.setEnabled(isNewAuthor);
                txtNewAuthorYear.setEnabled(isNewAuthor);

                if (!isNewAuthor) {
                    txtNewAuthorName.setText("");
                    txtNewAuthorYear.setText("");
                }
            });

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
                        if (qty <= 0) { JOptionPane.showMessageDialog(panel, "Quantity must be greater than 0!"); continue; }

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
                    } catch (Exception ex) { JOptionPane.showMessageDialog(panel, "Invalid input format!"); }
                } else break;
            }
        });

        btnDelete.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) return;
            long bookId = ((Number) table.getValueAt(selectedRow, 0)).longValue();
            if (JOptionPane.showConfirmDialog(panel, "Confirm deletion?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (bookTool.deleteBook(bookId)) btnLoadBooksTab1.doClick();
                else JOptionPane.showMessageDialog(panel, "Cannot delete a book that is currently borrowed!");
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
                String res = JOptionPane.showInputDialog("Days to skip:");
                if (res != null) {
                    int days = Integer.parseInt(res);
                    SystemClock.addDays(days);
                    lblDate.setText("System Date: " + SystemClock.now());
                    btnLoad.doClick();
                }
            } catch (Exception ex) {}
        });

        btnResetTime.addActionListener(e -> {
            SystemClock.reset();
            lblDate.setText("System Date: " + SystemClock.now());
            btnLoad.doClick();
        });

        btnLoad.addActionListener(e -> {
            displayArea.setText("=== BORROWED BOOKS & FINES LIST ===\n\n" + transactionTool.getBorrowerListForUI());
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

        JButton btnEditUser = new JButton("Edit User");
        btnEditUser.setBounds(160, 55, 130, 35);
        panel.add(btnEditUser);

        JButton btnDelete = new JButton("Delete User");
        btnDelete.setBounds(300, 55, 130, 35);
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

        // Add User functionality with strict input mode
        btnAdd.addActionListener(e -> {
            JTextField txtName = new JTextField();
            JTextField txtEmail = new JTextField();
            JTextField txtPhone = new JTextField();

            // Strict mode: Prevent typing any characters other than numbers
            ((javax.swing.text.AbstractDocument) txtPhone.getDocument()).setDocumentFilter(new javax.swing.text.DocumentFilter() {
                @Override
                public void replace(FilterBypass fb, int offset, int length, String text, javax.swing.text.AttributeSet attrs) throws javax.swing.text.BadLocationException {
                    if (text.matches("[0-9]*")) {
                        super.replace(fb, offset, length, text, attrs);
                    }
                }
            });

            Object[] form = { "Full Name:", txtName, "Email (@gmail.com):", txtEmail, "Phone Number (7-8 digits):", txtPhone };

            while (true) {
                int opt = JOptionPane.showConfirmDialog(panel, form, "Register New Borrower", JOptionPane.OK_CANCEL_OPTION);
                if (opt == JOptionPane.OK_OPTION) {
                    String name = txtName.getText().trim();
                    String email = txtEmail.getText().trim();
                    String phone = txtPhone.getText().trim();

                    if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
                        JOptionPane.showMessageDialog(panel, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                        continue;
                    }

                    if (!borrowerTool.isValidPhoneNumber(phone)) {
                        JOptionPane.showMessageDialog(panel, "Invalid Phone Number! Must contain exactly 7 or 8 digits.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                        continue;
                    }

                    if (!borrowerTool.isValidEmail(email)) {
                        JOptionPane.showMessageDialog(panel, "Invalid Email! Must end with @gmail.com", "Validation Error", JOptionPane.ERROR_MESSAGE);
                        continue;
                    }

                    if (borrowerTool.addBorrower(name, email, phone)) {
                        JOptionPane.showMessageDialog(panel, "User registered successfully!");
                        loadData.run();
                        break;
                    } else {
                        JOptionPane.showMessageDialog(panel, "Database error or duplicated code!", "Error", JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                } else break;
            }
        });

        // Edit User functionality with strict input mode
        btnEditUser.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(panel, "Please select a user to edit first!");
                return;
            }

            long id = ((Number) table.getValueAt(row, 0)).longValue();
            String currentName = (String) table.getValueAt(row, 2);
            String currentEmail = (String) table.getValueAt(row, 3);
            String currentPhone = (String) table.getValueAt(row, 4);

            JTextField txtEditName = new JTextField(currentName);
            JTextField txtEditEmail = new JTextField(currentEmail);
            JTextField txtEditPhone = new JTextField(currentPhone);

            // Strict mode: Prevent typing any characters other than numbers
            ((javax.swing.text.AbstractDocument) txtEditPhone.getDocument()).setDocumentFilter(new javax.swing.text.DocumentFilter() {
                @Override
                public void replace(FilterBypass fb, int offset, int length, String text, javax.swing.text.AttributeSet attrs) throws javax.swing.text.BadLocationException {
                    if (text.matches("[0-9]*")) {
                        super.replace(fb, offset, length, text, attrs);
                    }
                }
            });

            Object[] form = {
                    "Full Name:", txtEditName,
                    "Email (@gmail.com):", txtEditEmail,
                    "Phone Number (7-8 digits):", txtEditPhone
            };

            while (true) {
                int opt = JOptionPane.showConfirmDialog(panel, form, "Edit Borrower Information", JOptionPane.OK_CANCEL_OPTION);
                if (opt == JOptionPane.OK_OPTION) {
                    String newName = txtEditName.getText().trim();
                    String newEmail = txtEditEmail.getText().trim();
                    String newPhone = txtEditPhone.getText().trim();

                    if (newName.isEmpty() || newPhone.isEmpty() || newEmail.isEmpty()) {
                        JOptionPane.showMessageDialog(panel, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                        continue;
                    }

                    if (!borrowerTool.isValidPhoneNumber(newPhone)) {
                        JOptionPane.showMessageDialog(panel, "Invalid Phone Number! Must be exactly 7 or 8 digits.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                        continue;
                    }

                    if (!borrowerTool.isValidEmail(newEmail)) {
                        JOptionPane.showMessageDialog(panel, "Invalid Email! Must end with @gmail.com", "Validation Error", JOptionPane.ERROR_MESSAGE);
                        continue;
                    }

                    if (borrowerTool.updateBorrowerInfo(id, newName, newEmail, newPhone)) {
                        JOptionPane.showMessageDialog(panel, "User updated successfully!");
                        loadData.run();
                        break;
                    } else {
                        JOptionPane.showMessageDialog(panel, "Database error! Update failed.", "Error", JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                } else {
                    break;
                }
            }
        });

        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(panel, "Please select a user first!"); return;
            }
            long id = ((Number) table.getValueAt(row, 0)).longValue();
            String name = (String) table.getValueAt(row, 2);

            int confirm = JOptionPane.showConfirmDialog(panel, "Are you sure you want to delete user: " + name + "?", "Delete User", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (borrowerTool.deleteBorrower(id)) {
                    JOptionPane.showMessageDialog(panel, "User deleted successfully!");
                    loadData.run();
                } else {
                    JOptionPane.showMessageDialog(panel, "Cannot delete! This user still has books to return.", "Delete Blocked", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        SwingUtilities.invokeLater(loadData);
        return panel;
    }

    // NEW TAB: DAMAGED BOOKS
    private JPanel createDamagedPanel() {
        JPanel panel = new JPanel(null);

        JLabel lblSearch = new JLabel("Search by Book Code/Title:");
        lblSearch.setBounds(20, 15, 250, 30);
        lblSearch.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(lblSearch);

        JTextField txtSearch = new JTextField();
        txtSearch.setBounds(250, 15, 300, 35);
        panel.add(txtSearch);

        JButton btnSearch = new JButton("Search");
        btnSearch.setBounds(570, 15, 100, 35);
        panel.add(btnSearch);

        JButton btnLoad = new JButton("Refresh List");
        btnLoad.setBounds(680, 15, 120, 35);
        panel.add(btnLoad);

        JButton btnAddIssue = new JButton("Report Damage");
        btnAddIssue.setBounds(20, 60, 150, 35);
        panel.add(btnAddIssue);

        JButton btnMarkRepaired = new JButton("Mark as Repaired");
        btnMarkRepaired.setBounds(180, 60, 160, 35);
        panel.add(btnMarkRepaired);

        // Clear repaired history button
        JButton btnClearHistory = new JButton("Clear Repaired History");
        btnClearHistory.setBounds(350, 60, 200, 35);
        btnClearHistory.setForeground(Color.RED);
        panel.add(btnClearHistory);

        String[] cols = {"Issue ID", "Book ID", "Book Code", "Book Title", "Damage Type", "Notes", "Status"};
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(30);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));

        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(1).setMinWidth(0);
        table.getColumnModel().getColumn(1).setMaxWidth(0);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(20, 110, 1140, 550);
        panel.add(scrollPane);

        Runnable loadData = () -> {
            model.setRowCount(0);
            List<Object[]> issues = damagedTool.getAllDamagedBooks(txtSearch.getText().trim());
            for (Object[] row : issues) model.addRow(row);
        };

        btnLoad.addActionListener(e -> { txtSearch.setText(""); loadData.run(); });
        btnSearch.addActionListener(e -> loadData.run());
        txtSearch.addActionListener(e -> loadData.run());

        btnAddIssue.addActionListener(e -> {
            JTextField txtBookCode = new JTextField();
            String[] damageTypes = {"Torn Pages", "Water Damage", "Lost/Missing", "Other"};
            JComboBox<String> cbDamageType = new JComboBox<>(damageTypes);
            JTextArea txtNotes = new JTextArea(3, 20);
            txtNotes.setLineWrap(true);

            Object[] form = {
                    "Enter EXACT Book Code (e.g. THE0001):", txtBookCode,
                    "Select Damage Type:", cbDamageType,
                    "Additional Notes:", new JScrollPane(txtNotes)
            };

            if (JOptionPane.showConfirmDialog(panel, form, "Report a Damaged Book", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                String code = txtBookCode.getText().trim();
                String type = (String) cbDamageType.getSelectedItem();
                String notes = txtNotes.getText().trim();

                if(code.isEmpty()) {
                    JOptionPane.showMessageDialog(panel, "Book Code is required!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (damagedTool.reportDamage(code, type, notes)) {
                    JOptionPane.showMessageDialog(panel, "Damage reported! Book moved out of available inventory.");
                    loadData.run();
                    btnLoadBooksTab1.doClick();
                } else {
                    JOptionPane.showMessageDialog(panel, "Failed! Book Code not found or Available Quantity is 0.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnMarkRepaired.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(panel, "Please select an issue to mark as repaired!"); return;
            }

            String status = (String) table.getValueAt(row, 6);
            if ("REPAIRED".equals(status)) {
                JOptionPane.showMessageDialog(panel, "This book is already marked as repaired!"); return;
            }

            int confirm = JOptionPane.showConfirmDialog(panel, "Is this book fully repaired and ready to be borrowed again?", "Confirm Repair", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                int issueId = ((Number) table.getValueAt(row, 0)).intValue();
                long bookId = ((Number) table.getValueAt(row, 1)).longValue();

                if (damagedTool.markAsRepaired(issueId, bookId)) {
                    JOptionPane.showMessageDialog(panel, "Book repaired! Added back to available inventory.");
                    loadData.run();
                    btnLoadBooksTab1.doClick();
                } else {
                    JOptionPane.showMessageDialog(panel, "Error updating database!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Delete repaired records from history button functionality
        btnClearHistory.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(panel, "Are you sure you want to delete all 'REPAIRED' records from history?", "Clear History", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (damagedTool.deleteRepairedIssues()) {
                    JOptionPane.showMessageDialog(panel, "Repaired history cleared successfully!");
                    loadData.run();
                }
            }
        });

        SwingUtilities.invokeLater(loadData);
        return panel;
    }
}