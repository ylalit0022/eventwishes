package com.ds.eventwishes.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.util.Log;
import com.ds.eventwishes.R;
import com.ds.eventwishes.api.ApiClient;
import com.ds.eventwishes.api.Template;
import com.ds.eventwishes.model.Category;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeViewModel extends ViewModel {
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final MutableLiveData<List<Template>> templates = new MutableLiveData<>();
    private final MutableLiveData<Category> selectedCategory = new MutableLiveData<>();
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    
    private final MediatorLiveData<List<Template>> filteredTemplates = new MediatorLiveData<>();

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

    private void loadCategories(List<Template> templates) {
        Set<String> uniqueCategories = new HashSet<>();
        for (Template template : templates) {
            uniqueCategories.add(template.getCategory());
        }

        List<Category> dynamicCategories = new ArrayList<>();
        // Add "All" category first
        dynamicCategories.add(new Category("ALL", "All", R.drawable.ic_other, templates.size()));

        // Map category names to appropriate icons
        for (String categoryId : uniqueCategories) {
            int iconResId;
            switch (categoryId.toUpperCase()) {
                case "BIRTHDAY":
                    iconResId = R.drawable.ic_birthday;
                    break;
                case "WEDDING":
                    iconResId = R.drawable.ic_wedding;
                    break;
                case "ANNIVERSARY":
                    iconResId = R.drawable.ic_anniversary;
                    break;
                case "GRADUATION":
                    iconResId = R.drawable.ic_graduation;
                    break;
                default:
                    iconResId = R.drawable.ic_other;
            }

            // Count templates in this category
            int count = 0;
            for (Template template : templates) {
                if (template.getCategory().equals(categoryId)) {
                    count++;
                }
            }

            dynamicCategories.add(new Category(categoryId, categoryId, iconResId, count));
        }
        
        Log.d("HomeViewModel", "Loading categories:");
        for (Category category : dynamicCategories) {
            Log.d("HomeViewModel", String.format("Category - id: %s, name: %s, count: %d", 
                category.getId(), category.getName(), category.getWishCount()));
        }
        
        categories.setValue(dynamicCategories);
    }

    public void refreshData() {
        loading.setValue(true);
        error.setValue(null);

        ApiClient.getInstance().getTemplates().enqueue(new Callback<List<Template>>() {
            @Override
            public void onResponse(Call<List<Template>> call, Response<List<Template>> response) {
                loading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Template> receivedTemplates = response.body();
                    Log.d("HomeViewModel", "Received templates: " + receivedTemplates.size());
                    templates.setValue(receivedTemplates);
                    loadCategories(receivedTemplates);
                } else {
                    String errorMsg = "Failed to load templates: " + response.code();
                    Log.e("HomeViewModel", errorMsg);
                    error.setValue(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<List<Template>> call, Throwable t) {
                String errorMsg = "Network error: " + t.getMessage();
                Log.e("HomeViewModel", errorMsg, t);
                loading.setValue(false);
                error.setValue(errorMsg);
            }
        });
    }

    private boolean matchesFilter(Template template, Category category, String query) {
        // If category is null or "ALL", show all templates
        boolean matchesCategory = category == null || 
                                category.getId().equals("ALL") ||
                                template.getCategory().equals(category.getId());
                                
        boolean matchesSearch = query == null || query.isEmpty() ||
                              template.getTitle().toLowerCase().contains(query.toLowerCase());
                              
        Log.d("HomeViewModel", String.format("Filter check - template: %s, category: %s, templateCategory: %s, matches: %b", 
            template.getTitle(),
            category != null ? category.getId() : "null",
            template.getCategory(),
            matchesCategory && matchesSearch));
            
        return matchesCategory && matchesSearch;
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

    public LiveData<String> getError() {
        return error;
    }

    public void setSelectedCategory(Category category) {
        selectedCategory.setValue(category);
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }
}
