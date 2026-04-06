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

    // 🌟 BIẾN LƯU TRỮ QUYỀN CỦA NGƯỜI DÙNG
    private String userRole;

    // 🌟 SỬA CONSTRUCTOR ĐỂ NHẬN QUYỀN KHI ĐĂNG NHẬP
    public MainDashBoard(String role) {
        this.userRole = role; // Lưu lại quyền

        // Hiển thị quyền lên tiêu đề của cửa sổ luôn cho ngầu
        JFrame frame = new JFrame("Library Management System - Role: " + role.toUpperCase());
        frame.setSize(950, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Book Inventory", createBookPanel());
        tabbedPane.addTab("Borrow & Return", createTransactionPanel());

        frame.add(tabbedPane);
        frame.setVisible(true);
    }

    private JPanel createBookPanel() {
        JPanel panel = new JPanel(null);

        btnLoadBooksTab1 = new JButton("1. Load Books");
        btnLoadBooksTab1.setBounds(20, 10, 120, 35);
        panel.add(btnLoadBooksTab1);

        // THÊM DROPDOWN FILTER LỌC THỂ LOẠI
        JLabel lblCategory = new JLabel("Filter:");
        lblCategory.setBounds(150, 10, 50, 35);
        lblCategory.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(lblCategory);

        List<Category> dbCategories = bookTool.getAllCategories();
        JComboBox<String> cbFilter = new JComboBox<>();
        cbFilter.addItem("0 - All Categories"); // Lựa chọn mặc định
        for (Category c : dbCategories) {
            cbFilter.addItem(c.getCategoryId() + " - " + c.getName());
        }
        cbFilter.setBounds(200, 10, 140, 35);
        panel.add(cbFilter);

        // THANH SEARCH ĐƯỢC DI CHUYỂN DỊCH SANG BÊN PHẢI
        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setBounds(350, 10, 60, 35);
        lblSearch.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(lblSearch);

        JTextField txtSearchBook = new JTextField();
        txtSearchBook.setBounds(410, 10, 200, 35);
        panel.add(txtSearchBook);

        JButton btnSearchAll = new JButton("Search All");
        btnSearchAll.setBounds(620, 10, 130, 35);
        panel.add(btnSearchAll);

        JButton btnAdd = new JButton("Add New Book");
        btnAdd.setBounds(20, 55, 150, 35);
        panel.add(btnAdd);

        JButton btnUpdateQty = new JButton("Update Qty (+/-)");
        btnUpdateQty.setBounds(180, 55, 170, 35);
        panel.add(btnUpdateQty);

        JButton btnDelete = new JButton("3. Delete Book");
        btnDelete.setBounds(360, 55, 130, 35);
        panel.add(btnDelete);

        // KHIÊN BẢO VỆ PHÂN QUYỀN (RBAC)
        if ("STAFF".equalsIgnoreCase(userRole)) {
            // Nếu là Staff, làm mờ 3 nút này đi và hiện thông báo cấm
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
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.setRowHeight(25);

        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);

        table.getColumnModel().getColumn(2).setPreferredWidth(250);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(20, 100, 890, 450);
        panel.add(scrollPane);

        // SỰ KIỆN TÌM KIẾM KẾT HỢP LỌC (Lấy dữ liệu từ cả 2 thanh)
        btnSearchAll.addActionListener(e -> {
            String keyword = txtSearchBook.getText().trim();
            String selectedFilter = (String) cbFilter.getSelectedItem();
            long categoryId = Long.parseLong(selectedFilter.split(" - ")[0]); // Lấy ra số ID

            tableModel.setRowCount(0);

            // Gọi hàm Omni-Search mới có Lọc Thể Loại
            List<Book> books = bookTool.searchOmni(keyword, categoryId);

            for (Book b : books) {
                tableModel.addRow(new Object[]{ b.getBookId(), b.getBookCode(), b.getTitle(), b.getAuthorName(), b.getCategoryName(), b.getTotalQuantity(), b.getAvailableQuantity() });
            }
            if (books.isEmpty() && (!keyword.isEmpty() || categoryId > 0)) {
                JOptionPane.showMessageDialog(panel, "No results found!", "Search Result", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Bấm thả Dropdown là Tự động Search luôn
        cbFilter.addActionListener(e -> btnSearchAll.doClick());

        // Nút Load mặc định kích hoạt tìm kiếm trống (Hiện tất cả)
        btnLoadBooksTab1.addActionListener(e -> {
            txtSearchBook.setText("");
            cbFilter.setSelectedIndex(0); // Đưa về "All Categories"
            btnSearchAll.doClick();
        });

        // SỰ KIỆN UPDATE SỐ LƯỢNG
        btnUpdateQty.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(panel, "Please select a book from the table first!");
                return;
            }

            // Lấy ID, Tên sách và SỐ LƯỢNG HIỆN TẠI trên bảng
            long bookId = ((Number) table.getValueAt(selectedRow, 0)).longValue();
            String bookTitle = (String) table.getValueAt(selectedRow, 2);
            int currentAvailableQty = ((Number) table.getValueAt(selectedRow, 6)).intValue(); // Lấy cột số 6 (Available Qty)

            String input = JOptionPane.showInputDialog(panel,
                    "Book: " + bookTitle + "\nCurrent Available: " + currentAvailableQty +
                            "\n\n• Enter a POSITIVE number to add copies\n• Enter a NEGATIVE number to remove copies",
                    "Update Quantity", JOptionPane.QUESTION_MESSAGE);

            if (input != null && !input.trim().isEmpty()) {
                try {
                    int amountChange = Integer.parseInt(input.trim());
                    if (amountChange == 0) return;

                    //BẢO VỆ CHỐNG ÂM SÁCH
                    if (amountChange < 0 && Math.abs(amountChange) > currentAvailableQty) {
                        JOptionPane.showMessageDialog(panel,
                                "You can't throw away more books than you have on the shelves!\n" +
                                        "Currently have: " + currentAvailableQty + " books.",
                                "Invalid Quantity", JOptionPane.ERROR_MESSAGE);
                        return; // Dừng luôn, không cho gọi xuống Database
                    }

                    // Nếu qua được lớp bảo vệ thì mới cho cập nhật
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

        // SỰ KIỆN THÊM SÁCH (Đã nâng cấp chọn Tác giả có sẵn)
        btnAdd.addActionListener(e -> {
            JTextField txtTitle = new JTextField();

            // 🌟 1. TẠO COMBOBOX CHỨA TÁC GIẢ
            JComboBox<String> cbAuthor = new JComboBox<>();
            cbAuthor.addItem("CREATE A NEW AUTHOR"); // Option mặc định
            List<String> dbAuthors = bookTool.getAllAuthorsForUI();
            for (String a : dbAuthors) {
                cbAuthor.addItem(a);
            }

            // Các ô nhập liệu cho Tác giả mới (Sẽ chỉ dùng nếu chọn option ➕)
            JTextField txtNewAuthorName = new JTextField();
            JTextField txtNewAuthorYear = new JTextField();
            JTextField txtQuantity = new JTextField();

            List<Category> dbCategoriesAdd = bookTool.getAllCategories();
            String[] categoryOptions = new String[dbCategoriesAdd.size()];
            for (int i = 0; i < dbCategoriesAdd.size(); i++) {
                Category c = dbCategoriesAdd.get(i);
                categoryOptions[i] = c.getCategoryId() + " - " + c.getName();
            }
            JComboBox<String> cbCategoryAdd = new JComboBox<>(categoryOptions);

            // Bố cục Form mới nhìn chuyên nghiệp hơn
            Object[] formFields = {
                    "Book Title:", txtTitle,
                    "Choose from the available authors:", cbAuthor,
                    "OR Enter a New Author:", txtNewAuthorName,
                    "Year of Birth (If creating a new one):", txtNewAuthorYear,
                    "Category:", cbCategoryAdd,
                    "Quantity:", txtQuantity
            };

            int option = JOptionPane.showConfirmDialog(panel, formFields, "Add New Book", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (option == JOptionPane.OK_OPTION) {
                String title = txtTitle.getText().trim();
                String qtyStr = txtQuantity.getText().trim();
                boolean isCreatingNewAuthor = (cbAuthor.getSelectedIndex() == 0); // Kiểm tra xem người dùng chọn có sẵn hay tạo mới

                // 🌟 KIỂM TRA LỖI NHẬP LIỆU THÔNG MINH
                if (title.isEmpty() || qtyStr.isEmpty()) {
                    JOptionPane.showMessageDialog(panel, "Please fill in the Book Title and Quantity!");
                    return;
                }
                if (isCreatingNewAuthor && (txtNewAuthorName.getText().trim().isEmpty() || txtNewAuthorYear.getText().trim().isEmpty())) {
                    JOptionPane.showMessageDialog(panel, "You have selected 'Create new author', please fill in your full name and year of birth!");
                    return;
                }

                try {
                    int qty = Integer.parseInt(qtyStr);
                    String selectedCategory = (String) cbCategoryAdd.getSelectedItem();
                    long categoryId = Long.parseLong(selectedCategory.split(" - ")[0]);
                    String categoryName = selectedCategory.split(" - ")[1];

                    boolean success;

                    if (isCreatingNewAuthor) {
                        // KỊCH BẢN 1: Tạo tác giả mới (Dùng hàm cũ)
                        String authorName = txtNewAuthorName.getText().trim();
                        int birthYear = Integer.parseInt(txtNewAuthorYear.getText().trim());
                        success = bookTool.addBookSmart(title, authorName, birthYear, categoryId, categoryName, qty);
                    } else {
                        // KỊCH BẢN 2: Dùng tác giả có sẵn (Cắt ID từ Dropdown và gửi xuống DB)
                        String selectedAuthor = (String) cbAuthor.getSelectedItem();
                        long authorId = Long.parseLong(selectedAuthor.split(" - ")[0]);
                        success = bookTool.addBookWithExistingAuthor(title, authorId, categoryId, categoryName, qty);
                    }

                    if (success) {
                        JOptionPane.showMessageDialog(panel, "Book added successfully!");
                        btnLoadBooksTab1.doClick();
                    } else {
                        JOptionPane.showMessageDialog(panel, "Database error! Cannot add book.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(panel, "The number and year of birth must be valid integers!");
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

        JButton btnLoad = new JButton("5. View Fines & Dues");
        btnLoad.setBounds(20, 20, 160, 35);
        panel.add(btnLoad);

        JButton btnBorrow = new JButton("4. Borrow Book");
        btnBorrow.setBounds(190, 20, 160, 35);
        panel.add(btnBorrow);

        JButton btnReturn = new JButton("6. Return Book");
        btnReturn.setBounds(360, 20, 160, 35);
        panel.add(btnReturn);

        //THÊM ĐỒNG HỒ HIỂN THỊ NGÀY CỦA HỆ THỐNG
        JLabel lblDate = new JLabel("System Date:" + SystemClock.now());
        lblDate.setBounds(550, 20, 250, 35);
        lblDate.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblDate.setForeground(new Color(25, 28, 27)); // Màu đỏ cho nổi bật
        panel.add(lblDate);

        //NÚT TUA NHANH THỜI GIAN
        JButton btnTimeTravel = new JButton("Skip Time(+)");
        btnTimeTravel.setBounds(760, 20, 150, 35);
        panel.add(btnTimeTravel);

        JTextArea displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(displayArea);
        scrollPane.setBounds(20, 70, 890, 480);
        panel.add(scrollPane);

        //SỰ KIỆN TUA NHANH THỜI GIAN
        btnTimeTravel.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(panel, "Enter number of days to skip forward (e.g., 5):", "Time Travel Debug", JOptionPane.WARNING_MESSAGE);
            if (input != null && !input.trim().isEmpty()) {
                try {
                    int days = Integer.parseInt(input.trim());
                    SystemClock.addDays(days); // Cộng thêm ngày
                    lblDate.setText("System Date: " + SystemClock.now()); // Cập nhật chữ trên màn hình
                    JOptionPane.showMessageDialog(panel, "Time skipped! Current date is now: " + SystemClock.now());
                    btnLoad.doClick(); // Tự động load lại danh sách để xem người ta bị phạt chưa!
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(panel, "Please enter a valid number!");
                }
            }
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