//////package org.example;
//////
//////import org.example.Data_access_object.BookDAO;
//////import org.example.Data_access_object.TransactionDAO;
//////import org.example.models.Book;
//////
//////import javax.swing.*;
//////import java.awt.*;
//////import java.util.List;
//////
//////public class MainDashboard {
//////    // Khởi tạo sẵn "động cơ" Database
//////    private BookDAO bookTool = new BookDAO();
//////    private TransactionDAO transactionTool = new TransactionDAO();
//////
//////    public MainDashboard() {
//////        // 1. Tạo Khung cửa sổ chính (To và chuyên nghiệp hơn)
//////        JFrame frame = new JFrame("Hệ Thống Thư Viện PRO - Dành Cho Tech Lead");
//////        frame.setSize(800, 600); // Mở rộng kích thước
//////        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//////        frame.setLocationRelativeTo(null); // Luôn hiện ra ở giữa màn hình
//////
//////        // 2. KHỞI TẠO THANH TAB (Bí quyết để dễ dàng mở rộng sau này)
//////        JTabbedPane tabbedPane = new JTabbedPane();
//////
//////        // --- TÍCH HỢP CÁC TÍNH NĂNG VÀO TỪNG TAB ---
//////
//////        // Tab 1: Quản lý kho sách
//////        tabbedPane.addTab("📚 Quản Lý Kho Sách", createBookPanel());
//////
//////        // Tab 2: Quản lý mượn trả
//////        tabbedPane.addTab("📝 Cấp Phiếu Mượn / Trả", createTransactionPanel());
//////
//////        // 💡 HƯỚNG DẪN DÀNH CHO TECH LEAD:
//////        // Sau này coder A làm xong phần Quản lý Người Dùng, bạn chỉ cần gỡ comment dòng dưới:
//////        // tabbedPane.addTab("👤 Quản Lý Độc Giả", createBorrowerPanel());
//////
//////        // Sau này coder B làm xong phần Thống kê Doanh thu Phạt, bạn thêm dòng này:
//////        // tabbedPane.addTab("📊 Thống Kê Báo Cáo", createReportPanel());
//////
//////        // Gắn thanh Tab vào cửa sổ chính
//////        frame.add(tabbedPane);
//////        frame.setVisible(true);
//////    }
//////
//////    // =========================================================================
//////    // KHU VỰC CHỨA GIAO DIỆN TỪNG TAB (Tách riêng ra cho code đỡ rối)
//////    // =========================================================================
//////
//////    // GIAO DIỆN TAB 1: KHO SÁCH
//////    private JPanel createBookPanel() {
//////        JPanel panel = new JPanel(null); // Dùng layout tự do
//////
//////        // Nút Tải dữ liệu
//////        JButton btnLoad = new JButton("🔄 Làm mới danh sách");
//////        btnLoad.setBounds(20, 20, 180, 35);
//////        panel.add(btnLoad);
//////
//////        // Nút Thêm sách (Tích hợp Popup nhập liệu)
//////        JButton btnAdd = new JButton("➕ Thêm sách mới");
//////        btnAdd.setBounds(210, 20, 150, 35);
//////        panel.add(btnAdd);
//////
//////        // Nút Xóa sách
//////        JButton btnDelete = new JButton("❌ Xóa sách");
//////        btnDelete.setBounds(370, 20, 150, 35);
//////        panel.add(btnDelete);
//////
//////        // Khung hiển thị danh sách sách (Có thanh cuộn JScrollPane)
//////        JTextArea displayArea = new JTextArea();
//////        displayArea.setEditable(false);
//////        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 14)); // Font chữ cho ngay ngắn
//////        JScrollPane scrollPane = new JScrollPane(displayArea);
//////        scrollPane.setBounds(20, 70, 740, 430);
//////        panel.add(scrollPane);
//////
//////        // --- GẮN SỰ KIỆN CHO CÁC NÚT BẤM (Nối Database vào UI) ---
//////
//////        // Bấm làm mới
//////        btnLoad.addActionListener(e -> {
//////            displayArea.setText("=== DANH SÁCH KHO SÁCH HIỆN TẠI ===\n\n");
//////            List<Book> books = bookTool.getAllBooks();
//////            for (Book b : books) {
//////                displayArea.append("ID: " + b.getId() + " | Tên: " + b.getTitle() + " | Còn: " + b.getAvailableQuantity() + " cuốn\n");
//////            }
//////        });
//////
//////        // Bấm thêm sách
//////        btnAdd.addActionListener(e -> {
//////            // Hiển thị khung popup hỏi tên sách
//////            String title = JOptionPane.showInputDialog(panel, "Nhập tên sách mới:");
//////            if (title != null && !title.trim().isEmpty()) {
//////                String qtyStr = JOptionPane.showInputDialog(panel, "Nhập số lượng nhập kho:");
//////                try {
//////                    int qty = Integer.parseInt(qtyStr);
//////                    bookTool.addBook(title, 1, 1, qty); // Tạm fix cứng author và category
//////                    JOptionPane.showMessageDialog(panel, "Đã thêm thành công!");
//////                    btnLoad.doClick(); // Tự động giả lập thao tác bấm nút "Làm mới" để cập nhật UI
//////                } catch (Exception ex) {
//////                    JOptionPane.showMessageDialog(panel, "⚠️ Số lượng phải là số nguyên!");
//////                }
//////            }
//////        });
//////
//////        // Bấm xóa sách
//////        btnDelete.addActionListener(e -> {
//////            String idStr = JOptionPane.showInputDialog(panel, "Nhập ID sách cần xóa:");
//////            try {
//////                int id = Integer.parseInt(idStr);
//////                bookTool.deleteBook(id);
//////                JOptionPane.showMessageDialog(panel, "Đã thực hiện lệnh xóa!");
//////                btnLoad.doClick(); // Tự động làm mới
//////            } catch (Exception ex) {
//////                JOptionPane.showMessageDialog(panel, "⚠️ ID không hợp lệ!");
//////            }
//////        });
//////
//////        return panel;
//////    }
//////
//////    // GIAO DIỆN TAB 2: MƯỢN TRẢ SÁCH (Dựng khung sẵn)
//////    private JPanel createTransactionPanel() {
//////        JPanel panel = new JPanel(null);
//////
//////        JLabel title = new JLabel("Chức năng Mượn/Trả sẽ được tích hợp tại đây.");
//////        title.setBounds(20, 20, 400, 30);
//////        panel.add(title);
//////
//////        JButton btnBorrow = new JButton("Tạo phiếu mượn");
//////        btnBorrow.setBounds(20, 60, 150, 35);
//////        panel.add(btnBorrow);
//////
//////        // Sự kiện mẫu gọi TransactionDAO
//////        btnBorrow.addActionListener(e -> {
//////            String borrowerId = JOptionPane.showInputDialog(panel, "Nhập ID Người mượn:");
//////            String bookId = JOptionPane.showInputDialog(panel, "Nhập ID Sách:");
//////            try {
//////                transactionTool.borrowBook(Integer.parseInt(borrowerId), Integer.parseInt(bookId));
//////                JOptionPane.showMessageDialog(panel, "✅ Đã tạo phiếu mượn tự động (Hạn trả: 14 ngày)!");
//////            } catch (Exception ex) {
//////                JOptionPane.showMessageDialog(panel, "⚠️ Nhập sai thông tin!");
//////            }
//////        });
//////
//////        return panel;
//////    }
//////}
////package org.example;
////
////import org.example.Data_access_object.BookDAO;
////import org.example.Data_access_object.TransactionDAO;
////import org.example.models.Book;
////
////import javax.swing.*;
////import java.awt.*;
////import java.util.List;
////
////public class MainDashboard {
////    private BookDAO bookTool = new BookDAO();
////    private TransactionDAO transactionTool = new TransactionDAO();
////
////    public MainDashboard() {
////        JFrame frame = new JFrame("Library Management System");
////        frame.setSize(800, 600);
////        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
////        frame.setLocationRelativeTo(null);
////
////        JTabbedPane tabbedPane = new JTabbedPane();
////        tabbedPane.addTab("📚 Quản Lý Kho Sách", createBookPanel());
////        tabbedPane.addTab("📝 Mượn Trả & Phạt", createTransactionPanel());
////
////        frame.add(tabbedPane);
////        frame.setVisible(true);
////    }
////
////// =========================================================
////    // TAB 1: KHO SÁCH (Đã nâng cấp lên JTable Click Chuột)
////    // =========================================================
////    private JPanel createBookPanel() {
////        JPanel panel = new JPanel(null);
////
////        JButton btnLoad = new JButton("1. Tải danh sách");
////        btnLoad.setBounds(20, 20, 150, 35);
////        panel.add(btnLoad);
////
////        JButton btnAdd = new JButton("2. Thêm sách");
////        btnAdd.setBounds(180, 20, 150, 35);
////        panel.add(btnAdd);
////
////        JButton btnDelete = new JButton("3. Xóa sách (Click chọn)");
////        btnDelete.setBounds(340, 20, 180, 35);
////        panel.add(btnDelete);
////
////        // --- NÂNG CẤP VŨ KHÍ: JTABLE ---
////        // 1. Tạo "khuôn" cho bảng (Tên các cột)
////        String[] columnNames = {"ID Sách", "Tên Cuốn Sách", "Số Lượng Còn"};
////
////        // 2. Tạo mô hình dữ liệu (Cho phép sửa đổi, thêm bớt dòng)
////        javax.swing.table.DefaultTableModel tableModel = new javax.swing.table.DefaultTableModel(columnNames, 0);
////
////        // 3. Tạo Bảng và nạp mô hình vào
////        JTable table = new JTable(tableModel);
////        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Chỉ cho phép click chọn 1 dòng mỗi lần
////        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
////        table.setRowHeight(25); // Cho dòng cao lên dễ click
////
////        // 4. Bỏ bảng vào thanh cuộn
////        JScrollPane scrollPane = new JScrollPane(table);
////        scrollPane.setBounds(20, 70, 740, 430);
////        panel.add(scrollPane);
////
////        // --- GẮN SỰ KIỆN TƯƠNG TÁC ---
////
////        // Bấm Tải Danh Sách: Lấy từ MySQL đổ vào Bảng
////        btnLoad.addActionListener(e -> {
////            tableModel.setRowCount(0); // Xóa sạch dữ liệu cũ trên bảng
////            List<Book> books = bookTool.getAllBooks();
////            for (Book b : books) {
////                // Thêm từng dòng vào bảng (Không dùng String nữa mà dùng Object)
////                tableModel.addRow(new Object[]{b.getId(), b.getTitle(), b.getAvailableQuantity()});
////            }
////        });
////
////        btnAdd.addActionListener(e -> {
////            String title = JOptionPane.showInputDialog(panel, "Nhập tên sách:");
////            if (title != null && !title.isEmpty()) {
////                String qtyStr = JOptionPane.showInputDialog(panel, "Số lượng:");
////                try {
////                    bookTool.addBook(title, 1, 1, Integer.parseInt(qtyStr));
////                    btnLoad.doClick(); // Tự động load lại
////                } catch (Exception ex) {
////                    JOptionPane.showMessageDialog(panel, "Số lượng phải là số!");
////                }
////            }
////        });
////
////        // ĐỈNH CAO UX: Xóa sách bằng cách click chuột
////        btnDelete.addActionListener(e -> {
////            // Lấy ra vị trí dòng mà chuột đang click
////            int selectedRow = table.getSelectedRow();
////
////            if (selectedRow == -1) { // -1 nghĩa là chưa click chọn dòng nào
////                JOptionPane.showMessageDialog(panel, "⚠️ Vui lòng click chuột chọn một cuốn sách trong bảng trước!");
////                return; // Dừng lại không làm tiếp
////            }
////
////            // Lấy ID (Cột số 0) và Tên sách (Cột số 1) từ dòng đã click
////            int bookId = (int) table.getValueAt(selectedRow, 0);
////            String bookName = (String) table.getValueAt(selectedRow, 1);
////
////            // Xác nhận lần cuối
////            int confirm = JOptionPane.showConfirmDialog(panel, "Bạn có chắc chắn muốn xóa sách: " + bookName + "?", "Xác nhận Xóa", JOptionPane.YES_NO_OPTION);
////
////            if (confirm == JOptionPane.YES_OPTION) {
////                bookTool.deleteBook(bookId); // Gọi MySQL xóa
////                btnLoad.doClick(); // Làm mới bảng
////            }
////        });
////
////        return panel;
////    }
////    // =========================================================
////    // TAB 2: MƯỢN TRẢ SÁCH (MƯỢN, XEM PHẠT, TRẢ)
////    // =========================================================
////    private JPanel createTransactionPanel() {
////        JPanel panel = new JPanel(null);
////
////        JButton btnLoad = new JButton("5. Xem nợ & Phạt");
////        btnLoad.setBounds(20, 20, 160, 35);
////        panel.add(btnLoad);
////
////        JButton btnBorrow = new JButton("4. Cho mượn sách");
////        btnBorrow.setBounds(190, 20, 160, 35);
////        panel.add(btnBorrow);
////
////        JButton btnReturn = new JButton("6. Nhận trả sách");
////        btnReturn.setBounds(360, 20, 160, 35);
////        panel.add(btnReturn);
////
////        JTextArea displayArea = new JTextArea();
////        displayArea.setEditable(false);
////        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
////        JScrollPane scrollPane = new JScrollPane(displayArea);
////        scrollPane.setBounds(20, 70, 740, 430);
////        panel.add(scrollPane);
////
////        // --- Gắn Sự Kiện ---
////        btnLoad.addActionListener(e -> {
////            displayArea.setText("=== DANH SÁCH MƯỢN SÁCH & TIỀN PHẠT ===\n\n");
////            displayArea.append(transactionTool.getBorrowerListForUI());
////        });
////
////        btnBorrow.addActionListener(e -> {
////            // Mở cái Popup siêu xịn bạn vừa tạo lên
////            BorrowPopup popup = new BorrowPopup((JFrame) SwingUtilities.getWindowAncestor(panel));
////            popup.setVisible(true);
////
////            // Sau khi tắt popup (chốt mượn xong), tự động bấm nút Load để cập nhật danh sách
////            btnLoad.doClick();
////        });
////
////
////        btnReturn.addActionListener(e -> {
////            String borrowerId = JOptionPane.showInputDialog(panel, "ID Người trả:");
////            String bookId = JOptionPane.showInputDialog(panel, "ID Sách:");
////            if (borrowerId != null && bookId != null) {
////                transactionTool.returnBook(Integer.parseInt(borrowerId), Integer.parseInt(bookId));
////                JOptionPane.showMessageDialog(panel, "✅ Đã nhận sách trả!");
////                btnLoad.doClick();
////            }
////        });
////
////        return panel;
////    }
////}
////package org.example;
////
////import org.example.Data_access_object.BookDAO;
////import org.example.Data_access_object.TransactionDAO;
////import org.example.models.Book;
////
////import javax.swing.*;
////import java.awt.*;
////import java.util.List;
////
////public class MainDashboard {
////    private BookDAO bookTool = new BookDAO();
////    private TransactionDAO transactionTool = new TransactionDAO();
////
////    public MainDashboard() {
////        JFrame frame = new JFrame("Library Management System - Tech Lead Edition");
////        frame.setSize(800, 600);
////        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
////        frame.setLocationRelativeTo(null);
////
////        JTabbedPane tabbedPane = new JTabbedPane();
////        tabbedPane.addTab("📚 Book Inventory", createBookPanel());
////        tabbedPane.addTab("📝 Borrow & Return", createTransactionPanel());
////
////        frame.add(tabbedPane);
////        frame.setVisible(true);
////    }
////
////    // =========================================================
////    // TAB 1: BOOK INVENTORY (View, Add, Delete with JTable)
////    // =========================================================
////    private JPanel createBookPanel() {
////        JPanel panel = new JPanel(null);
////
////        JButton btnLoad = new JButton("1. Load Books");
////        btnLoad.setBounds(20, 20, 150, 35);
////        panel.add(btnLoad);
////
////        JButton btnAdd = new JButton("2. Add Book");
////        btnAdd.setBounds(180, 20, 150, 35);
////        panel.add(btnAdd);
////
////        JButton btnDelete = new JButton("3. Delete Book");
////        btnDelete.setBounds(340, 20, 150, 35);
////        panel.add(btnDelete);
////
////        // --- JTABLE SETUP ---
////        String[] columnNames = {"Book ID", "Title", "Available Qty"};
////        javax.swing.table.DefaultTableModel tableModel = new javax.swing.table.DefaultTableModel(columnNames, 0);
////
////        JTable table = new JTable(tableModel);
////        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
////        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
////        table.setRowHeight(25);
////
////        JScrollPane scrollPane = new JScrollPane(table);
////        scrollPane.setBounds(20, 70, 740, 430);
////        panel.add(scrollPane);
////
////        // --- EVENTS ---
////
////        // Nút Load sách
////        btnLoad.addActionListener(e -> {
////            tableModel.setRowCount(0);
////            List<Book> books = bookTool.getAllBooks();
////            for (Book b : books) {
////                tableModel.addRow(new Object[]{b.getId(), b.getTitle(), b.getAvailableQuantity()});
////            }
////        });
////
////        // 🌟 NÚT ADD BOOK (Gom 1 Form duy nhất)
////        btnAdd.addActionListener(e -> {
////            // Tạo 2 ô nhập liệu
////            JTextField txtTitle = new JTextField();
////            JTextField txtQuantity = new JTextField();
////
////            // Gom vào 1 khối
////            Object[] formFields = {
////                    "Book Title (*):", txtTitle,
////                    "Quantity (*):", txtQuantity
////            };
////
////            // Bật 1 Popup duy nhất
////            int option = JOptionPane.showConfirmDialog(panel, formFields, "📚 Add New Book", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
////
////            // Xử lý khi bấm OK
////            if (option == JOptionPane.OK_OPTION) {
////                String title = txtTitle.getText().trim();
////                String qtyStr = txtQuantity.getText().trim();
////
////                // Kiểm tra xem có để trống không
////                if (title.isEmpty() || qtyStr.isEmpty()) {
////                    JOptionPane.showMessageDialog(panel, "❌ Please fill in all fields!");
////                } else {
////                    try {
////                        int qty = Integer.parseInt(qtyStr);
////                        // Tạm thời fix cứng author_id = 1 và category_id = 1 để hệ thống chạy mượt
////                        bookTool.addBook(title, 1, 1, qty);
////                        JOptionPane.showMessageDialog(panel, "✅ Book added successfully!");
////                        btnLoad.doClick(); // Tự động load lại bảng
////                    } catch (NumberFormatException ex) {
////                        JOptionPane.showMessageDialog(panel, "⚠️ Quantity must be a valid number!");
////                    }
////                }
////            }
////        });
////
////        // Nút Xóa sách (Click chuột)
////        btnDelete.addActionListener(e -> {
////            int selectedRow = table.getSelectedRow();
////
////            if (selectedRow == -1) {
////                JOptionPane.showMessageDialog(panel, "⚠️ Please select a book from the table first!");
////                return;
////            }
////
////            int bookId = (int) table.getValueAt(selectedRow, 0);
////            String bookName = (String) table.getValueAt(selectedRow, 1);
////
////            int confirm = JOptionPane.showConfirmDialog(panel, "Are you sure you want to delete: " + bookName + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
////
////            if (confirm == JOptionPane.YES_OPTION) {
////                bookTool.deleteBook(bookId);
////                btnLoad.doClick();
////            }
////        });
////
////        return panel;
////    }
////
////    // =========================================================
////    // TAB 2: TRANSACTIONS (Borrow, Return, View Fines)
////    // =========================================================
////    private JPanel createTransactionPanel() {
////        JPanel panel = new JPanel(null);
////
////        JButton btnLoad = new JButton("5. View Fines & Dues");
////        btnLoad.setBounds(20, 20, 160, 35);
////        panel.add(btnLoad);
////
////        JButton btnBorrow = new JButton("4. Borrow Book");
////        btnBorrow.setBounds(190, 20, 160, 35);
////        panel.add(btnBorrow);
////
////        JButton btnReturn = new JButton("6. Return Book");
////        btnReturn.setBounds(360, 20, 160, 35);
////        panel.add(btnReturn);
////
////        JTextArea displayArea = new JTextArea();
////        displayArea.setEditable(false);
////        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
////        JScrollPane scrollPane = new JScrollPane(displayArea);
////        scrollPane.setBounds(20, 70, 740, 430);
////        panel.add(scrollPane);
////
////        // --- EVENTS ---
////        btnLoad.addActionListener(e -> {
////            displayArea.setText("=== BORROWED BOOKS & FINES LIST ===\n\n");
////            displayArea.append(transactionTool.getBorrowerListForUI());
////        });
////
////        btnBorrow.addActionListener(e -> {
////            BorrowPopup popup = new BorrowPopup((JFrame) SwingUtilities.getWindowAncestor(panel));
////            popup.setVisible(true);
////            btnLoad.doClick();
////        });
////
////        btnReturn.addActionListener(e -> {
////            String borrowerId = JOptionPane.showInputDialog(panel, "Enter Borrower ID:");
////            String bookId = JOptionPane.showInputDialog(panel, "Enter Book ID:");
////            if (borrowerId != null && bookId != null) {
////                try {
////                    transactionTool.returnBook(Integer.parseInt(borrowerId), Integer.parseInt(bookId));
////                    JOptionPane.showMessageDialog(panel, "✅ Book returned successfully!");
////                    btnLoad.doClick();
////                } catch (Exception ex) {
////                    JOptionPane.showMessageDialog(panel, "⚠️ IDs must be valid numbers!");
////                }
////            }
////        });
////
////        return panel;
////    }
////}
//
//package org.example;
//
//import org.example.Data_access_object.BookDAO;
//import org.example.Data_access_object.TransactionDAO;
//import org.example.models.Book;
//
//import javax.swing.*;
//import java.awt.*;
//import java.util.List;
//
//public class MainDashboard {
//    private BookDAO bookTool = new BookDAO();
//    private TransactionDAO transactionTool = new TransactionDAO();
//
//    public MainDashboard() {
//        JFrame frame = new JFrame("Library Management System - Tech Lead Edition");
//        frame.setSize(800, 600);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setLocationRelativeTo(null);
//
//        JTabbedPane tabbedPane = new JTabbedPane();
//        tabbedPane.addTab("📚 Book Inventory", createBookPanel());
//        tabbedPane.addTab("📝 Borrow & Return", createTransactionPanel());
//
//        frame.add(tabbedPane);
//        frame.setVisible(true);
//    }
//
//    private JPanel createBookPanel() {
//        JPanel panel = new JPanel(null);
//
//        JButton btnLoad = new JButton("1. Load Books");
//        btnLoad.setBounds(20, 20, 150, 35);
//        panel.add(btnLoad);
//
//        JButton btnAdd = new JButton("2. Add Book");
//        btnAdd.setBounds(180, 20, 150, 35);
//        panel.add(btnAdd);
//
//        JButton btnDelete = new JButton("3. Delete Book");
//        btnDelete.setBounds(340, 20, 150, 35);
//        panel.add(btnDelete);
//
//        String[] columnNames = {"Book ID", "Title", "Available Qty"};
//        javax.swing.table.DefaultTableModel tableModel = new javax.swing.table.DefaultTableModel(columnNames, 0);
//
//        JTable table = new JTable(tableModel);
//        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
//        table.setRowHeight(25);
//
//        JScrollPane scrollPane = new JScrollPane(table);
//        scrollPane.setBounds(20, 70, 740, 430);
//        panel.add(scrollPane);
//
//        // --- EVENTS ---
//        btnLoad.addActionListener(e -> {
//            tableModel.setRowCount(0);
//            List<Book> books = bookTool.getAllBooks();
//            for (Book b : books) {
//                tableModel.addRow(new Object[]{b.getId(), b.getTitle(), b.getAvailableQuantity()});
//            }
//        });
//
//        // 🌟 NÚT ADD BOOK ĐÃ ĐƯỢC ĐỒNG BỘ VỚI BookDAO MỚI
//        btnAdd.addActionListener(e -> {
//            JTextField txtTitle = new JTextField();
//            JTextField txtAuthor = new JTextField(); // Nhập thẳng tên tác giả
//            JTextField txtQuantity = new JTextField();
//
//            // Menu xổ xuống chọn Thể loại (Lấy thẳng ID đầu tiên)
//            String[] categoryOptions = {
//                    "1 - Fiction",
//                    "2 - Self-Help",
//                    "3 - Programming",
//                    "4 - Astrophysics",
//                    "5 - Light Novel"
//            };
//            JComboBox<String> cbCategory = new JComboBox<>(categoryOptions);
//
//            Object[] formFields = {
//                    "Book Title (*):", txtTitle,
//                    "Author Name (*):", txtAuthor,
//                    "Category:", cbCategory,
//                    "Quantity (*):", txtQuantity
//            };
//
//            int option = JOptionPane.showConfirmDialog(panel, formFields, "📚 Add New Book", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
//
//            if (option == JOptionPane.OK_OPTION) {
//                String title = txtTitle.getText().trim();
//                String authorName = txtAuthor.getText().trim();
//                String qtyStr = txtQuantity.getText().trim();
//
//                if (title.isEmpty() || authorName.isEmpty() || qtyStr.isEmpty()) {
//                    JOptionPane.showMessageDialog(panel, "❌ Please fill in all (*) fields!");
//                } else {
//                    try {
//                        int qty = Integer.parseInt(qtyStr);
//                        String selectedCategory = (String) cbCategory.getSelectedItem();
//                        int categoryId = Integer.parseInt(selectedCategory.split(" - ")[0]); // Cắt lấy số đầu tiên làm ID
//
//                        // Truyền 4 thông số xuống cỗ máy BookDAO
//                        bookTool.addBook(title, authorName, categoryId, qty);
//
//                        JOptionPane.showMessageDialog(panel, "✅ Book added successfully!");
//                        btnLoad.doClick();
//                    } catch (NumberFormatException ex) {
//                        JOptionPane.showMessageDialog(panel, "⚠️ Quantity must be a valid number!");
//                    }
//                }
//            }
//        });
//
//        btnDelete.addActionListener(e -> {
//            int selectedRow = table.getSelectedRow();
//            if (selectedRow == -1) {
//                JOptionPane.showMessageDialog(panel, "⚠️ Please select a book from the table first!");
//                return;
//            }
//            int bookId = (int) table.getValueAt(selectedRow, 0);
//            String bookName = (String) table.getValueAt(selectedRow, 1);
//
//            int confirm = JOptionPane.showConfirmDialog(panel, "Are you sure you want to delete: " + bookName + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
//            if (confirm == JOptionPane.YES_OPTION) {
//                bookTool.deleteBook(bookId);
//                btnLoad.doClick();
//            }
//        });
//
//        return panel;
//    }
//
//    private JPanel createTransactionPanel() {
//        JPanel panel = new JPanel(null);
//
//        JButton btnLoad = new JButton("5. View Fines & Dues");
//        btnLoad.setBounds(20, 20, 160, 35);
//        panel.add(btnLoad);
//
//        JButton btnBorrow = new JButton("4. Borrow Book");
//        btnBorrow.setBounds(190, 20, 160, 35);
//        panel.add(btnBorrow);
//
//        JButton btnReturn = new JButton("6. Return Book");
//        btnReturn.setBounds(360, 20, 160, 35);
//        panel.add(btnReturn);
//
//        JTextArea displayArea = new JTextArea();
//        displayArea.setEditable(false);
//        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
//        JScrollPane scrollPane = new JScrollPane(displayArea);
//        scrollPane.setBounds(20, 70, 740, 430);
//        panel.add(scrollPane);
//
//        btnLoad.addActionListener(e -> {
//            displayArea.setText("=== BORROWED BOOKS & FINES LIST ===\n\n");
//            displayArea.append(transactionTool.getBorrowerListForUI());
//        });
//
//        btnBorrow.addActionListener(e -> {
//            BorrowPopup popup = new BorrowPopup((JFrame) SwingUtilities.getWindowAncestor(panel));
//            popup.setVisible(true);
//            btnLoad.doClick();
//        });
//
//        btnReturn.addActionListener(e -> {
//            String borrowerId = JOptionPane.showInputDialog(panel, "Enter Borrower ID:");
//            String bookId = JOptionPane.showInputDialog(panel, "Enter Book ID:");
//            if (borrowerId != null && bookId != null) {
//                try {
//                    transactionTool.returnBook(Integer.parseInt(borrowerId), Integer.parseInt(bookId));
//                    JOptionPane.showMessageDialog(panel, "✅ Book returned successfully!");
//                    btnLoad.doClick();
//                } catch (Exception ex) {
//                    JOptionPane.showMessageDialog(panel, "⚠️ IDs must be valid numbers!");
//                }
//            }
//        });
//
//        return panel;
//    }
//}


//package org.example;
//
//import org.example.Data_access_object.BookDAO;
//import org.example.Data_access_object.TransactionDAO;
//import org.example.models.Book;
//
//import javax.swing.*;
//import java.awt.*;
//import java.util.List;
//
//public class MainDashboard {
//    private BookDAO bookTool = new BookDAO();
//    private TransactionDAO transactionTool = new TransactionDAO();
//
//    public MainDashboard() {
//        JFrame frame = new JFrame("Library Management System");
//        frame.setSize(900, 650); // Mở rộng màn hình ra một chút cho thoáng
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setLocationRelativeTo(null);
//
//        JTabbedPane tabbedPane = new JTabbedPane();
//        tabbedPane.addTab("📚 Book Inventory", createBookPanel());
//        tabbedPane.addTab("📝 Borrow & Return", createTransactionPanel());
//
//        frame.add(tabbedPane);
//        frame.setVisible(true);
//    }
//
//    private JPanel createBookPanel() {
//        JPanel panel = new JPanel(null);
//
//        // Nút Load
//        JButton btnLoad = new JButton("1. Load Books");
//        btnLoad.setBounds(20, 20, 120, 35);
//        panel.add(btnLoad);
//
//        // --- MỚI: TÍNH NĂNG FILTER THEO THỂ LOẠI ---
//        JLabel lblFilter = new JLabel("Filter:");
//        lblFilter.setBounds(150, 20, 50, 35);
//        lblFilter.setFont(new Font("SansSerif", Font.BOLD, 14));
//        panel.add(lblFilter);
//
//        String[] filterOptions = {"All", "Fiction", "Self-Help", "Programming", "Astrophysics", "Light Novel"};
//        JComboBox<String> cbFilter = new JComboBox<>(filterOptions);
//        cbFilter.setBounds(200, 20, 130, 35);
//        panel.add(cbFilter);
//
//        JButton btnAdd = new JButton("2. Add Book");
//        btnAdd.setBounds(340, 20, 130, 35);
//        panel.add(btnAdd);
//
//        JButton btnDelete = new JButton("3. Delete Book");
//        btnDelete.setBounds(480, 20, 130, 35);
//        panel.add(btnDelete);
//
//        // --- BẢNG ĐÃ NÂNG CẤP LÊN 6 CỘT CHUYÊN NGHIỆP ---
//        String[] columnNames = {"Book ID", "Title", "Author", "Category", "Total Qty", "Available Qty"};
//        javax.swing.table.DefaultTableModel tableModel = new javax.swing.table.DefaultTableModel(columnNames, 0);
//
//        JTable table = new JTable(tableModel);
//        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
//        table.setRowHeight(25);
//        // Chỉnh độ rộng cột Title cho nó rộng ra dễ đọc
//        table.getColumnModel().getColumn(1).setPreferredWidth(250);
//
//        JScrollPane scrollPane = new JScrollPane(table);
//        scrollPane.setBounds(20, 70, 840, 480);
//        panel.add(scrollPane);
//
//        // --- SỰ KIỆN NÚT BẤM ---
//
//        // Sự kiện: Khi chọn Filter thả xuống, nó tự động lọc luôn!
//        cbFilter.addActionListener(e -> {
//            String selectedCategory = (String) cbFilter.getSelectedItem();
//            tableModel.setRowCount(0); // Xóa bảng cũ
//            // Truyền tên Thể loại xuống DB để lấy đúng sách
//            List<Book> books = bookTool.getBooks(selectedCategory);
//            for (Book b : books) {
//                // Đổ đủ 6 thông tin ra bảng
//                tableModel.addRow(new Object[]{
//                        b.getId(), b.getTitle(), b.getAuthorName(), b.getCategoryName(), b.getTotalQuantity(), b.getAvailableQuantity()
//                });
//            }
//        });
//
//        // Bấm Load là lấy All
//        btnLoad.addActionListener(e -> {
//            cbFilter.setSelectedIndex(0); // Chọn lại mức "All"
//        });
//
//        // Sự kiện Add Book (Giữ nguyên)
//        btnAdd.addActionListener(e -> {
//            JTextField txtTitle = new JTextField();
//            JTextField txtAuthor = new JTextField();
//            JTextField txtQuantity = new JTextField();
//
//            String[] categoryOptions = {"1 - Fiction", "2 - Self-Help", "3 - Programming", "4 - Astrophysics", "5 - Light Novel"};
//            JComboBox<String> cbCategory = new JComboBox<>(categoryOptions);
//
//            Object[] formFields = {
//                    "Book Title (*):", txtTitle,
//                    "Author Name (*):", txtAuthor,
//                    "Category:", cbCategory,
//                    "Quantity (*):", txtQuantity
//            };
//
//            int option = JOptionPane.showConfirmDialog(panel, formFields, "📚 Add New Book", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
//
//            if (option == JOptionPane.OK_OPTION) {
//                String title = txtTitle.getText().trim();
//                String authorName = txtAuthor.getText().trim();
//                String qtyStr = txtQuantity.getText().trim();
//
//                if (title.isEmpty() || authorName.isEmpty() || qtyStr.isEmpty()) {
//                    JOptionPane.showMessageDialog(panel, "❌ Please fill in all (*) fields!");
//                } else {
//                    try {
//                        int qty = Integer.parseInt(qtyStr);
//                        String selectedCategory = (String) cbCategory.getSelectedItem();
//                        int categoryId = Integer.parseInt(selectedCategory.split(" - ")[0]);
//
//                        bookTool.addBook(title, authorName, categoryId, qty);
//                        JOptionPane.showMessageDialog(panel, "✅ Book added successfully!");
//                        btnLoad.doClick();
//                    } catch (NumberFormatException ex) {
//                        JOptionPane.showMessageDialog(panel, "⚠️ Quantity must be a valid number!");
//                    }
//                }
//            }
//        });
//
//        btnDelete.addActionListener(e -> {
//            int selectedRow = table.getSelectedRow();
//            if (selectedRow == -1) {
//                JOptionPane.showMessageDialog(panel, "⚠️ Please select a book from the table first!");
//                return;
//            }
//            int bookId = (int) table.getValueAt(selectedRow, 0);
//            String bookName = (String) table.getValueAt(selectedRow, 1);
//
//            int confirm = JOptionPane.showConfirmDialog(panel, "Are you sure you want to delete: " + bookName + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
//            if (confirm == JOptionPane.YES_OPTION) {
//                bookTool.deleteBook(bookId);
//                btnLoad.doClick();
//            }
//        });
//
//        // Chạy nút Load 1 lần đầu tiên để khi mở app lên là có data ngay
//        SwingUtilities.invokeLater(() -> btnLoad.doClick());
//
//        return panel;
//    }
//
//    private JPanel createTransactionPanel() {
//        JPanel panel = new JPanel(null);
//
//        JButton btnLoad = new JButton("5. View Fines & Dues");
//        btnLoad.setBounds(20, 20, 160, 35);
//        panel.add(btnLoad);
//
//        JButton btnBorrow = new JButton("4. Borrow Book");
//        btnBorrow.setBounds(190, 20, 160, 35);
//        panel.add(btnBorrow);
//
//        JButton btnReturn = new JButton("6. Return Book");
//        btnReturn.setBounds(360, 20, 160, 35);
//        panel.add(btnReturn);
//
//        JTextArea displayArea = new JTextArea();
//        displayArea.setEditable(false);
//        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
//        JScrollPane scrollPane = new JScrollPane(displayArea);
//        scrollPane.setBounds(20, 70, 840, 480);
//        panel.add(scrollPane);
//
//        btnLoad.addActionListener(e -> {
//            displayArea.setText("=== BORROWED BOOKS & FINES LIST ===\n\n");
//            displayArea.append(transactionTool.getBorrowerListForUI());
//        });
//
//        btnBorrow.addActionListener(e -> {
//            BorrowPopup popup = new BorrowPopup((JFrame) SwingUtilities.getWindowAncestor(panel));
//            popup.setVisible(true);
//            btnLoad.doClick();
//        });
//
//        btnReturn.addActionListener(e -> {
//            String borrowerId = JOptionPane.showInputDialog(panel, "Enter Borrower ID:");
//            String bookId = JOptionPane.showInputDialog(panel, "Enter Book ID:");
//            if (borrowerId != null && bookId != null) {
//                try {
//                    transactionTool.returnBook(Integer.parseInt(borrowerId), Integer.parseInt(bookId));
//                    JOptionPane.showMessageDialog(panel, "✅ Book returned successfully!");
//                    btnLoad.doClick();
//                } catch (Exception ex) {
//                    JOptionPane.showMessageDialog(panel, "⚠️ IDs must be valid numbers!");
//                }
//            }
//        });
//
//        return panel;
//    }
//}

//package org.example;
//
//import org.example.Data_access_object.BookDAO;
//import org.example.Data_access_object.TransactionDAO;
//import org.example.models.Book;
//
//import javax.swing.*;
//import java.awt.*;
//import java.util.List;
//
//public class MainDashboard {
//    private BookDAO bookTool = new BookDAO();
//    private TransactionDAO transactionTool = new TransactionDAO();
//
//    // 🌟 Biến toàn cục để Tab 2 (Borrow) có thể "bấm ké" nút Load của Tab 1 (Inventory)
//    private JButton btnLoadBooksTab1;
//
//    public MainDashboard() {
//        JFrame frame = new JFrame("Library Management System");
//        frame.setSize(900, 650); // Mở rộng màn hình ra một chút cho thoáng
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setLocationRelativeTo(null);
//
//        JTabbedPane tabbedPane = new JTabbedPane();
//        tabbedPane.addTab("📚 Book Inventory", createBookPanel());
//        tabbedPane.addTab("📝 Borrow & Return", createTransactionPanel());
//
//        frame.add(tabbedPane);
//        frame.setVisible(true);
//    }
//
//    private JPanel createBookPanel() {
//        JPanel panel = new JPanel(null);
//
//        // Nút Load (Đã gắn vào biến toàn cục)
//        btnLoadBooksTab1 = new JButton("1. Load Books");
//        btnLoadBooksTab1.setBounds(20, 20, 120, 35);
//        panel.add(btnLoadBooksTab1);
//
//        // --- MỚI: TÍNH NĂNG FILTER THEO THỂ LOẠI ---
//        JLabel lblFilter = new JLabel("Filter:");
//        lblFilter.setBounds(150, 20, 50, 35);
//        lblFilter.setFont(new Font("SansSerif", Font.BOLD, 14));
//        panel.add(lblFilter);
//
//        String[] filterOptions = {"All", "Fiction", "Self-Help", "Programming", "Astrophysics", "Light Novel"};
//        JComboBox<String> cbFilter = new JComboBox<>(filterOptions);
//        cbFilter.setBounds(200, 20, 130, 35);
//        panel.add(cbFilter);
//
//        JButton btnAdd = new JButton("2. Add Book");
//        btnAdd.setBounds(340, 20, 130, 35);
//        panel.add(btnAdd);
//
//        JButton btnDelete = new JButton("3. Delete Book");
//        btnDelete.setBounds(480, 20, 130, 35);
//        panel.add(btnDelete);
//
//        // --- BẢNG ĐÃ NÂNG CẤP LÊN 6 CỘT CHUYÊN NGHIỆP ---
//        String[] columnNames = {"Book ID", "Title", "Author", "Category", "Total Qty", "Available Qty"};
//        javax.swing.table.DefaultTableModel tableModel = new javax.swing.table.DefaultTableModel(columnNames, 0);
//
//        JTable table = new JTable(tableModel);
//        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
//        table.setRowHeight(25);
//        // Chỉnh độ rộng cột Title cho nó rộng ra dễ đọc
//        table.getColumnModel().getColumn(1).setPreferredWidth(250);
//
//        JScrollPane scrollPane = new JScrollPane(table);
//        scrollPane.setBounds(20, 70, 840, 480);
//        panel.add(scrollPane);
//
//        // --- SỰ KIỆN NÚT BẤM ---
//        cbFilter.addActionListener(e -> {
//            String selectedCategory = (String) cbFilter.getSelectedItem();
//            tableModel.setRowCount(0); // Xóa bảng cũ
//            List<Book> books = bookTool.getBooks(selectedCategory);
//            for (Book b : books) {
//                tableModel.addRow(new Object[]{
//                        b.getId(), b.getTitle(), b.getAuthorName(), b.getCategoryName(), b.getTotalQuantity(), b.getAvailableQuantity()
//                });
//            }
//        });
//
//        btnLoadBooksTab1.addActionListener(e -> {
//            cbFilter.setSelectedIndex(0);
//        });
//
//        btnAdd.addActionListener(e -> {
//            JTextField txtTitle = new JTextField();
//            JTextField txtAuthor = new JTextField();
//            JTextField txtQuantity = new JTextField();
//
//            String[] categoryOptions = {"1 - Fiction", "2 - Self-Help", "3 - Programming", "4 - Astrophysics", "5 - Light Novel"};
//            JComboBox<String> cbCategory = new JComboBox<>(categoryOptions);
//
//            Object[] formFields = {
//                    "Book Title (*):", txtTitle,
//                    "Author Name (*):", txtAuthor,
//                    "Category:", cbCategory,
//                    "Quantity (*):", txtQuantity
//            };
//
//            int option = JOptionPane.showConfirmDialog(panel, formFields, "📚 Add New Book", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
//
//            if (option == JOptionPane.OK_OPTION) {
//                String title = txtTitle.getText().trim();
//                String authorName = txtAuthor.getText().trim();
//                String qtyStr = txtQuantity.getText().trim();
//
//                if (title.isEmpty() || authorName.isEmpty() || qtyStr.isEmpty()) {
//                    JOptionPane.showMessageDialog(panel, "❌ Please fill in all (*) fields!");
//                } else {
//                    try {
//                        int qty = Integer.parseInt(qtyStr);
//                        String selectedCategory = (String) cbCategory.getSelectedItem();
//                        int categoryId = Integer.parseInt(selectedCategory.split(" - ")[0]);
//
//                        bookTool.addBook(title, authorName, categoryId, qty);
//                        JOptionPane.showMessageDialog(panel, "✅ Book added successfully!");
//                        btnLoadBooksTab1.doClick();
//                    } catch (NumberFormatException ex) {
//                        JOptionPane.showMessageDialog(panel, "⚠️ Quantity must be a valid number!");
//                    }
//                }
//            }
//        });
//
//        // 🌟 SỰ KIỆN DELETE MỚI (Có khiên bảo vệ chống lỗi Bóng Ma)
//        btnDelete.addActionListener(e -> {
//            int selectedRow = table.getSelectedRow();
//            if (selectedRow == -1) {
//                JOptionPane.showMessageDialog(panel, "⚠️ Please select a book from the table first!");
//                return;
//            }
//            int bookId = (int) table.getValueAt(selectedRow, 0);
//            String bookName = (String) table.getValueAt(selectedRow, 1);
//
//            int confirm = JOptionPane.showConfirmDialog(panel, "Are you sure you want to delete: " + bookName + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
//            if (confirm == JOptionPane.YES_OPTION) {
//                boolean success = bookTool.deleteBook(bookId);
//
//                if (success) {
//                    JOptionPane.showMessageDialog(panel, "✅ Deleted successfully!");
//                    btnLoadBooksTab1.doClick();
//                } else {
//                    JOptionPane.showMessageDialog(panel, "❌ ERROR: Cannot delete! This book is currently borrowed by someone.", "Delete Blocked", JOptionPane.ERROR_MESSAGE);
//                }
//            }
//        });
//
//        SwingUtilities.invokeLater(() -> btnLoadBooksTab1.doClick());
//
//        return panel;
//    }
//
//    private JPanel createTransactionPanel() {
//        JPanel panel = new JPanel(null);
//
//        JButton btnLoad = new JButton("5. View Fines & Dues");
//        btnLoad.setBounds(20, 20, 160, 35);
//        panel.add(btnLoad);
//
//        JButton btnBorrow = new JButton("4. Borrow Book");
//        btnBorrow.setBounds(190, 20, 160, 35);
//        panel.add(btnBorrow);
//
//        JButton btnReturn = new JButton("6. Return Book");
//        btnReturn.setBounds(360, 20, 160, 35);
//        panel.add(btnReturn);
//
//        JTextArea displayArea = new JTextArea();
//        displayArea.setEditable(false);
//        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
//        JScrollPane scrollPane = new JScrollPane(displayArea);
//        scrollPane.setBounds(20, 70, 840, 480);
//        panel.add(scrollPane);
//
//        btnLoad.addActionListener(e -> {
//            displayArea.setText("=== BORROWED BOOKS & FINES LIST ===\n\n");
//            displayArea.append(transactionTool.getBorrowerListForUI());
//        });
//
//        btnBorrow.addActionListener(e -> {
//            BorrowPopup popup = new BorrowPopup((JFrame) SwingUtilities.getWindowAncestor(panel));
//            popup.setVisible(true);
//
//            btnLoad.doClick();
//            if (btnLoadBooksTab1 != null) {
//                btnLoadBooksTab1.doClick();
//            }
//        });
//
//        // 🌟 NÂNG CẤP TRẢ SÁCH: Tích hợp ReturnPopup an toàn tuyệt đối
//        btnReturn.addActionListener(e -> {
//            ReturnPopup popup = new ReturnPopup((JFrame) SwingUtilities.getWindowAncestor(panel));
//            popup.setVisible(true);
//
//            // Tự động F5 cả 2 Tab sau khi đóng popup
//            btnLoad.doClick();
//            if (btnLoadBooksTab1 != null) {
//                btnLoadBooksTab1.doClick();
//            }
//        });
//
//        return panel;
//    }
//}

package org.example;

import org.example.Data_access_object.BookDAO;
import org.example.Data_access_object.TransactionDAO;
import org.example.models.Book;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MainDashboard {
    private BookDAO bookTool = new BookDAO();
    private TransactionDAO transactionTool = new TransactionDAO();
    private JButton btnLoadBooksTab1;

    public MainDashboard() {
        JFrame frame = new JFrame("Library Management System");
        frame.setSize(900, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("📚 Book Inventory", createBookPanel());
        tabbedPane.addTab("📝 Borrow & Return", createTransactionPanel());

        frame.add(tabbedPane);
        frame.setVisible(true);
    }

    private JPanel createBookPanel() {
        JPanel panel = new JPanel(null);

        // ================= HÀNG 1: LOAD, FILTER, SEARCH =================
        btnLoadBooksTab1 = new JButton("1. Load Books");
        btnLoadBooksTab1.setBounds(20, 10, 120, 35);
        panel.add(btnLoadBooksTab1);

        JLabel lblFilter = new JLabel("Filter:");
        lblFilter.setBounds(150, 10, 50, 35);
        lblFilter.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(lblFilter);

        String[] filterOptions = {"All", "Fiction", "Self-Help", "Programming", "Astrophysics", "Light Novel"};
        JComboBox<String> cbFilter = new JComboBox<>(filterOptions);
        cbFilter.setBounds(200, 10, 130, 35);
        panel.add(cbFilter);

        // --- MỚI: TÍNH NĂNG TÌM KIẾM SÁCH BẰNG CHỮ ---
        JTextField txtSearchBook = new JTextField();
        txtSearchBook.setBounds(350, 10, 200, 35);
        panel.add(txtSearchBook);

        JButton btnSearch = new JButton("🔍 Search");
        btnSearch.setBounds(560, 10, 110, 35);
        panel.add(btnSearch);

        // ================= HÀNG 2: ADD, UPDATE QTY, DELETE =================
        JButton btnAdd = new JButton("2. Add New Book");
        btnAdd.setBounds(20, 55, 150, 35);
        panel.add(btnAdd);

        // --- MỚI: TÍNH NĂNG THÊM/BỚT SỐ LƯỢNG CHO SÁCH CŨ ---
        JButton btnUpdateQty = new JButton("🔄 Update Qty (+/-)");
        btnUpdateQty.setBounds(180, 55, 170, 35);
        panel.add(btnUpdateQty);

        JButton btnDelete = new JButton("3. Delete Book");
        btnDelete.setBounds(360, 55, 130, 35);
        panel.add(btnDelete);

        // ================= BẢNG HIỂN THỊ =================
        String[] columnNames = {"Book ID", "Title", "Author", "Category", "Total Qty", "Available Qty"};
        javax.swing.table.DefaultTableModel tableModel = new javax.swing.table.DefaultTableModel(columnNames, 0);

        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.setRowHeight(25);
        table.getColumnModel().getColumn(1).setPreferredWidth(250);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(20, 100, 840, 450); // Lùi bảng xuống một chút để nhường chỗ cho 2 hàng nút
        panel.add(scrollPane);

        // ================= SỰ KIỆN NÚT BẤM =================

        // Lọc Thể Loại
        cbFilter.addActionListener(e -> {
            String selectedCategory = (String) cbFilter.getSelectedItem();
            tableModel.setRowCount(0);
            List<Book> books = bookTool.getBooks(selectedCategory);
            for (Book b : books) {
                tableModel.addRow(new Object[]{ b.getId(), b.getTitle(), b.getAuthorName(), b.getCategoryName(), b.getTotalQuantity(), b.getAvailableQuantity() });
            }
        });

        // Nút Load
        btnLoadBooksTab1.addActionListener(e -> {
            cbFilter.setSelectedIndex(0);
            txtSearchBook.setText(""); // Xóa trắng ô tìm kiếm
        });

        // 🌟 NÚT TÌM KIẾM
        btnSearch.addActionListener(e -> {
            String keyword = txtSearchBook.getText().trim();
            tableModel.setRowCount(0);
            List<Book> books = bookTool.searchBooksForUI(keyword);
            for (Book b : books) {
                tableModel.addRow(new Object[]{ b.getId(), b.getTitle(), b.getAuthorName(), b.getCategoryName(), b.getTotalQuantity(), b.getAvailableQuantity() });
            }
        });

        // 🌟 NÚT CẬP NHẬT THÊM / BỚT SỐ LƯỢNG
        btnUpdateQty.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(panel, "⚠️ Please select a book from the table first!");
                return;
            }
            int bookId = (int) table.getValueAt(selectedRow, 0);
            String bookTitle = (String) table.getValueAt(selectedRow, 1);

            // Bật bảng hỏi số lượng
            String input = JOptionPane.showInputDialog(panel,
                    "Book: " + bookTitle + "\n\n" +
                            "• Enter a POSITIVE number to ADD copies (e.g., 5)\n" +
                            "• Enter a NEGATIVE number to REMOVE copies (e.g., -2)",
                    "Update Quantity", JOptionPane.QUESTION_MESSAGE);

            if (input != null && !input.trim().isEmpty()) {
                try {
                    int amountChange = Integer.parseInt(input.trim());
                    if (amountChange == 0) return;

                    boolean success = bookTool.updateBookQuantity(bookId, amountChange);

                    if (success) {
                        JOptionPane.showMessageDialog(panel, "✅ Quantity updated successfully!");
                        btnLoadBooksTab1.doClick(); // Tự động F5 bảng
                    } else {
                        // Chặn lại nếu bớt sách quá tay
                        JOptionPane.showMessageDialog(panel, "❌ ERROR: Cannot remove that many copies.\nThere are not enough available books on the shelf (some might be currently borrowed).", "Update Blocked", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(panel, "⚠️ Please enter a valid whole number!");
                }
            }
        });

        // Thêm Sách Mới
        btnAdd.addActionListener(e -> {
            JTextField txtTitle = new JTextField();
            JTextField txtAuthor = new JTextField();
            JTextField txtQuantity = new JTextField();

            String[] categoryOptions = {"1 - Fiction", "2 - Self-Help", "3 - Programming", "4 - Astrophysics", "5 - Light Novel"};
            JComboBox<String> cbCategory = new JComboBox<>(categoryOptions);

            Object[] formFields = { "Book Title (*):", txtTitle, "Author Name (*):", txtAuthor, "Category:", cbCategory, "Quantity (*):", txtQuantity };
            int option = JOptionPane.showConfirmDialog(panel, formFields, "📚 Add New Book", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (option == JOptionPane.OK_OPTION) {
                String title = txtTitle.getText().trim();
                String authorName = txtAuthor.getText().trim();
                String qtyStr = txtQuantity.getText().trim();

                if (title.isEmpty() || authorName.isEmpty() || qtyStr.isEmpty()) {
                    JOptionPane.showMessageDialog(panel, "❌ Please fill in all (*) fields!");
                } else {
                    try {
                        int qty = Integer.parseInt(qtyStr);
                        String selectedCategory = (String) cbCategory.getSelectedItem();
                        int categoryId = Integer.parseInt(selectedCategory.split(" - ")[0]);

                        bookTool.addBook(title, authorName, categoryId, qty);
                        JOptionPane.showMessageDialog(panel, "✅ Book added successfully!");
                        btnLoadBooksTab1.doClick();
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(panel, "⚠️ Quantity must be a valid number!");
                    }
                }
            }
        });

        // Xóa Sách
        btnDelete.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(panel, "⚠️ Please select a book from the table first!");
                return;
            }
            int bookId = (int) table.getValueAt(selectedRow, 0);
            String bookName = (String) table.getValueAt(selectedRow, 1);

            int confirm = JOptionPane.showConfirmDialog(panel, "Are you sure you want to delete: " + bookName + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = bookTool.deleteBook(bookId);
                if (success) {
                    JOptionPane.showMessageDialog(panel, "✅ Deleted successfully!");
                    btnLoadBooksTab1.doClick();
                } else {
                    JOptionPane.showMessageDialog(panel, "❌ ERROR: Cannot delete! This book is currently borrowed by someone.", "Delete Blocked", JOptionPane.ERROR_MESSAGE);
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

        JTextArea displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(displayArea);
        scrollPane.setBounds(20, 70, 840, 480);
        panel.add(scrollPane);

        btnLoad.addActionListener(e -> {
            displayArea.setText("=== BORROWED BOOKS & FINES LIST ===\n\n");
            displayArea.append(transactionTool.getBorrowerListForUI());
        });

        btnBorrow.addActionListener(e -> {
            BorrowPopup popup = new BorrowPopup((JFrame) SwingUtilities.getWindowAncestor(panel));
            popup.setVisible(true);

            btnLoad.doClick();
            if (btnLoadBooksTab1 != null) {
                btnLoadBooksTab1.doClick();
            }
        });

        btnReturn.addActionListener(e -> {
            ReturnPopup popup = new ReturnPopup((JFrame) SwingUtilities.getWindowAncestor(panel));
            popup.setVisible(true);

            btnLoad.doClick();
            if (btnLoadBooksTab1 != null) {
                btnLoadBooksTab1.doClick();
            }
        });

        return panel;
    }
}