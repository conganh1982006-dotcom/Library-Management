package org.example.models;

public class Book {
    private int id;
    private String title;
    private String authorName;   // Mới: Tên tác giả
    private String categoryName; // Mới: Tên thể loại
    private int totalQuantity;   // Mới: Tổng số lượng ban đầu
    private int availableQuantity;

    // Cập nhật Constructor
    public Book(int id, String title, String authorName, String categoryName, int totalQuantity, int availableQuantity) {
        this.id = id;
        this.title = title;
        this.authorName = authorName;
        this.categoryName = categoryName;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = availableQuantity;
    }

    // Các hàm Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthorName() { return authorName; }
    public String getCategoryName() { return categoryName; }
    public int getTotalQuantity() { return totalQuantity; }
    public int getAvailableQuantity() { return availableQuantity; }
}