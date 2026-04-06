package org.example.models;
import java.sql.Date;

public class Transaction {
    private long tnsId;
    private long bookId;
    private long borrowerId;
    private Date borrowDate;
    private Date dueDate;
    private Date returnDate;
    private String status; // BORROWED, RETURNED, OVERDUE

    public Transaction() {}

    public Transaction(long tnsId, long bookId, long borrowerId, Date borrowDate, Date dueDate, Date returnDate, String status) {
        this.tnsId = tnsId;
        this.bookId = bookId;
        this.borrowerId = borrowerId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
    }

    // Getters and Setters
    public long getTnsId() { return tnsId; }
    public void setTnsId(long tnsId) { this.tnsId = tnsId; }
    public long getBookId() { return bookId; }
    public void setBookId(long bookId) { this.bookId = bookId; }
    public long getBorrowerId() { return borrowerId; }
    public void setBorrowerId(long borrowerId) { this.borrowerId = borrowerId; }
    public Date getBorrowDate() { return borrowDate; }
    public void setBorrowDate(Date borrowDate) { this.borrowDate = borrowDate; }
    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }
    public Date getReturnDate() { return returnDate; }
    public void setReturnDate(Date returnDate) { this.returnDate = returnDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}