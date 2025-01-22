package com.ds.eventwishes.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.ds.eventwishes.utils.DateConverter;
import java.util.Date;

@Entity(tableName = "wish_history")
public class WishHistoryItem {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String wishTitle;
    private String description;
    private String shortCode;
    private String shareUrl;
    private String recipientName;
    private String senderName;
    
    @TypeConverters(DateConverter.class)
    private Date dateShared;
    
    private String tags;
    private String previewImageUrl;

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public String getWishTitle() { return wishTitle; }
    public void setWishTitle(String wishTitle) { this.wishTitle = wishTitle; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }
    
    public String getShareUrl() { return shareUrl; }
    public void setShareUrl(String shareUrl) { this.shareUrl = shareUrl; }
    
    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    
    public Date getDateShared() { return dateShared; }
    public void setDateShared(Date dateShared) { this.dateShared = dateShared; }
    
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    
    public String getPreviewImageUrl() { return previewImageUrl; }
    public void setPreviewImageUrl(String previewImageUrl) { this.previewImageUrl = previewImageUrl; }
}
