package com.ds.eventwishes.utils;

import android.content.Context;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ds.eventwishes.R;

public class ImageLoader {
    public static void loadCategoryIcon(Context context, String iconUrl, ImageView imageView) {
        if (context == null || imageView == null) return;

        Glide.with(context)
            .load(iconUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.ic_other)
            .error(R.drawable.ic_other)
            .into(imageView);
    }
}
