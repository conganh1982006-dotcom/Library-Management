package org.example.models;

public class Author {
    private long authorId;
    private String name;

    // No-parameter constructor (Used when you need to create an empty object)
    public Author() {}

    // Parameterized constructor (Used when retrieving data from a database)
    public Author(long authorId, String name) {
        this.authorId = authorId;
        this.name = name;
    }

    // 3. Getter and Setter (For retrieving and assigning values)
    public long getAuthorId() { return authorId; }
    public void setAuthorId(long authorId) { this.authorId = authorId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // 4. The toString function (To quickly print the code to the screen for verification)
    @Override
    public String toString() {
        return "Author [ID=" + authorId + ", Name=" + name + "]";
    }
}