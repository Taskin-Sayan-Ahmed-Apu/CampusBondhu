package com.campusbondhu.data.model;

public class EmergencyAlert {
    private String alertId;
    private String studentId;
    private String studentName;
    private double latitude;
    private double longitude;
    private long timestamp;
    private String status; // "ACTIVE", "RESOLVED"

    public EmergencyAlert() {}

    public String getAlertId() { return alertId; }
    public void setAlertId(String v) { alertId = v; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String v) { studentId = v; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String v) { studentName = v; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double v) { latitude = v; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double v) { longitude = v; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long v) { timestamp = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { status = v; }
}
