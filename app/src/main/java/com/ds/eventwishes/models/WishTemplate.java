package com.ds.eventwishes.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "wish_templates")
public class WishTemplate {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String title;
    private String description;
    private String htmlContent;
    private String previewImageUrl;
    private String category;
    private boolean isFavorite;

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getHtmlContent() { return htmlContent; }
    public void setHtmlContent(String htmlContent) { this.htmlContent = htmlContent; }

    public String getPreviewImageUrl() { return previewImageUrl; }
    public void setPreviewImageUrl(String previewImageUrl) { this.previewImageUrl = previewImageUrl; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
}
