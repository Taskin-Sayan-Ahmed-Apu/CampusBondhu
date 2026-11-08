package com.campusbondhu.data.model;

public class MarketplaceListing {
    private String listingId;
    private String title;
    private String description;
    private double price;
    private String sellerId;
    private String sellerName;
    private String imageUrl;
    private String status; // "AVAILABLE", "SOLD"
    private long timestamp;

    public MarketplaceListing() {}

    public String getListingId() { return listingId; }
    public void setListingId(String v) { listingId = v; }
    public String getTitle() { return title; }
    public void setTitle(String v) { title = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { description = v; }
    public double getPrice() { return price; }
    public void setPrice(double v) { price = v; }
    public String getSellerId() { return sellerId; }
    public void setSellerId(String v) { sellerId = v; }
    public String getSellerName() { return sellerName; }
    public void setSellerName(String v) { sellerName = v; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String v) { imageUrl = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { status = v; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long v) { timestamp = v; }
}
