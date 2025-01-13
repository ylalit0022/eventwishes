package com.ds.eventwishes.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.ds.eventwishes.data.local.Converters;
import com.ds.eventwishes.data.local.dao.SharedWishDao;
import com.ds.eventwishes.data.local.entity.CachedSharedWish;

@Database(
    entities = {
        CachedTemplate.class,
        CachedSharedWish.class
    },
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters.class)
public abstract class WishDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "wish_database";
    private static volatile WishDatabase instance;

    public abstract TemplateDao templateDao();
    public abstract SharedWishDao sharedWishDao();

    public static WishDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (WishDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            WishDatabase.class,
                            DATABASE_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}
