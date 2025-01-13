package com.ds.eventwishes.db;

import androidx.room.*;
import java.util.List;

@Dao
public interface TemplateDao {
    @Query("SELECT * FROM cached_templates ORDER BY timestamp DESC")
    List<CachedTemplate> getAllTemplates();

    @Query("SELECT * FROM cached_templates WHERE id = :templateId")
    CachedTemplate getTemplateById(String templateId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTemplate(CachedTemplate template);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTemplates(List<CachedTemplate> templates);

    @Delete
    void deleteTemplate(CachedTemplate template);

    @Query("DELETE FROM cached_templates WHERE timestamp < :timestamp")
    void deleteOldTemplates(long timestamp);

    @Query("SELECT * FROM cached_templates WHERE category = :category")
    List<CachedTemplate> getTemplatesByCategory(String category);
}
