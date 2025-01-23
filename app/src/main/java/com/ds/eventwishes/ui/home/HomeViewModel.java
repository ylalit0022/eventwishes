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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.stream.Collectors;

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
            category != null ? category.getId() + " (name: " + category.getName() + ")" : "null", 
            query != null ? query : "null"));

        // Don't filter if no category or search query
        if ((category == null || category.getId().equalsIgnoreCase("all")) && 
            (query == null || query.isEmpty())) {
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
        if (category == null) return "";
        // Convert to lowercase and replace spaces with underscores
        return category.trim().toLowerCase().replaceAll("\\s+", "_");
    }

    private void loadCategories(PaginatedResponse<Template> response) {
        if (response == null || response.getCategories() == null) {
            Log.d("HomeViewModel", "loadCategories: response or categories is null");
            categories.setValue(new ArrayList<>());
            return;
        }

        Map<String, Integer> categoryCountMap = response.getCategories();
        Log.d("HomeViewModel", "Raw categories from server: " + categoryCountMap.toString());

        List<Category> categoryList = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : categoryCountMap.entrySet()) {
            String categoryId = entry.getKey();
            if (categoryId != null && !categoryId.trim().isEmpty()) {
                // Normalize category ID (lowercase with underscores)
                String normalizedId = normalizeCategory(categoryId);
                // Format display name (capitalize each word)
                String displayName = Arrays.stream(categoryId.split("\\s+"))
                    .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                    .collect(Collectors.joining(" "));
                
                Category category = new Category(
                    normalizedId,  // id (normalized)
                    displayName,   // name (properly capitalized)
                    getCategoryIcon(normalizedId),  // iconResId
                    entry.getValue()  // count
                );
                categoryList.add(category);
                Log.d("HomeViewModel", String.format("Added category: id=%s, name=%s, count=%d", 
                    category.getId(), category.getName(), category.getCount()));
            }
        }

        // Sort categories by count (descending) and then by name
        Collections.sort(categoryList, (a, b) -> {
            int countCompare = Integer.compare(b.getCount(), a.getCount());
            return countCompare != 0 ? countCompare : a.getName().compareTo(b.getName());
        });

        // Add "All" category at the beginning with total count
        int totalCount = categoryList.stream().mapToInt(Category::getCount).sum();
        Category allCategory = new Category("all", "All", R.drawable.ic_other, totalCount);
        categoryList.add(0, allCategory);
        Log.d("HomeViewModel", String.format("Added ALL category with total count: %d", totalCount));

        categories.setValue(categoryList);
        
        // If no category is selected yet, select "All"
        if (selectedCategory.getValue() == null) {
            Log.d("HomeViewModel", "No category selected, selecting ALL");
            selectedCategory.setValue(allCategory);
        }
    }

    private int getCategoryIcon(String category) {
        // For now, use ic_other for all categories until we have proper icons
        return R.drawable.ic_other;
    }

    public void refreshData() {
        if (loading.getValue()) return;
        
        loading.setValue(true);
        error.setValue(null);
        currentPage = 1;
        
        // Get the current category
        Category currentCategory = selectedCategory.getValue();
        String categoryParam = (currentCategory != null && !currentCategory.getId().equalsIgnoreCase("all")) 
            ? currentCategory.getId() 
            : null;
            
        Log.d("HomeViewModel", String.format("Refreshing data with category: %s", categoryParam));
        
        ApiClient.getInstance().getTemplates(currentPage, PAGE_SIZE, categoryParam).enqueue(new Callback<PaginatedResponse<Template>>() {
            @Override
            public void onResponse(Call<PaginatedResponse<Template>> call, Response<PaginatedResponse<Template>> response) {
                try {
                    loading.setValue(false);
                    
                    if (response.isSuccessful() && response.body() != null) {
                        PaginatedResponse<Template> paginatedResponse = response.body();
                        templates.setValue(new ArrayList<>(paginatedResponse.getData()));
                        hasMore.setValue(paginatedResponse.getCurrentPage() < paginatedResponse.getTotalPages());
                        loadCategories(paginatedResponse);
                        
                        Log.d("HomeViewModel", String.format("Loaded %d templates for category: %s", 
                            paginatedResponse.getData().size(), categoryParam));
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
                } catch (Exception e) {
                    Log.e("HomeViewModel", "Error in onResponse", e);
                    error.setValue("An unexpected error occurred: " + e.getMessage());
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
        
        // Get the current category
        Category currentCategory = selectedCategory.getValue();
        String categoryParam = (currentCategory != null && !currentCategory.getId().equalsIgnoreCase("all")) 
            ? currentCategory.getId() 
            : null;
            
        Log.d("HomeViewModel", String.format("Loading next page with category: %s", categoryParam));
        
        ApiClient.getInstance().getTemplates(currentPage, PAGE_SIZE, categoryParam).enqueue(new Callback<PaginatedResponse<Template>>() {
            @Override
            public void onResponse(Call<PaginatedResponse<Template>> call, Response<PaginatedResponse<Template>> response) {
                try {
                    loadingMore.setValue(false);
                    
                    if (response.isSuccessful() && response.body() != null) {
                        PaginatedResponse<Template> paginatedResponse = response.body();
                        List<Template> currentTemplates = new ArrayList<>(templates.getValue());
                        currentTemplates.addAll(paginatedResponse.getData());
                        templates.setValue(currentTemplates);
                        hasMore.setValue(paginatedResponse.getCurrentPage() < paginatedResponse.getTotalPages());
                        
                        Log.d("HomeViewModel", String.format("Loaded %d more templates for category: %s", 
                            paginatedResponse.getData().size(), categoryParam));
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
                } catch (Exception e) {
                    currentPage--; // Revert page increment on error
                    loadingMore.setValue(false);
                    error.setValue("An unexpected error occurred: " + e.getMessage());
                    Log.e("HomeViewModel", "Error in onResponse", e);
                }
            }

            @Override
            public void onFailure(Call<PaginatedResponse<Template>> call, Throwable t) {
                currentPage--; // Revert page increment on error
                loadingMore.setValue(false);
                error.setValue("Network error: " + t.getMessage());
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
        Log.d("HomeViewModel", String.format("Setting selected category: id=%s, name=%s", 
            category != null ? category.getId() : "null",
            category != null ? category.getName() : "null"));

        if (category == null || category.equals(selectedCategory.getValue())) {
            Log.d("HomeViewModel", "Category unchanged or null, skipping refresh");
            return;
        }

        selectedCategory.setValue(category);

        // Reset and reload when category changes
        Log.d("HomeViewModel", "Category changed, resetting and reloading data");
        templates.setValue(new ArrayList<>());
        currentPage = 1;
        refreshData();
    }

    public void setSearchQuery(String query) {
        if (query == null || query.equals(searchQuery.getValue())) return;
        searchQuery.setValue(query);
        // Reset and reload when search query changes
        templates.setValue(new ArrayList<>());
        currentPage = 1;
        refreshData();
    }
}
