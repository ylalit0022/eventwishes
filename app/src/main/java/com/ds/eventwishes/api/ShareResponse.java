package com.ds.eventwishes.api;

import com.google.gson.annotations.SerializedName;

public class ShareResponse {
    @SerializedName("shareUrl")
    private String shareUrl;

    @SerializedName("shortUrl")
    private String shortUrl;

    @SerializedName("previewContent")
    private String previewContent;

    @SerializedName("socialPreviewHtml")
    private String socialPreviewHtml;

    // Getters and Setters
    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }

    public String getPreviewContent() {
        return previewContent;
    }

    public void setPreviewContent(String previewContent) {
        this.previewContent = previewContent;
    }

    public String getSocialPreviewHtml() {
        return socialPreviewHtml;
    }

    public void setSocialPreviewHtml(String socialPreviewHtml) {
        this.socialPreviewHtml = socialPreviewHtml;
    }
}
