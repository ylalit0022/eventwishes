package com.ds.eventwishes.api;

import com.google.gson.annotations.SerializedName;

public class SharedWish {
    @SerializedName("id")
    private String id;

    @SerializedName("shortCode")
    private String shortCode;

    @SerializedName("templateId")
    private String templateId;

    @SerializedName("recipientName")
    private String recipientName;

    @SerializedName("senderName")
    private String senderName;

    @SerializedName("customizedHtml")
    private String customizedHtml;

    @SerializedName("views")
    private int views;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("lastViewedAt")
    private String lastViewedAt;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getCustomizedHtml() {
        return customizedHtml;
    }

    public void setCustomizedHtml(String customizedHtml) {
        this.customizedHtml = customizedHtml;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getLastViewedAt() {
        return lastViewedAt;
    }

    public void setLastViewedAt(String lastViewedAt) {
        this.lastViewedAt = lastViewedAt;
    }
}
