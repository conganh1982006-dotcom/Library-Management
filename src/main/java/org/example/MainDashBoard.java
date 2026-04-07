package org.example;

import org.example.Data_access_object.BookDAO;
import org.example.Data_access_object.TransactionDAO;
import org.example.models.Book;
import org.example.models.Category;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MainDashBoard {
    private final BookDAO bookTool = new BookDAO();
    private final TransactionDAO transactionTool = new TransactionDAO();
    private JButton btnLoadBooksTab1;

    // BIẾN LƯU TRỮ QUYỀN CỦA NGƯỜI DÙNG
    private String userRole;

    // SỬA CONSTRUCTOR ĐỂ NHẬN QUYỀN KHI ĐĂNG NHẬP
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

        frame.add(tabbedPane);
        frame.setVisible(true);
    }

    private JPanel createBookPanel() {
        JPanel panel = new JPanel(null);

        btnLoadBooksTab1 = new JButton("Load Books");
        btnLoadBooksTab1.setBounds(20, 10, 130, 35);
        panel.add(btnLoadBooksTab1);

        JLabel lblCategory = new JLabel("Filter:");
        lblCategory.setBounds(170, 10, 50, 35);
        lblCategory.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(lblCategory);

        List<Category> dbCategories = bookTool.getAllCategories();
        JComboBox<String> cbFilter = new JComboBox<>();
        cbFilter.addItem("All Categories");
        for (Category c : dbCategories) {
            cbFilter.addItem(c.getName());
        }
        cbFilter.setBounds(220, 10, 160, 35);
        panel.add(cbFilter);

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setBounds(700, 10, 60, 35);
        lblSearch.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(lblSearch);

        JTextField txtSearchBook = new JTextField();
        txtSearchBook.setBounds(770, 10, 230, 35);
        panel.add(txtSearchBook);

        JButton btnSearchAll = new JButton("Search All");
        btnSearchAll.setBounds(1010, 10, 140, 35);
        panel.add(btnSearchAll);

        // HINT ĐÃ ĐƯỢC LÀM NGẮN GỌN VÀ RÕ RÀNG HƠN
        JLabel lblSearchHint = new JLabel(" Tip: Search BookID = category + number (e.g. P0001) or Author ID = acronym + birth year (e.g. TKD1990)");
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
            btnAdd.setToolTipText("Only ADMIN can add new books!");

            btnUpdateQty.setEnabled(false);
            btnUpdateQty.setToolTipText("Only ADMIN can update inventory!");

            btnDelete.setEnabled(false);
            btnDelete.setToolTipText("Only ADMIN can delete books!");
        }

        String[] columnNames = {"Book ID", "Code", "Title", "Author", "Category", "Total Qty", "Available Qty"};
        javax.swing.table.DefaultTableModel tableModel = new javax.swing.table.DefaultTableModel(columnNames, 0);

        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFont(new Font("SansSerif", Font.PLAIN, 15));
        table.setRowHeight(30);

        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);

        table.getColumnModel().getColumn(2).setPreferredWidth(350);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(20, 100, 1140, 560);
        panel.add(scrollPane);

        btnSearchAll.addActionListener(e -> {
            String keyword = txtSearchBook.getText().trim();
            int selectedIndex = cbFilter.getSelectedIndex();
            long categoryId = 0;
            if (selectedIndex > 0) {
                categoryId = dbCategories.get(selectedIndex - 1).getCategoryId();
            }

            tableModel.setRowCount(0);
            List<Book> books = bookTool.searchOmni(keyword, categoryId);

            for (Book b : books) {
                tableModel.addRow(new Object[]{ b.getBookId(), b.getBookCode(), b.getTitle(), b.getAuthorName(), b.getCategoryName(), b.getTotalQuantity(), b.getAvailableQuantity() });
            }
            if (books.isEmpty() && (!keyword.isEmpty() || categoryId > 0)) {
                JOptionPane.showMessageDialog(panel, "No results found!", "Search Result", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        cbFilter.addActionListener(e -> btnSearchAll.doClick());

        btnLoadBooksTab1.addActionListener(e -> {
            txtSearchBook.setText("");
            cbFilter.setSelectedIndex(0);
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

            String input = JOptionPane.showInputDialog(panel,
                    "Book: " + bookTitle + "\nCurrent Available: " + currentAvailableQty +
                            "\n\n• Enter a POSITIVE number to add copies\n• Enter a NEGATIVE number to remove copies",
                    "Update Quantity", JOptionPane.QUESTION_MESSAGE);

            if (input != null && !input.trim().isEmpty()) {
                try {
                    int amountChange = Integer.parseInt(input.trim());
                    if (amountChange == 0) return;

                    if (amountChange < 0 && Math.abs(amountChange) > currentAvailableQty) {
                        JOptionPane.showMessageDialog(panel,
                                "You can't throw away more books than you have on the shelves!\n" +
                                        "Currently have: " + currentAvailableQty + " books.",
                                "Invalid Quantity", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    boolean success = bookTool.updateBookQuantity(bookId, amountChange);
                    if (success) {
                        JOptionPane.showMessageDialog(panel, "Quantity updated successfully!");
                        btnLoadBooksTab1.doClick();
                    } else {
                        JOptionPane.showMessageDialog(panel, "ERROR: Cannot update quantity.", "Update Blocked", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(panel, "Please enter a valid whole number!");
                }
            }
        });

        btnAdd.addActionListener(e -> {
            JTextField txtTitle = new JTextField();
            JComboBox<String> cbAuthor = new JComboBox<>();
            cbAuthor.addItem("CREATE A NEW AUTHOR");
            List<String> dbAuthors = bookTool.getAllAuthorsForUI();
            for (String a : dbAuthors) {
                cbAuthor.addItem(a);
            }

            JTextField txtNewAuthorName = new JTextField();
            JTextField txtNewAuthorYear = new JTextField();
            JTextField txtQuantity = new JTextField();

            List<Category> dbCategoriesAdd = bookTool.getAllCategories();
            String[] categoryOptions = new String[dbCategoriesAdd.size()];
            for (int i = 0; i < dbCategoriesAdd.size(); i++) {
                categoryOptions[i] = dbCategoriesAdd.get(i).getName();
            }
            JComboBox<String> cbCategoryAdd = new JComboBox<>(categoryOptions);

            Object[] formFields = {
                    "Book Title:", txtTitle,
                    "Choose from the available authors:", cbAuthor,
                    "OR Enter a New Author:", txtNewAuthorName,
                    "Year of Birth (If creating a new one):", txtNewAuthorYear,
                    "Category:", cbCategoryAdd,
                    "Quantity:", txtQuantity
            };

            while (true) {
                int option = JOptionPane.showConfirmDialog(panel, formFields, "Add New Book", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (option == JOptionPane.OK_OPTION) {
                    String title = txtTitle.getText().trim();
                    String qtyStr = txtQuantity.getText().trim();
                    boolean isCreatingNewAuthor = (cbAuthor.getSelectedIndex() == 0);

                    if (title.isEmpty() || qtyStr.isEmpty()) {
                        JOptionPane.showMessageDialog(panel, "Please fill in the Book Title and Quantity!");
                        continue;
                    }
                    if (isCreatingNewAuthor && (txtNewAuthorName.getText().trim().isEmpty() || txtNewAuthorYear.getText().trim().isEmpty())) {
                        JOptionPane.showMessageDialog(panel, "You have selected 'Create new author', please fill in your full name and year of birth!");
                        continue;
                    }

                    try {
                        int qty = Integer.parseInt(qtyStr);
                        int catIndex = cbCategoryAdd.getSelectedIndex();
                        long categoryId = dbCategoriesAdd.get(catIndex).getCategoryId();
                        String categoryName = dbCategoriesAdd.get(catIndex).getName();

                        boolean success;

                        if (isCreatingNewAuthor) {
                            String authorName = txtNewAuthorName.getText().trim();
                            int birthYear = Integer.parseInt(txtNewAuthorYear.getText().trim());

                            int currentYear = SystemClock.now().getYear();
                            if (birthYear < 1000 || birthYear > currentYear) {
                                JOptionPane.showMessageDialog(panel,
                                        " Invalid Year of Birth!\nPlease enter a valid 4-digit year (e.g., 1990) that does not exceed the current year (" + currentYear + ").",
                                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
                                continue;
                            }

                            success = bookTool.addBookSmart(title, authorName, birthYear, categoryId, categoryName, qty);
                        } else {
                            String selectedAuthor = (String) cbAuthor.getSelectedItem();
                            long authorId = Long.parseLong(selectedAuthor.split(" - ")[0]);
                            success = bookTool.addBookWithExistingAuthor(title, authorId, categoryId, categoryName, qty);
                        }

                        if (success) {
                            JOptionPane.showMessageDialog(panel, "Book added successfully!");
                            btnLoadBooksTab1.doClick();
                            break;
                        } else {
                            JOptionPane.showMessageDialog(panel, "Database error! Cannot add book.", "Error", JOptionPane.ERROR_MESSAGE);
                            continue;
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(panel, "The Quantity and Year of Birth must be valid integers!");
                        continue;
                    }
                } else {
                    break;
                }
            }
        });

        btnDelete.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(panel, "Please select a book from the table first!");
                return;
            }
            long bookId = ((Number) table.getValueAt(selectedRow, 0)).longValue();
            String bookName = (String) table.getValueAt(selectedRow, 2);

            int confirm = JOptionPane.showConfirmDialog(panel, "Are you sure you want to delete:" + bookName + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = bookTool.deleteBook(bookId);
                if (success) {
                    JOptionPane.showMessageDialog(panel, "Deleted successfully!");
                    btnLoadBooksTab1.doClick();
                } else {
                    JOptionPane.showMessageDialog(panel, "ERROR: Cannot delete! This book is currently borrowed.", "Delete Blocked", JOptionPane.ERROR_MESSAGE);
                }
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

        JButton btnReturn = new JButton("Return Book");
        btnReturn.setBounds(360, 20, 160, 35);
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
            String input = JOptionPane.showInputDialog(panel, "Enter number of days to skip forward (e.g., 5):", "Time Travel Debug", JOptionPane.WARNING_MESSAGE);
            if (input != null && !input.trim().isEmpty()) {
                try {
                    int days = Integer.parseInt(input.trim());
                    SystemClock.addDays(days);
                    lblDate.setText("System Date: " + SystemClock.now());
                    JOptionPane.showMessageDialog(panel, "Time skipped! Current date is now: " + SystemClock.now());
                    btnLoad.doClick();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(panel, "Please enter a valid number!");
                }
            }
        });

        btnResetTime.addActionListener(e -> {
            SystemClock.reset();
            lblDate.setText("System Date: " + SystemClock.now());
            JOptionPane.showMessageDialog(panel, "Time has been reset to the real world date!");
            btnLoad.doClick();
        });

        btnLoad.addActionListener(e -> {
            displayArea.setText("===BORROWED BOOKS & FINES LIST===\n\n");
            displayArea.append(transactionTool.getBorrowerListForUI());
        });

        btnBorrow.addActionListener(e -> {
            BorrowPopup popup = new BorrowPopup((JFrame) SwingUtilities.getWindowAncestor(panel));
            popup.setVisible(true);
            btnLoad.doClick();
            if (btnLoadBooksTab1 != null) btnLoadBooksTab1.doClick();
        });

        btnReturn.addActionListener(e -> {
            ReturnPopup popup = new ReturnPopup((JFrame) SwingUtilities.getWindowAncestor(panel));
            popup.setVisible(true);
            btnLoad.doClick();
            if (btnLoadBooksTab1 != null) btnLoadBooksTab1.doClick();
        });

        return panel;
    }
}