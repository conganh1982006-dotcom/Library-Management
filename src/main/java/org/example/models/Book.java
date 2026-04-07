package org.example.models;

public class Book {
    private long bookId;
    private String bookCode;
    private String title;
    private long authorId;
    private long categoryId;
    private int totalQuantity;
    private int availableQuantity;

    // ADD THESE 2 VARIABLES TO CONTAIN THE NAMES TO BE DISPLAYED ON THE TABLE
    private String authorName;
    private String categoryName;

    public Book() {}

    public Book(long bookId, String bookCode, String title, long authorId, long categoryId, int totalQuantity, int availableQuantity) {
        this.bookId = bookId;
        this.bookCode = bookCode;
        this.title = title;
        this.authorId = authorId;
        this.categoryId = categoryId;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = availableQuantity;
    }

    // Original Getters and Setters
    public long getBookId() { return bookId; }
    public void setBookId(long bookId) { this.bookId = bookId; }
    public String getBookCode() { return bookCode; }
    public void setBookCode(String bookCode) { this.bookCode = bookCode; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public long getAuthorId() { return authorId; }
    public void setAuthorId(long authorId) { this.authorId = authorId; }
    public long getCategoryId() { return categoryId; }
    public void setCategoryId(long categoryId) { this.categoryId = categoryId; }
    public int getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(int totalQuantity) { this.totalQuantity = totalQuantity; }
    public int getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }

    // Getters and Setters for Author Name & Genre
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    @Override
    public String toString() {
        return "Book: [" + bookCode + "] " + title;
    }
}