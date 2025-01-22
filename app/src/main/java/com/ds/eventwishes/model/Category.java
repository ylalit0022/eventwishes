package com.ds.eventwishes.model;

import androidx.annotation.DrawableRes;

public class Category {
    private String id;
    private String name;
    @DrawableRes
    private int iconResId;
    private int wishCount;

    public Category(String id, String name, @DrawableRes int iconResId, int wishCount) {
        this.id = id;
        this.name = name;
        this.iconResId = iconResId;
        this.wishCount = wishCount;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public @DrawableRes int getIconResId() {
        return iconResId;
    }

    public int getWishCount() {
        return wishCount;
    }

    public void setWishCount(int wishCount) {
        this.wishCount = wishCount;
    }
}
