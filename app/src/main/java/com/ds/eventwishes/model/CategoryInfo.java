package com.ds.eventwishes.model;

import com.google.gson.annotations.SerializedName;

public class CategoryInfo {
    @SerializedName("count")
    private int count;

    @SerializedName("icon")
    private String iconUrl;  // Changed to store URL instead of resource name

    public CategoryInfo() {
        // Default constructor
    }

    public CategoryInfo(int count, String iconUrl) {
        this.count = count;
        this.iconUrl = iconUrl;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
}
