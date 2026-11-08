package com.campusbondhu.data.model;

public class Bus {
    private String busId;
    private String busName;
    private String busNumber;
    private String routeName;
    private double latitude;
    private double longitude;
    private long lastUpdated;

    public Bus() {}

    public String getBusId() { return busId; }
    public void setBusId(String v) { busId = v; }
    public String getBusName() { return busName; }
    public void setBusName(String v) { busName = v; }
    public String getBusNumber() { return busNumber; }
    public void setBusNumber(String v) { busNumber = v; }
    public String getRouteName() { return routeName; }
    public void setRouteName(String v) { routeName = v; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double v) { latitude = v; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double v) { longitude = v; }
    public long getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(long v) { lastUpdated = v; }
}
