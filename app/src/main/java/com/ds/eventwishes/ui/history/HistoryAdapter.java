package com.ds.eventwishes.ui.history;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.chip.Chip;
import com.ds.eventwishes.R;
import com.ds.eventwishes.databinding.ItemWishHistoryBinding;
import com.ds.eventwishes.models.WishHistoryItem;
import com.ds.eventwishes.ui.resource.ResourceActivity;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

public class HistoryAdapter extends ListAdapter<WishHistoryItem, HistoryAdapter.HistoryViewHolder> {
    private static final String TAG = "HistoryAdapter";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    private static final DiffUtil.ItemCallback<WishHistoryItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<WishHistoryItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull WishHistoryItem oldItem, @NonNull WishHistoryItem newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull WishHistoryItem oldItem, @NonNull WishHistoryItem newItem) {
            return Objects.equals(oldItem.getWishTitle(), newItem.getWishTitle()) &&
                   Objects.equals(oldItem.getDateShared(), newItem.getDateShared()) &&
                   Objects.equals(oldItem.getPreviewImageUrl(), newItem.getPreviewImageUrl());
        }
    };

    public HistoryAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWishHistoryBinding binding = ItemWishHistoryBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new HistoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemWishHistoryBinding binding;

        HistoryViewHolder(ItemWishHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(WishHistoryItem item) {
            binding.wishTitle.setText(item.getWishTitle());
            binding.recipientName.setText(String.format("To: %s", item.getRecipientName()));
            binding.dateShared.setText(String.format("Shared on: %s", 
                dateFormat.format(item.getDateShared())));

            // Load preview image with improved error handling
            String previewUrl = item.getPreviewImageUrl();
            Log.d(TAG, "Loading preview for " + item.getWishTitle() + 
                ", URL length: " + (previewUrl != null ? previewUrl.length() : 0));

            // Set default placeholder immediately
            binding.previewImage.setImageResource(R.drawable.placeholder_image);

            if (previewUrl != null && !previewUrl.isEmpty()) {
                RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .transform(new RoundedCorners(24))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .override(300, 300) // Limit image size
                    .encodeQuality(85);  // Reduce quality slightly for better performance

                try {
                    // Handle both http/https URLs and data URIs
                    Object imageSource;
                    if (previewUrl.startsWith("data:")) {
                        // For data URIs, split at the comma to get the base64 part
                        String[] parts = previewUrl.split(",");
                        if (parts.length > 1) {
                            byte[] imageData = Base64.decode(parts[1], Base64.DEFAULT);
                            imageSource = imageData;
                            Log.d(TAG, "Using decoded base64 data, length: " + imageData.length);
                        } else {
                            Log.e(TAG, "Invalid data URI format");
                            binding.previewImage.setImageResource(R.drawable.error_image);
                            return;
                        }
                    } else if (previewUrl.startsWith("android.resource://")) {
                        // Handle resource URLs
                        imageSource = Uri.parse(previewUrl);
                        Log.d(TAG, "Using resource URI: " + previewUrl);
                    } else {
                        // Handle regular URLs
                        imageSource = previewUrl;
                        Log.d(TAG, "Using direct URL: " + previewUrl);
                    }

                    Glide.with(binding.previewImage)
                        .load(imageSource)
                        .apply(options)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                    Target<Drawable> target, boolean isFirstResource) {
                                Log.e(TAG, "Failed to load preview image: " + 
                                    (e != null ? e.getMessage() : "unknown error"));
                                binding.previewImage.setImageResource(R.drawable.error_image);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model,
                                    Target<Drawable> target, DataSource dataSource, 
                                    boolean isFirstResource) {
                                Log.d(TAG, "Successfully loaded preview image for " + 
                                    item.getWishTitle() + " from " + dataSource);
                                return false;
                            }
                        })
                        .into(binding.previewImage);
                } catch (Exception e) {
                    Log.e(TAG, "Error loading preview image", e);
                    binding.previewImage.setImageResource(R.drawable.error_image);
                }
            } else {
                Log.d(TAG, "No preview URL available for " + item.getWishTitle());
            }

            // Set up tags
            binding.tagGroup.removeAllViews();
            if (item.getTags() != null && !item.getTags().isEmpty()) {
                String[] tags = item.getTags().split(",");
                for (String tag : tags) {
                    Chip chip = new Chip(binding.tagGroup.getContext());
                    chip.setText(tag.trim());
                    chip.setClickable(false);
                    chip.setCheckable(false);
                    binding.tagGroup.addView(chip);
                }
            }

            // Set click listener to open preview
            itemView.setOnClickListener(v -> {
                String shortCode = item.getShortCode();
                if (shortCode == null || shortCode.isEmpty()) {
                    String shareUrl = item.getShareUrl();
                    if (shareUrl != null && shareUrl.contains("/wish/")) {
                        shortCode = shareUrl.substring(shareUrl.lastIndexOf("/wish/") + 6);
                    }
                }

                if (shortCode != null && !shortCode.isEmpty()) {
                    Intent intent = new Intent(v.getContext(), ResourceActivity.class);
                    intent.setAction(Intent.ACTION_VIEW);
                    Uri uri = Uri.parse("app://eventwishes/wish/" + shortCode);
                    intent.setData(uri);
                    v.getContext().startActivity(intent);
                } else {
                    Toast.makeText(v.getContext(), 
                        R.string.error_invalid_link, 
                        Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
