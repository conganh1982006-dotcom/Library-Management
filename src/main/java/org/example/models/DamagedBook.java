package org.example.models;

import java.sql.Timestamp;

public class DamagedBook {
    private int issueId;
    private long bookId;
    private String damageType;
    private String notes;
    private String status; // UNFIXED or REPAIRED
    private Timestamp reportedDate;

    // Additional fields for UI display (populated via JOIN queries)
    private String bookCode;
    private String bookTitle;

    // 1. No-parameter constructor
    public DamagedBook() {}

    // 2. Parameterized constructor (Core database fields)
    public DamagedBook(int issueId, long bookId, String damageType, String notes, String status, Timestamp reportedDate) {
        this.issueId = issueId;
        this.bookId = bookId;
        this.damageType = damageType;
        this.notes = notes;
        this.status = status;
        this.reportedDate = reportedDate;
    }

    // 3. Getters and Setters
    public int getIssueId() { return issueId; }
    public void setIssueId(int issueId) { this.issueId = issueId; }

    public long getBookId() { return bookId; }
    public void setBookId(long bookId) { this.bookId = bookId; }

    public String getDamageType() { return damageType; }
    public void setDamageType(String damageType) { this.damageType = damageType; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getReportedDate() { return reportedDate; }
    public void setReportedDate(Timestamp reportedDate) { this.reportedDate = reportedDate; }

    // Getters and Setters for UI display fields
    public String getBookCode() { return bookCode; }
    public void setBookCode(String bookCode) { this.bookCode = bookCode; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    // 4. The toString function for quick debugging
    @Override
    public String toString() {
        return "DamagedBook [" + bookCode + "] " + bookTitle + " - Status: " + status;
    }
}