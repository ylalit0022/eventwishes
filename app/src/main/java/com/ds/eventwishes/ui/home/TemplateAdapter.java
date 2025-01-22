package com.ds.eventwishes.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.ds.eventwishes.R;
import com.ds.eventwishes.api.Template;
import com.ds.eventwishes.databinding.ItemTemplateBinding;

public class TemplateAdapter extends ListAdapter<Template, TemplateAdapter.TemplateViewHolder> {
    private final Context context;
    private final OnTemplateClickListener listener;
    private int lastPosition = -1;

    public interface OnTemplateClickListener {
        void onTemplateClick(Template template);
    }

    public TemplateAdapter(Context context, OnTemplateClickListener listener) {
        super(new DiffUtil.ItemCallback<Template>() {
            @Override
            public boolean areItemsTheSame(@NonNull Template oldItem, @NonNull Template newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull Template oldItem, @NonNull Template newItem) {
                return oldItem.getTitle().equals(newItem.getTitle()) &&
                       oldItem.getCategory().equals(newItem.getCategory()) &&
                       oldItem.getPreviewUrl().equals(newItem.getPreviewUrl());
            }
        });
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TemplateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTemplateBinding binding = ItemTemplateBinding.inflate(
            LayoutInflater.from(context), parent, false);
        return new TemplateViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TemplateViewHolder holder, int position) {
        Template template = getItem(position);
        holder.bind(template);
        setAnimation(holder.itemView, position);
    }

    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull TemplateViewHolder holder) {
        holder.itemView.clearAnimation();
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
            
            if (template.getPreviewUrl() != null && !template.getPreviewUrl().isEmpty()) {
                Glide.with(context)
                    .load(template.getPreviewUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(binding.previewImage);
            } else {
                binding.previewImage.setImageResource(R.drawable.placeholder_image);
            }
        }
    }
}
