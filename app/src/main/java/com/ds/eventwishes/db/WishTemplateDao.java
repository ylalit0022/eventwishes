package com.ds.eventwishes.db;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.ds.eventwishes.models.WishTemplate;
import java.util.List;

@Dao
public interface WishTemplateDao {
    @Query("SELECT * FROM wish_templates ORDER BY title ASC")
    LiveData<List<WishTemplate>> getAllTemplates();

    @Query("SELECT * FROM wish_templates WHERE category = :category ORDER BY title ASC")
    LiveData<List<WishTemplate>> getTemplatesByCategory(String category);

    @Query("SELECT * FROM wish_templates WHERE isFavorite = 1 ORDER BY title ASC")
    LiveData<List<WishTemplate>> getFavoriteTemplates();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(WishTemplate template);

    @Update
    void update(WishTemplate template);

    @Delete
    void delete(WishTemplate template);

    @Query("DELETE FROM wish_templates")
    void deleteAll();
}
