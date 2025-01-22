package com.ds.eventwishes.model;

import androidx.annotation.DrawableRes;
import java.util.Objects;

public class Category {
    private String id;
    private String name;
    @DrawableRes
    private int iconResId;
    private int count;

    public Category(String id, String name, @DrawableRes int iconResId, int count) {
        this.id = id;
        this.name = name;
        this.iconResId = iconResId;
        this.count = count;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public @DrawableRes int getIconResId() {
        return iconResId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return iconResId == category.iconResId &&
               count == category.count &&
               Objects.equals(id, category.id) &&
               Objects.equals(name, category.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, iconResId, count);
    }
}
