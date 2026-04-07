package org.example;

import org.example.Data_access_object.BookDAO;
import org.example.Data_access_object.BorrowerDAO;
import org.example.Data_access_object.TransactionDAO;
import org.example.models.Book;
import org.example.models.Borrower; // Nhớ import khuôn Borrower mới

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

        JLabel lblUser = new JLabel("BORROWER (Search by Name or Add New):");
        lblUser.setBounds(20, 10, 400, 25);
        add(lblUser);

        JTextField txtSearchUser = new JTextField();
        txtSearchUser.setBounds(20, 40, 200, 30);
        add(txtSearchUser);

        JButton btnSearchUser = new JButton("Search User");
        btnSearchUser.setBounds(230, 40, 150, 30);
        add(btnSearchUser);

        JButton btnAddUser = new JButton("Add New User");
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

            int option = JOptionPane.showConfirmDialog(this, formFields, "Register New User", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (option == JOptionPane.OK_OPTION) {
                String newName = txtName.getText().trim();
                String newPhone = txtPhone.getText().trim();
                String newEmail = txtEmail.getText().trim();

                if (newName.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Name cannot be empty!");
                } else {
                    // FIX 1: Đóng gói vào Model Borrower trước khi truyền cho DAO
                    Borrower newBorrower = new Borrower(0, newName, newEmail, newPhone);
                    borrowerDAO.addBorrower(newBorrower);

                    JOptionPane.showMessageDialog(this, "User registered successfully for: " + newName);

                    txtSearchUser.setText(newName);
                    btnSearchUser.doClick();
                }
            }
        });

        // PART 2: SELECT BOOK

        JLabel lblBook = new JLabel("BOOK INVENTORY (Search by Title or Load All):");
        lblBook.setBounds(20, 250, 400, 25);
        add(lblBook);

        JTextField txtSearchBook = new JTextField();
        txtSearchBook.setBounds(20, 280, 200, 30);
        add(txtSearchBook);

        JButton btnSearchBook = new JButton("Search Book");
        btnSearchBook.setBounds(230, 280, 150, 30);
        add(btnSearchBook);

        JButton btnLoadBooks = new JButton("Load All Books");
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
                // FIX 2: Gọi đúng hàm getBookId()
                bookModel.addRow(new Object[]{b.getBookId(), b.getTitle(), b.getAvailableQuantity()});
            }
        });

        btnSearchBook.addActionListener(e -> {
            bookModel.setRowCount(0);
            String keyword = txtSearchBook.getText();

            // ĐÃ FIX: Đổi từ searchBooksByTitle sang searchOmni cho đồng bộ với BookDAO mới
            List<Book> books = bookDAO.searchOmni(keyword);

            for (Book b : books) {
                bookModel.addRow(new Object[]{b.getBookId(), b.getTitle(), b.getAvailableQuantity()});
            }
        });

        // PART 3: CONFIRM BORROW

        JButton btnConfirm = new JButton("CONFIRM BORROW");
        btnConfirm.setBounds(270, 500, 300, 50);
        btnConfirm.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnConfirm.setBackground(new Color(50, 205, 50));
        btnConfirm.setForeground(Color.WHITE);
        add(btnConfirm);

        btnConfirm.addActionListener(e -> {
            int userRow = tblUser.getSelectedRow();
            int bookRow = tblBook.getSelectedRow();

            if (userRow == -1 || bookRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select 1 User and 1 Book!");
                return;
            }

            // FIX 4: Cast safely to long/int to avoid ClassCastException
            long userId = ((Number) tblUser.getValueAt(userRow, 0)).longValue();
            String userName = (String) tblUser.getValueAt(userRow, 1);
            int currentlyBorrowing = ((Number) tblUser.getValueAt(userRow, 3)).intValue();

            long bookId = ((Number) tblBook.getValueAt(bookRow, 0)).longValue();
            String bookName = (String) tblBook.getValueAt(bookRow, 1);
            int availableQty = ((Number) tblBook.getValueAt(bookRow, 2)).intValue();

            if (availableQty <= 0) {
                JOptionPane.showMessageDialog(this, "This book is currently out of stock!");
                return;
            }

            if (currentlyBorrowing > 0) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "User '" + userName + "' is already holding " + currentlyBorrowing + " book(s).\nDo you want to proceed?",
                        "Warning", JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) return;
            }

            // Calling a book borrowing function with Transaction Control
            transactionDAO.borrowBook(userId, bookId);
            JOptionPane.showMessageDialog(this, "Borrow Ticket Created for: " + userName + "\nBook: " + bookName);

            this.dispose();
        });

        //Automatically load the latest data when the popup is opened
        SwingUtilities.invokeLater(() -> {
            btnSearchUser.doClick();
            btnLoadBooks.doClick();
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BorrowPopup dialog = new BorrowPopup(null);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        });
    }
}