package com.ds.eventwishes.ui.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ds.eventwishes.R;
import com.ds.eventwishes.databinding.ItemCategoryBinding;
import com.ds.eventwishes.model.Category;
import com.ds.eventwishes.utils.ImageLoader;
import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private List<Category> categories = new ArrayList<>();
    private Category selectedCategory;
    private OnCategorySelectedListener onCategorySelectedListener;

    public interface OnCategorySelectedListener {
        void onCategorySelected(Category category);
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories != null ? categories : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setSelectedCategory(Category category) {
        this.selectedCategory = category;
        notifyDataSetChanged();
    }

    public void setOnCategorySelectedListener(OnCategorySelectedListener listener) {
        this.onCategorySelectedListener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryBinding binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.getContext()),
            parent,
            false
        );
        return new CategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.bind(category);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemCategoryBinding binding;

        CategoryViewHolder(ItemCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Category category) {
            binding.categoryName.setText(category.getName());
            binding.wishCount.setText(String.valueOf(category.getCount()));
            
            // Load category icon from URL
            ImageLoader.loadCategoryIcon(itemView.getContext(), category.getIconUrl(), binding.categoryIcon);
            
            // Update selection state
            boolean isSelected = selectedCategory != null && selectedCategory.equals(category);
            itemView.setSelected(isSelected);
            itemView.setBackgroundResource(isSelected ? 
                R.drawable.bg_category_selected : 
                R.drawable.bg_category);

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (onCategorySelectedListener != null) {
                    onCategorySelectedListener.onCategorySelected(category);
                }
            });
        }
    }
}
