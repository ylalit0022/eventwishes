package com.ds.eventwishes.api;

import com.google.gson.annotations.SerializedName;

public class ShareRequest {
    @SerializedName("templateId")
    private String templateId;

    @SerializedName("recipientName")
    private String recipientName;

    @SerializedName("senderName")
    private String senderName;

    @SerializedName("htmlContent")
    private String htmlContent;

    public ShareRequest(String templateId, String recipientName, String senderName) {
        this.templateId = templateId;
        this.recipientName = recipientName;
        this.senderName = senderName;
    }

    public ShareRequest(String templateId, String recipientName, String senderName, String htmlContent) {
        this.templateId = templateId;
        this.recipientName = recipientName;
        this.senderName = senderName;
        this.htmlContent = htmlContent;
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

    public String getHtmlContent() {
        return htmlContent;
    }

    public void setHtmlContent(String htmlContent) {
        this.htmlContent = htmlContent;
    }
}
