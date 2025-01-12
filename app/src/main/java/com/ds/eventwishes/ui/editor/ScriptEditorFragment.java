package com.ds.eventwishes.ui.editor;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.android.material.snackbar.Snackbar;
import com.ds.eventwishes.R;
import com.ds.eventwishes.api.ApiClient;
import com.ds.eventwishes.api.ShareRequest;
import com.ds.eventwishes.api.ShareResponse;
import com.ds.eventwishes.databinding.FragmentScriptEditorBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScriptEditorFragment extends Fragment {
    private FragmentScriptEditorBinding binding;
    private String wishId;
    private String htmlContent;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                           ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentScriptEditorBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get arguments
        if (getArguments() != null) {
            wishId = getArguments().getString("wishId");
            htmlContent = getArguments().getString("htmlContent");
        }

        // Setup WebView
        setupWebView();

        // Setup share button
        binding.shareButton.setOnClickListener(v -> createShareLink());
    }

    private void setupWebView() {
        WebView webView = binding.webView;
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // Update placeholders with user input
                updatePlaceholders();
            }
        });

        // Load HTML content
        if (htmlContent != null) {
            webView.loadData(htmlContent, "text/html", "UTF-8");
        }
    }

    private void updatePlaceholders() {
        String recipientName = binding.recipientInput.getText().toString();
        String senderName = binding.senderInput.getText().toString();

        String js = String.format("javascript:(function() {" +
                "document.getElementById('recipient').textContent = '%s';" +
                "document.getElementById('sender').textContent = '%s';" +
                "})()", recipientName, senderName);

        binding.webView.evaluateJavascript(js, null);
    }

    private void createShareLink() {
        if (wishId == null) {
            showError(getString(R.string.error_loading_template));
            return;
        }

        ShareRequest request = new ShareRequest(
            wishId,
            binding.recipientInput.getText().toString(),
            binding.senderInput.getText().toString()
        );

        ApiClient.getInstance().createShareLink(request).enqueue(new Callback<ShareResponse>() {
            @Override
            public void onResponse(Call<ShareResponse> call, Response<ShareResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ShareResponse shareResponse = response.body();
                    shareTemplate(shareResponse.getShortUrl());
                } else {
                    showError(getString(R.string.error_dynamic_link));
                }
            }

            @Override
            public void onFailure(Call<ShareResponse> call, Throwable t) {
                showError(getString(R.string.error_dynamic_link));
            }
        });
    }

    private void shareTemplate(String shareUrl) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_message));
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareUrl);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)));
    }

    private void showError(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
