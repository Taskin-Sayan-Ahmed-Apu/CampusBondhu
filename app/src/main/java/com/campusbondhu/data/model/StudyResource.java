package com.campusbondhu.data.model;

public class StudyResource {
    private String resourceId;
    private String title;
    private String courseCode;
    private String type; // "Notes", "Past Question"
    private String fileUrl;
    private String uploadedBy;
    private long timestamp;

    public StudyResource() {}

    public String getResourceId() { return resourceId; }
    public void setResourceId(String v) { resourceId = v; }
    public String getTitle() { return title; }
    public void setTitle(String v) { title = v; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String v) { courseCode = v; }
    public String getType() { return type; }
    public void setType(String v) { type = v; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String v) { fileUrl = v; }
    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String v) { uploadedBy = v; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long v) { timestamp = v; }
}
