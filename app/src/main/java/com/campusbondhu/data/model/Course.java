package com.campusbondhu.data.model;

public class Course {
    private String courseId;
    private String code;
    private String name;
    private double credit;

    public Course() {}

    public String getCourseId() { return courseId; }
    public void setCourseId(String v) { courseId = v; }
    public String getCode() { return code; }
    public void setCode(String v) { code = v; }
    public String getName() { return name; }
    public void setName(String v) { name = v; }
    public double getCredit() { return credit; }
    public void setCredit(double v) { credit = v; }
}
