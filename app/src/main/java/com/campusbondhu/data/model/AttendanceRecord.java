package com.campusbondhu.data.model;

public class AttendanceRecord {
    private String recordId;
    private String sessionId;
    private String courseCode;
    private String studentId;
    private String studentName;
    private long timestamp;
    private boolean faceVerified;

    public AttendanceRecord() {}

    public String getRecordId() { return recordId; }
    public void setRecordId(String v) { recordId = v; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String v) { sessionId = v; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String v) { courseCode = v; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String v) { studentId = v; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String v) { studentName = v; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long v) { timestamp = v; }
    public boolean isFaceVerified() { return faceVerified; }
    public void setFaceVerified(boolean v) { faceVerified = v; }
}
