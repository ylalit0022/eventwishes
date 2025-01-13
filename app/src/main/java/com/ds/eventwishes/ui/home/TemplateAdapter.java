package com.ds.eventwishes.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ds.eventwishes.R;
import com.ds.eventwishes.api.Template;

import java.util.ArrayList;
import java.util.List;

public class TemplateAdapter extends RecyclerView.Adapter<TemplateAdapter.TemplateViewHolder> implements Filterable {
    private final Context context;
    private List<Template> templates;
    private List<Template> templatesFiltered;
    private final OnTemplateClickListener listener;
    private int lastPosition = -1;

    public interface OnTemplateClickListener {
        void onTemplateClick(Template template);
    }

    public TemplateAdapter(Context context, List<Template> templates, OnTemplateClickListener listener) {
        this.context = context;
        this.templates = templates;
        this.templatesFiltered = templates;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TemplateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_template, parent, false);
        return new TemplateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TemplateViewHolder holder, int position) {
        Template template = templatesFiltered.get(position);
        holder.titleText.setText(template.getTitle());
        holder.categoryText.setText(template.getCategory());
        
        // Load preview image using Glide
        if (template.getPreviewUrl() != null && !template.getPreviewUrl().isEmpty()) {
            Glide.with(context)
                .load(template.getPreviewUrl())
                .placeholder(R.drawable.placeholder_template)
                .error(R.drawable.placeholder_template)
                .into(holder.previewImage);
        } else {
            holder.previewImage.setImageResource(R.drawable.placeholder_template);
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTemplateClick(template);
            }
        });

        // Apply animation
        setAnimation(holder.itemView, position);
    }

    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.item_animation_fall_down);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return templatesFiltered != null ? templatesFiltered.size() : 0;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString().toLowerCase().trim();
                if (charString.isEmpty()) {
                    templatesFiltered = templates;
                } else {
                    List<Template> filteredList = new ArrayList<>();
                    for (Template template : templates) {
                        if (template.getTitle().toLowerCase().contains(charString) ||
                            template.getCategory().toLowerCase().contains(charString)) {
                            filteredList.add(template);
                        }
                    }
                    templatesFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = templatesFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                templatesFiltered = (List<Template>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public void updateData(List<Template> newTemplates) {
        this.templates = newTemplates;
        this.templatesFiltered = newTemplates;
        notifyDataSetChanged();
    }

    public void updateTemplates(List<Template> newTemplates) {
        this.templates = newTemplates;
        this.templatesFiltered = newTemplates;
        notifyDataSetChanged();
        lastPosition = -1; // Reset animation
    }

    static class TemplateViewHolder extends RecyclerView.ViewHolder {
        ImageView previewImage;
        TextView titleText;
        TextView categoryText;

        TemplateViewHolder(View itemView) {
            super(itemView);
            previewImage = itemView.findViewById(R.id.previewImage);
            titleText = itemView.findViewById(R.id.titleText);
            categoryText = itemView.findViewById(R.id.categoryText);
        }
    }
}
