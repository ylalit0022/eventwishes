package com.ds.eventwishes.db;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.ds.eventwishes.models.WishHistoryItem;
import java.util.List;

@Dao
public interface WishHistoryDao {
    @Query("SELECT * FROM wish_history ORDER BY dateShared DESC")
    LiveData<List<WishHistoryItem>> getAllWishes();
    
    @Query("SELECT * FROM wish_history WHERE id = :id LIMIT 1")
    WishHistoryItem getWishById(long id);
    
    @Query("SELECT * FROM wish_history WHERE wishTitle LIKE :searchQuery OR tags LIKE :searchQuery ORDER BY dateShared DESC")
    LiveData<List<WishHistoryItem>> searchWishes(String searchQuery);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(WishHistoryItem wishHistoryItem);
    
    @Delete
    void delete(WishHistoryItem wishHistoryItem);
    
    @Query("DELETE FROM wish_history")
    void deleteAll();
}
