package com.ds.eventwishes.ui.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.ds.eventwishes.R;
import com.ds.eventwishes.databinding.ItemCategoryBinding;
import com.ds.eventwishes.model.Category;

public class CategoryAdapter extends ListAdapter<Category, CategoryAdapter.CategoryViewHolder> {
    private OnCategoryClickListener listener;
    private int selectedPosition = -1;

    public CategoryAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Category> DIFF_CALLBACK = new DiffUtil.ItemCallback<Category>() {
        @Override
        public boolean areItemsTheSame(@NonNull Category oldItem, @NonNull Category newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Category oldItem, @NonNull Category newItem) {
            return oldItem.equals(newItem);
        }
    };

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category, int position);
    }

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int position) {
        int oldPosition = selectedPosition;
        selectedPosition = position;
        if (oldPosition != -1) notifyItemChanged(oldPosition);
        if (position != -1) notifyItemChanged(position);
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryBinding binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new CategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        holder.bind(getItem(position), position == selectedPosition);
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemCategoryBinding binding;

        CategoryViewHolder(ItemCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onCategoryClick(getItem(position), position);
                }
            });
        }

        void bind(Category category, boolean isSelected) {
            binding.categoryIcon.setImageResource(category.getIconResId());
            binding.categoryName.setText(category.getName());
            binding.wishCount.setText(String.valueOf(category.getCount()));
            itemView.setSelected(isSelected);
            
            // Update background based on selection
            itemView.setBackgroundResource(isSelected ? 
                R.drawable.bg_category_selected : 
                R.drawable.bg_category);
        }
    }
}
