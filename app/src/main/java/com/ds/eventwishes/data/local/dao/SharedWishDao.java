package com.ds.eventwishes.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.ds.eventwishes.data.local.entity.CachedSharedWish;
import java.util.List;

@Dao
public interface SharedWishDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CachedSharedWish wish);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CachedSharedWish> wishes);

    @Update
    void update(CachedSharedWish wish);

    @Delete
    void delete(CachedSharedWish wish);

    @Query("DELETE FROM cached_shared_wishes")
    void deleteAll();

    @Query("SELECT * FROM cached_shared_wishes WHERE wishId = :wishId LIMIT 1")
    LiveData<CachedSharedWish> getWishById(String wishId);

    @Query("SELECT * FROM cached_shared_wishes ORDER BY createdAt DESC")
    LiveData<List<CachedSharedWish>> getAllWishes();

    @Query("SELECT * FROM cached_shared_wishes WHERE isViewed = 0 ORDER BY createdAt DESC")
    LiveData<List<CachedSharedWish>> getUnviewedWishes();

    @Query("UPDATE cached_shared_wishes SET isViewed = 1 WHERE wishId = :wishId")
    void markAsViewed(String wishId);

    @Query("DELETE FROM cached_shared_wishes WHERE expiresAt < :currentTime")
    void deleteExpiredWishes(long currentTime);
}
