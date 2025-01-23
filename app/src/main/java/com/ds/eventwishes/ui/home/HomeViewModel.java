package com.ds.eventwishes.ui.home;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.ds.eventwishes.api.ApiResponse;
import com.ds.eventwishes.api.PaginatedResponse;
import com.ds.eventwishes.api.RetrofitClient;
import com.ds.eventwishes.model.Category;
import com.ds.eventwishes.model.CategoryInfo;
import com.ds.eventwishes.model.Template;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeViewModel extends ViewModel {
    private static final String TAG = "HomeViewModel";
    private static final int PAGE_SIZE = 20;
    
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Category> selectedCategory = new MutableLiveData<>();
    private final MutableLiveData<List<Template>> templates = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> hasMore = new MutableLiveData<>(true);
    private final MediatorLiveData<List<Template>> filteredTemplates = new MediatorLiveData<>();
    private int currentPage = 1;

    public HomeViewModel() {
        setupFilteredTemplates();
    }

    private void setupFilteredTemplates() {
        filteredTemplates.addSource(templates, this::filterTemplates);
        filteredTemplates.addSource(selectedCategory, category -> filterTemplates(templates.getValue()));
    }

    private void filterTemplates(List<Template> allTemplates) {
        if (allTemplates == null) {
            Log.d("HomeViewModel", "filterTemplates: allTemplates is null");
            filteredTemplates.setValue(new ArrayList<>());
            return;
        }

        Category category = selectedCategory.getValue();
        String query = null; // Removed search query
        
        Log.d("HomeViewModel", String.format("Filtering templates: total=%d, category=%s, query=%s", 
            allTemplates.size(), 
            category != null ? category.getId() + " (name: " + category.getName() + ")" : "null", 
            query != null ? query : "null"));

        // Don't filter if no category 
        if (category == null || category.getId().equalsIgnoreCase("all")) {
            Log.d("HomeViewModel", "No filters applied, showing all templates");
            filteredTemplates.setValue(allTemplates);
            return;
        }

        List<Template> filtered = new ArrayList<>();
        for (Template template : allTemplates) {
            if (matchesFilter(template, category, query)) {
                filtered.add(template);
            }
        }
        
        Log.d("HomeViewModel", String.format("Filtered templates: %d (from %d total)", 
            filtered.size(), allTemplates.size()));
        filteredTemplates.setValue(filtered);
    }

    private boolean matchesFilter(Template template, Category selectedCategory, String query) {
        if (template == null) {
            Log.d("HomeViewModel", "matchesFilter: template is null");
            return false;
        }

        // Category filter
        boolean categoryMatches = selectedCategory == null || 
                                selectedCategory.getId().equalsIgnoreCase("all") ||
                                (template.getCategory() != null && 
                                 normalizeCategory(template.getCategory()).equals(selectedCategory.getId()));

        // Search filter
        boolean queryMatches = query == null || 
                             query.isEmpty() || 
                             (template.getTitle() != null && 
                              template.getTitle().toLowerCase().contains(query.toLowerCase()));

        Log.d("HomeViewModel", String.format("Filter check - template: %s, category: %s, templateCategory: %s, normalizedCategory: %s, categoryMatches: %b, queryMatches: %b",
            template.getTitle(),
            selectedCategory != null ? selectedCategory.getId() : "null",
            template.getCategory(),
            template.getCategory() != null ? normalizeCategory(template.getCategory()) : "null",
            categoryMatches,
            queryMatches));

        return categoryMatches && queryMatches;
    }

    private String normalizeCategory(String category) {
        return category.toLowerCase().trim().replaceAll("\\s+", "_");
    }

    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    public LiveData<Category> getSelectedCategory() {
        return selectedCategory;
    }

    public LiveData<List<Template>> getTemplates() {
        return templates;
    }

    public LiveData<List<Template>> getFilteredTemplates() {
        return filteredTemplates;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> hasMore() {
        return hasMore;
    }

    public void loadTemplates(boolean refresh) {
        if (refresh) {
            currentPage = 1;
            hasMore.setValue(true);
            templates.setValue(new ArrayList<>());
        }

        if (!hasMore.getValue() || isLoading.getValue() == Boolean.TRUE) {
            return;
        }

        isLoading.setValue(true);
        Category category = selectedCategory.getValue();
        String categoryParam = category != null && !"all".equalsIgnoreCase(category.getId()) ? category.getId() : null;

        RetrofitClient.getApiService().getTemplates(currentPage, PAGE_SIZE, categoryParam)
            .enqueue(new Callback<ApiResponse<PaginatedResponse<Template>>>() {
                @Override
                public void onResponse(Call<ApiResponse<PaginatedResponse<Template>>> call,
                                   Response<ApiResponse<PaginatedResponse<Template>>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        PaginatedResponse<Template> paginatedResponse = response.body().getData();
                        if (paginatedResponse != null) {
                            List<Template> currentTemplates = templates.getValue();
                            if (currentTemplates == null) {
                                currentTemplates = new ArrayList<>();
                            }
                            if (refresh) {
                                currentTemplates.clear();
                            }
                            currentTemplates.addAll(paginatedResponse.getData());
                            templates.setValue(currentTemplates);

                            hasMore.setValue(paginatedResponse.isHasMore());
                            if (hasMore.getValue()) {
                                currentPage++;
                            }

                            loadCategories(paginatedResponse);
                        }
                    } else {
                        error.setValue("Failed to load templates");
                    }
                    isLoading.setValue(false);
                }

                @Override
                public void onFailure(Call<ApiResponse<PaginatedResponse<Template>>> call, Throwable t) {
                    Log.e(TAG, "Error loading templates", t);
                    error.setValue("Network error: " + t.getMessage());
                    isLoading.setValue(false);
                }
            });
    }

    private void loadCategories(PaginatedResponse<Template> response) {
        if (response == null) {
            Log.d(TAG, "loadCategories: response is null");
            return;
        }

        Map<String, CategoryInfo> categoryMap = response.getCategories();
        if (categoryMap == null) {
            Log.d(TAG, "loadCategories: categories map is null");
            return;
        }

        Log.d(TAG, "Raw categories from server: " + categoryMap);
        Log.d(TAG, "Total templates: " + response.getTotalTemplates());

        // Keep existing categories if we have them
        List<Category> currentCategories = categories.getValue();
        if (currentCategories == null) {
            currentCategories = new ArrayList<>();
        }

        // Update counts for existing categories and add new ones
        Map<String, Category> existingCategoryMap = new HashMap<>();
        for (Category existingCategory : currentCategories) {
            existingCategoryMap.put(existingCategory.getId(), existingCategory);
        }

        List<Category> categoryList = new ArrayList<>();
        for (Map.Entry<String, CategoryInfo> entry : categoryMap.entrySet()) {
            String categoryName = entry.getKey();
            CategoryInfo info = entry.getValue();
            
            if (categoryName != null && !categoryName.trim().isEmpty()) {
                // Use the original category name for display
                String displayName = Arrays.stream(categoryName.split("\\s+"))
                    .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                    .collect(Collectors.joining(" "));
                
                // Normalize category ID (lowercase with underscores)
                String normalizedId = normalizeCategory(categoryName);
                
                // Check if we already have this category
                Category existingCategory = existingCategoryMap.get(normalizedId);
                if (existingCategory != null) {
                    // Update count and icon URL but keep other properties
                    existingCategory.setCount(info.getCount());
                    existingCategory.setIconUrl(info.getIconUrl());
                    categoryList.add(existingCategory);
                } else {
                    // Create new category with icon URL from server
                    Category category = new Category(
                        normalizedId,      // id (normalized)
                        displayName,       // name (properly capitalized)
                        info.getIconUrl(), // icon URL from server
                        info.getCount()    // count
                    );
                    categoryList.add(category);
                }
                
                Log.d(TAG, String.format("Category: id=%s, name=%s, count=%d, iconUrl=%s", 
                    normalizedId, displayName, info.getCount(), info.getIconUrl()));
            }
        }

        // Sort categories by count (descending) and then by name
        Collections.sort(categoryList, (a, b) -> {
            int countCompare = Integer.compare(b.getCount(), a.getCount());
            return countCompare != 0 ? countCompare : a.getName().compareTo(b.getName());
        });

        // Add "All" category at the beginning with total count
        Category allCategory = existingCategoryMap.get("all");
        if (allCategory == null) {
            allCategory = new Category(
                "all", 
                "All", 
                "https://raw.githubusercontent.com/ylalit0022/eventwishes/main/assets/icons/ic_all.png",
                response.getTotalTemplates()
            );
        } else {
            allCategory.setCount(response.getTotalTemplates());
        }
        
        // Always add "All" category at the beginning
        categoryList.add(0, allCategory);
        Log.d(TAG, String.format("Added ALL category with total count: %d", allCategory.getCount()));

        // Only update categories if we have data
        categories.setValue(categoryList);
        
        // If no category is selected yet, select "All"
        if (selectedCategory.getValue() == null) {
            Log.d(TAG, "No category selected, selecting ALL");
            selectedCategory.setValue(allCategory);
        }
    }

    public void selectCategory(Category category) {
        if (category != null && !category.equals(selectedCategory.getValue())) {
            selectedCategory.setValue(category);
            loadTemplates(true);
        }
    }

    public void loadMore() {
        loadTemplates(false);
    }

    public void refresh() {
        loadTemplates(true);
    }
}
