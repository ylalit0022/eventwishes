package com.ds.eventwishes.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.util.Log;
import com.ds.eventwishes.R;
import com.ds.eventwishes.api.ApiClient;
import com.ds.eventwishes.api.Template;
import com.ds.eventwishes.api.PaginatedResponse;
import com.ds.eventwishes.model.Category;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeViewModel extends ViewModel {
    private static final int PAGE_SIZE = 20;
    
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final MutableLiveData<List<Template>> templates = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Category> selectedCategory = new MutableLiveData<>();
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> loadingMore = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> hasMore = new MutableLiveData<>(true);
    
    private final MediatorLiveData<List<Template>> filteredTemplates = new MediatorLiveData<>();
    private int currentPage = 1;

    public HomeViewModel() {
        setupFilteredTemplates();
        refreshData();
    }

    private void setupFilteredTemplates() {
        filteredTemplates.addSource(templates, this::filterTemplates);
        filteredTemplates.addSource(selectedCategory, category -> filterTemplates(templates.getValue()));
        filteredTemplates.addSource(searchQuery, query -> filterTemplates(templates.getValue()));
    }

    private void filterTemplates(List<Template> allTemplates) {
        if (allTemplates == null) {
            Log.d("HomeViewModel", "filterTemplates: allTemplates is null");
            filteredTemplates.setValue(new ArrayList<>());
            return;
        }

        Category category = selectedCategory.getValue();
        String query = searchQuery.getValue();
        
        Log.d("HomeViewModel", String.format("Filtering templates: total=%d, category=%s, query=%s", 
            allTemplates.size(), 
            category != null ? category.getId() : "null", 
            query != null ? query : "null"));

        List<Template> filtered = new ArrayList<>();
        for (Template template : allTemplates) {
            if (matchesFilter(template, category, query)) {
                filtered.add(template);
            }
        }
        
        Log.d("HomeViewModel", "Filtered templates: " + filtered.size());
        filteredTemplates.setValue(filtered);
    }

    private boolean matchesFilter(Template template, Category selectedCategory, String query) {
        // If no category is selected or "ALL" is selected, don't filter by category
        boolean categoryMatches = selectedCategory == null || 
                                selectedCategory.getId().equals("ALL") ||
                                (template.getCategory() != null && 
                                 template.getCategory().equalsIgnoreCase(selectedCategory.getId()));

        // If no query, don't filter by search
        boolean queryMatches = query == null || 
                             query.isEmpty() || 
                             (template.getTitle() != null && 
                              template.getTitle().toLowerCase().contains(query.toLowerCase()));

        Log.d("HomeViewModel", String.format("Filter check - template: %s, selectedCategory: %s, templateCategory: %s, categoryMatches: %b, queryMatches: %b",
            template.getTitle(),
            selectedCategory != null ? selectedCategory.getId() : "null",
            template.getCategory(),
            categoryMatches,
            queryMatches));

        return categoryMatches && queryMatches;
    }

    private void loadCategories(List<Template> templates) {
        if (templates == null) {
            Log.d("HomeViewModel", "loadCategories: templates list is null");
            return;
        }

        Log.d("HomeViewModel", "Loading categories:");
        
        // Count templates per category
        Map<String, Integer> categoryCounts = new HashMap<>();
        int totalCount = 0;

        for (Template template : templates) {
            String category = template.getCategory();
            if (category != null) {
                categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);
                totalCount++;
            }
        }

        List<Category> dynamicCategories = new ArrayList<>();
        
        // Add "All" category first
        Category allCategory = new Category("ALL", "All", R.drawable.ic_other, totalCount);
        dynamicCategories.add(allCategory);
        Log.d("HomeViewModel", String.format("Category - id: %s, name: %s, count: %d", 
            allCategory.getId(), allCategory.getName(), allCategory.getCount()));

        // Add other categories sorted by count (descending)
        categoryCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEach(entry -> {
                String categoryId = entry.getKey();
                int count = entry.getValue();
                int iconResId = getCategoryIcon(categoryId);
                Category category = new Category(categoryId, categoryId, iconResId, count);
                dynamicCategories.add(category);
                Log.d("HomeViewModel", String.format("Category - id: %s, name: %s, count: %d", 
                    category.getId(), category.getName(), category.getCount()));
            });

        categories.setValue(dynamicCategories);
        
        // If no category is selected yet, select "All"
        if (selectedCategory.getValue() == null) {
            selectedCategory.setValue(allCategory);
        }
    }

    private int getCategoryIcon(String category) {
        if (category == null) return R.drawable.ic_other;
        
        switch (category.toUpperCase()) {
            case "BIRTHDAY":
                return R.drawable.ic_birthday;
            case "WEDDING":
                return R.drawable.ic_wedding;
            case "ANNIVERSARY":
                return R.drawable.ic_anniversary;
            case "GRADUATION":
                return R.drawable.ic_graduation;
            case "HOLI":
                return R.drawable.ic_holi;
            default:
                return R.drawable.ic_other;
        }
    }

    public void refreshData() {
        if (loading.getValue()) return;
        
        loading.setValue(true);
        error.setValue(null);
        currentPage = 1;
        
        ApiClient.getInstance().getTemplates(currentPage, PAGE_SIZE).enqueue(new Callback<PaginatedResponse<Template>>() {
            @Override
            public void onResponse(Call<PaginatedResponse<Template>> call, Response<PaginatedResponse<Template>> response) {
                loading.setValue(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    PaginatedResponse<Template> paginatedResponse = response.body();
                    templates.setValue(new ArrayList<>(paginatedResponse.getData()));
                    hasMore.setValue(paginatedResponse.getCurrentPage() < paginatedResponse.getTotalPages());
                    loadCategories(paginatedResponse.getData());
                } else {
                    String errorMsg = "Failed to load templates. Please try again.";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e("HomeViewModel", "Error reading error body", e);
                    }
                    error.setValue(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<PaginatedResponse<Template>> call, Throwable t) {
                loading.setValue(false);
                error.setValue("Network error: " + t.getMessage() + "\nPlease check your connection and try again.");
                Log.e("HomeViewModel", "API call failed", t);
            }
        });
    }

    public void loadNextPage() {
        if (loadingMore.getValue() || !hasMore.getValue()) return;
        
        loadingMore.setValue(true);
        currentPage++;
        
        ApiClient.getInstance().getTemplates(currentPage, PAGE_SIZE).enqueue(new Callback<PaginatedResponse<Template>>() {
            @Override
            public void onResponse(Call<PaginatedResponse<Template>> call, Response<PaginatedResponse<Template>> response) {
                loadingMore.setValue(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    PaginatedResponse<Template> paginatedResponse = response.body();
                    List<Template> currentTemplates = new ArrayList<>(templates.getValue());
                    currentTemplates.addAll(paginatedResponse.getData());
                    templates.setValue(currentTemplates);
                    hasMore.setValue(paginatedResponse.getCurrentPage() < paginatedResponse.getTotalPages());
                } else {
                    currentPage--; // Revert page increment on error
                    String errorMsg = "Failed to load more templates";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e("HomeViewModel", "Error reading error body", e);
                    }
                    error.setValue(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<PaginatedResponse<Template>> call, Throwable t) {
                loadingMore.setValue(false);
                currentPage--; // Revert page increment on error
                error.setValue("Network error while loading more templates: " + t.getMessage());
                Log.e("HomeViewModel", "API call failed", t);
            }
        });
    }

    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    public LiveData<List<Template>> getFilteredTemplates() {
        return filteredTemplates;
    }

    public LiveData<Boolean> isLoading() {
        return loading;
    }

    public LiveData<Boolean> isLoadingMore() {
        return loadingMore;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> hasMore() {
        return hasMore;
    }

    public void setSelectedCategory(Category category) {
        selectedCategory.setValue(category);
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }
}
