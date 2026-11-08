package com.campusbondhu.data.model;

public class CanteenItem {
    private String itemId;
    private String name;
    private double price;
    private String description;
    private boolean available;

    public CanteenItem() {}

    public String getItemId() { return itemId; }
    public void setItemId(String v) { itemId = v; }
    public String getName() { return name; }
    public void setName(String v) { name = v; }
    public double getPrice() { return price; }
    public void setPrice(double v) { price = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { description = v; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean v) { available = v; }
}
