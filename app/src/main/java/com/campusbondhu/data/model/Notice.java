package com.campusbondhu.data.model;

public class Notice {
    private String noticeId;
    private String title;
    private String body;
    private String category; // General, Exam, Cancellation
    private boolean isImportant;
    private String authorId;
    private String authorName;
    private long timestamp;

    public Notice() {}

    public String getNoticeId() { return noticeId; }
    public void setNoticeId(String v) { noticeId = v; }
    public String getTitle() { return title; }
    public void setTitle(String v) { title = v; }
    public String getBody() { return body; }
    public void setBody(String v) { body = v; }
    public String getCategory() { return category; }
    public void setCategory(String v) { category = v; }
    public boolean isImportant() { return isImportant; }
    public void setImportant(boolean v) { isImportant = v; }
    public String getAuthorId() { return authorId; }
    public void setAuthorId(String v) { authorId = v; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String v) { authorName = v; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long v) { timestamp = v; }
}
