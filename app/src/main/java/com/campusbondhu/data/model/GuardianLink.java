package com.campusbondhu.data.model;

public class GuardianLink {
    private String linkId;
    private String guardianId;
    private String studentId;

    public GuardianLink() {}

    public String getLinkId() { return linkId; }
    public void setLinkId(String v) { linkId = v; }
    public String getGuardianId() { return guardianId; }
    public void setGuardianId(String v) { guardianId = v; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String v) { studentId = v; }
}
