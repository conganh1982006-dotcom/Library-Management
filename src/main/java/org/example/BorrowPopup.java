//package org.example;
//
//import org.example.Data_access_object.BookDAO;
//import org.example.Data_access_object.BorrowerDAO;
//import org.example.Data_access_object.TransactionDAO;
//import org.example.models.Book;
//
//import javax.swing.*;
//import javax.swing.table.DefaultTableModel;
//import java.awt.*;
//import java.util.List;
//
//public class BorrowPopup extends JDialog {
//    private BorrowerDAO borrowerDAO = new BorrowerDAO();
//    private BookDAO bookDAO = new BookDAO();
//    private TransactionDAO transactionDAO = new TransactionDAO();
//
//    public BorrowPopup(JFrame parentFrame) {
//        super(parentFrame, "Tạo Phiếu Mượn Sách (Tương tác chuột)", true); // Mở cửa sổ này lên là khóa cửa sổ dưới
//        setSize(800, 650);
//        setLayout(null);
//        setLocationRelativeTo(parentFrame);
//
//        // ========================================================
//        // PHẦN 1: TÌM VÀ CHỌN NGƯỜI MƯỢN
//        // ========================================================
//        JLabel lblUser = new JLabel("1. TÌM THÀNH VIÊN (Gõ tên để tìm):");
//        lblUser.setBounds(20, 10, 300, 25);
//        add(lblUser);
//
//        JTextField txtSearchUser = new JTextField();
//        txtSearchUser.setBounds(20, 40, 200, 30);
//        add(txtSearchUser);
//
//        JButton btnSearchUser = new JButton("🔍 Tìm Khách");
//        btnSearchUser.setBounds(230, 40, 120, 30);
//        add(btnSearchUser);
//
//        // Bảng danh sách Khách hàng
//        String[] userCols = {"ID", "Tên Khách", "SĐT", "ĐANG MƯỢN (Cuốn)"};
//        DefaultTableModel userModel = new DefaultTableModel(userCols, 0);
//        JTable tblUser = new JTable(userModel);
//        tblUser.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        JScrollPane scrollUser = new JScrollPane(tblUser);
//        scrollUser.setBounds(20, 80, 740, 150);
//        add(scrollUser);
//
//        // Sự kiện: Bấm nút tìm khách
//        btnSearchUser.addActionListener(e -> {
//            userModel.setRowCount(0); // Xóa bảng cũ
//            List<Object[]> users = borrowerDAO.searchBorrower(txtSearchUser.getText());
//            for (Object[] u : users) {
//                userModel.addRow(u);
//            }
//        });
//
//        // ========================================================
//        // PHẦN 2: TÌM VÀ CHỌN SÁCH
//        // ========================================================
//        JLabel lblBook = new JLabel("2. TÌM SÁCH (Click chọn sách):");
//        lblBook.setBounds(20, 250, 300, 25);
//        add(lblBook);
//
//        JButton btnLoadBooks = new JButton("🔄 Tải Toàn Bộ Sách");
//        btnLoadBooks.setBounds(20, 280, 180, 30);
//        add(btnLoadBooks);
//
//        // Bảng danh sách Sách
//        String[] bookCols = {"ID Sách", "Tên Cuốn Sách", "Số Lượng Còn"};
//        DefaultTableModel bookModel = new DefaultTableModel(bookCols, 0);
//        JTable tblBook = new JTable(bookModel);
//        tblBook.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        JScrollPane scrollBook = new JScrollPane(tblBook);
//        scrollBook.setBounds(20, 320, 740, 150);
//        add(scrollBook);
//
//        // Sự kiện: Bấm tải sách
//        btnLoadBooks.addActionListener(e -> {
//            bookModel.setRowCount(0);
//            List<Book> books = bookDAO.getAllBooks();
//            for (Book b : books) {
//                bookModel.addRow(new Object[]{b.getId(), b.getTitle(), b.getAvailableQuantity()});
//            }
//        });
//
//        // ========================================================
//        // PHẦN 3: NÚT CHỐT ĐƠN (Tuyệt kỹ liên kết UI)
//        // ========================================================
//        JButton btnConfirm = new JButton("✅ XÁC NHẬN CHO MƯỢN SÁCH");
//        btnConfirm.setBounds(250, 500, 300, 50);
//        btnConfirm.setFont(new Font("SansSerif", Font.BOLD, 16));
//        btnConfirm.setBackground(new Color(50, 205, 50)); // Màu xanh lá cho ngầu
//        btnConfirm.setForeground(Color.WHITE);
//        add(btnConfirm);
//
//        btnConfirm.addActionListener(e -> {
//            int userRow = tblUser.getSelectedRow();
//            int bookRow = tblBook.getSelectedRow();
//
//            // Kiểm tra xem thủ thư đã click chọn đủ 2 bảng chưa
//            if (userRow == -1 || bookRow == -1) {
//                JOptionPane.showMessageDialog(this, "⚠️ Vui lòng click chọn 1 Khách hàng và 1 Cuốn sách!");
//                return;
//            }
//
//            // Lấy data từ dòng mà chuột đã click
//            int userId = (int) tblUser.getValueAt(userRow, 0);
//            String userName = (String) tblUser.getValueAt(userRow, 1);
//            int dangMuon = (int) tblUser.getValueAt(userRow, 3);
//
//            int bookId = (int) tblBook.getValueAt(bookRow, 0);
//            String bookName = (String) tblBook.getValueAt(bookRow, 1);
//            int availableQty = (int) tblBook.getValueAt(bookRow, 2);
//
//            // Kiểm tra kho sách
//            if (availableQty <= 0) {
//                JOptionPane.showMessageDialog(this, "❌ Sách này đã hết trong kho!");
//                return;
//            }
//
//            // Đỉnh cao UX: Cảnh báo nếu khách đang nợ sách
//            if (dangMuon > 0) {
//                int confirm = JOptionPane.showConfirmDialog(this,
//                        "⚠️ Khách '" + userName + "' ĐANG GIỮ " + dangMuon + " CUỐN CHƯA TRẢ.\nBạn có chắc chắn muốn cho mượn thêm không?",
//                        "Cảnh báo mượn lố", JOptionPane.YES_NO_OPTION);
//                if (confirm != JOptionPane.YES_OPTION) return; // Nếu chọn No thì hủy lệnh
//            }
//
//            // Chốt hạ: Bơm vào Database
//            transactionDAO.borrowBook(userId, bookId);
//            JOptionPane.showMessageDialog(this, "🎉 Đã tạo phiếu mượn tự động cho: " + userName + "\n📖 Sách: " + bookName);
//
//            this.dispose(); // Đóng popup lại
//        });
//    }
//}
//package org.example;
//
//import org.example.Data_access_object.BookDAO;
//import org.example.Data_access_object.BorrowerDAO;
//import org.example.Data_access_object.TransactionDAO;
//import org.example.models.Book;
//
//import javax.swing.*;
//import javax.swing.table.DefaultTableModel;
//import java.awt.*;
//import java.util.List;
//
//public class BorrowPopup extends JDialog {
//    private BorrowerDAO borrowerDAO = new BorrowerDAO();
//    private BookDAO bookDAO = new BookDAO();
//    private TransactionDAO transactionDAO = new TransactionDAO();
//
//    public BorrowPopup(JFrame parentFrame) {
//        super(parentFrame, "Tạo Phiếu Mượn Sách (Tương tác chuột)", true);
//        setSize(800, 650);
//        setLayout(null);
//        setLocationRelativeTo(parentFrame);
//
//        // ========================================================
//        // PHẦN 1: TÌM / THÊM NGƯỜI MƯỢN
//        // ========================================================
//        JLabel lblUser = new JLabel("1. KHÁCH HÀNG (Gõ tên để tìm hoặc bấm Thêm Mới):");
//        lblUser.setBounds(20, 10, 400, 25);
//        add(lblUser);
//
//        JTextField txtSearchUser = new JTextField();
//        txtSearchUser.setBounds(20, 40, 200, 30);
//        add(txtSearchUser);
//
//        JButton btnSearchUser = new JButton("🔍 Tìm Khách");
//        btnSearchUser.setBounds(230, 40, 120, 30);
//        add(btnSearchUser);
//
//        // NÚT MỚI: Thêm khách hàng ngay tại chỗ
//        JButton btnAddUser = new JButton("➕ Thêm Khách Mới");
//        btnAddUser.setBounds(360, 40, 150, 30);
//        add(btnAddUser);
//
//        String[] userCols = {"ID", "Tên Khách", "SĐT", "ĐANG MƯỢN (Cuốn)"};
//        DefaultTableModel userModel = new DefaultTableModel(userCols, 0);
//        JTable tblUser = new JTable(userModel);
//        tblUser.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        JScrollPane scrollUser = new JScrollPane(tblUser);
//        scrollUser.setBounds(20, 80, 740, 150);
//        add(scrollUser);
//
//        // Sự kiện: Tìm Khách
//        btnSearchUser.addActionListener(e -> {
//            userModel.setRowCount(0);
//            List<Object[]> users = borrowerDAO.searchBorrower(txtSearchUser.getText());
//            for (Object[] u : users) {
//                userModel.addRow(u);
//            }
//        });
//
//        // Sự kiện: Thêm Khách
//        btnAddUser.addActionListener(e -> {
//            String newName = JOptionPane.showInputDialog(this, "Nhập Tên Khách Hàng Mới:");
//            if (newName != null && !newName.trim().isEmpty()) {
//                borrowerDAO.addBorrower(newName);
//                JOptionPane.showMessageDialog(this, "✅ Đã đăng ký thành công!");
//                txtSearchUser.setText(newName); // Điền luôn tên vừa tạo vào ô tìm kiếm
//                btnSearchUser.doClick(); // Tự động bấm tìm kiếm để hiện ra bảng luôn
//            }
//        });
//
//        // ========================================================
//        // PHẦN 2: TÌM VÀ CHỌN SÁCH (Theo tên)
//        // ========================================================
//        JLabel lblBook = new JLabel("2. KHO SÁCH (Gõ tên sách để Lọc/Tìm kiếm):");
//        lblBook.setBounds(20, 250, 400, 25);
//        add(lblBook);
//
//        // Ô TÌM KIẾM SÁCH MỚI
//        JTextField txtSearchBook = new JTextField();
//        txtSearchBook.setBounds(20, 280, 200, 30);
//        add(txtSearchBook);
//
//        JButton btnSearchBook = new JButton("🔍 Tìm Sách");
//        btnSearchBook.setBounds(230, 280, 120, 30);
//        add(btnSearchBook);
//
//        JButton btnLoadBooks = new JButton("🔄 Tải Toàn Bộ");
//        btnLoadBooks.setBounds(360, 280, 150, 30);
//        add(btnLoadBooks);
//
//        String[] bookCols = {"ID Sách", "Tên Cuốn Sách", "Số Lượng Còn"};
//        DefaultTableModel bookModel = new DefaultTableModel(bookCols, 0);
//        JTable tblBook = new JTable(bookModel);
//        tblBook.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        JScrollPane scrollBook = new JScrollPane(tblBook);
//        scrollBook.setBounds(20, 320, 740, 150);
//        add(scrollBook);
//
//        // Sự kiện: Tải toàn bộ sách
//        btnLoadBooks.addActionListener(e -> {
//            bookModel.setRowCount(0);
//            List<Book> books = bookDAO.getAllBooks();
//            for (Book b : books) {
//                bookModel.addRow(new Object[]{b.getId(), b.getTitle(), b.getAvailableQuantity()});
//            }
//        });
//
//        // Sự kiện: Tìm/Lọc sách theo tên
//        btnSearchBook.addActionListener(e -> {
//            bookModel.setRowCount(0);
//            String keyword = txtSearchBook.getText();
//            List<Book> books = bookDAO.searchBooksForUI(keyword);
//            for (Book b : books) {
//                bookModel.addRow(new Object[]{b.getId(), b.getTitle(), b.getAvailableQuantity()});
//            }
//        });
//
//        // ========================================================
//        // PHẦN 3: NÚT CHỐT ĐƠN
//        // ========================================================
//        JButton btnConfirm = new JButton("✅ XÁC NHẬN CHO MƯỢN SÁCH");
//        btnConfirm.setBounds(250, 500, 300, 50);
//        btnConfirm.setFont(new Font("SansSerif", Font.BOLD, 16));
//        btnConfirm.setBackground(new Color(50, 205, 50));
//        btnConfirm.setForeground(Color.WHITE);
//        add(btnConfirm);
//
//        btnConfirm.addActionListener(e -> {
//            int userRow = tblUser.getSelectedRow();
//            int bookRow = tblBook.getSelectedRow();
//
//            if (userRow == -1 || bookRow == -1) {
//                JOptionPane.showMessageDialog(this, "⚠️ Vui lòng click chọn 1 Khách hàng và 1 Cuốn sách!");
//                return;
//            }
//
//            int userId = (int) tblUser.getValueAt(userRow, 0);
//            String userName = (String) tblUser.getValueAt(userRow, 1);
//            int dangMuon = (int) tblUser.getValueAt(userRow, 3);
//
//            int bookId = (int) tblBook.getValueAt(bookRow, 0);
//            String bookName = (String) tblBook.getValueAt(bookRow, 1);
//            int availableQty = (int) tblBook.getValueAt(bookRow, 2);
//
//            if (availableQty <= 0) {
//                JOptionPane.showMessageDialog(this, "❌ Sách này đã hết trong kho!");
//                return;
//            }
//
//            if (dangMuon > 0) {
//                int confirm = JOptionPane.showConfirmDialog(this,
//                        "⚠️ Khách '" + userName + "' ĐANG GIỮ " + dangMuon + " CUỐN CHƯA TRẢ.\nBạn có chắc chắn muốn cho mượn thêm không?",
//                        "Cảnh báo mượn lố", JOptionPane.YES_NO_OPTION);
//                if (confirm != JOptionPane.YES_OPTION) return;
//            }
//
//            transactionDAO.borrowBook(userId, bookId);
//            JOptionPane.showMessageDialog(this, "🎉 Đã tạo phiếu mượn tự động cho: " + userName + "\n📖 Sách: " + bookName);
//            this.dispose();
//        });
//    }
//}


//package org.example;
//
//import org.example.Data_access_object.BookDAO;
//import org.example.Data_access_object.BorrowerDAO;
//import org.example.Data_access_object.TransactionDAO;
//import org.example.models.Book;
//
//import javax.swing.*;
//import javax.swing.table.DefaultTableModel;
//import java.awt.*;
//import java.util.List;
//
//public class BorrowPopup extends JDialog {
//    private BorrowerDAO borrowerDAO = new BorrowerDAO();
//    private BookDAO bookDAO = new BookDAO();
//    private TransactionDAO transactionDAO = new TransactionDAO();
//
//    public BorrowPopup(JFrame parentFrame) {
//        super(parentFrame, "Tạo Phiếu Mượn Sách (Tương tác chuột)", true);
//        setSize(800, 650);
//        setLayout(null);
//        setLocationRelativeTo(parentFrame);
//
//        // ========================================================
//        // PHẦN 1: TÌM / THÊM NGƯỜI MƯỢN
//        // ========================================================
//        JLabel lblUser = new JLabel("1. KHÁCH HÀNG (Gõ tên để tìm hoặc bấm Thêm Mới):");
//        lblUser.setBounds(20, 10, 400, 25);
//        add(lblUser);
//
//        JTextField txtSearchUser = new JTextField();
//        txtSearchUser.setBounds(20, 40, 200, 30);
//        add(txtSearchUser);
//
//        JButton btnSearchUser = new JButton("🔍 Tìm Khách");
//        btnSearchUser.setBounds(230, 40, 120, 30);
//        add(btnSearchUser);
//
//        // NÚT MỚI: Thêm khách hàng ngay tại chỗ
//        JButton btnAddUser = new JButton("➕ Thêm Khách Mới");
//        btnAddUser.setBounds(360, 40, 150, 30);
//        add(btnAddUser);
//
//        String[] userCols = {"ID", "Tên Khách", "SĐT", "ĐANG MƯỢN (Cuốn)"};
//        DefaultTableModel userModel = new DefaultTableModel(userCols, 0);
//        JTable tblUser = new JTable(userModel);
//        tblUser.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        JScrollPane scrollUser = new JScrollPane(tblUser);
//        scrollUser.setBounds(20, 80, 740, 150);
//        add(scrollUser);
//
//        // Sự kiện: Tìm Khách
//        btnSearchUser.addActionListener(e -> {
//            userModel.setRowCount(0);
//            List<Object[]> users = borrowerDAO.searchBorrower(txtSearchUser.getText());
//            for (Object[] u : users) {
//                userModel.addRow(u);
//            }
//        });
//
//        // 🌟 SỰ KIỆN NÂNG CẤP: Gom tất cả vào 1 Form điền thông tin duy nhất!
//        btnAddUser.addActionListener(e -> {
//            // Tạo 3 ô nhập liệu
//            JTextField txtName = new JTextField();
//            JTextField txtPhone = new JTextField();
//            JTextField txtEmail = new JTextField();
//
//            // Gom Label và Ô nhập liệu vào 1 khối (Form)
//            Object[] formFields = {
//                    "Tên Khách Hàng (*):", txtName,
//                    "Số Điện Thoại:", txtPhone,
//                    "Email:", txtEmail
//            };
//
//            // Bật 1 Popup duy nhất chứa cả khối Form đó
//            int option = JOptionPane.showConfirmDialog(this, formFields, "📝 Đăng Ký Thành Viên Mới", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
//
//            // Nếu người dùng bấm OK (Xác nhận)
//            if (option == JOptionPane.OK_OPTION) {
//                String newName = txtName.getText().trim();
//                String newPhone = txtPhone.getText().trim();
//                String newEmail = txtEmail.getText().trim();
//
//                // Bắt lỗi nếu quên điền Tên
//                if (newName.isEmpty()) {
//                    JOptionPane.showMessageDialog(this, "❌ Tên khách hàng không được để trống!");
//                } else {
//                    // Gọi DAO lưu vào Database
//                    borrowerDAO.addBorrower(newName, newPhone, newEmail);
//                    JOptionPane.showMessageDialog(this, "✅ Đã đăng ký thành công hồ sơ cho: " + newName);
//
//                    // Tự động tìm kiếm ngay lập tức
//                    txtSearchUser.setText(newName);
//                    btnSearchUser.doClick();
//                }
//            }
//        });
//
//        // ========================================================
//        // PHẦN 2: TÌM VÀ CHỌN SÁCH (Theo tên)
//        // ========================================================
//        JLabel lblBook = new JLabel("2. KHO SÁCH (Gõ tên sách để Lọc/Tìm kiếm):");
//        lblBook.setBounds(20, 250, 400, 25);
//        add(lblBook);
//
//        JTextField txtSearchBook = new JTextField();
//        txtSearchBook.setBounds(20, 280, 200, 30);
//        add(txtSearchBook);
//
//        JButton btnSearchBook = new JButton("🔍 Tìm Sách");
//        btnSearchBook.setBounds(230, 280, 120, 30);
//        add(btnSearchBook);
//
//        JButton btnLoadBooks = new JButton("🔄 Tải Toàn Bộ");
//        btnLoadBooks.setBounds(360, 280, 150, 30);
//        add(btnLoadBooks);
//
//        String[] bookCols = {"ID Sách", "Tên Cuốn Sách", "Số Lượng Còn"};
//        DefaultTableModel bookModel = new DefaultTableModel(bookCols, 0);
//        JTable tblBook = new JTable(bookModel);
//        tblBook.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        JScrollPane scrollBook = new JScrollPane(tblBook);
//        scrollBook.setBounds(20, 320, 740, 150);
//        add(scrollBook);
//
//        btnLoadBooks.addActionListener(e -> {
//            bookModel.setRowCount(0);
//            List<Book> books = bookDAO.getAllBooks();
//            for (Book b : books) {
//                bookModel.addRow(new Object[]{b.getId(), b.getTitle(), b.getAvailableQuantity()});
//            }
//        });
//
//        btnSearchBook.addActionListener(e -> {
//            bookModel.setRowCount(0);
//            String keyword = txtSearchBook.getText();
//            List<Book> books = bookDAO.searchBooksForUI(keyword);
//            for (Book b : books) {
//                bookModel.addRow(new Object[]{b.getId(), b.getTitle(), b.getAvailableQuantity()});
//            }
//        });
//
//        // ========================================================
//        // PHẦN 3: NÚT CHỐT ĐƠN
//        // ========================================================
//        JButton btnConfirm = new JButton("✅ XÁC NHẬN CHO MƯỢN SÁCH");
//        btnConfirm.setBounds(250, 500, 300, 50);
//        btnConfirm.setFont(new Font("SansSerif", Font.BOLD, 16));
//        btnConfirm.setBackground(new Color(50, 205, 50));
//        btnConfirm.setForeground(Color.WHITE);
//        add(btnConfirm);
//
//        btnConfirm.addActionListener(e -> {
//            int userRow = tblUser.getSelectedRow();
//            int bookRow = tblBook.getSelectedRow();
//
//            if (userRow == -1 || bookRow == -1) {
//                JOptionPane.showMessageDialog(this, "⚠️ Vui lòng click chọn 1 Khách hàng và 1 Cuốn sách!");
//                return;
//            }
//
//            int userId = (int) tblUser.getValueAt(userRow, 0);
//            String userName = (String) tblUser.getValueAt(userRow, 1);
//            int dangMuon = (int) tblUser.getValueAt(userRow, 3);
//
//            int bookId = (int) tblBook.getValueAt(bookRow, 0);
//            String bookName = (String) tblBook.getValueAt(bookRow, 1);
//            int availableQty = (int) tblBook.getValueAt(bookRow, 2);
//
//            if (availableQty <= 0) {
//                JOptionPane.showMessageDialog(this, "❌ Sách này đã hết trong kho!");
//                return;
//            }
//
//            if (dangMuon > 0) {
//                int confirm = JOptionPane.showConfirmDialog(this,
//                        "⚠️ Khách '" + userName + "' ĐANG GIỮ " + dangMuon + " CUỐN CHƯA TRẢ.\nBạn có chắc chắn muốn cho mượn thêm không?",
//                        "Cảnh báo mượn lố", JOptionPane.YES_NO_OPTION);
//                if (confirm != JOptionPane.YES_OPTION) return;
//            }
//
//            transactionDAO.borrowBook(userId, bookId);
//            JOptionPane.showMessageDialog(this, "🎉 Đã tạo phiếu mượn tự động cho: " + userName + "\n📖 Sách: " + bookName);
//            this.dispose();
//        });
//    }
//}

package org.example;

import org.example.Data_access_object.BookDAO;
import org.example.Data_access_object.BorrowerDAO;
import org.example.Data_access_object.TransactionDAO;
import org.example.models.Book;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class BorrowPopup extends JDialog {
    private BorrowerDAO borrowerDAO = new BorrowerDAO();
    private BookDAO bookDAO = new BookDAO();
    private TransactionDAO transactionDAO = new TransactionDAO();

    public BorrowPopup(JFrame parentFrame) {
        super(parentFrame, "Create Borrow Ticket", true);
        setSize(850, 700);
        setLayout(null);
        setLocationRelativeTo(parentFrame);


        // PART 1: SELECT / ADD BORROWER

        JLabel lblUser = new JLabel("1. BORROWER (Search by Name or Add New):");
        lblUser.setBounds(20, 10, 400, 25);
        add(lblUser);

        JTextField txtSearchUser = new JTextField();
        txtSearchUser.setBounds(20, 40, 200, 30);
        add(txtSearchUser);

        JButton btnSearchUser = new JButton("🔍 Search User");
        btnSearchUser.setBounds(230, 40, 150, 30);
        add(btnSearchUser);

        JButton btnAddUser = new JButton("➕ Add New User");
        btnAddUser.setBounds(390, 40, 150, 30);
        add(btnAddUser);

        String[] userCols = {"User ID", "Name", "Phone", "Currently Borrowing"};
        DefaultTableModel userModel = new DefaultTableModel(userCols, 0);
        JTable tblUser = new JTable(userModel);
        tblUser.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollUser = new JScrollPane(tblUser);
        scrollUser.setBounds(20, 80, 790, 150);
        add(scrollUser);

        btnSearchUser.addActionListener(e -> {
            userModel.setRowCount(0);
            List<Object[]> users = borrowerDAO.searchBorrower(txtSearchUser.getText());
            for (Object[] u : users) {
                userModel.addRow(u);
            }
        });

        btnAddUser.addActionListener(e -> {
            JTextField txtName = new JTextField();
            JTextField txtPhone = new JTextField();
            JTextField txtEmail = new JTextField();

            Object[] formFields = {
                    "Full Name (*):", txtName,
                    "Phone Number:", txtPhone,
                    "Email:", txtEmail
            };

            int option = JOptionPane.showConfirmDialog(this, formFields, "📝 Register New User", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (option == JOptionPane.OK_OPTION) {
                String newName = txtName.getText().trim();
                String newPhone = txtPhone.getText().trim();
                String newEmail = txtEmail.getText().trim();

                if (newName.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "❌ Name cannot be empty!");
                } else {
                    borrowerDAO.addBorrower(newName, newPhone, newEmail);
                    JOptionPane.showMessageDialog(this, "✅ User registered successfully for: " + newName);

                    txtSearchUser.setText(newName);
                    btnSearchUser.doClick();
                }
            }
        });

        // PART 2: SELECT BOOK

        JLabel lblBook = new JLabel("2. BOOK INVENTORY (Search by Title or Load All):");
        lblBook.setBounds(20, 250, 400, 25);
        add(lblBook);

        JTextField txtSearchBook = new JTextField();
        txtSearchBook.setBounds(20, 280, 200, 30);
        add(txtSearchBook);

        JButton btnSearchBook = new JButton("🔍 Search Book");
        btnSearchBook.setBounds(230, 280, 150, 30);
        add(btnSearchBook);

        JButton btnLoadBooks = new JButton("🔄 Load All Books");
        btnLoadBooks.setBounds(390, 280, 150, 30);
        add(btnLoadBooks);

        String[] bookCols = {"Book ID", "Title", "Available Qty"};
        DefaultTableModel bookModel = new DefaultTableModel(bookCols, 0);
        JTable tblBook = new JTable(bookModel);
        tblBook.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollBook = new JScrollPane(tblBook);
        scrollBook.setBounds(20, 320, 790, 150);
        add(scrollBook);

        btnLoadBooks.addActionListener(e -> {
            bookModel.setRowCount(0);
            List<Book> books = bookDAO.getAllBooks();
            for (Book b : books) {
                bookModel.addRow(new Object[]{b.getId(), b.getTitle(), b.getAvailableQuantity()});
            }
        });

        btnSearchBook.addActionListener(e -> {
            bookModel.setRowCount(0);
            String keyword = txtSearchBook.getText();
            List<Book> books = bookDAO.searchBooksForUI(keyword);
            for (Book b : books) {
                bookModel.addRow(new Object[]{b.getId(), b.getTitle(), b.getAvailableQuantity()});
            }
        });


        // PART 3: CONFIRM BORROW

        JButton btnConfirm = new JButton("✅ CONFIRM BORROW");
        btnConfirm.setBounds(270, 500, 300, 50);
        btnConfirm.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnConfirm.setBackground(new Color(50, 205, 50));
        btnConfirm.setForeground(Color.WHITE);
        add(btnConfirm);

        btnConfirm.addActionListener(e -> {
            int userRow = tblUser.getSelectedRow();
            int bookRow = tblBook.getSelectedRow();

            if (userRow == -1 || bookRow == -1) {
                JOptionPane.showMessageDialog(this, "⚠️ Please select 1 User and 1 Book!");
                return;
            }

            int userId = (int) tblUser.getValueAt(userRow, 0);
            String userName = (String) tblUser.getValueAt(userRow, 1);
            int currentlyBorrowing = (int) tblUser.getValueAt(userRow, 3);

            int bookId = (int) tblBook.getValueAt(bookRow, 0);
            String bookName = (String) tblBook.getValueAt(bookRow, 1);
            int availableQty = (int) tblBook.getValueAt(bookRow, 2);

            if (availableQty <= 0) {
                JOptionPane.showMessageDialog(this, "❌ This book is currently out of stock!");
                return;
            }

            if (currentlyBorrowing > 0) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "⚠️ User '" + userName + "' is already holding " + currentlyBorrowing + " book(s).\nDo you want to proceed?",
                        "Warning", JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) return;
            }

            transactionDAO.borrowBook(userId, bookId);
            JOptionPane.showMessageDialog(this, "🎉 Borrow Ticket Created for: " + userName + "\n📖 Book: " + bookName);

            this.dispose();
        });

        //Tự động tải dữ liệu mới nhất khi vừa mở Popup
        SwingUtilities.invokeLater(() -> {
            btnSearchUser.doClick();
            btnLoadBooks.doClick();
        });
    }
}