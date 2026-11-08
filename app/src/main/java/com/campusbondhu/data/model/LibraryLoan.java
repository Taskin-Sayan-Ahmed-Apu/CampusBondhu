package com.campusbondhu.data.model;

public class LibraryLoan {
    private String loanId;
    private String bookId;
    private String bookTitle;
    private String studentId;
    private String studentName;
    private long issueDate;
    private long dueDate;
    private long returnDate;
    private String status; // ISSUED, RETURNED

    public LibraryLoan() {}

    public String getLoanId() { return loanId; }
    public void setLoanId(String v) { loanId = v; }
    public String getBookId() { return bookId; }
    public void setBookId(String v) { bookId = v; }
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String v) { bookTitle = v; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String v) { studentId = v; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String v) { studentName = v; }
    public long getIssueDate() { return issueDate; }
    public void setIssueDate(long v) { issueDate = v; }
    public long getDueDate() { return dueDate; }
    public void setDueDate(long v) { dueDate = v; }
    public long getReturnDate() { return returnDate; }
    public void setReturnDate(long v) { returnDate = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { status = v; }
}
