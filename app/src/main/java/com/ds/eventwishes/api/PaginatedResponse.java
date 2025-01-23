package com.ds.eventwishes.api;

import com.ds.eventwishes.model.CategoryInfo;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class PaginatedResponse<T> {
    @SerializedName("data")
    private List<T> data;

    @SerializedName("page")
    private int page;

    @SerializedName("totalPages")
    private int totalPages;

    @SerializedName("totalItems")
    private int totalItems;

    @SerializedName("hasMore")
    private boolean hasMore;

    @SerializedName("categories")
    private Map<String, CategoryInfo> categories;

    @SerializedName("totalTemplates")
    private int totalTemplates;

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public Map<String, CategoryInfo> getCategories() {
        return categories;
    }

    public void setCategories(Map<String, CategoryInfo> categories) {
        this.categories = categories;
    }

    public int getTotalTemplates() {
        return totalTemplates;
    }

    public void setTotalTemplates(int totalTemplates) {
        this.totalTemplates = totalTemplates;
    }
}
