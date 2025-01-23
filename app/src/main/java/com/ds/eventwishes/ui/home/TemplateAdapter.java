package com.ds.eventwishes.ui.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.ds.eventwishes.R;
import com.ds.eventwishes.databinding.ItemTemplateBinding;
import com.ds.eventwishes.model.Template;

public class TemplateAdapter extends ListAdapter<Template, TemplateAdapter.TemplateViewHolder> {
    private OnTemplateClickListener listener;

    public interface OnTemplateClickListener {
        void onTemplateClick(Template template);
    }

    public TemplateAdapter() {
        super(new DiffUtil.ItemCallback<Template>() {
            @Override
            public boolean areItemsTheSame(@NonNull Template oldItem, @NonNull Template newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull Template oldItem, @NonNull Template newItem) {
                return oldItem.getTitle().equals(newItem.getTitle()) &&
                       oldItem.getCategory().equals(newItem.getCategory()) &&
                       oldItem.getCategoryIconUrl().equals(newItem.getCategoryIconUrl());
            }
        });
    }

    public void setOnTemplateClickListener(OnTemplateClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TemplateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTemplateBinding binding = ItemTemplateBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new TemplateViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TemplateViewHolder holder, int position) {
        Template template = getItem(position);
        holder.bind(template);
    }

    class TemplateViewHolder extends RecyclerView.ViewHolder {
        private final ItemTemplateBinding binding;

        TemplateViewHolder(ItemTemplateBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onTemplateClick(getItem(position));
                }
            });
        }

        void bind(Template template) {
            binding.titleText.setText(template.getTitle());
            binding.categoryText.setText(template.getCategory());
            
            // Load category icon
            String iconUrl = template.getCategoryIconUrl();
            if (iconUrl != null && !iconUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                    .load(iconUrl)
                    .placeholder(R.drawable.ic_other)
                    .error(R.drawable.ic_other)
                    .into(binding.categoryIcon);
            } else {
                binding.categoryIcon.setImageResource(R.drawable.ic_other);
            }
            
            // Show likes and shares if available
            binding.likesCount.setText(String.valueOf(template.getLikes()));
            binding.sharesCount.setText(String.valueOf(template.getShares()));
        }
    }
}
