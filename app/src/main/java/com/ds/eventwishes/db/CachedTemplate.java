package com.ds.eventwishes.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "cached_templates")
public class CachedTemplate {
    @PrimaryKey
    @NonNull
    private String id;
    private String title;
    private String category;
    private String htmlContent;
    private String previewUrl;
    private long timestamp;

    public CachedTemplate(@NonNull String id, String title, String category,
                         String htmlContent, String previewUrl) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.htmlContent = htmlContent;
        this.previewUrl = previewUrl;
        this.timestamp = System.currentTimeMillis();
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getHtmlContent() { return htmlContent; }
    public void setHtmlContent(String htmlContent) { this.htmlContent = htmlContent; }

    public String getPreviewUrl() { return previewUrl; }
    public void setPreviewUrl(String previewUrl) { this.previewUrl = previewUrl; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
