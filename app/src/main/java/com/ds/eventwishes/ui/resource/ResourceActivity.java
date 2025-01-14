package com.ds.eventwishes.ui.resource;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.ds.eventwishes.R;
import com.ds.eventwishes.api.ApiClient;
import com.ds.eventwishes.api.ApiService;
import com.ds.eventwishes.api.SharedWish;
import com.ds.eventwishes.databinding.ActivityResourceBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResourceActivity extends AppCompatActivity {
    private ActivityResourceBinding binding;
    private ApiService apiService;
    private String currentShortCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResourceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = ApiClient.getInstance();
        setupRetryButton();

        // Handle the deep link
        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data != null) {
            String path = data.getPath();
            if (path != null && path.startsWith("/wish/")) {
                currentShortCode = path.substring("/wish/".length());
                loadSharedWish(currentShortCode);
            }
        }
    }

    private void setupRetryButton() {
        View errorLayout = findViewById(R.id.errorView);
        if (errorLayout != null) {
            errorLayout.findViewById(R.id.btnRetry).setOnClickListener(v -> {
                if (currentShortCode != null) {
                    loadSharedWish(currentShortCode);
                }
            });
        }
    }

    private void loadSharedWish(String shortCode) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.contentContainer.setVisibility(View.GONE);
        findViewById(R.id.errorView).setVisibility(View.GONE);

        apiService.getSharedWish(shortCode).enqueue(new Callback<SharedWish>() {
            @Override
            public void onResponse(Call<SharedWish> call, Response<SharedWish> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    SharedWish wish = response.body();
                    displayWish(wish);
                } else {
                    showError(getString(R.string.error_loading));
                }
            }

            @Override
            public void onFailure(Call<SharedWish> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                showError(getString(R.string.retry_button));
            }
        });
    }

    private void displayWish(SharedWish wish) {
        binding.contentContainer.setVisibility(View.VISIBLE);
        binding.webView.loadData(wish.getCustomizedHtml(), "text/html", "UTF-8");
        binding.recipientText.setText(String.format("To: %s", wish.getRecipientName()));
        binding.senderText.setText(String.format("From: %s", wish.getSenderName()));
    }

    private void showError(String message) {
        findViewById(R.id.errorView).setVisibility(View.VISIBLE);
        binding.contentContainer.setVisibility(View.GONE);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
