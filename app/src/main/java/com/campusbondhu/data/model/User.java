package com.campusbondhu.data.model;

public class User {
    private String uid, name, email, role, studentId, department, phone;
    private String profileImageUrl, faceImageUrl;
    private boolean active;
    private long createdAt;

    public User() {}

    public String getUid() { return uid; }
    public void setUid(String v) { uid = v; }
    public String getName() { return name; }
    public void setName(String v) { name = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { email = v; }
    public String getRole() { return role; }
    public void setRole(String v) { role = v; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String v) { studentId = v; }
    public String getDepartment() { return department; }
    public void setDepartment(String v) { department = v; }
    public String getPhone() { return phone; }
    public void setPhone(String v) { phone = v; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String v) { profileImageUrl = v; }
    public String getFaceImageUrl() { return faceImageUrl; }
    public void setFaceImageUrl(String v) { faceImageUrl = v; }
    public boolean isActive() { return active; }
    public void setActive(boolean v) { active = v; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long v) { createdAt = v; }
}
