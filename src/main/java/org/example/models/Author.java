package org.example.models;

public class Author {
    private long authorId;
    private String authorCode; // Added to match ERD and database
    private String name;
    private int birthYear;     // Added to match ERD and database

    // No-parameter constructor (Used when you need to create an empty object)
    public Author() {}

    // Parameterized constructor (Used when retrieving data from a database)
    public Author(long authorId, String authorCode, String name, int birthYear) {
        this.authorId = authorId;
        this.authorCode = authorCode;
        this.name = name;
        this.birthYear = birthYear;
    }

    // Getters and Setters (For retrieving and assigning values)
    public long getAuthorId() { return authorId; }
    public void setAuthorId(long authorId) { this.authorId = authorId; }

    public String getAuthorCode() { return authorCode; }
    public void setAuthorCode(String authorCode) { this.authorCode = authorCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getBirthYear() { return birthYear; }
    public void setBirthYear(int birthYear) { this.birthYear = birthYear; }

    // The toString function (To quickly print the code to the screen for verification)
    @Override
    public String toString() {
        return "Author [" + authorCode + "] " + name + " (" + birthYear + ")";
    }
}