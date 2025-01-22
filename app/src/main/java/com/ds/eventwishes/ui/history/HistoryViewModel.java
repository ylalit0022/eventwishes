package com.ds.eventwishes.ui.history;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.ds.eventwishes.db.WishDatabase;
import com.ds.eventwishes.db.WishHistoryDao;
import com.ds.eventwishes.models.WishHistoryItem;
import android.os.Handler;
import android.os.Looper;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistoryViewModel extends AndroidViewModel {
    private final WishHistoryDao wishHistoryDao;
    private final ExecutorService executorService;
    private final Handler mainHandler;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final LiveData<List<WishHistoryItem>> wishHistory;

    public HistoryViewModel(Application application) {
        super(application);
        wishHistoryDao = WishDatabase.getInstance(application).wishHistoryDao();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        
        // Transform searchQuery into wishHistory
        wishHistory = Transformations.switchMap(searchQuery, query -> {
            if (query == null || query.isEmpty()) {
                return wishHistoryDao.getAllWishes();
            } else {
                return wishHistoryDao.searchWishes(query);
            }
        });
    }

    public LiveData<List<WishHistoryItem>> getWishHistory() {
        return wishHistory;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public void searchWishes(String query) {
        isLoading.setValue(true);
        searchQuery.setValue(query);
        isLoading.setValue(false);
    }

    public void refreshWishes() {
        isLoading.setValue(true);
        searchQuery.setValue("");
        isLoading.setValue(false);
    }

    public void clearHistory() {
        isLoading.postValue(true);
        executorService.execute(() -> {
            wishHistoryDao.deleteAll();
            isLoading.postValue(false);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
