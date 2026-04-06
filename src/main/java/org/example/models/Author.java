package org.example.models;

public class Author {
    private long authorId;
    private String name;

    // Constructor không tham số (Dùng khi cần tạo đối tượng trống)
    public Author() {}

    // Constructor có tham số (Dùng khi lấy dữ liệu từ Database lên)
    public Author(long authorId, String name) {
        this.authorId = authorId;
        this.name = name;
    }

    // 3. Getter và Setter (Để lấy và gán giá trị)
    public long getAuthorId() { return authorId; }
    public void setAuthorId(long authorId) { this.authorId = authorId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // 4. Hàm toString (Để in ra màn hình kiểm tra code cho lẹ)
    @Override
    public String toString() {
        return "Author [ID=" + authorId + ", Name=" + name + "]";
    }
}