package com.ds.eventwishes.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class PaginatedResponse<T> {
    @SerializedName("data")
    private List<T> data;

    @SerializedName("page")
    private int currentPage;

    @SerializedName("totalPages")
    private int totalPages;

    @SerializedName("totalItems")
    private int totalItems;

    @SerializedName("hasMore")
    private boolean hasMore;

    @SerializedName("categories")
    private Map<String, Integer> categories;

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
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

    public boolean hasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public Map<String, Integer> getCategories() {
        return categories;
    }

    public void setCategories(Map<String, Integer> categories) {
        this.categories = categories;
    }
}
