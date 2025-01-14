package com.ds.eventwishes.api;

import com.google.gson.annotations.SerializedName;

public class ShareRequest {
    @SerializedName("templateId")
    private String templateId;

    @SerializedName("recipientName")
    private String recipientName;

    @SerializedName("senderName")
    private String senderName;

    public ShareRequest(String templateId, String recipientName, String senderName) {
        this.templateId = templateId;
        this.recipientName = recipientName;
        this.senderName = senderName;
    }

    // Getters and Setters
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
}
