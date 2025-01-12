package com.ds.eventwishes.ui.home;

public class TemplateItem {
    private int imageResId;
    private String category;
    private String title;

    public TemplateItem(int imageResId, String category, String title) {
        this.imageResId = imageResId;
        this.category = category;
        this.title = title;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getCategory() {
        return category;
    }

    public String getTitle() {
        return title;
    }
}
