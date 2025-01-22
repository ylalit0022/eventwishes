package com.ds.eventwishes.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.ds.eventwishes.models.WishTemplate;
import com.ds.eventwishes.models.WishHistoryItem;
import com.ds.eventwishes.utils.DateConverter;

@Database(
    entities = {
        WishTemplate.class,
        WishHistoryItem.class
    },
    version = 2,
    exportSchema = false
)
@TypeConverters(DateConverter.class)
public abstract class WishDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "wish_database";
    private static volatile WishDatabase instance;

    public abstract WishTemplateDao wishTemplateDao();
    public abstract WishHistoryDao wishHistoryDao();

    public static synchronized WishDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                context.getApplicationContext(),
                WishDatabase.class,
                DATABASE_NAME
            )
            .fallbackToDestructiveMigration()
            .build();
        }
        return instance;
    }
}
