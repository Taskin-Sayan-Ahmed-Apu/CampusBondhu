package com.campusbondhu.data.model;

public class FoodOrder {
    private String orderId;
    private String uid;
    private String studentName;
    private String itemId;
    private String itemName;
    private int quantity;
    private double totalAmount;
    private String pickupCode;
    private String status; // PENDING, COLLECTED
    private long timestamp;

    public FoodOrder() {}

    public String getOrderId() { return orderId; }
    public void setOrderId(String v) { orderId = v; }
    public String getUid() { return uid; }
    public void setUid(String v) { uid = v; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String v) { studentName = v; }
    public String getItemId() { return itemId; }
    public void setItemId(String v) { itemId = v; }
    public String getItemName() { return itemName; }
    public void setItemName(String v) { itemName = v; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int v) { quantity = v; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double v) { totalAmount = v; }
    public String getPickupCode() { return pickupCode; }
    public void setPickupCode(String v) { pickupCode = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { status = v; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long v) { timestamp = v; }
}
