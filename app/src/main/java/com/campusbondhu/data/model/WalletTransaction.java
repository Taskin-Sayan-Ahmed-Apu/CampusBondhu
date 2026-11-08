package com.campusbondhu.data.model;

public class WalletTransaction {
    private String txnId;
    private String uid;
    private double amount;
    private String type; // "CREDIT" or "DEBIT"
    private String description;
    private long timestamp;

    public WalletTransaction() {}

    public String getTxnId() { return txnId; }
    public void setTxnId(String v) { txnId = v; }
    public String getUid() { return uid; }
    public void setUid(String v) { uid = v; }
    public double getAmount() { return amount; }
    public void setAmount(double v) { amount = v; }
    public String getType() { return type; }
    public void setType(String v) { type = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { description = v; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long v) { timestamp = v; }
}
