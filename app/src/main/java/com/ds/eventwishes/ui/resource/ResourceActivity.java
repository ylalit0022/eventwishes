package com.ds.eventwishes.ui.resource;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
    private static final String TAG = "ResourceActivity";
    private ActivityResourceBinding binding;
    private ApiService apiService;
    private String currentShortCode;
    private Call<SharedWish> currentCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Starting ResourceActivity");
        
        binding = ActivityResourceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = ApiClient.getInstance();
        setupWebView();
        setupRetryButton();

        // Handle the deep link
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent: Received new intent");
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent == null) {
            Log.e(TAG, "handleIntent: Intent is null");
            showError(getString(R.string.error_loading));
            return;
        }

        String action = intent.getAction();
        Uri data = intent.getData();
        
        Log.d(TAG, "handleIntent: Action: " + action);
        Log.d(TAG, "handleIntent: Data URI: " + (data != null ? data.toString() : "null"));
        Log.d(TAG, "handleIntent: Host: " + (data != null ? data.getHost() : "null"));
        Log.d(TAG, "handleIntent: Scheme: " + (data != null ? data.getScheme() : "null"));
        Log.d(TAG, "handleIntent: Path: " + (data != null ? data.getPath() : "null"));

        if (Intent.ACTION_VIEW.equals(action) && data != null) {
            try {
                String path = data.getPath();
                Log.d(TAG, "handleIntent: Full path: " + path);
                
                if (path != null && path.startsWith("/wish/")) {
                    String shortCode = path.substring("/wish/".length());
                    Log.d(TAG, "handleIntent: Extracted shortCode: " + shortCode);
                    
                    if (shortCode.isEmpty()) {
                        Log.e(TAG, "handleIntent: Empty shortCode");
                        showError(getString(R.string.error_invalid_link));
                        return;
                    }
                    
                    currentShortCode = shortCode;
                    
                    if (!isNetworkAvailable()) {
                        Log.e(TAG, "handleIntent: No network connection");
                        showError(getString(R.string.error_no_internet));
                        return;
                    }
                    
                    loadSharedWish(shortCode);
                } else {
                    Log.e(TAG, "handleIntent: Invalid path format: " + path);
                    showError(getString(R.string.error_invalid_link));
                }
            } catch (Exception e) {
                Log.e(TAG, "handleIntent: Error processing URI", e);
                showError(getString(R.string.error_invalid_link));
            }
        } else {
            Log.e(TAG, "handleIntent: Invalid intent: action=" + action + ", data=" + data);
            showError(getString(R.string.error_invalid_link));
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
            return capabilities != null && 
                   capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                   capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        } else {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
    }

    private void setupWebView() {
        WebSettings webSettings = binding.webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setDefaultTextEncodingName("UTF-8");
        
        // Enable mixed content if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        binding.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                binding.progressBar.setVisibility(View.GONE);
                Log.d(TAG, "onPageFinished: WebView content loaded");
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                Log.e(TAG, "onReceivedError: WebView error: " + description);
                showError(getString(R.string.error_loading));
            }
        });
    }

    private void setupRetryButton() {
        View errorLayout = findViewById(R.id.errorView);
        if (errorLayout != null) {
            View retryButton = errorLayout.findViewById(R.id.btnRetry);
            if (retryButton != null) {
                retryButton.setOnClickListener(v -> {
                    if (currentShortCode != null) {
                        if (!isNetworkAvailable()) {
                            showError(getString(R.string.error_no_internet));
                            return;
                        }
                        loadSharedWish(currentShortCode);
                    }
                });
            }
        }
    }

    private void loadSharedWish(String shortCode) {
        if (currentCall != null) {
            currentCall.cancel();
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.contentContainer.setVisibility(View.GONE);
        View errorView = findViewById(R.id.errorView);
        if (errorView != null) {
            errorView.setVisibility(View.GONE);
        }

        currentCall = apiService.getSharedWish(shortCode);
        currentCall.enqueue(new Callback<SharedWish>() {
            @Override
            public void onResponse(Call<SharedWish> call, Response<SharedWish> response) {
                if (isFinishing()) return;

                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    displayWish(response.body());
                } else {
                    showError(getString(R.string.error_loading));
                }
            }

            @Override
            public void onFailure(Call<SharedWish> call, Throwable t) {
                if (isFinishing() || call.isCanceled()) return;
                binding.progressBar.setVisibility(View.GONE);
                showError(getString(R.string.error_network));
            }
        });
    }

    private void displayWish(SharedWish wish) {
        try {
            if (wish == null || wish.getCustomizedHtml() == null || wish.getCustomizedHtml().trim().isEmpty()) {
                Log.e(TAG, "displayWish: Missing customizedHtml");
                showError(getString(R.string.error_loading));
                return;
            }

            Log.d(TAG, "displayWish: Loading HTML content");
            binding.contentContainer.setVisibility(View.VISIBLE);
            
            // Base URL for relative paths in HTML
            String baseUrl = "https://eventwishes.onrender.com";
            
            // Show recipient and sender names
            String recipientName = wish.getRecipientName();
            String senderName = wish.getSenderName();
            
            Log.d(TAG, "displayWish: Recipient: " + recipientName);
            Log.d(TAG, "displayWish: Sender: " + senderName);
            
            // Set recipient name
            if (recipientName != null && !recipientName.trim().isEmpty()) {
                binding.recipientText.setVisibility(View.VISIBLE);
                binding.recipientText.setText(getString(R.string.to_recipient, recipientName.trim()));
            } else {
                binding.recipientText.setVisibility(View.GONE);
            }
            
            // Set sender name
            if (senderName != null && !senderName.trim().isEmpty()) {
                binding.senderText.setVisibility(View.VISIBLE);
                binding.senderText.setText(getString(R.string.from_sender, senderName.trim()));
            } else {
                binding.senderText.setVisibility(View.GONE);
            }
            
            // Wrap HTML in proper structure and add viewport meta tag
            String htmlContent = String.format(
                "<!DOCTYPE html><html><head>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no'>" +
                "<style>" +
                    "body { margin: 0; padding: 16px; word-wrap: break-word; font-family: Arial, sans-serif; }" +
                    "img { max-width: 100%; height: auto; display: block; margin: 0 auto; }" +
                    "@media (prefers-color-scheme: dark) { body { background-color: #121212; color: #ffffff; } }" +
                "</style>" +
                "</head><body>%s</body></html>",
                wish.getCustomizedHtml()
            );
            
            // Load the HTML content
            binding.webView.loadDataWithBaseURL(
                baseUrl,
                htmlContent,
                "text/html",
                "UTF-8",
                null
            );
            
            // Add debug logging
            Log.d(TAG, "displayWish: Content loaded successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "displayWish: Error displaying wish", e);
            showError(getString(R.string.error_loading));
        }
    }

    private void showError(String message) {
        View errorView = findViewById(R.id.errorView);
        if (errorView != null) {
            errorView.setVisibility(View.VISIBLE);
        }
        binding.contentContainer.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.GONE);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentCall != null) {
            currentCall.cancel();
        }
    }
}
