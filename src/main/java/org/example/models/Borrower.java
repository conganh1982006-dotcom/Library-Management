package org.example.models;

public class Borrower {
    private long borrowerId;
    private String name;
    private String email;
    private String phoneNumber;

    public Borrower() {}

    public Borrower(long borrowerId, String name, String email, String phoneNumber) {
        this.borrowerId = borrowerId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    // Getters and Setters
    public long getBorrowerId() { return borrowerId; }
    public void setBorrowerId(long borrowerId) { this.borrowerId = borrowerId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}
