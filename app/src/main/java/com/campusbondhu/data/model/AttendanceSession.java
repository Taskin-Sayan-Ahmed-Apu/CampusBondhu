package com.campusbondhu.data.model;

public class AttendanceSession {
    private String sessionId;
    private String courseCode;
    private String courseName;
    private String teacherId;
    private String teacherName;
    private long createdAt;
    private long expiresAt;
    private boolean active;
    private int durationMinutes;

    public AttendanceSession() {}

    public String getSessionId() { return sessionId; }
    public void setSessionId(String v) { sessionId = v; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String v) { courseCode = v; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String v) { courseName = v; }
    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String v) { teacherId = v; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String v) { teacherName = v; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long v) { createdAt = v; }
    public long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(long v) { expiresAt = v; }
    public boolean isActive() { return active; }
    public void setActive(boolean v) { active = v; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int v) { durationMinutes = v; }
}
