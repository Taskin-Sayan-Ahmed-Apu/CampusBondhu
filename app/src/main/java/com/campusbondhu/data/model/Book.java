package com.campusbondhu.data.model;

public class Book {
    private String bookId;
    private String title;
    private String author;
    private String barcode;
    private boolean available; // isAvailable থেকে available করা হলো
    private String currentHolderId;

    public Book() {}

    public String getBookId() { return bookId; }
    public void setBookId(String v) { bookId = v; }
    public String getTitle() { return title; }
    public void setTitle(String v) { title = v; }
    public String getAuthor() { return author; }
    public void setAuthor(String v) { author = v; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String v) { barcode = v; }

    // গেটার এবং সেটার আগের মতোই থাকবে, তাই অন্য ফাইলে কিছু চেঞ্জ করতে হবে না
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean v) { available = v; }

    public String getCurrentHolderId() { return currentHolderId; }
    public void setCurrentHolderId(String v) { currentHolderId = v; }
}