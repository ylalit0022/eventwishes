package com.ds.eventwishes.ui.home;

public class Template {
    private String id;
    private String title;
    private String category;
    private String htmlContent;
    private String previewUrl;

    // Required empty constructor for Firestore
    public Template() {}

    public Template(String id, String title, String category, String htmlContent, String previewUrl) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.htmlContent = htmlContent;
        this.previewUrl = previewUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getHtmlContent() {
        return htmlContent;
    }

    public void setHtmlContent(String htmlContent) {
        this.htmlContent = htmlContent;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }
}
