package com.ds.eventwishes.api;

import com.google.gson.annotations.SerializedName;

public class ShareResponse {
    @SerializedName("shareUrl")
    private String shareUrl;

    @SerializedName("shortUrl")
    private String shortUrl;

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
}
